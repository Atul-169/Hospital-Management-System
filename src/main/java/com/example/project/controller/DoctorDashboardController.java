package com.example.project.controller;

import com.example.project.util.DatabaseConnection;
import com.example.project.util.SceneManager;
import com.example.project.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DoctorDashboardController {

    // Dashboard Statistics Labels
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label pendingReportsLabel;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalPostsLabel;
    @FXML private Label answeredQuestionsLabel;

    // Navigation Buttons
    @FXML private Button dashboardBtn;
    @FXML private Button appointmentsBtn;
    @FXML private Button patientsBtn;
    @FXML private Button reportsBtn;
    @FXML private Button prescriptionsBtn;
    @FXML private Button healthPostsBtn;
    @FXML private Button qaBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;

    // Content Panels
    @FXML private VBox dashboardPanel;
    @FXML private VBox appointmentsPanel;
    @FXML private VBox patientsPanel;
    @FXML private VBox reportsPanel;
    @FXML private VBox prescriptionsPanel;
    @FXML private VBox healthPostsPanel;
    @FXML private VBox qaPanel;
    @FXML private VBox profilePanel;

    // Alert/Status Labels
    @FXML private Label statusMessageLabel;
    @FXML private ListView<NotificationItem> notificationsList;

    // Appointments Components
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, Integer> appointmentIdCol;
    @FXML private TableColumn<Appointment, String> patientNameCol;
    @FXML private TableColumn<Appointment, String> appointmentDateCol;
    @FXML private TableColumn<Appointment, String> appointmentTimeCol;
    @FXML private TableColumn<Appointment, String> appointmentReasonCol;
    @FXML private TableColumn<Appointment, String> appointmentStatusCol;
    @FXML private ComboBox<String> filterAppointmentStatus;

    // Patient Details Components
    @FXML private TableView<PatientInfo> patientsTable;
    @FXML private TableColumn<PatientInfo, String> patientNameDetailCol;
    @FXML private TableColumn<PatientInfo, String> patientPhoneCol;
    @FXML private TableColumn<PatientInfo, String> patientBloodGroupCol;
    @FXML private TableColumn<PatientInfo, Integer> patientVisitsCol;
    @FXML private TextField patientSearchField;
    @FXML private TextArea patientMedicalHistory;

    // Write Report Components
    @FXML private ComboBox<String> reportAppointmentCombo;
    @FXML private Label reportPatientInfoLabel;
    @FXML private TextField reportTitleField;
    @FXML private TextArea reportLabTests;
    @FXML private TextArea reportDiagnosis;
    @FXML private TextArea reportTreatmentNotes;
    @FXML private TextArea reportFollowUpAdvice;
    @FXML private Button saveReportBtn;

    // Prescription Components
    @FXML private ComboBox<String> prescriptionAppointmentCombo;
    @FXML private Label prescriptionPatientInfoLabel;
    @FXML private TextField medicineName;
    @FXML private TextField medicineDosage;
    @FXML private TextField medicineDuration;
    @FXML private TextArea specialInstructions;
    @FXML private Button addPrescriptionBtn;
    @FXML private TextArea prescriptionPreview;

    // Update Appointment Status Components
    @FXML private ComboBox<String> updateAppointmentCombo;
    @FXML private ComboBox<String> newStatusCombo;
    @FXML private Button updateStatusBtn;

    // Health Posts Components
    @FXML private TextField postTitle;
    @FXML private ComboBox<String> postCategory;
    @FXML private TextArea postContent;
    @FXML private Button createPostBtn;
    @FXML private TableView<HealthPost> myPostsTable;
    @FXML private TableColumn<HealthPost, String> postTitleCol;
    @FXML private TableColumn<HealthPost, String> postCategoryCol;
    @FXML private TableColumn<HealthPost, String> postDateCol;

    // Q&A Components
    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, String> questionPatientCol;
    @FXML private TableColumn<Question, String> questionTextCol;
    @FXML private TableColumn<Question, String> questionDateCol;
    @FXML private TextArea answerText;
    @FXML private Button submitAnswerBtn;

    // Profile Components
    @FXML private TextField profileSpecializationField;
    @FXML private TextField profileQualificationField;
    @FXML private TextField profileExperienceField;
    @FXML private TextField profilePhoneField;
    @FXML private TextField profileFeeField;
    @FXML private Button saveProfileBtn;
    @FXML private Button btnLightMode, btnDarkMode;
    @FXML private StackPane contentArea;   // make sure fx:id="contentArea" exists on your StackPane in FXML
    private boolean isDarkMode = false;

    private int currentDoctorId;

    @FXML
    public void initialize() {
        loadDoctorId();
        setupTableColumns();
        populateComboBoxes();
        checkProfileCompletion();
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


    private void checkProfileCompletion() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT profile_completed FROM doctors WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt("profile_completed") != 1) {
                    showProfile();
                } else {
                    showDashboard();
                }
            } else {
                createDoctorProfilePlaceholder();
                showProfile();
            }
        } catch (SQLException e) {
            showStatusMessage("Error checking profile: " + e.getMessage(), false);
            showDashboard();
        }
    }


    private void populateComboBoxes() {
        // Populate appointment filter
        if (filterAppointmentStatus != null) {
            filterAppointmentStatus.setItems(FXCollections.observableArrayList(
                "All", "Today", "Upcoming", "Completed", "Cancelled"
            ));
            filterAppointmentStatus.setValue("All");
        }

        // Populate status update combo
        if (newStatusCombo != null) {
            newStatusCombo.setItems(FXCollections.observableArrayList(
                "Pending", "Confirmed", "Completed", "Cancelled"
            ));
        }

        // Populate post category
        if (postCategory != null) {
            postCategory.setItems(FXCollections.observableArrayList(
                "General", "Nutrition", "Exercise", "Mental Health", "Disease Prevention", "First Aid"
            ));
            postCategory.setValue("General");
        }

        if (reportAppointmentCombo != null) {
            reportAppointmentCombo.valueProperty().addListener((obs, oldValue, newValue) ->
                    updateSelectedPatientLabel(reportPatientInfoLabel, newValue));
        }

        if (prescriptionAppointmentCombo != null) {
            prescriptionAppointmentCombo.valueProperty().addListener((obs, oldValue, newValue) ->
                    updateSelectedPatientLabel(prescriptionPatientInfoLabel, newValue));
        }
    }

    private void loadDoctorId() {
        int userId = SessionManager.getUserId();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id FROM doctors WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentDoctorId = rs.getInt("id");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load doctor profile: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        // Appointments Table
        if (appointmentIdCol != null) {
            appointmentIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            patientNameCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
            appointmentDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
            appointmentTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
            appointmentReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
            appointmentStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        // Patients Table
        if (patientNameDetailCol != null) {
            patientNameDetailCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            patientPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            patientBloodGroupCol.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
            patientVisitsCol.setCellValueFactory(new PropertyValueFactory<>("totalVisits"));
        }

        // Health Posts Table
        if (postTitleCol != null) {
            postTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            postCategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
            postDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        }

        // Questions Table
        if (questionPatientCol != null) {
            questionPatientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
            questionTextCol.setCellValueFactory(new PropertyValueFactory<>("question"));
            questionDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        }
    }

    // Dashboard Statistics
    private void loadDashboardStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();

            // Today's Appointments
            String todayQuery = "SELECT COUNT(*) as count FROM appointments WHERE doctor_id = " + currentDoctorId +
                    " AND appointment_date = '" + LocalDate.now().toString() + "'";
            ResultSet rs = stmt.executeQuery(todayQuery);
            if (rs.next()) {
                todayAppointmentsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Total Patients Treated
            String patientsQuery = "SELECT COUNT(DISTINCT patient_id) as count FROM appointments WHERE doctor_id = " + currentDoctorId;
            rs = stmt.executeQuery(patientsQuery);
            if (rs.next()) {
                totalPatientsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Pending Reports
            String pendingReportsQuery = "SELECT COUNT(*) as count FROM appointments WHERE doctor_id = " + currentDoctorId +
                    " AND status = 'completed' AND id NOT IN (SELECT appointment_id FROM medical_reports WHERE appointment_id IS NOT NULL)";
            rs = stmt.executeQuery(pendingReportsQuery);
            if (rs.next()) {
                pendingReportsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Total Income
            String incomeQuery = "SELECT SUM(d.fee) as total FROM appointments a JOIN doctors d ON a.doctor_id = d.id " +
                    "WHERE a.doctor_id = " + currentDoctorId + " AND a.status = 'completed'";
            rs = stmt.executeQuery(incomeQuery);
            if (rs.next()) {
                double income = rs.getDouble("total");
                totalIncomeLabel.setText(String.format("Tk %.2f", income));
            }

            // Total Posts
            String postsQuery = "SELECT COUNT(*) as count FROM doctor_posts WHERE doctor_id = " + currentDoctorId;
            rs = stmt.executeQuery(postsQuery);
            if (rs.next()) {
                totalPostsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Answered Questions
            String answeredQuery = "SELECT COUNT(*) as count FROM questions WHERE answered_by = " + currentDoctorId;
            rs = stmt.executeQuery(answeredQuery);
            if (rs.next()) {
                answeredQuestionsLabel.setText(String.valueOf(rs.getInt("count")));
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load dashboard statistics: " + e.getMessage());
        }

        loadRecentNotifications();
    }

    // Navigation Methods
    @FXML
    private void showDashboard() {
        hideAllPanels();
        if (dashboardPanel != null) {
            dashboardPanel.setVisible(true);
            loadDashboardStatistics();
        }
    }

    @FXML
    private void showAppointments() {
        hideAllPanels();
        if (appointmentsPanel != null) {
            appointmentsPanel.setVisible(true);
            loadAppointments("all");
            loadAppointmentStatusOptions();
        }
    }

    @FXML
    private void showPatients() {
        hideAllPanels();
        if (patientsPanel != null) {
            patientsPanel.setVisible(true);
            loadPatients();
        }
    }

    @FXML
    private void showReports() {
        hideAllPanels();
        if (reportsPanel != null) {
            reportsPanel.setVisible(true);
            loadAppointmentsForReport();
        }
    }

    @FXML
    private void showPrescriptions() {
        hideAllPanels();
        if (prescriptionsPanel != null) {
            prescriptionsPanel.setVisible(true);
            loadAppointmentsForPrescription();
        }
    }

    @FXML
    private void showHealthPosts() {
        hideAllPanels();
        if (healthPostsPanel != null) {
            healthPostsPanel.setVisible(true);
            loadMyPosts();
        }
    }

    @FXML
    private void showQA() {
        hideAllPanels();
        if (qaPanel != null) {
            qaPanel.setVisible(true);
            loadQuestions();
        }
    }

    @FXML
    private void showProfile() {
        hideAllPanels();
        if (profilePanel != null) {
            profilePanel.setVisible(true);
            loadDoctorProfile();
        }
    }

    private void hideAllPanels() {
        if (dashboardPanel != null) dashboardPanel.setVisible(false);
        if (appointmentsPanel != null) appointmentsPanel.setVisible(false);
        if (patientsPanel != null) patientsPanel.setVisible(false);
        if (reportsPanel != null) reportsPanel.setVisible(false);
        if (prescriptionsPanel != null) prescriptionsPanel.setVisible(false);
        if (healthPostsPanel != null) healthPostsPanel.setVisible(false);
        if (qaPanel != null) qaPanel.setVisible(false);
        if (profilePanel != null) profilePanel.setVisible(false);
        removeActiveClass();
    }

    private void removeActiveClass() {
        if (dashboardBtn != null) dashboardBtn.getStyleClass().remove("active");
        if (appointmentsBtn != null) appointmentsBtn.getStyleClass().remove("active");
        if (patientsBtn != null) patientsBtn.getStyleClass().remove("active");
        if (reportsBtn != null) reportsBtn.getStyleClass().remove("active");
        if (prescriptionsBtn != null) prescriptionsBtn.getStyleClass().remove("active");
        if (healthPostsBtn != null) healthPostsBtn.getStyleClass().remove("active");
        if (qaBtn != null) qaBtn.getStyleClass().remove("active");
        if (profileBtn != null) profileBtn.getStyleClass().remove("active");
    }

    // Appointments Methods
    private void loadAppointments(String filter) {
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String baseQuery = "SELECT a.id, u.name as patient_name, a.appointment_date, a.appointment_time, " +
                    "a.visit_reason, a.status FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id " +
                    "WHERE a.doctor_id = " + currentDoctorId;

            String query = baseQuery;
            if (filter.equals("today")) {
                query += " AND a.appointment_date = '" + LocalDate.now().toString() + "'";
            } else if (filter.equals("upcoming")) {
                query += " AND a.status = 'upcoming'";
            } else if (filter.equals("completed")) {
                query += " AND a.status = 'completed'";
            } else if (filter.equals("cancelled")) {
                query += " AND a.status = 'cancelled'";
            }

            query += " ORDER BY a.appointment_date DESC, a.appointment_time DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                appointments.add(new Appointment(
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("appointment_date"),
                    rs.getString("appointment_time"),
                    rs.getString("visit_reason"),
                    rs.getString("status")
                ));
            }
            appointmentsTable.setItems(appointments);
            populateUpdateAppointmentCombo(appointments);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load appointments: " + e.getMessage());
        }
    }

    private void loadAppointmentStatusOptions() {
        if (newStatusCombo != null && newStatusCombo.getValue() == null) {
            newStatusCombo.setValue("Confirmed");
        }
    }

    private void populateUpdateAppointmentCombo(ObservableList<Appointment> appointments) {
        if (updateAppointmentCombo == null) {
            return;
        }

        ObservableList<String> appointmentOptions = FXCollections.observableArrayList();
        for (Appointment appointment : appointments) {
            appointmentOptions.add(appointment.getId() + " - " + appointment.getPatientName() + " (" +
                    appointment.getDate() + " " + appointment.getTime() + ") [" + appointment.getStatus() + "]");
        }

        updateAppointmentCombo.setItems(appointmentOptions);
        if (!appointmentOptions.isEmpty() && updateAppointmentCombo.getValue() == null) {
            updateAppointmentCombo.setValue(appointmentOptions.getFirst());
        }
    }

    @FXML
    private void filterAppointments() {
        String filter = filterAppointmentStatus.getValue();
        if (filter == null) filter = "all";
        loadAppointments(filter.toLowerCase());
    }

    @FXML
    private void updateAppointmentStatus() {
        String selectedAppointment = updateAppointmentCombo.getValue();
        String newStatus = newStatusCombo.getValue();

        if (selectedAppointment == null || newStatus == null) {
            showAlert("Error", "Please select appointment and status!");
            return;
        }

        int appointmentId = Integer.parseInt(selectedAppointment.split(" - ")[0]);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE appointments SET status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, newStatus.toLowerCase());
            pstmt.setInt(2, appointmentId);
            pstmt.executeUpdate();

            showAlert("Success", "Appointment status updated successfully!");
            loadAppointments("all");
            loadAppointmentsForPrescription();
            loadAppointmentsForReport();
        } catch (SQLException e) {
            showAlert("Error", "Failed to update status: " + e.getMessage());
        }
    }

    // Patient Details Methods
    private void loadPatients() {
        ObservableList<PatientInfo> patients = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT DISTINCT u.name, p.phone, p.blood_group, " +
                    "(SELECT COUNT(*) FROM appointments WHERE patient_id = p.id AND doctor_id = " + currentDoctorId + ") as visits " +
                    "FROM patients p " +
                    "JOIN users u ON p.user_id = u.id " +
                    "JOIN appointments a ON p.id = a.patient_id " +
                    "WHERE a.doctor_id = " + currentDoctorId;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                patients.add(new PatientInfo(
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("blood_group"),
                    rs.getInt("visits")
                ));
            }
            patientsTable.setItems(patients);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load patients: " + e.getMessage());
        }
    }

    @FXML
    private void searchPatients() {
        String searchTerm = patientSearchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadPatients();
            return;
        }

        ObservableList<PatientInfo> patients = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT DISTINCT u.name, p.phone, p.blood_group, " +
                    "(SELECT COUNT(*) FROM appointments WHERE patient_id = p.id AND doctor_id = " + currentDoctorId + ") as visits " +
                    "FROM patients p " +
                    "JOIN users u ON p.user_id = u.id " +
                    "JOIN appointments a ON p.id = a.patient_id " +
                    "WHERE a.doctor_id = " + currentDoctorId + " AND LOWER(u.name) LIKE ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                patients.add(new PatientInfo(
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("blood_group"),
                    rs.getInt("visits")
                ));
            }
            patientsTable.setItems(patients);
        } catch (SQLException e) {
            showAlert("Error", "Failed to search patients: " + e.getMessage());
        }
    }

    // Write Report Methods
    private void loadAppointmentsForReport() {
        ObservableList<String> appointments = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT a.id, u.name as patient_name, a.appointment_date " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id " +
                    "WHERE a.doctor_id = " + currentDoctorId + " AND a.status = 'completed' " +
                    "AND (a.id NOT IN (SELECT appointment_id FROM medical_reports WHERE appointment_id IS NOT NULL) " +
                    "OR a.id IN (SELECT appointment_id FROM medical_reports WHERE COALESCE(TRIM(diagnosis), '') = ''))";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                appointments.add(formatAppointmentDisplay(
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("appointment_date")
                ));
            }

            if (reportAppointmentCombo != null) {
                reportAppointmentCombo.setItems(appointments);
                reportAppointmentCombo.setValue(appointments.isEmpty() ? null : appointments.getFirst());
            }
            updateSelectedPatientLabel(reportPatientInfoLabel,
                    reportAppointmentCombo != null ? reportAppointmentCombo.getValue() : null);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load appointments: " + e.getMessage());
        }
    }

    @FXML
    private void saveReport() {
        String selectedAppointment = reportAppointmentCombo.getValue();
        String reportTitle = reportTitleField.getText().trim();
        String labTests = reportLabTests.getText().trim();
        String diagnosis = reportDiagnosis.getText().trim();
        String treatmentNotes = reportTreatmentNotes.getText().trim();
        String followUpAdvice = reportFollowUpAdvice.getText().trim();

        if (selectedAppointment == null || reportTitle.isEmpty() || labTests.isEmpty() || diagnosis.isEmpty()) {
            showAlert("Error", "Please select appointment and enter report title, lab tests and report details!");
            return;
        }

        int appointmentId = Integer.parseInt(selectedAppointment.split(" - ")[0]);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get patient_id from appointment
            String getPatientQuery = "SELECT patient_id FROM appointments WHERE id = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(getPatientQuery);
            pstmt1.setInt(1, appointmentId);
            ResultSet rs = pstmt1.executeQuery();

            if (rs.next()) {
                int patientId = rs.getInt("patient_id");

                String checkReportQuery = "SELECT id, prescription FROM medical_reports WHERE appointment_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkReportQuery);
                checkStmt.setInt(1, appointmentId);
                ResultSet reportRs = checkStmt.executeQuery();

                if (reportRs.next()) {
                    String updateQuery = "UPDATE medical_reports SET patient_id = ?, doctor_id = ?, report_title = ?, lab_tests = ?, diagnosis = ?, doctor_notes = ?, report_date = ? WHERE appointment_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, patientId);
                    updateStmt.setInt(2, currentDoctorId);
                    updateStmt.setString(3, reportTitle);
                    updateStmt.setString(4, labTests);
                    updateStmt.setString(5, diagnosis);
                    updateStmt.setString(6, treatmentNotes + "\n\nFollow-up Advice:\n" + followUpAdvice);
                    updateStmt.setString(7, LocalDate.now().toString());
                    updateStmt.setInt(8, appointmentId);
                    updateStmt.executeUpdate();
                } else {
                    String insertQuery = "INSERT INTO medical_reports (patient_id, doctor_id, appointment_id, report_title, lab_tests, diagnosis, doctor_notes, report_date) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(insertQuery);
                    pstmt.setInt(1, patientId);
                    pstmt.setInt(2, currentDoctorId);
                    pstmt.setInt(3, appointmentId);
                    pstmt.setString(4, reportTitle);
                    pstmt.setString(5, labTests);
                    pstmt.setString(6, diagnosis);
                    pstmt.setString(7, treatmentNotes + "\n\nFollow-up Advice:\n" + followUpAdvice);
                    pstmt.setString(8, LocalDate.now().toString());
                    pstmt.executeUpdate();
                }

                int medicalReportId = getMedicalReportIdByAppointment(conn, appointmentId);
                if (medicalReportId > 0) {
                    ensureLabReportRequestExists(conn, medicalReportId, patientId, appointmentId);
                }

                showAlert("Success", "Lab report request saved successfully!");
                clearReportForm();
                loadAppointmentsForReport();
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to save report: " + e.getMessage());
        }
    }

    private void clearReportForm() {
        reportTitleField.clear();
        reportLabTests.clear();
        reportDiagnosis.clear();
        reportTreatmentNotes.clear();
        reportFollowUpAdvice.clear();
        reportAppointmentCombo.setValue(null);
        updateSelectedPatientLabel(reportPatientInfoLabel, null);
    }

    // Prescription Methods
    private void loadAppointmentsForPrescription() {
        ObservableList<String> appointments = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT a.id, u.name as patient_name, a.appointment_date " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id " +
                    "WHERE a.doctor_id = " + currentDoctorId + " AND (a.status = 'confirmed' OR a.status = 'upcoming') " +
                    "AND (a.id NOT IN (SELECT appointment_id FROM medical_reports WHERE COALESCE(TRIM(prescription), '') <> ''))";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                appointments.add(formatAppointmentDisplay(
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getString("appointment_date")
                ));
            }

            if (prescriptionAppointmentCombo != null) {
                prescriptionAppointmentCombo.setItems(appointments);
                prescriptionAppointmentCombo.setValue(appointments.isEmpty() ? null : appointments.getFirst());
            }
            updateSelectedPatientLabel(prescriptionPatientInfoLabel,
                    prescriptionAppointmentCombo != null ? prescriptionAppointmentCombo.getValue() : null);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load appointments: " + e.getMessage());
        }
    }

    @FXML
    private void addPrescription() {
        String selectedAppointment = prescriptionAppointmentCombo.getValue();
        String medicine = medicineName.getText().trim();
        String dosage = medicineDosage.getText().trim();
        String duration = medicineDuration.getText().trim();
        String instructions = specialInstructions.getText().trim();

        if (selectedAppointment == null || medicine.isEmpty()) {
            showAlert("Error", "Please select appointment and enter medicine name!");
            return;
        }

        int appointmentId = Integer.parseInt(selectedAppointment.split(" - ")[0]);

        String prescriptionText = String.format("Medicine: %s\nDosage: %s\nDuration: %s\nInstructions: %s\n\n",
                medicine, dosage, duration, instructions);

        if (prescriptionPreview != null) {
            String current = prescriptionPreview.getText();
            prescriptionPreview.setText(current + prescriptionText);
        }

        // Save to database
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get existing prescription or create new
            String checkQuery = "SELECT prescription FROM medical_reports WHERE appointment_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(checkQuery);
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();

            String updateQuery;
            if (rs.next()) {
                String existing = rs.getString("prescription");
                String updated = (existing != null ? existing + "\n" : "") + prescriptionText;
                updateQuery = "UPDATE medical_reports SET prescription = ? WHERE appointment_id = ?";
                pstmt = conn.prepareStatement(updateQuery);
                pstmt.setString(1, updated);
                pstmt.setInt(2, appointmentId);
            } else {
                // Get patient_id
                String getPatientQuery = "SELECT patient_id FROM appointments WHERE id = ?";
                PreparedStatement pstmt2 = conn.prepareStatement(getPatientQuery);
                pstmt2.setInt(1, appointmentId);
                ResultSet rs2 = pstmt2.executeQuery();

                if (rs2.next()) {
                    int patientId = rs2.getInt("patient_id");
                    updateQuery = "INSERT INTO medical_reports (patient_id, doctor_id, appointment_id, prescription, report_date) VALUES (?, ?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setInt(1, patientId);
                    pstmt.setInt(2, currentDoctorId);
                    pstmt.setInt(3, appointmentId);
                    pstmt.setString(4, prescriptionText);
                    pstmt.setString(5, LocalDate.now().toString());
                }
            }

            pstmt.executeUpdate();
            showAlert("Success", "Prescription added successfully!");
            clearPrescriptionForm();
            loadAppointmentsForPrescription();
            loadAppointmentsForReport();

        } catch (SQLException e) {
            showAlert("Error", "Failed to add prescription: " + e.getMessage());
        }
    }

    private void clearPrescriptionForm() {
        medicineName.clear();
        medicineDosage.clear();
        medicineDuration.clear();
        specialInstructions.clear();
        prescriptionAppointmentCombo.setValue(null);
        updateSelectedPatientLabel(prescriptionPatientInfoLabel, null);
    }

    private String formatAppointmentDisplay(int appointmentId, String patientName, String appointmentDate) {
        return appointmentId + " - " + patientName + " (" + appointmentDate + ")";
    }

    private int getMedicalReportIdByAppointment(Connection conn, int appointmentId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM medical_reports WHERE appointment_id = ?")) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    private void ensureLabReportRequestExists(Connection conn, int medicalReportId, int patientId, int appointmentId) throws SQLException {
        String existsSql = "SELECT id FROM lab_report_requests WHERE medical_report_id = ?";
        try (PreparedStatement existsStmt = conn.prepareStatement(existsSql)) {
            existsStmt.setInt(1, medicalReportId);
            ResultSet rs = existsStmt.executeQuery();
            if (rs.next()) {
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE lab_report_requests SET doctor_id = ?, patient_id = ?, appointment_id = ?, updated_at = CURRENT_TIMESTAMP WHERE medical_report_id = ?")) {
                    updateStmt.setInt(1, currentDoctorId);
                    updateStmt.setInt(2, patientId);
                    updateStmt.setInt(3, appointmentId);
                    updateStmt.setInt(4, medicalReportId);
                    updateStmt.executeUpdate();
                }
                return;
            }
        }

        try (PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO lab_report_requests (medical_report_id, patient_id, doctor_id, appointment_id, request_status, result_status, payment_status, updated_at) " +
                        "VALUES (?, ?, ?, ?, 'not_requested', 'pending', 'unpaid', CURRENT_TIMESTAMP)")) {
            insertStmt.setInt(1, medicalReportId);
            insertStmt.setInt(2, patientId);
            insertStmt.setInt(3, currentDoctorId);
            insertStmt.setInt(4, appointmentId);
            insertStmt.executeUpdate();
        }
    }

    private void updateSelectedPatientLabel(Label targetLabel, String appointmentDisplay) {
        if (targetLabel == null) return;
        if (appointmentDisplay == null || appointmentDisplay.isBlank()) {
            targetLabel.setText("Selected Patient: None");
            return;
        }

        int separatorIndex = appointmentDisplay.indexOf(" - ");
        int dateStartIndex = appointmentDisplay.lastIndexOf(" (");
        if (separatorIndex >= 0 && dateStartIndex > separatorIndex) {
            String patientName = appointmentDisplay.substring(separatorIndex + 3, dateStartIndex).trim();
            targetLabel.setText("Selected Patient: " + patientName);
        } else {
            targetLabel.setText("Selected Patient: " + appointmentDisplay);
        }
    }

    // Health Posts Methods
    private void loadMyPosts() {
        ObservableList<HealthPost> posts = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT title, category, created_at FROM doctor_posts WHERE doctor_id = " + currentDoctorId +
                    " ORDER BY created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                posts.add(new HealthPost(
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("created_at")
                ));
            }
            if (myPostsTable != null) myPostsTable.setItems(posts);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load posts: " + e.getMessage());
        }
    }

    @FXML
    private void createPost() {
        String title = postTitle.getText().trim();
        String category = postCategory.getValue();
        String content = postContent.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert("Error", "Title and content are required!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO doctor_posts (doctor_id, title, content, category) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, currentDoctorId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, category != null ? category : "General");
            pstmt.executeUpdate();

            showAlert("Success", "Health post created successfully!");
            clearPostForm();
            loadMyPosts();

        } catch (SQLException e) {
            showAlert("Error", "Failed to create post: " + e.getMessage());
        }
    }

    private void clearPostForm() {
        postTitle.clear();
        postContent.clear();
        postCategory.setValue(null);
    }

    // Q&A Methods
    private void loadQuestions() {
        ObservableList<Question> questions = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT q.id, u.name as patient_name, q.question, q.created_at " +
                    "FROM questions q " +
                    "JOIN patients p ON q.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id " +
                    "WHERE q.answer IS NULL " +
                    "ORDER BY q.created_at DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("question"),
                    rs.getString("created_at")
                ));
            }
            questionsTable.setItems(questions);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load questions: " + e.getMessage());
        }
    }

    @FXML
    private void submitAnswer() {
        Question selected = questionsTable.getSelectionModel().getSelectedItem();
        String answer = answerText.getText().trim();

        if (selected == null || answer.isEmpty()) {
            showAlert("Error", "Please select a question and enter answer!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE questions SET answer = ?, answered_by = ?, answered_at = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, answer);
            pstmt.setInt(2, currentDoctorId);
            pstmt.setInt(3, selected.getId());
            pstmt.executeUpdate();

            showAlert("Success", "Answer submitted successfully!");
            answerText.clear();
            loadQuestions();

        } catch (SQLException e) {
            showAlert("Error", "Failed to submit answer: " + e.getMessage());
        }
    }

    private void loadDoctorProfile() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM doctors WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && profileSpecializationField != null) {
                profileSpecializationField.setText(rs.getString("specialization"));
                profileQualificationField.setText(rs.getString("qualification"));
                profileExperienceField.setText(rs.getString("experience"));
                profilePhoneField.setText(rs.getString("phone"));
                profileFeeField.setText(String.valueOf(rs.getDouble("fee")));
            }
        } catch (SQLException e) {
            showStatusMessage("Failed to load profile: " + e.getMessage(), false);
        }
    }

    @FXML
    private void saveProfile() {
        if (profileSpecializationField == null || profileQualificationField == null ||
                profileExperienceField == null || profilePhoneField == null || profileFeeField == null) {
            showStatusMessage("Profile form is not loaded correctly.", false);
            return;
        }

        String spec = profileSpecializationField.getText() == null ? "" : profileSpecializationField.getText().trim();
        String qual = profileQualificationField.getText() == null ? "" : profileQualificationField.getText().trim();
        String exp = profileExperienceField.getText() == null ? "" : profileExperienceField.getText().trim();
        String phone = profilePhoneField.getText() == null ? "" : profilePhoneField.getText().trim();
        String feeStr = profileFeeField.getText() == null ? "" : profileFeeField.getText().trim();

        if (spec.isEmpty() || qual.isEmpty() || exp.isEmpty() || phone.isEmpty() || feeStr.isEmpty()) {
            showStatusMessage("Please fill all fields.", false);
            return;
        }

        double fee;
        try {
            fee = Double.parseDouble(feeStr);
        } catch (NumberFormatException e) {
            showStatusMessage("Fee must be a valid number.", false);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String ensureSql = "INSERT INTO doctors (user_id, profile_completed) " +
                    "SELECT ?, 0 WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE user_id = ?)";
            PreparedStatement ensureStmt = conn.prepareStatement(ensureSql);
            ensureStmt.setInt(1, SessionManager.getUserId());
            ensureStmt.setInt(2, SessionManager.getUserId());
            ensureStmt.executeUpdate();

            String sql = "UPDATE doctors SET specialization=?, qualification=?, experience=?, phone=?, fee=?, profile_completed=1 WHERE user_id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, spec);
            pstmt.setString(2, qual);
            pstmt.setString(3, exp);
            pstmt.setString(4, phone);
            pstmt.setDouble(5, fee);
            pstmt.setInt(6, SessionManager.getUserId());

            int updated = pstmt.executeUpdate();
            if (updated == 0) {
                String insertSql = "INSERT INTO doctors (user_id, specialization, qualification, experience, phone, fee, profile_completed) VALUES (?, ?, ?, ?, ?, ?, 1)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, SessionManager.getUserId());
                insertStmt.setString(2, spec);
                insertStmt.setString(3, qual);
                insertStmt.setString(4, exp);
                insertStmt.setString(5, phone);
                insertStmt.setDouble(6, fee);
                insertStmt.executeUpdate();
            }

            loadDoctorId();
            showStatusMessage("Profile updated successfully!", true);
            showDashboard();
        } catch (SQLException e) {
            showStatusMessage("Failed to save profile: " + e.getMessage(), false);
        }
    }

    @FXML
    private void logout() {
        SessionManager.clearSession();
        SceneManager.switchScene(logoutBtn, "/fxml/role-selection.fxml");
    }

    private void createDoctorProfilePlaceholder() {
        String query = "INSERT INTO doctors (user_id, profile_completed) " +
                "SELECT ?, 0 WHERE NOT EXISTS (SELECT 1 FROM doctors WHERE user_id = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, SessionManager.getUserId());
            pstmt.setInt(2, SessionManager.getUserId());
            pstmt.executeUpdate();
            loadDoctorId();
        } catch (SQLException e) {
            showStatusMessage("Failed to prepare doctor profile: " + e.getMessage(), false);
        }
    }

    private void showAlert(String title, String content) {
        showStatusMessage(content, title.equals("Success"));
    }

    private void loadRecentNotifications() {
        if (notificationsList == null) {
            return;
        }

        ObservableList<NotificationItem> notifications = FXCollections.observableArrayList();
        String query = "SELECT title, message, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 6";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, SessionManager.getUserId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notifications.add(new NotificationItem(
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getString("created_at")
                ));
            }

            notificationsList.setItems(notifications);
            notificationsList.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(NotificationItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        setGraphic(createNotificationCell(item));
                        setText(null);
                    }
                }
            });
        } catch (SQLException e) {
            showStatusMessage("Failed to load notifications: " + e.getMessage(), false);
        }
    }

    private VBox createNotificationCell(NotificationItem item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("notification-card");

        Label title = new Label(item.getTitle());
        title.getStyleClass().add("notification-title");

        Label message = new Label(item.getMessage());
        message.getStyleClass().add("notification-message");
        message.setWrapText(true);

        Label date = new Label(item.getCreatedAt());
        date.getStyleClass().add("notification-date");

        card.getChildren().addAll(title, message, date);
        return card;
    }

    private void showStatusMessage(String message, boolean isSuccess) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
            statusMessageLabel.getStyleClass().clear();
            statusMessageLabel.getStyleClass().add(isSuccess ? "success-label" : "error-label");

            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), statusMessageLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
            pause.setOnFinished(e -> {
                javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), statusMessageLabel);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.play();
            });
            pause.play();
        }
    }

    // Model Classes
    public static class Appointment {
        private int id;
        private String patientName;
        private String date;
        private String time;
        private String reason;
        private String status;

        public Appointment(int id, String patientName, String date, String time, String reason, String status) {
            this.id = id;
            this.patientName = patientName;
            this.date = date;
            this.time = time;
            this.reason = reason;
            this.status = status;
        }

        public int getId() { return id; }
        public String getPatientName() { return patientName; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
    }

    public static class PatientInfo {
        private String name;
        private String phone;
        private String bloodGroup;
        private int totalVisits;

        public PatientInfo(String name, String phone, String bloodGroup, int totalVisits) {
            this.name = name;
            this.phone = phone;
            this.bloodGroup = bloodGroup;
            this.totalVisits = totalVisits;
        }

        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getBloodGroup() { return bloodGroup; }
        public int getTotalVisits() { return totalVisits; }
    }

    public static class NotificationItem {
        private final String title;
        private final String message;
        private final String createdAt;

        public NotificationItem(String title, String message, String createdAt) {
            this.title = title;
            this.message = message;
            this.createdAt = createdAt;
        }

        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getCreatedAt() { return createdAt; }
    }

    public static class HealthPost {
        private String title;
        private String category;
        private String date;

        public HealthPost(String title, String category, String date) {
            this.title = title;
            this.category = category;
            this.date = date;
        }

        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getDate() { return date; }
    }

    public static class Question {
        private int id;
        private String patientName;
        private String question;
        private String date;

        public Question(int id, String patientName, String question, String date) {
            this.id = id;
            this.patientName = patientName;
            this.question = question;
            this.date = date;
        }

        public int getId() { return id; }
        public String getPatientName() { return patientName; }
        public String getQuestion() { return question; }
        public String getDate() { return date; }
    }
}
