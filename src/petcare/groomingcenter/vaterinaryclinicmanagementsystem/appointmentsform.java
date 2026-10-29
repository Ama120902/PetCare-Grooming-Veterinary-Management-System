/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package petcare.groomingcenter.vaterinaryclinicmanagementsystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import java.util.HashMap;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
/**
 *
 * @author Ama
 */
public class appointmentsform extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(appointmentsform.class.getName());
    private String currentUser = "Admin";
    /**
     * Creates new form appointmentsform
     */
    public appointmentsform() {
        initComponents();
        setLocationRelativeTo(null);
        loadappointments();
    }
           
    private void loadappointments(){
         String sql =
        "SELECT " +
        "a.appointment_id AS Number, " +
        "p.pet_name AS `Pet Name`, " +
        "s.service_name AS Service, " +
        "a.appointment_datetime AS `Date & Time`, " +
        "a.status AS Status " +
        "FROM appointments a " +
        "JOIN pets p ON a.pet_id = p.pet_id " +
        "JOIN services s ON a.service_id = s.service_id " +
        "ORDER BY a.appointment_datetime DESC";

    TableLoader.loadTable(jTable1, sql);    
    } 
    
    private void showNewAppointmentForm() {

    // Customer dropdown
    JComboBox<String> customerCombo = new JComboBox<>();
    Map<String, Integer> customerIds = new LinkedHashMap<>();

    // Pet dropdown
    JComboBox<String> petCombo = new JComboBox<>();
    Map<String, Integer> petIds = new LinkedHashMap<>();

    // Service dropdown
    JComboBox<String> serviceCombo = new JComboBox<>();
    Map<String, Integer> serviceIds = new LinkedHashMap<>();

    // Date selector
        JSpinner dateSpinner = new JSpinner(
        new SpinnerDateModel(
                new Date(),
                null,
                null,
                Calendar.DAY_OF_MONTH
        )
        );

        dateSpinner.setEditor(
        new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd")
        );

    // Time selector
        JSpinner timeSpinner = new JSpinner(
        new SpinnerDateModel(
                new Date(),
                null,
                null,
                Calendar.MINUTE
        )
        );

timeSpinner.setEditor(
        new JSpinner.DateEditor(timeSpinner, "HH:mm")
        );

    // Status dropdown
    JComboBox<String> statusCombo = new JComboBox<>(
            new String[]{
                "Pending",
                "Confirmed"
            }
    );

    // Load customers
    String customerSQL =
            "SELECT customer_id, customer_name "
            + "FROM customers ORDER BY customer_name";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(customerSQL);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            int id = rs.getInt("customer_id");
            String name = rs.getString("customer_name");

            customerCombo.addItem(name);
            customerIds.put(name, id);
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Error loading customers: " + e.getMessage()
        );

        return;
    }

    // Load pets
    String petSQL =
            "SELECT pet_id, pet_name "
            + "FROM pets ORDER BY pet_name";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(petSQL);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            int id = rs.getInt("pet_id");
            String name = rs.getString("pet_name");

            petCombo.addItem(name);
            petIds.put(name, id);
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Error loading pets: " + e.getMessage()
        );

        return;
    }

    // Load active services
    String serviceSQL =
            "SELECT service_id, service_name "
            + "FROM services "
            + "WHERE status = 'Active' "
            + "ORDER BY service_name";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(serviceSQL);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            int id = rs.getInt("service_id");
            String name = rs.getString("service_name");

            serviceCombo.addItem(name);
            serviceIds.put(name, id);
        }

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Error loading services: " + e.getMessage()
        );

        return;
    }

    // Check dropdown data
    if (customerCombo.getItemCount() == 0) {

        JOptionPane.showMessageDialog(
                this,
                "No customers found. Please register a customer first."
        );

        return;
    }

    if (petCombo.getItemCount() == 0) {

        JOptionPane.showMessageDialog(
                this,
                "No pets found. Please register a pet first."
        );

        return;
    }

    if (serviceCombo.getItemCount() == 0) {

        JOptionPane.showMessageDialog(
                this,
                "No active services found. Please check the services table."
        );

        return;
    }

    // Create popup panel
    JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

    panel.add(new JLabel("Customer Name:"));
    panel.add(customerCombo);

    panel.add(new JLabel("Pet Name:"));
    panel.add(petCombo);

    panel.add(new JLabel("Service:"));
    panel.add(serviceCombo);

    panel.add(new JLabel("Appointment Date:"));
    panel.add(dateSpinner);

    panel.add(new JLabel("Appointment Time:"));
    panel.add(timeSpinner);
    panel.add(new JLabel("Status:"));
    panel.add(statusCombo);

    // Show popup
    int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "New Appointment",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (result != JOptionPane.OK_OPTION) {
        return;
    }

    // Validate selections
    String selectedCustomer =
            (String) customerCombo.getSelectedItem();

    String selectedPet =
            (String) petCombo.getSelectedItem();

    String selectedService =
            (String) serviceCombo.getSelectedItem();

    if (selectedCustomer == null
            || selectedPet == null
            || selectedService == null) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a customer, pet and service."
        );

        return;
    }

    try {

        int customerId = customerIds.get(selectedCustomer);
        int petId = petIds.get(selectedPet);
        int serviceId = serviceIds.get(selectedService);

         SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd");

        SimpleDateFormat timeFormat =
        new SimpleDateFormat("HH:mm");

        String selectedDate = dateFormat.format(
        (Date) dateSpinner.getValue()
            );

        String selectedTime = timeFormat.format(
        (Date) timeSpinner.getValue()
            );

        LocalDateTime dateTime = LocalDateTime.parse(
        selectedDate + "T" + selectedTime
            );
        String status = statusCombo.getSelectedItem().toString();
        // Insert appointment
        String insertSQL =
                "INSERT INTO appointments "
                + "(customer_id, pet_id, service_id, "
                + "appointment_datetime, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(insertSQL)) {

            ps.setInt(1, customerId);
            ps.setInt(2, petId);
            ps.setInt(3, serviceId);

            ps.setTimestamp(
                    4,
                    java.sql.Timestamp.valueOf(dateTime)
            );

            ps.setString(5, status);

            ps.executeUpdate();
        }

        JOptionPane.showMessageDialog(
                this,
                "Appointment saved successfully!"
        );

        // Refresh JTable
        loadappointments();

    } catch (java.time.format.DateTimeParseException e) {

        JOptionPane.showMessageDialog(
                this,
                "Invalid date or time.\n"
                + "Date example: 2026-10-27\n"
                + "Time example: 10:00"
        );

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Error saving appointment: " + e.getMessage()
        );

    } catch (NullPointerException e) {

        JOptionPane.showMessageDialog(
                this,
                "Please select all required details."
        );
    }
}
    
    private void editSelectedAppointment() {

    int selectedRow = jTable1.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(
            this, "Please select an appointment first."
        );
        return;
    }

    int appointmentId = Integer.parseInt(
        jTable1.getValueAt(
            selectedRow, 0
        ).toString()
    );

    JComboBox<String> customerCombo = new JComboBox<>();
    JComboBox<String> petCombo = new JComboBox<>();
    JComboBox<String> serviceCombo = new JComboBox<>();

    Map<String, Integer> customerIds = new LinkedHashMap<>();
    Map<String, Integer> petIds = new LinkedHashMap<>();
    Map<String, Integer> serviceIds = new LinkedHashMap<>();

    JSpinner dateSpinner = new JSpinner(
        new SpinnerDateModel(
            new Date(), null, null, Calendar.DAY_OF_MONTH
        )
    );
    dateSpinner.setEditor(
        new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd")
    );

    JSpinner timeSpinner = new JSpinner(
        new SpinnerDateModel(
            new Date(), null, null, Calendar.MINUTE
        )
    );
    timeSpinner.setEditor(
        new JSpinner.DateEditor(timeSpinner, "HH:mm")
    );

    JComboBox<String> statusCombo = new JComboBox<>(
        new String[]{
            "Pending", "Confirmed", "Completed", "Cancelled"
        }
    );

    int oldCustomerId;
    int oldPetId;
    int oldServiceId;
    String oldStatus;
    Date oldDateTime;

    // 1. Load existing appointment details
    String appointmentSQL =
        "SELECT customer_id, pet_id, service_id, " +
        "appointment_datetime, status " +
        "FROM appointments WHERE appointment_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps =
             con.prepareStatement(appointmentSQL)) {

        ps.setInt(1, appointmentId);

        try (ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                JOptionPane.showMessageDialog(
                    this, "Appointment not found."
                );
                return;
            }

            oldCustomerId = rs.getInt("customer_id");
            oldPetId = rs.getInt("pet_id");
            oldServiceId = rs.getInt("service_id");
            oldStatus = rs.getString("status");
            oldDateTime = rs.getTimestamp(
                "appointment_datetime"
            );
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Error loading appointment: " + e.getMessage()
        );
        return;
    }

    // 2. Load customers
    String customerSQL =
        "SELECT customer_id, customer_name " +
        "FROM customers ORDER BY customer_name";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(customerSQL);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String name = rs.getString("customer_name");
            int id = rs.getInt("customer_id");

            customerCombo.addItem(name);
            customerIds.put(name, id);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Error loading customers: " + e.getMessage()
        );
        return;
    }

    // 3. Load pets
    String petSQL =
        "SELECT pet_id, pet_name FROM pets ORDER BY pet_name";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(petSQL);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String name = rs.getString("pet_name");
            int id = rs.getInt("pet_id");

            petCombo.addItem(name);
            petIds.put(name, id);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Error loading pets: " + e.getMessage()
        );
        return;
    }

    // 4. Load services
    String serviceSQL =
        "SELECT service_id, service_name " +
        "FROM services ORDER BY service_name";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(serviceSQL);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String name = rs.getString("service_name");
            int id = rs.getInt("service_id");

            serviceCombo.addItem(name);
            serviceIds.put(name, id);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Error loading services: " + e.getMessage()
        );
        return;
    }

    // Check whether required records are available
    if (customerCombo.getItemCount() == 0 ||
        petCombo.getItemCount() == 0 ||
        serviceCombo.getItemCount() == 0) {

        JOptionPane.showMessageDialog(
            this, "Required customer, pet or service data is missing."
        );
        return;
    }

    // 5. Select the existing values
    customerCombo.setSelectedItem(
        findNameById(customerIds, oldCustomerId)
    );

    petCombo.setSelectedItem(
        findNameById(petIds, oldPetId)
    );

    serviceCombo.setSelectedItem(
        findNameById(serviceIds, oldServiceId)
    );

    if (oldDateTime != null) {
        dateSpinner.setValue(oldDateTime);
        timeSpinner.setValue(oldDateTime);
    }

    statusCombo.setSelectedItem(oldStatus);

    // 6. Create the edit dialog
    JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

    panel.add(new JLabel("Customer Name:"));
    panel.add(customerCombo);

    panel.add(new JLabel("Pet Name:"));
    panel.add(petCombo);

    panel.add(new JLabel("Service:"));
    panel.add(serviceCombo);

    panel.add(new JLabel("Appointment Date:"));
    panel.add(dateSpinner);

    panel.add(new JLabel("Appointment Time:"));
    panel.add(timeSpinner);

    panel.add(new JLabel("Status:"));
    panel.add(statusCombo);

    int result = JOptionPane.showConfirmDialog(
        this,
        panel,
        "Edit Appointment - No. " + appointmentId,
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (result != JOptionPane.OK_OPTION) {
        return;
    }

    // 7. Validate selections
    String customer = (String) customerCombo.getSelectedItem();
    String pet = (String) petCombo.getSelectedItem();
    String service = (String) serviceCombo.getSelectedItem();

    if (customer == null || pet == null || service == null) {
        JOptionPane.showMessageDialog(
            this, "Please select all required details."
        );
        return;
    }

    // 8. Update the appointment
    try {
        int customerId = customerIds.get(customer);
        int petId = petIds.get(pet);
        int serviceId = serviceIds.get(service);

        SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd");

        SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm");

        String selectedDate = dateFormat.format(
            (Date) dateSpinner.getValue()
        );

        String selectedTime = timeFormat.format(
            (Date) timeSpinner.getValue()
        );

        LocalDateTime dateTime = LocalDateTime.parse(
            selectedDate + "T" + selectedTime
        );

        String status = statusCombo.getSelectedItem().toString();

        String updateSQL =
            "UPDATE appointments SET customer_id = ?, " +
            "pet_id = ?, service_id = ?, " +
            "appointment_datetime = ?, status = ? " +
            "WHERE appointment_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                 con.prepareStatement(updateSQL)) {

            ps.setInt(1, customerId);
            ps.setInt(2, petId);
            ps.setInt(3, serviceId);

            ps.setTimestamp(
                4, java.sql.Timestamp.valueOf(dateTime)
            );

            ps.setString(5, status);
            ps.setInt(6, appointmentId);

            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(
                    this, "Appointment updated successfully!"
                );

                loadappointments();
            } else {
                JOptionPane.showMessageDialog(
                    this, "Appointment could not be updated."
                );
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Error updating appointment: " + e.getMessage()
        );
    } catch (Exception e) {
        JOptionPane.showMessageDialog(
            this, "Please check the selected details: "
                + e.getMessage()
        );
    }
}

