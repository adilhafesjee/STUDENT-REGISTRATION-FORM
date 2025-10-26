package studentapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ViewStudents extends JFrame {
    JTable table;
    DefaultTableModel model;

    public ViewStudents() {
        setTitle("Student Records");
        setSize(600, 400);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new String[] { "ID", "Name", "Email", "Phone", "Gender" }, 0);
        table = new JTable(model);
        loadStudents();

        JScrollPane scrollPane = new JScrollPane(table);

        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteStudent());

        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> editStudent());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadStudents() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                try (Connection conn = DBConnection.getConnection()) {
                    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM students");
                    while (rs.next()) {
                        rows.add(new Object[] {
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("gender")
                        });
                    }
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    model.setRowCount(0);
                    for (Object[] row : rows) {
                        model.addRow(row);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ViewStudents.this, "Error loading students.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.");
            return;
        }

        int id = (int) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete this student?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM students WHERE id = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                }
                return null;
            }

            @Override
            protected void done() {
                loadStudents();
            }
        }.execute();
    }

    private void editStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to edit.");
            return;
        }

        int id = (int) model.getValueAt(selectedRow, 0);
        String name = (String) model.getValueAt(selectedRow, 1);
        String email = (String) model.getValueAt(selectedRow, 2);
        String phone = (String) model.getValueAt(selectedRow, 3);
        String gender = (String) model.getValueAt(selectedRow, 4);

        JTextField tfName = new JTextField(name);
        JTextField tfEmail = new JTextField(email);
        JTextField tfPhone = new JTextField(phone);
        JComboBox<String> genderBox = new JComboBox<>(new String[] { "Male", "Female", "Other" });
        genderBox.setSelectedItem(gender);

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Name:"));
        panel.add(tfName);
        panel.add(new JLabel("Email:"));
        panel.add(tfEmail);
        panel.add(new JLabel("Phone:"));
        panel.add(tfPhone);
        panel.add(new JLabel("Gender:"));
        panel.add(genderBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Student", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String newName = tfName.getText().trim();
            String newEmail = tfEmail.getText().trim();
            String newPhone = tfPhone.getText().trim();
            String newGender = (String) genderBox.getSelectedItem();

            if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() || newGender == null) {
                JOptionPane.showMessageDialog(this, "All fields must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try (Connection conn = DBConnection.getConnection()) {
                        PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE students SET name=?, email=?, phone=?, gender=? WHERE id=?");
                        stmt.setString(1, newName);
                        stmt.setString(2, newEmail);
                        stmt.setString(3, newPhone);
                        stmt.setString(4, newGender);
                        stmt.setInt(5, id);
                        stmt.executeUpdate();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    loadStudents();
                }
            }.execute();
        }
    }
}
