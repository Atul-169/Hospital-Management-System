package com.example.project.controller;

import com.example.project.util.DatabaseConnection;
import com.example.project.util.SceneManager;
import com.example.project.util.SessionManager;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;

public class PatientDashboardController {

    // Navigation
    @FXML private Button btnDashboard, btnSearchDoctors, btnBookAppointment, btnViewAppointments;
    @FXML private Button btnViewReports, btnPayments, btnDoctorPosts, btnQA, btnBloodDonation, btnSmartAssistant, btnNotifications;
    @FXML private Button btnProfile, btnLogout, btnProfileSettings;
    @FXML private Label lblWelcome;
    @FXML private Button btnLightMode, btnDarkMode;

    // Main Content Area
    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;

    // Dashboard Section
    @FXML private VBox dashboardSection;
    @FXML private Label lblUpcomingAppointments, lblTotalReports, lblUnreadNotifs, lblBloodDonationStatus;
    @FXML private VBox notificationsSection;
    @FXML private ListView<NotificationItem> lstDashboardNotifications;
    @FXML private ListView<DoctorPost> lstDashboardPosts;
    @FXML private ListView<Question> lstDashboardQuestions;
    @FXML private FlowPane dashboardDonorGridPane;

    // Search Doctors Section
    @FXML private VBox searchDoctorsSection;
    @FXML private TextField txtSearchName, txtSearchSpecialization, txtSearchFee;
    @FXML private FlowPane doctorGridPane;

    // Book Appointment Section
    @FXML private VBox bookAppointmentSection;
    @FXML private ComboBox<String> cmbSelectDoctor;
    @FXML private DatePicker dpAppointmentDate;
    @FXML private ComboBox<String> cmbAppointmentTime;
    @FXML private TextArea txtVisitReason;

    // View Appointments Section
    @FXML private VBox viewAppointmentsSection;
    @FXML private FlowPane gridAppointments;
    @FXML private ComboBox<String> cmbAppointmentFilter;

    // View Reports Section
    @FXML private VBox viewReportsSection;
    @FXML private TableView<MedicalReport> tblReports;
    @FXML private TableColumn<MedicalReport, String> colReportAppointment, colReportDoctor, colReportTitle, colLabTests, colPrescription, colLabRequestStatus, colLabResultStatus, colReportDate;
    @FXML private Label lblSelectedReportInfo;
    @FXML private TextArea txtSelectedReportDetails;
    @FXML private Button btnRequestLabReport, btnDownloadLabReport;

    // Payment Section
    @FXML private VBox paymentSection;
    @FXML private TableView<LabPayment> tblPayments;
    @FXML private TableColumn<LabPayment, String> colPaymentType, colPaymentReport, colPaymentDoctor, colPaymentAmount, colPaymentStatus, colPaymentMethod;
    @FXML private ComboBox<String> cmbPaymentMethod;
    @FXML private Label lblPaymentInfo;
    @FXML private Button btnSubmitPayment;

    // Doctor Posts Section
    @FXML private VBox doctorPostsSection;
    @FXML private ComboBox<String> cmbPostCategory;
    @FXML private ListView<DoctorPost> lstDoctorPosts;

    // Q&A Section
    @FXML private VBox qaSection;
    @FXML private TextArea txtAskQuestion;
    @FXML private ListView<Question> lstMyQuestions;
    @FXML private ListView<NotificationItem> lstNotifications;

    // Blood Donation Section
    @FXML private VBox bloodDonationSection;
    @FXML private VBox donorRegistrationForm;
    @FXML private ComboBox<String> cmbBloodGroup;
    @FXML private TextField txtDonorLocation, txtDonorPhone;
    @FXML private ComboBox<String> cmbAvailabilityStatus;
    @FXML private FlowPane donorGridPane;
    @FXML private TextField txtSearchBloodGroup;

    // Smart Assistant Section
    @FXML private VBox smartAssistantSection;
    @FXML private TextArea txtSymptoms, txtAssistantResponse;

    private int patientId;
    private boolean isDarkMode = false;

    @FXML
    public void initialize() {
        patientId = getPatientId(SessionManager.getUserId());
        if (patientId == 0) {
             System.err.println("[DEBUG_LOG] Patient ID not found for User ID: " + SessionManager.getUserId());
        }
        lblWelcome.setText("Welcome, " + SessionManager.getUserName() + "!");

        checkProfileStatus();

        // Initialize time slots
        if (cmbAppointmentTime != null) {
            cmbAppointmentTime.setItems(FXCollections.observableArrayList(
                    "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
                    "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"
            ));
        }

        // Initialize filters
        if (cmbAppointmentFilter != null) {
            cmbAppointmentFilter.setItems(FXCollections.observableArrayList(
                    "All", "Upcoming", "Confirmed", "Completed", "Cancelled"
            ));
            cmbAppointmentFilter.setValue("All");
        }

        if (cmbPostCategory != null) {
            cmbPostCategory.setItems(FXCollections.observableArrayList(
                    "All", "General Health", "Nutrition", "Exercise", "Mental Health", "Disease Prevention"
            ));
            cmbPostCategory.setValue("All");
        }

        if (cmbBloodGroup != null) {
            cmbBloodGroup.setItems(FXCollections.observableArrayList(
                    "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
            ));
        }

        if (cmbAvailabilityStatus != null) {
            cmbAvailabilityStatus.setItems(FXCollections.observableArrayList(
                    "Available", "Unavailable"
            ));
            cmbAvailabilityStatus.setValue("Available");
        }

        if (cmbPaymentMethod != null) {
            cmbPaymentMethod.setItems(FXCollections.observableArrayList("bKash", "Bank", "Card"));
            cmbPaymentMethod.setValue("bKash");
        }

        // Setup table columns
        setupAppointmentGrid();
        setupReportTable();
        setupPaymentTable();

        // Load dashboard by default
        showDashboard();
        loadUnreadNotificationsCount();
    }

