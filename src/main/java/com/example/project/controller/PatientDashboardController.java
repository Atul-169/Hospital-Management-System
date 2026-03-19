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
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;

public class PatientDashboardController {

    // Navigation
    @FXML private Button btnDashboard, btnSearchDoctors, btnBookAppointment, btnViewAppointments;
    @FXML private Button btnViewReports, btnDoctorPosts, btnQA, btnBloodDonation, btnSmartAssistant;
    @FXML private Button btnProfile, btnLogout, btnProfileSettings;
    @FXML private Label lblWelcome;
    @FXML private Button btnLightMode, btnDarkMode;

    // Main Content Area
    @FXML private StackPane contentArea;
    @FXML private VBox sidebar;

    // Dashboard Section
    @FXML private VBox dashboardSection;
    @FXML private Label lblUpcomingAppointments, lblTotalReports, lblUnreadNotifs, lblBloodDonationStatus;

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
    @FXML private TableColumn<MedicalReport, String> colReportDoctor, colDiagnosis, colPrescription, colNotes, colReportDate;

    // Doctor Posts Section
    @FXML private VBox doctorPostsSection;
    @FXML private ComboBox<String> cmbPostCategory;
    @FXML private ListView<DoctorPost> lstDoctorPosts;

    // Q&A Section
    @FXML private VBox qaSection;
    @FXML private TextArea txtAskQuestion;
    @FXML private ListView<Question> lstMyQuestions;

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
                    "All", "Upcoming", "Completed", "Cancelled"
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

        // Setup table columns
        setupAppointmentGrid();
        setupReportTable();

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
        SceneManager.switchScene(btnLogout, "/fxml/login.fxml");
    }

    // ==================== DASHBOARD ====================
    private void loadDashboardData() {
        lblUpcomingAppointments.setText(String.valueOf(getUpcomingAppointmentsCount()));
        lblTotalReports.setText(String.valueOf(getTotalReportsCount()));
        lblUnreadNotifs.setText(String.valueOf(getUnreadNotificationsCount()));
        lblBloodDonationStatus.setText(getBloodDonationStatus());
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
        // Notifications moved to sidebar if needed, currently skipping badge update
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
        Label overlayFee = new Label("৳ " + doctor.getFee());
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
            cmbAppointmentFilter.setItems(FXCollections.observableArrayList("All", "Pending", "Confirmed", "Completed", "Cancelled"));
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
        String query = "SELECT u.name, a.appointment_date, a.appointment_time, a.visit_reason, a.status " +
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

    private VBox createAppointmentCard(String doctor, String date, String time, String reason, String status) {
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

        card.getChildren().addAll(drLabel, dateTimeLabel, reasonLabel, statusLabel);
        return card;
    }

    // ==================== VIEW REPORTS ====================
    private void setupReportTable() {
        if (tblReports != null) {
            colReportDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
            colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
            colPrescription.setCellValueFactory(new PropertyValueFactory<>("prescription"));
            colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
            colReportDate.setCellValueFactory(new PropertyValueFactory<>("reportDate"));
        }
    }

    private void loadMedicalReports() {
        ObservableList<MedicalReport> reports = FXCollections.observableArrayList();
        String query = "SELECT u.name, r.diagnosis, r.prescription, r.doctor_notes, r.report_date " +
                "FROM medical_reports r " +
                "JOIN doctors d ON r.doctor_id = d.id " +
                "JOIN users u ON d.user_id = u.id " +
                "WHERE r.patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reports.add(new MedicalReport(
                        rs.getString("name"),
                        rs.getString("diagnosis"),
                        rs.getString("prescription"),
                        rs.getString("doctor_notes"),
                        rs.getString("report_date")
                ));
            }
            tblReports.setItems(reports);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

            // Custom cell factory for styled post cards
            lstDoctorPosts.setCellFactory(param -> new javafx.scene.control.ListCell<DoctorPost>() {
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
            });

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

            // Custom cell factory for styled Q&A cards
            lstMyQuestions.setCellFactory(param -> new javafx.scene.control.ListCell<Question>() {
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
            });

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
        private String doctorName, diagnosis, prescription, notes, reportDate;

        public MedicalReport(String doctorName, String diagnosis, String prescription, String notes, String reportDate) {
            this.doctorName = doctorName;
            this.diagnosis = diagnosis;
            this.prescription = prescription;
            this.notes = notes;
            this.reportDate = reportDate;
        }

        public String getDoctorName() { return doctorName; }
        public String getDiagnosis() { return diagnosis; }
        public String getPrescription() { return prescription; }
        public String getNotes() { return notes; }
        public String getReportDate() { return reportDate; }
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
}
