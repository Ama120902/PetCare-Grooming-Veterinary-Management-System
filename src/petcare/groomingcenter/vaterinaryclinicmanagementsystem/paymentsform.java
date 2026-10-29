/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package petcare.groomingcenter.vaterinaryclinicmanagementsystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.*;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Ama
 */
public class paymentsform extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(paymentsform.class.getName());
    private String currentUser = "Admin";
    
    /**
     * Creates new form paymentsform
     */
    public paymentsform() {
        initComponents();
        setLocationRelativeTo(null);
        loadpayments();
    }
    
    private void loadpayments(){
      String sql =
        "SELECT " +
        "pay.invoice_number AS Invoice, " +
        "c.customer_name AS Customer, " +
        "p.pet_name AS `Pet Name`, " +
        "s.service_name AS Service, " +
        "pay.amount AS Amount, " +
        "pay.payment_method AS Method, " +
        "pay.status AS Status " +
        "FROM payments pay " +
        "JOIN appointments a ON pay.appointment_id = a.appointment_id " +
        "JOIN customers c ON a.customer_id = c.customer_id " +
        "JOIN pets p ON a.pet_id = p.pet_id " +
        "JOIN services s ON a.service_id = s.service_id " +
        "ORDER BY pay.payment_id DESC";

    TableLoader.loadTable(jTable1, sql);  
    }

