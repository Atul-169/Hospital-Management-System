package com.example.project.controller;

import com.example.project.util.DatabaseConnection;
import com.example.project.util.SceneManager;
import com.example.project.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminDashboardController {

    // Dashboard Statistics Labels
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label completedAppointmentsLabel;
    @FXML private Label pendingAppointmentsLabel;
    @FXML private Label totalReportsLabel;
    @FXML private Label totalRevenueLabel;

    // Navigation Buttons
    @FXML private Button dashboardBtn;
    @FXML private Button manageDoctorsBtn;
    @FXML private Button managePatientsBtn;
    @FXML private Button viewAppointmentsBtn;
    @FXML private Button reportsBtn;
    @FXML private Button transactionsBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button monitoringBtn;
    @FXML private Button logoutBtn;

    // Content Panels
    @FXML private VBox dashboardPanel;
    @FXML private VBox manageDoctorsPanel;
    @FXML private VBox managePatientsPanel;
    @FXML private VBox viewAppointmentsPanel;
    @FXML private VBox reportsPanel;
    @FXML private VBox transactionsPanel;
    @FXML private VBox notificationsPanel;
    @FXML private VBox monitoringPanel;

    // Manage Doctors Components
    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TableColumn<Doctor, Integer> doctorIdCol;
    @FXML private TableColumn<Doctor, String> doctorNameCol;
    @FXML private TableColumn<Doctor, String> doctorEmailCol;
    @FXML private TableColumn<Doctor, String> doctorSpecializationCol;
    @FXML private TableColumn<Doctor, String> doctorExperienceCol;
    @FXML private TableColumn<Doctor, String> doctorPhoneCol;
    @FXML private TableColumn<Doctor, Double> doctorFeeCol;
    @FXML private TextField doctorSearchField;
    @FXML private TextField addDoctorName;
    @FXML private TextField addDoctorEmail;
    @FXML private PasswordField addDoctorPassword;
    @FXML private TextField addDoctorSpecialization;
    @FXML private TextField addDoctorExperience;
    @FXML private TextField addDoctorQualification;
    @FXML private TextField addDoctorPhone;
    @FXML private TextField addDoctorFee;

    // Manage Patients Components
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, Integer> patientIdCol;
    @FXML private TableColumn<Patient, String> patientNameCol;
    @FXML private TableColumn<Patient, String> patientEmailCol;
    @FXML private TableColumn<Patient, String> patientPhoneCol;
    @FXML private TableColumn<Patient, String> patientBloodGroupCol;
    @FXML private TextField patientSearchField;

    // View Appointments Components
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, Integer> appointmentIdCol;
    @FXML private TableColumn<Appointment, String> appointmentPatientCol;
    @FXML private TableColumn<Appointment, String> appointmentDoctorCol;
    @FXML private TableColumn<Appointment, String> appointmentDateCol;
    @FXML private TableColumn<Appointment, String> appointmentTimeCol;
    @FXML private TableColumn<Appointment, String> appointmentStatusCol;
    @FXML private ComboBox<String> filterDoctorCombo;
    @FXML private ComboBox<String> filterPatientCombo;
    @FXML private DatePicker filterDatePicker;
    @FXML private ComboBox<String> filterStatusCombo;

    // Reports Components
    @FXML private TableView<Report> reportsTable;
    @FXML private TableColumn<Report, Integer> reportIdCol;
    @FXML private TableColumn<Report, String> reportPatientCol;
    @FXML private TableColumn<Report, String> reportDoctorCol;
    @FXML private TableColumn<Report, String> reportDateCol;
    @FXML private TableColumn<Report, String> reportDiagnosisCol;
    @FXML private Label totalReportsCountLabel;

    // Transactions Components
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> transactionIdCol;
    @FXML private TableColumn<Transaction, String> transactionTypeCol;
    @FXML private TableColumn<Transaction, String> transactionDoctorCol;
    @FXML private TableColumn<Transaction, Double> transactionAmountCol;
    @FXML private TableColumn<Transaction, String> transactionDateCol;
    @FXML private Label doctorFeeLabel;
    @FXML private Label reportFeeLabel;
    @FXML private Label hospitalShareLabel;
    @FXML private Label totalTransactionsLabel;

    // Notifications Components
    @FXML private TextField notificationTitle;
    @FXML private TextArea notificationMessage;
    @FXML private ComboBox<String> notificationCategory;
    @FXML private Button sendNotificationBtn;

    // Monitoring Components
    @FXML private TableView<BloodDonor> bloodDonorsTable;
    @FXML private TableColumn<BloodDonor, String> donorNameCol;
    @FXML private TableColumn<BloodDonor, String> donorBloodGroupCol;
    @FXML private TableColumn<BloodDonor, String> donorPhoneCol;
    @FXML private TableColumn<BloodDonor, String> donorLocationCol;
    @FXML private TableColumn<BloodDonor, String> donorStatusCol;

    @FXML private TableView<DoctorPost> doctorPostsTable;
    @FXML private TableColumn<DoctorPost, String> postTitleCol;
    @FXML private TableColumn<DoctorPost, String> postDoctorCol;
    @FXML private TableColumn<DoctorPost, String> postCategoryCol;
    @FXML private TableColumn<DoctorPost, String> postDateCol;

    @FXML private TableView<Question> questionsTable;
    @FXML private TableColumn<Question, String> questionPatientCol;
    @FXML private TableColumn<Question, String> questionTextCol;
    @FXML private TableColumn<Question, String> questionAnswerCol;
    @FXML private TableColumn<Question, String> questionDateCol;

    @FXML
    public void initialize() {
        setupTableColumns();
        populateComboBoxes();
        showDashboard();
        loadDashboardStatistics();
    }

    private void populateComboBoxes() {
        // Populate notification categories
        if (notificationCategory != null) {
            notificationCategory.setItems(FXCollections.observableArrayList(
                "General", "Important", "Maintenance", "Update", "Alert"
            ));
            notificationCategory.setValue("General");
        }
    }

    private void setupTableColumns() {
        // Doctors Table
        if (doctorIdCol != null) {
            doctorIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            doctorNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            doctorEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
            doctorSpecializationCol.setCellValueFactory(new PropertyValueFactory<>("specialization"));
            doctorExperienceCol.setCellValueFactory(new PropertyValueFactory<>("experience"));
            doctorPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            doctorFeeCol.setCellValueFactory(new PropertyValueFactory<>("fee"));
        }

        // Patients Table
        if (patientIdCol != null) {
            patientIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            patientNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            patientEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
            patientPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            patientBloodGroupCol.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        }

        // Appointments Table
        if (appointmentIdCol != null) {
            appointmentIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            appointmentPatientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
            appointmentDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
            appointmentDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
            appointmentTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
            appointmentStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        // Reports Table
        if (reportIdCol != null) {
            reportIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            reportPatientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
            reportDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
            reportDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
            reportDiagnosisCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        }

        // Transactions Table
        if (transactionIdCol != null) {
            transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            transactionDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
            transactionAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
            transactionDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        }

        // Blood Donors Table
        if (donorNameCol != null) {
            donorNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            donorBloodGroupCol.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
            donorPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            donorLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
            donorStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        // Doctor Posts Table
        if (postTitleCol != null) {
            postTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            postDoctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
            postCategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
            postDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        }

        // Questions Table
        if (questionPatientCol != null) {
            questionPatientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
            questionTextCol.setCellValueFactory(new PropertyValueFactory<>("question"));
            questionAnswerCol.setCellValueFactory(new PropertyValueFactory<>("answer"));
            questionDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        }
    }

    // Dashboard Statistics
    private void loadDashboardStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total Doctors
            String doctorQuery = "SELECT COUNT(*) as count FROM users WHERE role = 'doctor'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(doctorQuery);
            if (rs.next()) {
                totalDoctorsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Total Patients
            String patientQuery = "SELECT COUNT(*) as count FROM users WHERE role = 'patient'";
            rs = stmt.executeQuery(patientQuery);
            if (rs.next()) {
                totalPatientsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Total Appointments
            String appointmentQuery = "SELECT COUNT(*) as count FROM appointments";
            rs = stmt.executeQuery(appointmentQuery);
            if (rs.next()) {
                totalAppointmentsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Completed Appointments
            String completedQuery = "SELECT COUNT(*) as count FROM appointments WHERE status = 'completed'";
            rs = stmt.executeQuery(completedQuery);
            if (rs.next()) {
                completedAppointmentsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Pending Appointments
            String pendingQuery = "SELECT COUNT(*) as count FROM appointments WHERE status = 'pending' OR status = 'upcoming'";
            rs = stmt.executeQuery(pendingQuery);
            if (rs.next()) {
                pendingAppointmentsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Total Reports
            String reportsQuery = "SELECT COUNT(*) as count FROM medical_reports";
            rs = stmt.executeQuery(reportsQuery);
            if (rs.next()) {
                totalReportsLabel.setText(String.valueOf(rs.getInt("count")));
            }

            // Total Revenue (sum of doctor fees from completed appointments)
            String revenueQuery = "SELECT SUM(d.fee) as total FROM appointments a " +
                    "JOIN doctors d ON a.doctor_id = d.id WHERE a.status = 'completed'";
            rs = stmt.executeQuery(revenueQuery);
            if (rs.next()) {
                double revenue = rs.getDouble("total");
                totalRevenueLabel.setText(String.format("৳%.2f", revenue));
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load dashboard statistics: " + e.getMessage());
        }
    }

    // Navigation Methods
    @FXML
    private void showDashboard() {
        hideAllPanels();
        removeActiveClass();
        if (dashboardPanel != null) {
            dashboardPanel.setVisible(true);
            loadDashboardStatistics();
        }
        if (dashboardBtn != null) dashboardBtn.getStyleClass().add("active");
    }

    @FXML
    private void showManageDoctors() {
        hideAllPanels();
        removeActiveClass();
        if (manageDoctorsPanel != null) {
            manageDoctorsPanel.setVisible(true);
            loadDoctors();
        }
        if (manageDoctorsBtn != null) manageDoctorsBtn.getStyleClass().add("active");
    }

    @FXML
    private void showManagePatients() {
        hideAllPanels();
        removeActiveClass();
        if (managePatientsPanel != null) {
            managePatientsPanel.setVisible(true);
            loadPatients();
        }
        if (managePatientsBtn != null) managePatientsBtn.getStyleClass().add("active");
    }

    @FXML
    private void showViewAppointments() {
        hideAllPanels();
        removeActiveClass();
        if (viewAppointmentsPanel != null) {
            viewAppointmentsPanel.setVisible(true);
            loadAppointments();
            loadAppointmentFilters();
        }
        if (viewAppointmentsBtn != null) viewAppointmentsBtn.getStyleClass().add("active");
    }

    @FXML
    private void showReports() {
        hideAllPanels();
        removeActiveClass();
        if (reportsPanel != null) {
            reportsPanel.setVisible(true);
            loadReports();
        }
        if (reportsBtn != null) reportsBtn.getStyleClass().add("active");
    }

    @FXML
    private void showTransactions() {
        hideAllPanels();
        removeActiveClass();
        if (transactionsPanel != null) {
            transactionsPanel.setVisible(true);
            loadTransactions();
        }
        if (transactionsBtn != null) transactionsBtn.getStyleClass().add("active");
    }

    @FXML
    private void showNotifications() {
        hideAllPanels();
        removeActiveClass();
        if (notificationsPanel != null) {
            notificationsPanel.setVisible(true);
        }
        if (notificationsBtn != null) notificationsBtn.getStyleClass().add("active");
    }

    @FXML
    private void showMonitoring() {
        hideAllPanels();
        removeActiveClass();
        if (monitoringPanel != null) {
            monitoringPanel.setVisible(true);
            loadMonitoringData();
        }
        if (monitoringBtn != null) monitoringBtn.getStyleClass().add("active");
    }

    private void hideAllPanels() {
        if (dashboardPanel != null) dashboardPanel.setVisible(false);
        if (manageDoctorsPanel != null) manageDoctorsPanel.setVisible(false);
        if (managePatientsPanel != null) managePatientsPanel.setVisible(false);
        if (viewAppointmentsPanel != null) viewAppointmentsPanel.setVisible(false);
        if (reportsPanel != null) reportsPanel.setVisible(false);
        if (transactionsPanel != null) transactionsPanel.setVisible(false);
        if (notificationsPanel != null) notificationsPanel.setVisible(false);
        if (monitoringPanel != null) monitoringPanel.setVisible(false);
    }

    private void removeActiveClass() {
        if (dashboardBtn != null) dashboardBtn.getStyleClass().remove("active");
        if (manageDoctorsBtn != null) manageDoctorsBtn.getStyleClass().remove("active");
        if (managePatientsBtn != null) managePatientsBtn.getStyleClass().remove("active");
        if (viewAppointmentsBtn != null) viewAppointmentsBtn.getStyleClass().remove("active");
        if (reportsBtn != null) reportsBtn.getStyleClass().remove("active");
        if (transactionsBtn != null) transactionsBtn.getStyleClass().remove("active");
        if (notificationsBtn != null) notificationsBtn.getStyleClass().remove("active");
        if (monitoringBtn != null) monitoringBtn.getStyleClass().remove("active");
    }

    // Manage Doctors Methods
    private void loadDoctors() {
        ObservableList<Doctor> doctors = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.id, u.name, u.email, d.specialization, d.experience, d.phone, d.fee " +
                    "FROM users u JOIN doctors d ON u.id = d.user_id WHERE u.role = 'doctor'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                doctors.add(new Doctor(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("specialization"),
                    rs.getString("experience"),
                    rs.getString("phone"),
                    rs.getDouble("fee")
                ));
            }
            doctorsTable.setItems(doctors);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load doctors: " + e.getMessage());
        }
    }

    @FXML
    private void addDoctor() {
        String name = addDoctorName.getText().trim();
        String email = addDoctorEmail.getText().trim();
        String password = addDoctorPassword.getText().trim();
        String specialization = addDoctorSpecialization.getText().trim();
        String experience = addDoctorExperience.getText().trim();
        String qualification = addDoctorQualification.getText().trim();
        String phone = addDoctorPhone.getText().trim();
        String feeStr = addDoctorFee.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Name, email and password are required!");
            return;
        }

        double fee = 0;
        try {
            fee = Double.parseDouble(feeStr);
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid fee amount!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Insert user
            String insertUser = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'doctor')";
            PreparedStatement pstmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);

                // Insert doctor
                String insertDoctor = "INSERT INTO doctors (user_id, specialization, experience, qualification, phone, fee, profile_completed) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 1)";
                PreparedStatement pstmt2 = conn.prepareStatement(insertDoctor);
                pstmt2.setInt(1, userId);
                pstmt2.setString(2, specialization);
                pstmt2.setString(3, experience);
                pstmt2.setString(4, qualification);
                pstmt2.setString(5, phone);
                pstmt2.setDouble(6, fee);
                pstmt2.executeUpdate();

                showAlert("Success", "Doctor added successfully!");
                clearDoctorForm();
                loadDoctors();
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to add doctor: " + e.getMessage());
        }
    }

    @FXML
    private void deleteDoctor() {
        Doctor selected = doctorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a doctor to delete!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Doctor");
        confirm.setContentText("Are you sure you want to delete this doctor?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM users WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();

                showAlert("Success", "Doctor deleted successfully!");
                loadDoctors();
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete doctor: " + e.getMessage());
            }
        }
    }

    @FXML
    private void searchDoctors() {
        String searchTerm = doctorSearchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadDoctors();
            return;
        }

        ObservableList<Doctor> doctors = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.id, u.name, u.email, d.specialization, d.experience, d.phone, d.fee " +
                    "FROM users u JOIN doctors d ON u.id = d.user_id WHERE u.role = 'doctor' " +
                    "AND (LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(d.specialization) LIKE ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                doctors.add(new Doctor(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("specialization"),
                    rs.getString("experience"),
                    rs.getString("phone"),
                    rs.getDouble("fee")
                ));
            }
            doctorsTable.setItems(doctors);
        } catch (SQLException e) {
            showAlert("Error", "Failed to search doctors: " + e.getMessage());
        }
    }

    private void clearDoctorForm() {
        addDoctorName.clear();
        addDoctorEmail.clear();
        addDoctorPassword.clear();
        addDoctorSpecialization.clear();
        addDoctorExperience.clear();
        addDoctorQualification.clear();
        addDoctorPhone.clear();
        addDoctorFee.clear();
    }

    // Manage Patients Methods
    private void loadPatients() {
        ObservableList<Patient> patients = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.id, u.name, u.email, p.phone, p.blood_group " +
                    "FROM users u JOIN patients p ON u.id = p.user_id WHERE u.role = 'patient'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                patients.add(new Patient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("blood_group")
                ));
            }
            patientsTable.setItems(patients);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load patients: " + e.getMessage());
        }
    }

    @FXML
    private void deletePatient() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a patient to delete!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Patient");
        confirm.setContentText("Are you sure you want to delete this patient?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "DELETE FROM users WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();

                showAlert("Success", "Patient deleted successfully!");
                loadPatients();
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete patient: " + e.getMessage());
            }
        }
    }

    @FXML
    private void searchPatients() {
        String searchTerm = patientSearchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadPatients();
            return;
        }

        ObservableList<Patient> patients = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.id, u.name, u.email, p.phone, p.blood_group " +
                    "FROM users u JOIN patients p ON u.id = p.user_id WHERE u.role = 'patient' " +
                    "AND (LOWER(u.name) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(p.phone) LIKE ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                patients.add(new Patient(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("blood_group")
                ));
            }
            patientsTable.setItems(patients);
        } catch (SQLException e) {
            showAlert("Error", "Failed to search patients: " + e.getMessage());
        }
    }

    // View Appointments Methods
    private void loadAppointments() {
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT a.id, u1.name as patient_name, u2.name as doctor_name, " +
                    "a.appointment_date, a.appointment_time, a.status " +
                    "FROM appointments a " +
                    "JOIN patients p ON a.patient_id = p.id " +
                    "JOIN users u1 ON p.user_id = u1.id " +
                    "JOIN doctors d ON a.doctor_id = d.id " +
                    "JOIN users u2 ON d.user_id = u2.id " +
                    "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                appointments.add(new Appointment(
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getString("appointment_date"),
                    rs.getString("appointment_time"),
                    rs.getString("status")
                ));
            }
            appointmentsTable.setItems(appointments);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load appointments: " + e.getMessage());
        }
    }

    private void loadAppointmentFilters() {
        // Load doctors for filter
        ObservableList<String> doctors = FXCollections.observableArrayList("All Doctors");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.name FROM users u JOIN doctors d ON u.id = d.user_id WHERE u.role = 'doctor'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                doctors.add(rs.getString("name"));
            }
            if (filterDoctorCombo != null) {
                filterDoctorCombo.setItems(doctors);
                filterDoctorCombo.setValue("All Doctors");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Load status options
        ObservableList<String> statuses = FXCollections.observableArrayList(
            "All", "pending", "upcoming", "confirmed", "completed", "cancelled"
        );
        if (filterStatusCombo != null) {
            filterStatusCombo.setItems(statuses);
            filterStatusCombo.setValue("All");
        }
    }

    @FXML
    private void applyAppointmentFilters() {
        // Implementation for filtering appointments
        loadAppointments();
    }

    // Reports Methods
    private void loadReports() {
        ObservableList<Report> reports = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT r.id, u1.name as patient_name, u2.name as doctor_name, " +
                    "r.report_date, r.diagnosis " +
                    "FROM medical_reports r " +
                    "JOIN patients p ON r.patient_id = p.id " +
                    "JOIN users u1 ON p.user_id = u1.id " +
                    "JOIN doctors d ON r.doctor_id = d.id " +
                    "JOIN users u2 ON d.user_id = u2.id " +
                    "ORDER BY r.report_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            int count = 0;
            while (rs.next()) {
                reports.add(new Report(
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getString("report_date"),
                    rs.getString("diagnosis")
                ));
                count++;
            }
            reportsTable.setItems(reports);
            if (totalReportsCountLabel != null) {
                totalReportsCountLabel.setText("Total Reports: " + count);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load reports: " + e.getMessage());
        }
    }

    // Transactions Methods
    private void loadTransactions() {
        ObservableList<Transaction> transactions = FXCollections.observableArrayList();
        double totalDoctorFees = 0;
        double totalReportFees = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Load appointment-based transactions
            String query = "SELECT a.id, 'Appointment' as type, u.name as doctor_name, " +
                    "d.fee as amount, a.created_at as date " +
                    "FROM appointments a " +
                    "JOIN doctors d ON a.doctor_id = d.id " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE a.status = 'completed' " +
                    "ORDER BY a.created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                double amount = rs.getDouble("amount");
                totalDoctorFees += amount;
                transactions.add(new Transaction(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getString("doctor_name"),
                    amount,
                    rs.getString("date")
                ));
            }

            transactionsTable.setItems(transactions);

            // Calculate hospital share (30% of total)
            double total = totalDoctorFees + totalReportFees;
            double hospitalShare = total * 0.30;

            if (doctorFeeLabel != null) doctorFeeLabel.setText(String.format("৳%.2f", totalDoctorFees));
            if (reportFeeLabel != null) reportFeeLabel.setText(String.format("৳%.2f", totalReportFees));
            if (hospitalShareLabel != null) hospitalShareLabel.setText(String.format("৳%.2f", hospitalShare));
            if (totalTransactionsLabel != null) totalTransactionsLabel.setText(String.format("৳%.2f", total));

        } catch (SQLException e) {
            showAlert("Error", "Failed to load transactions: " + e.getMessage());
        }
    }

    // Notifications Methods
    @FXML
    private void sendNotification() {
        String title = notificationTitle.getText().trim();
        String message = notificationMessage.getText().trim();
        String category = notificationCategory.getValue();

        if (title.isEmpty() || message.isEmpty()) {
            showAlert("Error", "Title and message are required!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get all user IDs
            String userQuery = "SELECT id FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(userQuery);

            String insertQuery = "INSERT INTO notifications (user_id, title, message) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);

            int count = 0;
            while (rs.next()) {
                pstmt.setInt(1, rs.getInt("id"));
                pstmt.setString(2, title);
                pstmt.setString(3, message);
                pstmt.addBatch();
                count++;
            }

            pstmt.executeBatch();
            showAlert("Success", "Notification sent to " + count + " users!");
            notificationTitle.clear();
            notificationMessage.clear();

        } catch (SQLException e) {
            showAlert("Error", "Failed to send notification: " + e.getMessage());
        }
    }

    // Monitoring Methods
    private void loadMonitoringData() {
        loadBloodDonors();
        loadDoctorPosts();
        loadQuestions();
    }

    private void loadBloodDonors() {
        ObservableList<BloodDonor> donors = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.name, bd.blood_group, bd.phone, bd.location, bd.availability_status " +
                    "FROM blood_donors bd " +
                    "JOIN patients p ON bd.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                donors.add(new BloodDonor(
                    rs.getString("name"),
                    rs.getString("blood_group"),
                    rs.getString("phone"),
                    rs.getString("location"),
                    rs.getString("availability_status")
                ));
            }
            if (bloodDonorsTable != null) bloodDonorsTable.setItems(donors);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load blood donors: " + e.getMessage());
        }
    }

    private void loadDoctorPosts() {
        ObservableList<DoctorPost> posts = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT dp.title, u.name as doctor_name, dp.category, dp.created_at " +
                    "FROM doctor_posts dp " +
                    "JOIN doctors d ON dp.doctor_id = d.id " +
                    "JOIN users u ON d.user_id = u.id " +
                    "ORDER BY dp.created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                posts.add(new DoctorPost(
                    rs.getString("title"),
                    rs.getString("doctor_name"),
                    rs.getString("category"),
                    rs.getString("created_at")
                ));
            }
            if (doctorPostsTable != null) doctorPostsTable.setItems(posts);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load doctor posts: " + e.getMessage());
        }
    }

    private void loadQuestions() {
        ObservableList<Question> questions = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT u.name as patient_name, q.question, q.answer, q.created_at " +
                    "FROM questions q " +
                    "JOIN patients p ON q.patient_id = p.id " +
                    "JOIN users u ON p.user_id = u.id " +
                    "ORDER BY q.created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                questions.add(new Question(
                    rs.getString("patient_name"),
                    rs.getString("question"),
                    rs.getString("answer"),
                    rs.getString("created_at")
                ));
            }
            if (questionsTable != null) questionsTable.setItems(questions);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load questions: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        SessionManager.clearSession();
        SceneManager.switchScene(logoutBtn, "/fxml/login.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Model Classes
    public static class Doctor {
        private int id;
        private String name;
        private String email;
        private String specialization;
        private String experience;
        private String phone;
        private double fee;

        public Doctor(int id, String name, String email, String specialization, String experience, String phone, double fee) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.specialization = specialization;
            this.experience = experience;
            this.phone = phone;
            this.fee = fee;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getSpecialization() { return specialization; }
        public String getExperience() { return experience; }
        public String getPhone() { return phone; }
        public double getFee() { return fee; }
    }

    public static class Patient {
        private int id;
        private String name;
        private String email;
        private String phone;
        private String bloodGroup;

        public Patient(int id, String name, String email, String phone, String bloodGroup) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.bloodGroup = bloodGroup;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getBloodGroup() { return bloodGroup; }
    }

    public static class Appointment {
        private int id;
        private String patientName;
        private String doctorName;
        private String date;
        private String time;
        private String status;

        public Appointment(int id, String patientName, String doctorName, String date, String time, String status) {
            this.id = id;
            this.patientName = patientName;
            this.doctorName = doctorName;
            this.date = date;
            this.time = time;
            this.status = status;
        }

        public int getId() { return id; }
        public String getPatientName() { return patientName; }
        public String getDoctorName() { return doctorName; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getStatus() { return status; }
    }

    public static class Report {
        private int id;
        private String patientName;
        private String doctorName;
        private String date;
        private String diagnosis;

        public Report(int id, String patientName, String doctorName, String date, String diagnosis) {
            this.id = id;
            this.patientName = patientName;
            this.doctorName = doctorName;
            this.date = date;
            this.diagnosis = diagnosis;
        }

        public int getId() { return id; }
        public String getPatientName() { return patientName; }
        public String getDoctorName() { return doctorName; }
        public String getDate() { return date; }
        public String getDiagnosis() { return diagnosis; }
    }

    public static class Transaction {
        private int id;
        private String type;
        private String doctorName;
        private double amount;
        private String date;

        public Transaction(int id, String type, String doctorName, double amount, String date) {
            this.id = id;
            this.type = type;
            this.doctorName = doctorName;
            this.amount = amount;
            this.date = date;
        }

        public int getId() { return id; }
        public String getType() { return type; }
        public String getDoctorName() { return doctorName; }
        public double getAmount() { return amount; }
        public String getDate() { return date; }
    }

    public static class BloodDonor {
        private String name;
        private String bloodGroup;
        private String phone;
        private String location;
        private String status;

        public BloodDonor(String name, String bloodGroup, String phone, String location, String status) {
            this.name = name;
            this.bloodGroup = bloodGroup;
            this.phone = phone;
            this.location = location;
            this.status = status;
        }

        public String getName() { return name; }
        public String getBloodGroup() { return bloodGroup; }
        public String getPhone() { return phone; }
        public String getLocation() { return location; }
        public String getStatus() { return status; }
    }

    public static class DoctorPost {
        private String title;
        private String doctorName;
        private String category;
        private String date;

        public DoctorPost(String title, String doctorName, String category, String date) {
            this.title = title;
            this.doctorName = doctorName;
            this.category = category;
            this.date = date;
        }

        public String getTitle() { return title; }
        public String getDoctorName() { return doctorName; }
        public String getCategory() { return category; }
        public String getDate() { return date; }
    }

    public static class Question {
        private String patientName;
        private String question;
        private String answer;
        private String date;

        public Question(String patientName, String question, String answer, String date) {
            this.patientName = patientName;
            this.question = question;
            this.answer = answer;
            this.date = date;
        }

        public String getPatientName() { return patientName; }
        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
        public String getDate() { return date; }
    }
}
