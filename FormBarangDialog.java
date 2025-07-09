import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

class FormBarangDialog extends JDialog {
    JTextField tfNamaBarang, tfStok, tfHargaIns, tfHargaUser;
    Connection conn;
    DataBarangPanel parent;

    public FormBarangDialog(Connection conn, DataBarangPanel parent) {
        this.conn = conn;
        this.parent = parent;

        setTitle("Tambah Barang");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setModal(true);

        // ➤ Buat panel isi dengan grid
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margin luar

        tfNamaBarang = new JTextField();
        tfStok = new JTextField();
        tfHargaIns = new JTextField();
        tfHargaUser = new JTextField();

        formPanel.add(new JLabel("Nama Barang"));
        formPanel.add(tfNamaBarang);
        formPanel.add(new JLabel("Stok"));
        formPanel.add(tfStok);
        formPanel.add(new JLabel("Harga Installer"));
        formPanel.add(tfHargaIns);
        formPanel.add(new JLabel("Harga User"));
        formPanel.add(tfHargaUser);

        JButton btnSimpan = new JButton("Simpan");
        JButton btnBatal = new JButton("Batal");
        formPanel.add(btnSimpan);
        formPanel.add(btnBatal);

        setContentPane(formPanel); // ➤ Set panel sebagai konten dialog

        // ➤ Event tombol
        btnSimpan.addActionListener(e -> {
            try {
                PreparedStatement st = conn.prepareStatement(
                        "INSERT INTO barang (nama_barang, stok, harga_ins, harga_user) VALUES (?, ?, ?, ?)");
                st.setString(1, tfNamaBarang.getText());
                st.setInt(2, Integer.parseInt(tfStok.getText()));
                st.setInt(3, Integer.parseInt(tfHargaIns.getText()));
                st.setInt(4, Integer.parseInt(tfHargaUser.getText()));
                st.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Barang berhasil ditambahkan!");
                parent.loadTable();
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Gagal menambahkan barang!");
            }
        });

        btnBatal.addActionListener(e -> dispose());
    }
}
