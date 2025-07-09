import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PaketDialog extends JDialog {
    JTextField fieldNamaPaket, fieldHargaIns, fieldHargaUser, fieldJumlah;
    JComboBox<String> comboBarang;
    DefaultTableModel modelIsi;
    JTable tableIsi;
    Map<String, Integer> barangMap = new HashMap<>();
    java.util.List<CartItem> isiPaket = new ArrayList<>();
    Connection conn;
    PaketPanel paketPanel;

    public PaketDialog(Window owner, Connection conn, PaketPanel paketPanel) {
        super(owner, "Tambah Paket", ModalityType.APPLICATION_MODAL);
        this.conn = conn;
        this.paketPanel = paketPanel;
        initComponents();
        loadBarang();
        pack();
        setLocationRelativeTo(owner);
    }

    void initComponents() {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        fieldNamaPaket = new JTextField(15);
        fieldHargaIns = new JTextField(10);
        fieldHargaUser = new JTextField(10);
        comboBarang = new JComboBox<>();
        fieldJumlah = new JTextField(5);

        int y = 0;
        form.add(new JLabel("Nama Paket:"), gbc); gbc.gridx=1; gbc.gridy=y; form.add(fieldNamaPaket, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Harga Ins:"), gbc); gbc.gridx=1; form.add(fieldHargaIns, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Harga User:"), gbc); gbc.gridx=1; form.add(fieldHargaUser, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Barang:"), gbc); gbc.gridx=1; form.add(comboBarang, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Jumlah:"), gbc); gbc.gridx=1; form.add(fieldJumlah, gbc); y++;

        JButton btnTambahBarang = new JButton("+");
        btnTambahBarang.addActionListener(e -> tambahBarang());
        gbc.gridx=2; form.add(btnTambahBarang, gbc); y++;

        modelIsi = new DefaultTableModel(new String[]{"Barang","Jumlah"},0);
        tableIsi = new JTable(modelIsi);

        JButton btnSimpan = new JButton("Simpan");
        btnSimpan.addActionListener(e -> simpanPaket());

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(tableIsi), BorderLayout.CENTER);
        add(btnSimpan, BorderLayout.SOUTH);
    }

    void loadBarang() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_barang, nama_barang FROM barang")) {
            barangMap.clear();
            comboBarang.removeAllItems();
            while (rs.next()) {
                comboBarang.addItem(rs.getString("nama_barang"));
                barangMap.put(rs.getString("nama_barang"), rs.getInt("id_barang"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void tambahBarang() {
        try {
            String nama = (String) comboBarang.getSelectedItem();
            int jumlah = Integer.parseInt(fieldJumlah.getText());
            isiPaket.add(new CartItem(barangMap.get(nama), nama, jumlah));
            modelIsi.addRow(new Object[]{nama, jumlah});
            fieldJumlah.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"❌ Jumlah salah!");
        }
    }

    void simpanPaket() {
        try {
            String namaPaket = fieldNamaPaket.getText();
            int hargaIns = Integer.parseInt(fieldHargaIns.getText());
            int hargaUser = Integer.parseInt(fieldHargaUser.getText());

            // Simpan paket
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO paket(nama_paket,harga_ins,harga_user) VALUES(?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, namaPaket);
            ps.setInt(2, hargaIns);
            ps.setInt(3, hargaUser);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int idPaket = keys.getInt(1);

            // Simpan detail
            for (CartItem c : isiPaket) {
                PreparedStatement detailSt = conn.prepareStatement(
                        "INSERT INTO paket_detail(id_paket,id_barang,jumlah) VALUES(?,?,?)"
                );
                detailSt.setInt(1, idPaket);
                detailSt.setInt(2, c.idBarang);
                detailSt.setInt(3, c.jumlah);
                detailSt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "✅ Paket berhasil disimpan!");
            paketPanel.loadPaket();
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Gagal simpan paket!");
        }
    }

    static class CartItem {
        int idBarang;
        String nama;
        int jumlah;
        CartItem(int idBarang, String nama, int jumlah) {
            this.idBarang=idBarang;
            this.nama=nama;
            this.jumlah=jumlah;
        }
    }
}
