package studentapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class StudentRegistrationForm extends JFrame implements ActionListener {
    JTextField tfName, tfEmail, tfPhone;
    JComboBox<String> genderBox;
    JButton submitButton, viewButton;

    public StudentRegistrationForm() {
        setTitle("Student Registration Form");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Name:"));
        tfName = new JTextField();
        panel.add(tfName);

        panel.add(new JLabel("Email:"));
        tfEmail = new JTextField();
        panel.add(tfEmail);

        panel.add(new JLabel("Phone:"));
        tfPhone = new JTextField();
        panel.add(tfPhone);

        panel.add(new JLabel("Gender:"));
        genderBox = new JComboBox<>(new String[] { "Select", "Male", "Female", "Other" });
        panel.add(genderBox);

        submitButton = new JButton("Register");
        submitButton.addActionListener(this);
        panel.add(submitButton);

        viewButton = new JButton("View Students");
        viewButton.addActionListener(e -> new ViewStudents());
        panel.add(viewButton);

        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = tfName.getText().trim();
        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();
        String gender = (String) genderBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || gender.equals("Select")) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.matches("^\\S+@\\S+\\.\\S+$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Use SwingWorker for DB insert
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection()) {
                    String createTable = "CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, phone TEXT, gender TEXT)";
                    conn.createStatement().execute(createTable);

                    String sql = "INSERT INTO students(name, email, phone, gender) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, phone);
                    stmt.setString(4, gender);
                    stmt.executeUpdate();
                }
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(StudentRegistrationForm.this, "Student Registered Successfully!");
                clearFields();
            }
        }.execute();
    }

    private void clearFields() {
        tfName.setText("");
        tfEmail.setText("");
        tfPhone.setText("");
        genderBox.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentRegistrationForm::new);
    }
}