private void showRecordPaymentForm() {

            JTextField invoiceField = new JTextField(
        "INV-" + LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        )
    );
    invoiceField.setEditable(false);

    JComboBox<String> customerCombo = new JComboBox<>();
    JComboBox<String> petCombo = new JComboBox<>();
    JComboBox<String> appointmentCombo = new JComboBox<>();
    JComboBox<String> amountCombo = new JComboBox<>();

    JComboBox<String> methodCombo = new JComboBox<>(
        new String[]{"Cash", "Card", "Bank Transfer"}
    );

    JComboBox<String> statusCombo = new JComboBox<>(
        new String[]{"Paid", "Pending"}
    );

    JTextField dateField = new JTextField(LocalDate.now().toString());

    Map<String, Integer> customerIds = new LinkedHashMap<>();
    Map<String, Integer> petIds = new LinkedHashMap<>();
    Map<String, Integer> appointmentIds = new LinkedHashMap<>();
    Map<String, Double> appointmentPrices = new LinkedHashMap<>();

    // 1. Load customers from the database
    String customerSQL =
        "SELECT customer_id, customer_name " +
        "FROM customers ORDER BY customer_name";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(customerSQL);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            int id = rs.getInt("customer_id");
            String name = rs.getString("customer_name");

            String display = id + " - " + name;

            customerCombo.addItem(display);
            customerIds.put(display, id);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Error loading customers: " + e.getMessage()
        );
        return;
    }

    if (customerCombo.getItemCount() == 0) {
        JOptionPane.showMessageDialog(
            this, "No customers found. Please register a customer first."
        );
        return;
    }

    // 2. Load pets belonging to the selected customer
    Runnable loadPets = () -> {
        petCombo.removeAllItems();
        petIds.clear();

        appointmentCombo.removeAllItems();
        appointmentIds.clear();
        appointmentPrices.clear();
        amountCombo.removeAllItems();

        String selectedCustomer =
            (String) customerCombo.getSelectedItem();

        Integer customerId = customerIds.get(selectedCustomer);

        if (customerId == null) {
            return;
        }

        String petSQL =
            "SELECT pet_id, pet_name FROM pets " +
            "WHERE customer_id = ? ORDER BY pet_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(petSQL)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int petId = rs.getInt("pet_id");
                    String petName = rs.getString("pet_name");

                    String display = petId + " - " + petName;

                    petCombo.addItem(display);
                    petIds.put(display, petId);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this, "Error loading pets: " + e.getMessage()
            );
        }
    };

    // 3. Load appointments and actual service prices
    Runnable loadAppointments = () -> {
        appointmentCombo.removeAllItems();
        appointmentIds.clear();
        appointmentPrices.clear();
        amountCombo.removeAllItems();

        String selectedCustomer =
            (String) customerCombo.getSelectedItem();

        String selectedPet =
            (String) petCombo.getSelectedItem();

        Integer customerId = customerIds.get(selectedCustomer);
        Integer petId = petIds.get(selectedPet);

        if (customerId == null || petId == null) {
            return;
        }

        String appointmentSQL =
                "SELECT a.appointment_id, a.appointment_datetime, " +
                "a.customer_id, a.pet_id, s.service_name, s.price " +
                "FROM appointments a " +
                "INNER JOIN services s ON a.service_id = s.service_id " +
                "WHERE a.customer_id = ? AND a.pet_id = ? " +
                "ORDER BY a.appointment_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                 con.prepareStatement(appointmentSQL)) {

            ps.setInt(1, customerId);
            ps.setInt(2, petId);
            
            System.out.println(
                         "Selected customer ID: " + customerId);
            System.out.println(
                           "Selected pet ID: " + petId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int appointmentId =
                        rs.getInt("appointment_id");

                    String dateTime =
                        rs.getString("appointment_datetime");

                    String serviceName =
                        rs.getString("service_name");

                    double price = rs.getDouble("price");

                    String display =
                        "Appointment #" + appointmentId +
                        " | " + dateTime +
                        " | " + serviceName;

                    appointmentCombo.addItem(display);
                    appointmentIds.put(display, appointmentId);
                    appointmentPrices.put(display, price);
                    
                    System.out.println(
                            "Appointment found: " + rs.getInt("appointment_id"));
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this, "Error loading appointments: " + e.getMessage()
            );
        }

        // Display the actual service price for the selected appointment
        String selectedAppointment =
            (String) appointmentCombo.getSelectedItem();

        Double price = appointmentPrices.get(selectedAppointment);

        if (price != null) {
            amountCombo.addItem(
                "LKR " + String.format("%.2f", price)
            );
        }
    };

   // Update dropdowns when selections change
    customerCombo.addActionListener(e -> {
        loadPets.run();
    });

    petCombo.addActionListener(e -> {
        loadAppointments.run();
    });

    appointmentCombo.addActionListener(e -> {
    amountCombo.removeAllItems();

    String selectedAppointment =
        (String) appointmentCombo.getSelectedItem();

    Double price = appointmentPrices.get(selectedAppointment);

    if (price != null) {
        amountCombo.addItem(
            "LKR " + String.format("%.2f", price)
        );
    }
});

    // Load the first customer's pets and related appointments
    loadPets.run();

    JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));

    panel.add(new JLabel("Invoice Number:"));
    panel.add(invoiceField);

    panel.add(new JLabel("Customer Name:"));
    panel.add(customerCombo);

    panel.add(new JLabel("Pet Name:"));
    panel.add(petCombo);

    panel.add(new JLabel("Appointment:"));
    panel.add(appointmentCombo);

    panel.add(new JLabel("Amount (LKR):"));
    panel.add(amountCombo);

    panel.add(new JLabel("Payment Method:"));
    panel.add(methodCombo);

    panel.add(new JLabel("Status:"));
    panel.add(statusCombo);

    panel.add(new JLabel("Payment Date (YYYY-MM-DD):"));
    panel.add(dateField);

    int result = JOptionPane.showConfirmDialog(
        this,
        panel,
        "Record Payment",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (result != JOptionPane.OK_OPTION) {
        return;
    }

    String paymentDate = dateField.getText().trim();

    String selectedAppointment =
        (String) appointmentCombo.getSelectedItem();

    Integer appointmentId = appointmentIds.get(selectedAppointment);
    Double amount = appointmentPrices.get(selectedAppointment);

    if (appointmentId == null || amount == null) {
        JOptionPane.showMessageDialog(
            this,
            "Please select a customer, pet and valid appointment."
        );
        return;
    }

    try {
        LocalDate.parse(paymentDate);

        String invoiceNumber = invoiceField.getText().trim();
        String paymentMethod = (String) methodCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        String insertSQL =
            "INSERT INTO payments " +
            "(invoice_number, appointment_id, amount, " +
            "payment_method, status, payment_date) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                 con.prepareStatement(insertSQL)) {

            ps.setString(1, invoiceNumber);
            ps.setInt(2, appointmentId);
            ps.setDouble(3, amount);
            ps.setString(4, paymentMethod);
            ps.setString(5, status);
            ps.setDate(6, java.sql.Date.valueOf(paymentDate));

            ps.executeUpdate();
        }

        JOptionPane.showMessageDialog(
            this, "Payment recorded successfully!"
        );

        loadpayments();

    } catch (java.time.format.DateTimeParseException e) {
        JOptionPane.showMessageDialog(
            this, "Invalid date. Use YYYY-MM-DD format."
        );

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(
            this, "Error recording payment: " + e.getMessage()
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

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(0, 0, 51));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Payments & Invoicing");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 20, -1, -1));

        jPanel2.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 1340, 90));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Invoice #", "Customer", "Pet Name", "Service", "Amount", "Method", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 1300, -1));

        jButton1.setText("+ Record Payment");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 600, -1, -1));

        jButton2.setText("Close");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 600, 130, -1));

        jButton3.setText("Dashboard");
        jButton3.addActionListener(this::jButton3ActionPerformed);
        jPanel2.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 600, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        new dashboardform(currentUser).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        showRecordPaymentForm();
    }//GEN-LAST:event_jButton1ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new paymentsform().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