// Find the name belonging to a database ID
private String findNameById(
        Map<String, Integer> items, int id) {

    for (Map.Entry<String, Integer> entry : items.entrySet()) {
        if (entry.getValue() == id) {
            return entry.getKey();
        }
    }

    return null;
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setForeground(new java.awt.Color(0, 0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(1340, 720));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(0, 0, 51));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Appoimtments Management");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 20, -1, -1));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 1350, 90));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "No", "Pet Name", "Service", "Date & Time", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 167, 980, 500));

        jButton1.setText("+ New Appointment");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 220, 190, -1));

        jButton2.setText("Mark Confirmed");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 280, 190, -1));

        jButton3.setText("Mark Completed");
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 320, 190, -1));

        jButton4.setText("Cancel Appointment");
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 370, 190, -1));

        jButton5.setText("Close");
        jButton5.addActionListener(this::jButton5ActionPerformed);
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 540, 190, -1));

        jButton6.setText("Dashboard");
        jButton6.addActionListener(this::jButton6ActionPerformed);
        jPanel1.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 590, 190, -1));

        jButton7.setText("View Appointment Report");
        jButton7.addActionListener(this::jButton7ActionPerformed);
        jPanel1.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 480, 190, -1));

        jButton8.setText("Edit Appointment");
        jButton8.addActionListener(this::jButton8ActionPerformed);
        jPanel1.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 420, 190, 30));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1350, 700));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
         showNewAppointmentForm();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        new dashboardform(currentUser).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
            int selectedRow = jTable1.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(
                this,
                "Please select an appointment first."
        );
        return;
    }

    // Get appointment number from first column
    int appointmentId = Integer.parseInt(
            jTable1.getValueAt(selectedRow, 0).toString()
    );

    // Update status
    String sql = "UPDATE appointments SET status = ? "
               + "WHERE appointment_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, "Confirmed");
        ps.setInt(2, appointmentId);

        int updated = ps.executeUpdate();

        if (updated > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Appointment marked as Confirmed!"
            );

            loadappointments();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Appointment not found."
            );
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Error updating appointment: " + e.getMessage()
        );
    }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
            // Get selected appointment row
    int selectedRow = jTable1.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(
                this,
                "Please select an appointment first."
        );
        return;
    }

    // Get appointment number
    int appointmentId = Integer.parseInt(
            jTable1.getValueAt(selectedRow, 0).toString()
    );

    // Update appointment status
    String sql = "UPDATE appointments SET status = ? "
               + "WHERE appointment_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, "Completed");
        ps.setInt(2, appointmentId);

        int updated = ps.executeUpdate();

        if (updated > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Appointment marked as Completed!"
            );

            // Refresh table
            loadappointments();

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Appointment not found."
            );
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Error updating appointment: " + e.getMessage()
        );
    }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
         int selectedRow = jTable1.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(
                this,
                "Please select an appointment first."
        );
        return;
    }

    // Get Appointment Number
    int appointmentId = Integer.parseInt(
            jTable1.getValueAt(selectedRow, 0).toString()
    );

    // Confirm before deleting
    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel Appointment No: "
                    + appointmentId + "?",
            "Cancel Appointment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    // Delete appointment
    String sql = "DELETE FROM appointments WHERE appointment_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, appointmentId);

        int deleted = ps.executeUpdate();

        if (deleted > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Appointment cancelled successfully!"
            );

            // Refresh JTable
            loadappointments();

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Appointment not found."
            );
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Error cancelling appointment: " + e.getMessage()
        );
    }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
    try {
        Connection con = DBConnection.getConnection();

        java.io.InputStream reportStream =
             getClass().getResourceAsStream(
            "PetCareAppointmentReport.jrxml"
        );

        if (reportStream == null) {
            JOptionPane.showMessageDialog(
                this,
                "Report file not found!"
            );
            return;
        }

    JasperReport report =
        JasperCompileManager.compileReport(reportStream);

    JasperPrint print =
        JasperFillManager.fillReport(
            report,
            new HashMap<String, Object>(),
            con
        );

    JasperViewer viewer = new JasperViewer(print, false);
    viewer.setTitle("PetCare Appointment Report");
    viewer.setVisible(true);

    reportStream.close();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(
            this,
            "Report Error: " + e.getMessage()
    );
    e.printStackTrace();
    }

    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
         editSelectedAppointment();
    }//GEN-LAST:event_jButton8ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new appointmentsform().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
