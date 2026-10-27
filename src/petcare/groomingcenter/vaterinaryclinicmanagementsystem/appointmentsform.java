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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
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

    // Date and time fields
    JTextField dateField = new JTextField(
            java.time.LocalDate.now().toString()
    );

    JTextField timeField = new JTextField("10:00");

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

    panel.add(new JLabel("Date (YYYY-MM-DD):"));
    panel.add(dateField);

    panel.add(new JLabel("Time (HH:MM):"));
    panel.add(timeField);

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

        // Validate date and time
        LocalDateTime dateTime = LocalDateTime.parse(
                dateField.getText().trim()
                + "T"
                + timeField.getText().trim()
        );

        String status =
                statusCombo.getSelectedItem().toString();

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
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 220, -1, -1));

        jButton2.setText("Mark Confirmed");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 300, 140, -1));

        jButton3.setText("Mark Completed");
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 370, 140, -1));

        jButton4.setText("Cancel Appointment");
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 450, -1, -1));

        jButton5.setText("Close");
        jButton5.addActionListener(this::jButton5ActionPerformed);
        jPanel1.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 520, 140, -1));

        jButton6.setText("Dashboard");
        jButton6.addActionListener(this::jButton6ActionPerformed);
        jPanel1.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 600, 140, -1));

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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