    // ==================== NAVIGATION ====================
    private void showSection(VBox section, Button navBtn) {
        hideAllSections();
        section.setVisible(true);
        section.setManaged(true);

        // Update active button style
        btnDashboard.getStyleClass().remove("active");
        btnSearchDoctors.getStyleClass().remove("active");
        btnBookAppointment.getStyleClass().remove("active");
        btnViewAppointments.getStyleClass().remove("active");
        btnViewReports.getStyleClass().remove("active");
        btnPayments.getStyleClass().remove("active");
        btnNotifications.getStyleClass().remove("active");
        btnDoctorPosts.getStyleClass().remove("active");
        btnQA.getStyleClass().remove("active");
        btnBloodDonation.getStyleClass().remove("active");
        btnSmartAssistant.getStyleClass().remove("active");

        if (navBtn != null) {
            navBtn.getStyleClass().add("active");
        }

        applyTheme();

        // Fade In Animation
        FadeTransition ft = new FadeTransition(Duration.millis(400), section);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    @FXML
    private void showDashboard() {
        showSection(dashboardSection, btnDashboard);
        loadDashboardData();
        loadNotificationsPreview();
    }

    @FXML
    private void showSearchDoctors() {
        showSection(searchDoctorsSection, btnSearchDoctors);
        loadAllDoctors();
    }

    @FXML
    private void showBookAppointment() {
        showSection(bookAppointmentSection, btnBookAppointment);
        loadDoctorsForAppointment();
    }

    @FXML
    private void showViewAppointments() {
        showSection(viewAppointmentsSection, btnViewAppointments);
        loadAppointments("All");
    }

    @FXML
    private void showViewReports() {
        showSection(viewReportsSection, btnViewReports);
        loadMedicalReports();
    }

    @FXML
    private void showPayments() {
        showSection(paymentSection, btnPayments);
        loadPayments();
    }

    @FXML
    private void showNotifications() {
        showSection(notificationsSection, btnNotifications);
        loadNotifications();
    }

    @FXML
    private void showDoctorPosts() {
        showSection(doctorPostsSection, btnDoctorPosts);
        loadDoctorPosts("All");
    }

    @FXML
    private void showQA() {
        showSection(qaSection, btnQA);
        loadMyQuestions();
    }

    @FXML
    private void showBloodDonation() {
        showSection(bloodDonationSection, btnBloodDonation);
        loadBloodDonors();
    }

    @FXML
    private void showSmartAssistant() {
        showSection(smartAssistantSection, btnSmartAssistant);
    }

    private void hideAllSections() {
        dashboardSection.setVisible(false);
        dashboardSection.setManaged(false);
        searchDoctorsSection.setVisible(false);
        searchDoctorsSection.setManaged(false);
        bookAppointmentSection.setVisible(false);
        bookAppointmentSection.setManaged(false);
        viewAppointmentsSection.setVisible(false);
        viewAppointmentsSection.setManaged(false);
        viewReportsSection.setVisible(false);
        viewReportsSection.setManaged(false);
        paymentSection.setVisible(false);
        paymentSection.setManaged(false);
        notificationsSection.setVisible(false);
        notificationsSection.setManaged(false);
        doctorPostsSection.setVisible(false);
        doctorPostsSection.setManaged(false);
        qaSection.setVisible(false);
        qaSection.setManaged(false);
        bloodDonationSection.setVisible(false);
        bloodDonationSection.setManaged(false);
        smartAssistantSection.setVisible(false);
        smartAssistantSection.setManaged(false);
    }

    private void checkProfileStatus() {
        String query = "SELECT profile_completed FROM patients WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt("profile_completed") == 1) {
                btnProfile.setVisible(false);
                btnProfile.setManaged(false);
                btnProfileSettings.setVisible(true);
                btnProfileSettings.setManaged(true);
            } else {
                btnProfile.setVisible(true);
                btnProfile.setManaged(true);
                btnProfileSettings.setVisible(false);
                btnProfileSettings.setManaged(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void setLightMode() {
        isDarkMode = false;
        applyTheme();
    }

    @FXML
    private void setDarkMode() {
        isDarkMode = true;
        applyTheme();
    }

    private void applyTheme() {
        if (contentArea == null || contentArea.getScene() == null) return;
        
        String theme = isDarkMode ? "dark-theme" : "light-theme";
        contentArea.getScene().getRoot().getStyleClass().removeAll("light-theme", "dark-theme");
        contentArea.getScene().getRoot().getStyleClass().add(theme);

        if (isDarkMode) {
            btnDarkMode.getStyleClass().add("active");
            btnLightMode.getStyleClass().remove("active");
        } else {
            btnLightMode.getStyleClass().add("active");
            btnDarkMode.getStyleClass().remove("active");
        }
    }

    @FXML
    private void toggleTheme() {
        // Method kept for compatibility but logic moved to setLightMode/setDarkMode
    }

    @FXML
    private void showProfile() {
        SceneManager.switchScene(btnProfile, "/fxml/profile-setup.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        SceneManager.switchScene(btnLogout, "/fxml/role-selection.fxml");
    }

    // ==================== DASHBOARD ====================
    private void loadDashboardData() {
        lblUpcomingAppointments.setText(String.valueOf(getUpcomingAppointmentsCount()));
        lblTotalReports.setText(String.valueOf(getTotalReportsCount()));
        lblUnreadNotifs.setText(String.valueOf(getUnreadNotificationsCount()));
        lblBloodDonationStatus.setText(getBloodDonationStatus());
        loadDashboardNotificationsPreview();
        loadDashboardPostsPreview();
        loadDashboardQuestionsPreview();
        loadDashboardDonorsPreview();
    }

    private int getUpcomingAppointmentsCount() {
        String query = "SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status = 'upcoming'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalReportsCount() {
        String query = "SELECT COUNT(*) FROM medical_reports WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getUnreadNotificationsCount() {
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void loadUnreadNotificationsCount() {
        if (lblUnreadNotifs != null) {
            lblUnreadNotifs.setText(String.valueOf(getUnreadNotificationsCount()));
        }
    }

    private String getBloodDonationStatus() {
        String query = "SELECT availability_status FROM blood_donors WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("availability_status");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Not Registered";
    }

    // ==================== SEARCH DOCTORS ====================
    private void loadAllDoctors() {
        doctorGridPane.getChildren().clear();
        String query = "SELECT u.name, d.specialization, d.experience, d.qualification, d.fee, d.id " +
                "FROM doctors d JOIN users u ON d.user_id = u.id WHERE d.profile_completed = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Doctor doctor = new Doctor(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("experience"),
                        rs.getString("qualification"),
                        rs.getDouble("fee")
                );
                doctorGridPane.getChildren().add(createDoctorCard(doctor));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createDoctorCard(Doctor doctor) {
        StackPane container = new StackPane();
        container.getStyleClass().add("doctor-card-container");
        container.setPrefWidth(280);
        container.setMaxWidth(280);

        VBox card = new VBox(0);
        card.getStyleClass().add("doctor-card");

        // Image container
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("doctor-card-image-container");
        imageContainer.setMinHeight(140);
        imageContainer.setAlignment(Pos.CENTER);
        Label avatarLabel = new Label(getInitials(doctor.getName()));
        avatarLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: 900; -fx-text-fill: white;");
        imageContainer.getChildren().add(avatarLabel);

        // Card body (Visible by default)
        VBox body = new VBox(8);
        body.getStyleClass().add("doctor-card-body");
        body.setPadding(new javafx.geometry.Insets(15));
        Label nameLabel = new Label(doctor.getName());
        nameLabel.getStyleClass().add("doctor-name");
        Label specializationLabel = new Label(doctor.getSpecialization());
        specializationLabel.getStyleClass().add("doctor-specialization");
        body.getChildren().addAll(nameLabel, specializationLabel);

        card.getChildren().addAll(imageContainer, body);

        // Details Overlay (Hidden by default, slides up on hover)
        VBox overlay = new VBox(10);
        overlay.getStyleClass().add("doctor-card-overlay");
        overlay.setAlignment(Pos.CENTER);
        overlay.setOpacity(0);
        overlay.setTranslateY(200); // Start below
        overlay.setPadding(new javafx.geometry.Insets(15));
        overlay.setStyle("-fx-background-color: rgba(102, 126, 234, 0.95); -fx-background-radius: 20;");

        Label overlayExp = new Label("🎓 Experience: " + doctor.getExperience());
        overlayExp.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label overlayQual = new Label("📜 " + doctor.getQualification());
        overlayQual.setStyle("-fx-text-fill: white;");
        overlayQual.setWrapText(true);
        overlayQual.setAlignment(Pos.CENTER);
        Label overlayFee = new Label("Tk " + doctor.getFee());
        overlayFee.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: 900;");

        Button bookBtn = new Button("Book Now");
        bookBtn.getStyleClass().add("book-appointment-btn-white");
        bookBtn.setOnAction(e -> {
            cmbSelectDoctor.setValue(doctor.getName());
            showBookAppointment();
        });

        overlay.getChildren().addAll(overlayExp, overlayQual, overlayFee, bookBtn);

        container.getChildren().addAll(card, overlay);

        // Hover animations
        container.setOnMouseEntered(e -> {
            javafx.animation.TranslateTransition slideUp = new javafx.animation.TranslateTransition(Duration.millis(300), overlay);
            slideUp.setToY(0);
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(300), overlay);
            fadeIn.setToValue(1);
            slideUp.play();
            fadeIn.play();
        });

        container.setOnMouseExited(e -> {
            javafx.animation.TranslateTransition slideDown = new javafx.animation.TranslateTransition(Duration.millis(300), overlay);
            slideDown.setToY(200);
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(300), overlay);
            fadeOut.setToValue(0);
            slideDown.play();
            fadeOut.play();
        });

        VBox wrapper = new VBox(container);
        wrapper.setPadding(new javafx.geometry.Insets(10));
        return wrapper;
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "D";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return ("" + name.charAt(0)).toUpperCase();
    }

    @FXML
    private void searchDoctors() {
        doctorGridPane.getChildren().clear();
        String name = txtSearchName.getText().trim();
        String specialization = txtSearchSpecialization.getText().trim();
        String feeStr = txtSearchFee.getText().trim();

        StringBuilder query = new StringBuilder("SELECT u.name, d.specialization, d.experience, d.qualification, d.fee, d.id " +
                "FROM doctors d JOIN users u ON d.user_id = u.id WHERE d.profile_completed = 1");

        if (!name.isEmpty()) query.append(" AND u.name LIKE ?");
        if (!specialization.isEmpty()) query.append(" AND d.specialization LIKE ?");
        if (!feeStr.isEmpty()) {
            try {
                Double.parseDouble(feeStr);
                query.append(" AND d.fee <= ?");
            } catch (NumberFormatException ignored) {}
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (!name.isEmpty()) pstmt.setString(paramIndex++, "%" + name + "%");
            if (!specialization.isEmpty()) pstmt.setString(paramIndex++, "%" + specialization + "%");
            if (!feeStr.isEmpty()) {
                try {
                    pstmt.setDouble(paramIndex++, Double.parseDouble(feeStr));
                } catch (NumberFormatException ignored) {}
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Doctor doctor = new Doctor(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("experience"),
                        rs.getString("qualification"),
                        rs.getDouble("fee")
                );
                doctorGridPane.getChildren().add(createDoctorCard(doctor));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== BOOK APPOINTMENT ====================
    private void loadDoctorsForAppointment() {
        ObservableList<String> doctorNames = FXCollections.observableArrayList();
        String query = "SELECT u.name FROM doctors d JOIN users u ON d.user_id = u.id WHERE d.profile_completed = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                doctorNames.add(rs.getString("name"));
            }
            cmbSelectDoctor.setItems(doctorNames);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void submitAppointment() {
        String doctorName = cmbSelectDoctor.getValue();
        LocalDate date = dpAppointmentDate.getValue();
        String time = cmbAppointmentTime.getValue();
        String reason = txtVisitReason.getText().trim();

        if (doctorName == null || date == null || time == null || reason.isEmpty()) {
            showAlert("Error", "Please fill all fields", Alert.AlertType.ERROR);
            return;
        }

        int doctorId = getDoctorIdByName(doctorName);
        if (doctorId == -1) {
            showAlert("Error", "Doctor not found", Alert.AlertType.ERROR);
            return;
        }

        String query = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, visit_reason, status) VALUES (?, ?, ?, ?, ?, 'upcoming')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientId);
            pstmt.setInt(2, doctorId);
            pstmt.setString(3, date.toString());
            pstmt.setString(4, time);
            pstmt.setString(5, reason);

            pstmt.executeUpdate();
            showAlert("Success", "Appointment booked successfully!", Alert.AlertType.INFORMATION);

            // Clear fields
            cmbSelectDoctor.setValue(null);
            dpAppointmentDate.setValue(null);
            cmbAppointmentTime.setValue(null);
            txtVisitReason.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to book appointment", Alert.AlertType.ERROR);
        }
    }

    // ==================== VIEW APPOINTMENTS ====================
    private void setupAppointmentGrid() {
        if (cmbAppointmentFilter != null && cmbAppointmentFilter.getItems().isEmpty()) {
            cmbAppointmentFilter.setItems(FXCollections.observableArrayList("All", "Upcoming", "Confirmed", "Completed", "Cancelled"));
            cmbAppointmentFilter.setValue("All");
        }
    }

    @FXML
    private void filterAppointments() {
        String filter = cmbAppointmentFilter.getValue();
        loadAppointments(filter);
    }

    private void loadAppointments(String filter) {
        gridAppointments.getChildren().clear();
        String query = "SELECT a.id, u.name, a.appointment_date, a.appointment_time, a.visit_reason, a.status " +
                        "FROM appointments a " +
                        "JOIN doctors d ON a.doctor_id = d.id " +
                        "JOIN users u ON d.user_id = u.id " +
                        "WHERE a.patient_id = ?";

        if (!filter.equals("All")) {
            query += " AND a.status = '" + filter.toLowerCase() + "'";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                VBox card = createAppointmentCard(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("visit_reason"),
                        rs.getString("status")
                );
                gridAppointments.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createAppointmentCard(int appointmentId, String doctor, String date, String time, String reason, String status) {
        VBox card = new VBox(10);
        card.getStyleClass().add("appointment-card");
        card.setPrefWidth(260);
        card.setPadding(new javafx.geometry.Insets(15));

        Label drLabel = new Label("Dr. " + doctor);
        drLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label dateTimeLabel = new Label("📅 " + date + "  |  🕒 " + time);
        dateTimeLabel.setStyle("-fx-text-fill: #64748b;");

        Label reasonLabel = new Label(reason);
        reasonLabel.setWrapText(true);
        reasonLabel.setStyle("-fx-font-style: italic;");

        Label statusLabel = new Label(status.toUpperCase());
        statusLabel.getStyleClass().addAll("status-badge", "status-" + status.toLowerCase());

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button rebookBtn = new Button("Rebook");
        rebookBtn.getStyleClass().add("secondary-btn");
        rebookBtn.setOnAction(e -> {
            cmbSelectDoctor.setValue(doctor.replaceFirst("^Dr\\.\\s*", ""));
            showBookAppointment();
        });

        actions.getChildren().add(rebookBtn);

        if ("upcoming".equalsIgnoreCase(status) || "confirmed".equalsIgnoreCase(status)) {
            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("secondary-btn");
            cancelBtn.setOnAction(e -> cancelAppointment(appointmentId));
            actions.getChildren().add(cancelBtn);
        }

        if ("completed".equalsIgnoreCase(status)) {
            Button reportBtn = new Button("View Report");
            reportBtn.getStyleClass().add("primary-btn");
            reportBtn.setOnAction(e -> showViewReports());
            actions.getChildren().add(reportBtn);
        }

        card.getChildren().addAll(drLabel, dateTimeLabel, reasonLabel, statusLabel, actions);
        return card;
    }

    private void cancelAppointment(int appointmentId) {
        String query = "UPDATE appointments SET status = 'cancelled' WHERE id = ? AND patient_id = ? AND status IN ('upcoming', 'confirmed')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, appointmentId);
            pstmt.setInt(2, patientId);
            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                showAlert("Success", "Appointment cancelled successfully.", Alert.AlertType.INFORMATION);
                loadAppointments(cmbAppointmentFilter.getValue() != null ? cmbAppointmentFilter.getValue() : "All");
                loadDashboardData();
            } else {
                showAlert("Error", "This appointment can no longer be cancelled.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to cancel appointment", Alert.AlertType.ERROR);
        }
    }

    // ==================== VIEW REPORTS ====================
    private void setupReportTable() {
        if (tblReports != null) {
            colReportAppointment.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
            colReportDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
            colReportTitle.setCellValueFactory(new PropertyValueFactory<>("reportTitle"));
            colLabTests.setCellValueFactory(new PropertyValueFactory<>("labTests"));
            colPrescription.setCellValueFactory(new PropertyValueFactory<>("prescription"));
            colLabRequestStatus.setCellValueFactory(new PropertyValueFactory<>("requestStatus"));
            colLabResultStatus.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));
            colReportDate.setCellValueFactory(new PropertyValueFactory<>("reportDate"));
            tblReports.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateSelectedReportInfo(newValue));
        }
    }

    private void loadMedicalReports() {
        ObservableList<MedicalReport> reports = FXCollections.observableArrayList();
        String query = "SELECT r.id, u.name, a.appointment_date, COALESCE(r.report_title, '') AS report_title, " +
                "COALESCE(r.lab_tests, '') AS lab_tests, COALESCE(r.diagnosis, '') AS diagnosis, " +
                "COALESCE(r.prescription, '') AS prescription, COALESCE(r.doctor_notes, '') AS doctor_notes, r.report_date, " +
                "COALESCE(l.request_status, 'not_requested') AS request_status, " +
                "COALESCE(l.result_status, 'pending') AS result_status, " +
                "COALESCE(l.file_path, '') AS file_path, COALESCE(l.price, 0) AS price, " +
                "COALESCE(l.payment_status, 'unpaid') AS payment_status, COALESCE(l.payment_method, '') AS payment_method, " +
                "COALESCE(l.admin_notes, '') AS admin_notes " +
                "FROM medical_reports r " +
                "JOIN appointments a ON r.appointment_id = a.id " +
                "JOIN doctors d ON r.doctor_id = d.id " +
                "JOIN users u ON d.user_id = u.id " +
                "LEFT JOIN lab_report_requests l ON l.medical_report_id = r.id " +
                "WHERE r.patient_id = ? " +
                "AND (COALESCE(TRIM(r.report_title), '') <> '' OR COALESCE(TRIM(r.lab_tests), '') <> '' OR COALESCE(TRIM(r.diagnosis), '') <> '') " +
                "ORDER BY a.appointment_date DESC, r.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reports.add(new MedicalReport(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("appointment_date"),
                        rs.getString("report_title"),
                        rs.getString("lab_tests"),
                        rs.getString("diagnosis"),
                        rs.getString("prescription"),
                        rs.getString("doctor_notes"),
                        rs.getString("report_date"),
                        rs.getString("request_status"),
                        rs.getString("result_status"),
                        rs.getString("file_path"),
                        rs.getDouble("price"),
                        rs.getString("payment_status"),
                        rs.getString("payment_method"),
                        rs.getString("admin_notes")
                ));
            }
            tblReports.setItems(reports);
            updateSelectedReportInfo(tblReports.getSelectionModel().getSelectedItem());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupPaymentTable() {
        if (tblPayments != null) {
            colPaymentType.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
            colPaymentReport.setCellValueFactory(new PropertyValueFactory<>("reportTitle"));
            colPaymentDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
            colPaymentAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
            colPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
            colPaymentMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
            tblPayments.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                if (lblPaymentInfo == null) return;
                if (newValue == null) {
                    lblPaymentInfo.setText("Due payment will appear here after admin prepares your lab report.");
                } else {
                    lblPaymentInfo.setText("Selected: " + newValue.getReportTitle() + " | Due: " + newValue.getAmount() + " | Type: " + newValue.getPaymentType());
                }
            });
        }
    }

    @FXML
    private void requestLabReport() {
        MedicalReport selectedReport = tblReports != null ? tblReports.getSelectionModel().getSelectedItem() : null;
        if (selectedReport == null) {
            showAlert("Error", "Please select a report first.", Alert.AlertType.ERROR);
            return;
        }
        if (selectedReport.getLabTests() == null || selectedReport.getLabTests().isBlank()) {
            showAlert("Error", "This report does not include any lab test request.", Alert.AlertType.ERROR);
            return;
        }
        if ("requested".equalsIgnoreCase(selectedReport.getRequestStatus()) || "ready".equalsIgnoreCase(selectedReport.getRequestStatus())) {
            showAlert("Info", "This lab report has already been sent to hospital admin.", Alert.AlertType.INFORMATION);
            return;
        }

        String query = "UPDATE lab_report_requests SET request_status = 'requested', requested_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP " +
                "WHERE medical_report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, selectedReport.getId());
            int updated = pstmt.executeUpdate();
            if (updated == 0) {
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO lab_report_requests (medical_report_id, patient_id, request_status, result_status, payment_status, requested_at, updated_at) " +
                                "VALUES (?, ?, 'requested', 'pending', 'unpaid', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                    insert.setInt(1, selectedReport.getId());
                    insert.setInt(2, patientId);
                    updated = insert.executeUpdate();
                }
            }
            if (updated > 0) {
                notifyAdmins(conn,
                        "New Lab Report Request",
                        SessionManager.getUserName() + " requested lab tests for report: " + selectedReport.getReportTitle());
                showAlert("Success", "Lab report request sent to hospital admin.", Alert.AlertType.INFORMATION);
                loadMedicalReports();
                loadPayments();
            } else {
                showAlert("Error", "Unable to request the lab report right now.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to request lab report.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void downloadSelectedLabReport() {
        MedicalReport selectedReport = tblReports != null ? tblReports.getSelectionModel().getSelectedItem() : null;
        if (selectedReport == null) {
            showAlert("Error", "Please select a report first.", Alert.AlertType.ERROR);
            return;
        }
        if (!"ready".equalsIgnoreCase(selectedReport.getRequestStatus())) {
            showAlert("Info", "Lab report is still pending with hospital admin.", Alert.AlertType.INFORMATION);
            return;
        }
        if (selectedReport.getFilePath() == null || selectedReport.getFilePath().isBlank()) {
            showAlert("Error", "No downloadable file has been uploaded yet.", Alert.AlertType.ERROR);
            return;
        }

        File source = new File(selectedReport.getFilePath());
        if (!source.exists()) {
            showAlert("Error", "Uploaded lab report file is missing on disk.", Alert.AlertType.ERROR);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Lab Report");
        chooser.setInitialFileName(source.getName());
        File destination = chooser.showSaveDialog(getStageFromNode(btnDownloadLabReport));
        if (destination == null) return;

        try {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showAlert("Success", "Lab report downloaded successfully.", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Error", "Failed to download lab report file.", Alert.AlertType.ERROR);
        }
    }

    private void loadPayments() {
        ObservableList<LabPayment> payments = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement appointmentStmt = conn.prepareStatement(
                     "SELECT a.id, 'appointment' AS source_type, 'Chamber Fee' AS payment_type, " +
                             "('Appointment on ' || a.appointment_date || ' at ' || a.appointment_time) AS report_title, " +
                             "u.name AS doctor_name, COALESCE(d.fee, 0) AS amount, " +
                             "COALESCE(a.payment_status, 'unpaid') AS payment_status, COALESCE(a.payment_method, '') AS payment_method " +
                             "FROM appointments a " +
                             "JOIN doctors d ON a.doctor_id = d.id " +
                             "JOIN users u ON d.user_id = u.id " +
                             "WHERE a.patient_id = ? AND a.status = 'completed' " +
                             "ORDER BY a.appointment_date DESC");
             PreparedStatement labStmt = conn.prepareStatement(
                     "SELECT l.id, 'lab' AS source_type, 'Lab Test Fee' AS payment_type, " +
                             "COALESCE(r.report_title, 'Medical Report') AS report_title, u.name AS doctor_name, " +
                             "COALESCE(l.price, 0) AS amount, COALESCE(l.payment_status, 'unpaid') AS payment_status, " +
                             "COALESCE(l.payment_method, '') AS payment_method " +
                             "FROM lab_report_requests l " +
                             "JOIN medical_reports r ON l.medical_report_id = r.id " +
                             "JOIN doctors d ON r.doctor_id = d.id " +
                             "JOIN users u ON d.user_id = u.id " +
                             "WHERE l.patient_id = ? AND l.request_status = 'ready' " +
                             "ORDER BY l.updated_at DESC")) {
            appointmentStmt.setInt(1, patientId);
            ResultSet rs = appointmentStmt.executeQuery();
            while (rs.next()) {
                payments.add(new LabPayment(
                        rs.getInt("id"),
                        rs.getString("source_type"),
                        rs.getString("payment_type"),
                        rs.getString("report_title"),
                        rs.getString("doctor_name"),
                        String.format("Tk %.2f", rs.getDouble("amount")),
                        rs.getString("payment_status"),
                        rs.getString("payment_method")
                ));
            }

            labStmt.setInt(1, patientId);
            rs = labStmt.executeQuery();
            while (rs.next()) {
                payments.add(new LabPayment(
                        rs.getInt("id"),
                        rs.getString("source_type"),
                        rs.getString("payment_type"),
                        rs.getString("report_title"),
                        rs.getString("doctor_name"),
                        String.format("Tk %.2f", rs.getDouble("amount")),
                        rs.getString("payment_status"),
                        rs.getString("payment_method")
                ));
            }
            if (tblPayments != null) {
                tblPayments.setItems(payments);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load payments.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void submitSelectedPayment() {
        LabPayment selectedPayment = tblPayments != null ? tblPayments.getSelectionModel().getSelectedItem() : null;
        if (selectedPayment == null) {
            showAlert("Error", "Please select a due payment first.", Alert.AlertType.ERROR);
            return;
        }
        if ("paid".equalsIgnoreCase(selectedPayment.getPaymentStatus())) {
            showAlert("Info", "This payment is already completed.", Alert.AlertType.INFORMATION);
            return;
        }

        String paymentMethod = cmbPaymentMethod != null ? cmbPaymentMethod.getValue() : null;
        if (paymentMethod == null || paymentMethod.isBlank()) {
            showAlert("Error", "Please select a payment method.", Alert.AlertType.ERROR);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "appointment".equalsIgnoreCase(selectedPayment.getSourceType())
                             ? "UPDATE appointments SET payment_status = 'paid', payment_method = ? WHERE id = ?"
                             : "UPDATE lab_report_requests SET payment_status = 'paid', payment_method = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            pstmt.setString(1, paymentMethod);
            pstmt.setInt(2, selectedPayment.getId());
            pstmt.executeUpdate();
            showAlert("Success", "Payment marked as paid via " + paymentMethod + ".", Alert.AlertType.INFORMATION);
            loadPayments();
            loadMedicalReports();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to complete payment.", Alert.AlertType.ERROR);
        }
    }

    private void notifyAdmins(Connection conn, String title, String message) throws SQLException {
        String adminQuery = "SELECT id FROM users WHERE LOWER(role) = 'admin'";
        try (PreparedStatement pstmt = conn.prepareStatement(adminQuery);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)")) {
                    insert.setInt(1, rs.getInt("id"));
                    insert.setString(2, title);
                    insert.setString(3, message);
                    insert.executeUpdate();
                }
            }
        }
    }

    private void updateSelectedReportInfo(MedicalReport report) {
        if (lblSelectedReportInfo == null) return;
        if (report == null) {
            lblSelectedReportInfo.setText("Select a report to request lab tests or view ready results.");
            if (txtSelectedReportDetails != null) txtSelectedReportDetails.clear();
            return;
        }
        lblSelectedReportInfo.setText("Appointment: " + safeText(report.getAppointmentDate()) +
                " | Doctor: Dr. " + report.getDoctorName() +
                " | Request: " + report.getRequestStatus() +
                " | Result: " + report.getResultStatus());
        if (txtSelectedReportDetails != null) {
            txtSelectedReportDetails.setText(
                    "Report Name: " + safeText(report.getReportTitle()) +
                            "\nLab Tests: " + safeText(report.getLabTests()) +
                            "\nReport Details: " + safeText(report.getDiagnosis()) +
                            "\nPrescription: " + safeText(report.getPrescription()) +
                            "\nDoctor Notes: " + safeText(report.getNotes()) +
                            "\nAdmin Notes: " + safeText(report.getAdminNotes()) +
                            "\nLab File: " + safeText(report.getFilePath())
            );
        }
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private Stage getStageFromNode(Node node) {
        return (Stage) node.getScene().getWindow();
    }

    private void loadNotifications() {
        ObservableList<NotificationItem> notifications = FXCollections.observableArrayList();
        String query = "SELECT id, title, message, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notifications.add(new NotificationItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getInt("is_read") == 1,
                        rs.getString("created_at")
                ));
            }

            if (notifications.isEmpty()) {
                notifications.add(new NotificationItem(
                        -1,
                        "No notifications yet",
                        "Admin or doctors send update dile ekhane show korbe.",
                        true,
                        "Just now"
                ));
            }

            if (lstNotifications != null) {
                lstNotifications.setItems(notifications);
                lstNotifications.setCellFactory(param -> new ListCell<>() {
                    @Override
                    protected void updateItem(NotificationItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setGraphic(createNotificationCard(item));
                            setText(null);
                        }
                    }
                });
            }

            markNotificationsAsRead();
            loadUnreadNotificationsCount();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load notifications", Alert.AlertType.ERROR);
        }
    }

    private VBox createNotificationCard(NotificationItem item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("notification-card");
        if (!item.isRead()) {
            card.getStyleClass().add("notification-unread");
        }

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(item.getTitle());
        titleLabel.getStyleClass().add("notification-title");

        Label badgeLabel = new Label(item.isRead() ? "READ" : "NEW");
        badgeLabel.getStyleClass().add(item.isRead() ? "notification-read-badge" : "notification-new-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, badgeLabel);

        Label messageLabel = new Label(item.getMessage());
        messageLabel.getStyleClass().add("notification-message");
        messageLabel.setWrapText(true);

        Label dateLabel = new Label(item.getCreatedAt());
        dateLabel.getStyleClass().add("notification-date");

        card.getChildren().addAll(header, messageLabel, dateLabel);
        return card;
    }

    private void markNotificationsAsRead() {
        String query = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, SessionManager.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadNotificationsPreview() {
        if (lblUnreadNotifs == null) {
            return;
        }
        loadUnreadNotificationsCount();
    }

    private void loadDashboardNotificationsPreview() {
        if (lstDashboardNotifications == null) return;
        ObservableList<NotificationItem> notifications = FXCollections.observableArrayList();
        String query = "SELECT id, title, message, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 3";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notifications.add(new NotificationItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getInt("is_read") == 1,
                        rs.getString("created_at")
                ));
            }
            if (notifications.isEmpty()) {
                notifications.add(new NotificationItem(-1, "No updates yet", "Admin alerts and doctor messages will appear here.", true, "Now"));
            }
            lstDashboardNotifications.setItems(notifications);
            lstDashboardNotifications.setCellFactory(param -> createNotificationCellFactory());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboardPostsPreview() {
        if (lstDashboardPosts == null) return;
        ObservableList<DoctorPost> posts = FXCollections.observableArrayList();
        String query = "SELECT u.name, p.title, p.content, p.category, p.created_at " +
                "FROM doctor_posts p JOIN doctors d ON p.doctor_id = d.id JOIN users u ON d.user_id = u.id " +
                "ORDER BY p.created_at DESC LIMIT 3";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                posts.add(new DoctorPost(rs.getString("name"), rs.getString("title"), rs.getString("content"), rs.getString("category"), rs.getString("created_at")));
            }
            lstDashboardPosts.setItems(posts);
            lstDashboardPosts.setCellFactory(param -> createDoctorPostCellFactory());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboardQuestionsPreview() {
        if (lstDashboardQuestions == null) return;
        ObservableList<Question> questions = FXCollections.observableArrayList();
        String query = "SELECT q.question, q.answer, u.name as doctor_name, q.created_at FROM questions q " +
                "LEFT JOIN doctors d ON q.answered_by = d.id LEFT JOIN users u ON d.user_id = u.id " +
                "WHERE q.patient_id = ? ORDER BY q.created_at DESC LIMIT 3";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(new Question(rs.getString("question"), rs.getString("answer"), rs.getString("doctor_name"), rs.getString("created_at")));
            }
            lstDashboardQuestions.setItems(questions);
            lstDashboardQuestions.setCellFactory(param -> createQuestionCellFactory());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboardDonorsPreview() {
        if (dashboardDonorGridPane == null) return;
        dashboardDonorGridPane.getChildren().clear();
        String query = "SELECT u.name, b.blood_group, b.location, b.phone, b.availability_status " +
                "FROM blood_donors b JOIN patients p ON b.patient_id = p.id JOIN users u ON p.user_id = u.id " +
                "WHERE b.availability_status = 'available' LIMIT 4";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                dashboardDonorGridPane.getChildren().add(createDonorCard(new BloodDonor(
                        rs.getString("name"),
                        rs.getString("blood_group"),
                        rs.getString("location"),
                        rs.getString("phone"),
                        rs.getString("availability_status")
                )));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ListCell<NotificationItem> createNotificationCellFactory() {
        return new ListCell<>() {
            @Override
            protected void updateItem(NotificationItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(createNotificationCard(item));
                    setText(null);
                }
            }
        };
    }

    private ListCell<DoctorPost> createDoctorPostCellFactory() {
        return new ListCell<>() {
            @Override
            protected void updateItem(DoctorPost item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(createPostCard(item));
                    setText(null);
                }
            }
        };
    }

    private ListCell<Question> createQuestionCellFactory() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Question item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(createQACard(item));
                    setText(null);
                }
            }
        };
    }

    // ==================== DOCTOR POSTS ====================
    @FXML
    private void filterPosts() {
        String category = cmbPostCategory.getValue();
        loadDoctorPosts(category);
    }

    private void loadDoctorPosts(String category) {
        ObservableList<DoctorPost> posts = FXCollections.observableArrayList();
        StringBuilder query = new StringBuilder(
                "SELECT u.name, p.title, p.content, p.category, p.created_at " +
                        "FROM doctor_posts p " +
                        "JOIN doctors d ON p.doctor_id = d.id " +
                        "JOIN users u ON d.user_id = u.id"
        );

        if (!category.equals("All")) {
            query.append(" WHERE p.category = '").append(category).append("'");
        }
        query.append(" ORDER BY p.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query.toString())) {

            while (rs.next()) {
                posts.add(new DoctorPost(
                        rs.getString("name"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("category"),
                        rs.getString("created_at")
                ));
            }
            lstDoctorPosts.setItems(posts);
            lstDoctorPosts.setCellFactory(param -> createDoctorPostCellFactory());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createPostCard(DoctorPost post) {
        VBox card = new VBox(10);
        card.getStyleClass().add("qa-card");
        card.setStyle("-fx-padding: 18; -fx-background-radius: 14;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label categoryBadge = new Label(post.category);
        categoryBadge.setStyle(
            "-fx-background-color: rgba(102,126,234,0.15); " +
            "-fx-text-fill: #667eea; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: 700; " +
            "-fx-padding: 4 10; " +
            "-fx-background-radius: 12;"
        );

        Label dateLabel = new Label(post.createdAt);
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(categoryBadge, spacer, dateLabel);

        Label titleLabel = new Label(post.title);
        titleLabel.setStyle(
            "-fx-text-fill: #1a202c; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: 800;"
        );
        titleLabel.setWrapText(true);

        Label contentLabel = new Label(post.content);
        contentLabel.setStyle(
            "-fx-text-fill: #475569; " +
            "-fx-font-size: 13px; " +
            "-fx-line-spacing: 2;"
        );
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(700);

        Label authorLabel = new Label("By: Dr. " + post.doctorName);
        authorLabel.setStyle(
            "-fx-text-fill: #667eea; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: 600;"
        );

        card.getChildren().addAll(header, titleLabel, contentLabel, authorLabel);
        return card;
    }

    // ==================== Q&A ====================
    @FXML
    private void askQuestion() {
        String question = txtAskQuestion.getText().trim();
        if (question.isEmpty()) {
            showAlert("Error", "Please enter your question", Alert.AlertType.ERROR);
            return;
        }

        String query = "INSERT INTO questions (patient_id, question) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientId);
            pstmt.setString(2, question);
            pstmt.executeUpdate();

            showAlert("Success", "Question submitted successfully!", Alert.AlertType.INFORMATION);
            txtAskQuestion.clear();
            loadMyQuestions();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to submit question", Alert.AlertType.ERROR);
        }
    }

    private void loadMyQuestions() {
        ObservableList<Question> questions = FXCollections.observableArrayList();
        String query = "SELECT q.question, q.answer, u.name as doctor_name, q.created_at " +
                "FROM questions q " +
                "LEFT JOIN doctors d ON q.answered_by = d.id " +
                "LEFT JOIN users u ON d.user_id = u.id " +
                "WHERE q.patient_id = ? ORDER BY q.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                questions.add(new Question(
                        rs.getString("question"),
                        rs.getString("answer"),
                        rs.getString("doctor_name"),
                        rs.getString("created_at")
                ));
            }
            lstMyQuestions.setItems(questions);
            lstMyQuestions.setCellFactory(param -> createQuestionCellFactory());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createQACard(Question q) {
        VBox card = new VBox(12);
        card.getStyleClass().add("qa-card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label questionLabel = new Label(q.question);
        questionLabel.getStyleClass().add("question-text");
        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(500);

        Label badge;
        if (q.answer != null && !q.answer.isEmpty()) {
            badge = new Label("✓ Answered");
            badge.getStyleClass().add("answer-badge");
        } else {
            badge = new Label("⏳ Pending");
            badge.getStyleClass().add("pending-badge");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(questionLabel, spacer, badge);

        VBox content = new VBox(8);

        Label dateLabel = new Label("Asked on: " + q.createdAt);
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        if (q.answer != null && !q.answer.isEmpty()) {
            VBox answerBox = new VBox(5);

            Label answerHeader = new Label("Answer from Dr. " + (q.doctorName != null ? q.doctorName : "Unknown"));
            answerHeader.setStyle("-fx-text-fill: #10b981; -fx-font-size: 13px; -fx-font-weight: 700;");

            Label answerText = new Label(q.answer);
            answerText.getStyleClass().add("answer-text");
            answerText.setWrapText(true);
            answerText.setMaxWidth(600);

            answerBox.getChildren().addAll(answerHeader, answerText);
            content.getChildren().addAll(dateLabel, answerBox);
        } else {
            content.getChildren().add(dateLabel);
        }

        card.getChildren().addAll(header, content);
        return card;
    }

    // ==================== BLOOD DONATION ====================
    @FXML
    private void joinAsDonor() {
        String bloodGroup = cmbBloodGroup.getValue();
        String location = txtDonorLocation.getText().trim();
        String phone = txtDonorPhone.getText().trim();
        String status = cmbAvailabilityStatus.getValue();

        if (bloodGroup == null || location.isEmpty() || phone.isEmpty()) {
            showAlert("Error", "Please fill all fields", Alert.AlertType.ERROR);
            return;
        }

        String query = "INSERT OR REPLACE INTO blood_donors (patient_id, blood_group, location, phone, availability_status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientId);
            pstmt.setString(2, bloodGroup);
            pstmt.setString(3, location);
            pstmt.setString(4, phone);
            pstmt.setString(5, status.toLowerCase());

            pstmt.executeUpdate();
            showAlert("Success", "Registered as blood donor successfully!", Alert.AlertType.INFORMATION);

            // Hide registration form after successful registration
            donorRegistrationForm.setVisible(false);
            donorRegistrationForm.setManaged(false);

            loadBloodDonors();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to register as donor", Alert.AlertType.ERROR);
        }
    }

    private VBox createDonorCard(BloodDonor donor) {
        VBox card = new VBox(12);
        card.getStyleClass().add("donor-card");
        card.setPrefWidth(320);
        card.setMaxWidth(320);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Blood group badge
        Label bloodBadge = new Label(donor.getBloodGroup());
        bloodBadge.getStyleClass().add("blood-group-badge");

        VBox info = new VBox(5);
        Label nameLabel = new Label(donor.getName());
        nameLabel.getStyleClass().add("donor-name");

        Label statusLabel = new Label(donor.getStatus().toUpperCase());
        statusLabel.getStyleClass().add("donor-status-available");

        info.getChildren().addAll(nameLabel, statusLabel);
        header.getChildren().addAll(bloodBadge, info);

        // Details
        VBox details = new VBox(8);

        HBox locationBox = new HBox(8);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locIcon = new Label("📍");
        Label locText = new Label(donor.getLocation());
        locText.getStyleClass().add("donor-detail");
        locationBox.getChildren().addAll(locIcon, locText);

        HBox phoneBox = new HBox(8);
        phoneBox.setAlignment(Pos.CENTER_LEFT);
        Label phoneIcon = new Label("📞");
        Label phoneText = new Label(donor.getPhone());
        phoneText.getStyleClass().add("donor-detail");
        phoneBox.getChildren().addAll(phoneIcon, phoneText);

        details.getChildren().addAll(locationBox, phoneBox);

        card.getChildren().addAll(header, details);
        return card;
    }

    @FXML
    private void searchDonors() {
        donorGridPane.getChildren().clear();
        String bloodGroup = txtSearchBloodGroup.getText().trim();
        if (bloodGroup.isEmpty()) {
            loadBloodDonors();
            return;
        }

        String query = "SELECT u.name, b.blood_group, b.location, b.phone, b.availability_status " +
                "FROM blood_donors b " +
                "JOIN patients p ON b.patient_id = p.id " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE b.blood_group = ? AND b.availability_status = 'available'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, bloodGroup);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                BloodDonor donor = new BloodDonor(
                        rs.getString("name"),
                        rs.getString("blood_group"),
                        rs.getString("location"),
                        rs.getString("phone"),
                        rs.getString("availability_status")
                );
                donorGridPane.getChildren().add(createDonorCard(donor));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBloodDonors() {
        donorGridPane.getChildren().clear();

        // Check if current user is already registered, hide form if true
        checkAndHideDonorForm();

        String query = "SELECT u.name, b.blood_group, b.location, b.phone, b.availability_status " +
                "FROM blood_donors b " +
                "JOIN patients p ON b.patient_id = p.id " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE b.availability_status = 'available'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                BloodDonor donor = new BloodDonor(
                        rs.getString("name"),
                        rs.getString("blood_group"),
                        rs.getString("location"),
                        rs.getString("phone"),
                        rs.getString("availability_status")
                );
                donorGridPane.getChildren().add(createDonorCard(donor));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkAndHideDonorForm() {
        String query = "SELECT COUNT(*) as count FROM blood_donors WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                donorRegistrationForm.setVisible(false);
                donorRegistrationForm.setManaged(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== SMART ASSISTANT ====================
    @FXML
    private void analyzeSymptoms() {
        String symptoms = txtSymptoms.getText().trim().toLowerCase();
        if (symptoms.isEmpty()) {
            showAlert("Error", "Please enter your symptoms", Alert.AlertType.ERROR);
            return;
        }

        String response = getSmartResponse(symptoms);
        txtAssistantResponse.setText(response);
    }

    private String getSmartResponse(String symptoms) {
        // Emergency keywords
        if (symptoms.contains("chest pain") || symptoms.contains("heart attack") ||
                symptoms.contains("difficulty breathing") || symptoms.contains("severe bleeding") ||
                symptoms.contains("unconscious") || symptoms.contains("stroke")) {
            return "⚠️ EMERGENCY WARNING ⚠️\n\n" +
                    "Your symptoms indicate a potential medical emergency!\n" +
                    "Please call emergency services immediately or visit the nearest hospital.\n\n" +
                    "Emergency Hotline: 999";
        }

        // Department recommendations
        if (symptoms.contains("fever") || symptoms.contains("cough") || symptoms.contains("cold")) {
            return "Recommendation: General Medicine Department\n\n" +
                    "Your symptoms suggest a common respiratory condition. " +
                    "Please consult with a General Physician.\n\n" +
                    "Common treatments: Rest, hydration, and over-the-counter medications.";
        }

        if (symptoms.contains("stomach") || symptoms.contains("abdominal") || symptoms.contains("diarrhea")) {
            return "Recommendation: Gastroenterology Department\n\n" +
                    "Your symptoms are related to digestive issues. " +
                    "Please consult with a Gastroenterologist.\n\n" +
                    "General advice: Stay hydrated and avoid heavy meals.";
        }

        if (symptoms.contains("headache") || symptoms.contains("migraine") || symptoms.contains("dizziness")) {
            return "Recommendation: Neurology Department\n\n" +
                    "Your symptoms may require neurological evaluation. " +
                    "Please consult with a Neurologist.\n\n" +
                    "Immediate relief: Rest in a dark, quiet room.";
        }

        if (symptoms.contains("skin") || symptoms.contains("rash") || symptoms.contains("itching")) {
            return "Recommendation: Dermatology Department\n\n" +
                    "Your symptoms are related to skin conditions. " +
                    "Please consult with a Dermatologist.\n\n" +
                    "General advice: Avoid scratching and keep the area clean.";
        }

        if (symptoms.contains("anxiety") || symptoms.contains("depression") || symptoms.contains("stress")) {
            return "Recommendation: Psychiatry/Mental Health Department\n\n" +
                    "Your symptoms suggest mental health concerns. " +
                    "Please consult with a Psychiatrist or Mental Health Professional.\n\n" +
                    "Remember: Mental health is as important as physical health.";
        }

        return "Based on your symptoms, we recommend consulting with a General Physician first.\n\n" +
                "They will be able to provide an initial assessment and refer you to a specialist if needed.\n\n" +
                "Please book an appointment through our 'Book Appointment' section.";
    }

    // ==================== UTILITY METHODS ====================
    private int getPatientId(int userId) {
        String query = "SELECT id FROM patients WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Means not found
    }

    private int getDoctorIdByName(String name) {
        String query = "SELECT d.id FROM doctors d JOIN users u ON d.user_id = u.id WHERE u.name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==================== MODEL CLASSES ====================
    public static class Doctor {
        private int id;
        private String name, specialization, experience, qualification;
        private double fee;

        public Doctor(int id, String name, String specialization, String experience, String qualification, double fee) {
            this.id = id;
            this.name = name;
            this.specialization = specialization;
            this.experience = experience;
            this.qualification = qualification;
            this.fee = fee;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSpecialization() { return specialization; }
        public String getExperience() { return experience; }
        public String getQualification() { return qualification; }
        public double getFee() { return fee; }
    }

    public static class Appointment {
        private String doctorName, date, time, reason, status;

        public Appointment(String doctorName, String date, String time, String reason, String status) {
            this.doctorName = doctorName;
            this.date = date;
            this.time = time;
            this.reason = reason;
            this.status = status;
        }

        public String getDoctorName() { return doctorName; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
    }

    public static class MedicalReport {
        private int id;
        private String doctorName, appointmentDate, reportTitle, labTests, diagnosis, prescription, notes, reportDate;
        private String requestStatus, resultStatus, filePath, paymentStatus, paymentMethod, adminNotes;
        private double price;

        public MedicalReport(int id, String doctorName, String appointmentDate, String reportTitle, String labTests, String diagnosis,
                             String prescription, String notes, String reportDate, String requestStatus,
                             String resultStatus, String filePath, double price, String paymentStatus,
                             String paymentMethod, String adminNotes) {
            this.id = id;
            this.doctorName = doctorName;
            this.appointmentDate = appointmentDate;
            this.reportTitle = reportTitle;
            this.labTests = labTests;
            this.diagnosis = diagnosis;
            this.prescription = prescription;
            this.notes = notes;
            this.reportDate = reportDate;
            this.requestStatus = requestStatus;
            this.resultStatus = resultStatus;
            this.filePath = filePath;
            this.price = price;
            this.paymentStatus = paymentStatus;
            this.paymentMethod = paymentMethod;
            this.adminNotes = adminNotes;
        }

        public int getId() { return id; }
        public String getDoctorName() { return doctorName; }
        public String getAppointmentDate() { return appointmentDate; }
        public String getReportTitle() { return reportTitle; }
        public String getLabTests() { return labTests; }
        public String getDiagnosis() { return diagnosis; }
        public String getPrescription() { return prescription; }
        public String getNotes() { return notes; }
        public String getReportDate() { return reportDate; }
        public String getRequestStatus() { return requestStatus; }
        public String getResultStatus() { return resultStatus; }
        public String getFilePath() { return filePath; }
        public double getPrice() { return price; }
        public String getPaymentStatus() { return paymentStatus; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getAdminNotes() { return adminNotes; }
    }

    public static class LabPayment {
        private int id;
        private String sourceType, paymentType, reportTitle, doctorName, amount, paymentStatus, paymentMethod;

        public LabPayment(int id, String sourceType, String paymentType, String reportTitle, String doctorName, String amount, String paymentStatus, String paymentMethod) {
            this.id = id;
            this.sourceType = sourceType;
            this.paymentType = paymentType;
            this.reportTitle = reportTitle;
            this.doctorName = doctorName;
            this.amount = amount;
            this.paymentStatus = paymentStatus;
            this.paymentMethod = paymentMethod;
        }

        public int getId() { return id; }
        public String getSourceType() { return sourceType; }
        public String getPaymentType() { return paymentType; }
        public String getReportTitle() { return reportTitle; }
        public String getDoctorName() { return doctorName; }
        public String getAmount() { return amount; }
        public String getPaymentStatus() { return paymentStatus; }
        public String getPaymentMethod() { return paymentMethod; }
    }

    public static class DoctorPost {
        private String doctorName, title, content, category, createdAt;

        public DoctorPost(String doctorName, String title, String content, String category, String createdAt) {
            this.doctorName = doctorName;
            this.title = title;
            this.content = content;
            this.category = category;
            this.createdAt = createdAt;
        }

        @Override
        public String toString() {
            return "[" + category + "] " + title + "\nBy: Dr. " + doctorName + " | " + createdAt;
        }
    }

    public static class Question {
        private String question, answer, doctorName, createdAt;

        public Question(String question, String answer, String doctorName, String createdAt) {
            this.question = question;
            this.answer = answer;
            this.doctorName = doctorName;
            this.createdAt = createdAt;
        }

        @Override
        public String toString() {
            String display = "Q: " + question + "\n" + createdAt;
            if (answer != null && !answer.isEmpty()) {
                display += "\nA: " + answer + " (By: Dr. " + doctorName + ")";
            } else {
                display += "\n[Not answered yet]";
            }
            return display;
        }
    }

    public static class BloodDonor {
        private String name, bloodGroup, location, phone, status;

        public BloodDonor(String name, String bloodGroup, String location, String phone, String status) {
            this.name = name;
            this.bloodGroup = bloodGroup;
            this.location = location;
            this.phone = phone;
            this.status = status;
        }

        public String getName() { return name; }
        public String getBloodGroup() { return bloodGroup; }
        public String getLocation() { return location; }
        public String getPhone() { return phone; }
        public String getStatus() { return status; }
    }

    public static class NotificationItem {
        private final int id;
        private final String title;
        private final String message;
        private final boolean read;
        private final String createdAt;

        public NotificationItem(int id, String title, String message, boolean read, String createdAt) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.read = read;
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public boolean isRead() { return read; }
        public String getCreatedAt() { return createdAt; }
    }
}
