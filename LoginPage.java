import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPage extends JFrame {
    JTextField tfUsername;
    JPasswordField tfPassword;
    Connection conn;

    public LoginPage() {
        setTitle("Login - Electratech");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Panel Form ---
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Silakan Login"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        tfUsername = new JTextField(15);
        panel.add(tfUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        tfPassword = new JPasswordField(15);
        panel.add(tfPassword, gbc);

        JButton btnLogin = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnLogin, gbc);

        add(panel, BorderLayout.CENTER);

        // --- Koneksi DB ---
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/electratech", "root", "");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Gagal konek ke database!");
            e.printStackTrace();
        }

        // --- Event Login ---
        btnLogin.addActionListener(e -> {
            String username = tfUsername.getText();
            String password = new String(tfPassword.getPassword());

            try {
                PreparedStatement st = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                st.setString(1, username);
                st.setString(2, password);
                ResultSet rs = st.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "✅ Login Berhasil!");
                    showMenu();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Username atau Password salah!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }

    // --- Setelah login berhasil ---
    void showMenu() {
        JFrame menuFrame = new JFrame("Pilih Form");
        menuFrame.setSize(300, 200);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setLayout(new FlowLayout());

        JButton btnBarang = new JButton("Buka Form Barang");
        JButton btnCustomer = new JButton("Buka Data Customer");

        btnBarang.addActionListener(e -> {
            menuFrame.dispose();
            dispose();
            new FormBarang();  // Panggil class FormBarang
        });

        btnCustomer.addActionListener(e -> {
            menuFrame.dispose();
            dispose();
            new DataCustomer();  // Panggil class DataCustomer
        });

        menuFrame.add(btnBarang);
        menuFrame.add(btnCustomer);
        menuFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
