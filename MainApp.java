import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class MainApp extends JFrame {
    Connection conn;

    public MainApp() {
        setTitle("Sistem Administrasi Electratech");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel untuk tombol refresh
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("🔄 Refresh Semua Data");
        topPanel.add(btnRefresh);

        // TabbedPane dan koneksi database
        JTabbedPane tabbedPane = new JTabbedPane();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/electratech", "root", "");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Gagal koneksi ke database");
            e.printStackTrace();
            return;
        }

        tabbedPane.addTab("Data Customer", new DataCustomerPanel(conn));
        tabbedPane.addTab("Data Barang", new DataBarangPanel(conn));
        tabbedPane.addTab("Paket", new PaketPanel(conn));
        tabbedPane.addTab("Pembelian", new PembelianPanel(conn));

        btnRefresh.addActionListener(e -> refreshAllPanels());

        // Pasang topPanel dan tabbedPane ke dalam layout utama
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }
    public void refreshAllPanels() {
        if (getContentPane().getComponentCount() > 1) {
            Component comp = ((JTabbedPane)((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER)).getSelectedComponent();
        }

        // Iterate all tabs:
        Component c = null;
        if (getContentPane().getComponentCount() > 1) {
            c = getContentPane().getComponent(1); // tabbedPane is second
        }
        if (c instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane)c;
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Component comp = tabbedPane.getComponentAt(i);
                if (comp instanceof DataCustomerPanel) {
                    ((DataCustomerPanel) comp).loadTable("", "nama");
                } else if (comp instanceof DataBarangPanel) {
                    ((DataBarangPanel) comp).loadTable();
                } else if (comp instanceof PaketPanel) {
                    ((PaketPanel) comp).loadPaket();
                }
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp());
    }
}

// Panel DataCustomerPanel.java
class DataCustomerPanel extends JPanel {
    JTextField searchField;
    JComboBox<String> searchColumn;
    JTable table;
    DefaultTableModel model;
    Connection conn;

    public DataCustomerPanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        // Tabel customer
        model = new DefaultTableModel(new String[]{
                "ID", "Nama", "Kelurahan", "Kota/Kabupaten", "No. Telp"
        }, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Data Customer"));

        // Search
        JPanel searchPanel = new JPanel();
        searchColumn = new JComboBox<>(new String[]{"nama", "kelurahan", "kota_kabupaten", "no_telp"});
        searchField = new JTextField(20);
        JButton btnSearch = new JButton("Cari");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Hapus");

        searchPanel.add(new JLabel("Cari berdasarkan:"));
        searchPanel.add(searchColumn);
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        searchPanel.add(btnEdit);
        searchPanel.add(btnDelete);

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> loadTable(searchField.getText().trim(),
                searchColumn.getSelectedItem().toString()));
        btnEdit.addActionListener(e -> editSelectedCustomer());
        btnDelete.addActionListener(e -> deleteSelectedCustomer());

        loadTable("", "nama");
    }

    private boolean authenticateAdmin() {
        JPanel authPanel = new JPanel(new GridLayout(2, 2));
        authPanel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        authPanel.add(usernameField);
        authPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        authPanel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(
                this, authPanel, "Login Admin",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return false;

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        try (PreparedStatement st = conn.prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=?"
        )) {
            st.setString(1, username);
            st.setString(2, password);
            ResultSet rs = st.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void editSelectedCustomer() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        if (!authenticateAdmin()) {
            JOptionPane.showMessageDialog(this, "❌ Login gagal!");
            return;
        }

        // Ambil data customer
        int id = (int) model.getValueAt(row, 0);
        String nama = model.getValueAt(row, 1).toString();
        String kelurahan = model.getValueAt(row, 2).toString();
        String kotaKab = model.getValueAt(row, 3).toString();
        String noTelp = model.getValueAt(row, 4).toString();

        // Dialog edit
        JTextField fNama = new JTextField(nama);
        JTextField fKel = new JTextField(kelurahan);
        JTextField fKota = new JTextField(kotaKab);
        JTextField fTelp = new JTextField(noTelp);

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Nama:")); panel.add(fNama);
        panel.add(new JLabel("Kelurahan:")); panel.add(fKel);
        panel.add(new JLabel("Kota/Kab:")); panel.add(fKota);
        panel.add(new JLabel("No. Telp:")); panel.add(fTelp);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Edit Customer",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            try (PreparedStatement st = conn.prepareStatement(
                    "UPDATE customer SET nama=?, kelurahan=?, kota_kabupaten=?, no_telp=? WHERE id_customer=?"
            )) {
                st.setString(1, fNama.getText().trim());
                st.setString(2, fKel.getText().trim());
                st.setString(3, fKota.getText().trim());
                st.setString(4, fTelp.getText().trim());
                st.setInt(5, id);
                st.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Data berhasil diubah!");
                loadTable("", "nama");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "❌ Gagal ubah data!");
            }
        }
    }

    private void deleteSelectedCustomer() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        if (!authenticateAdmin()) {
            JOptionPane.showMessageDialog(this, "❌ Login gagal!");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
                this, "Yakin hapus data ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (PreparedStatement st = conn.prepareStatement(
                    "DELETE FROM customer WHERE id_customer=?"
            )) {
                st.setInt(1, id);
                st.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Data berhasil dihapus!");
                loadTable("", "nama");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "❌ Gagal hapus data!");
            }
        }
    }

    void loadTable(String keyword, String column) {
        try {
            model.setRowCount(0);
            String sql = "SELECT id_customer, nama, kelurahan, kota_kabupaten, no_telp FROM customer";
            if (!keyword.isEmpty()) {
                sql += " WHERE " + column + " LIKE ?";
            }
            PreparedStatement st = conn.prepareStatement(sql);
            if (!keyword.isEmpty()) {
                st.setString(1, "%" + keyword + "%");
            }
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_customer"),
                        rs.getString("nama"),
                        rs.getString("kelurahan"),
                        rs.getString("kota_kabupaten"),
                        rs.getString("no_telp")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class DataBarangPanel extends JPanel {
    JTable table;
    DefaultTableModel model;
    Connection conn;

    public DataBarangPanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"ID", "Nama Barang", "Stok", "Harga Installer", "Harga User"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Data Barang"));

        JButton btnTambah = new JButton("Tambah Barang");
        JButton btnHapus = new JButton("Hapus");

        JPanel panelButton = new JPanel();
        panelButton.add(btnTambah);
        panelButton.add(btnHapus);

        add(scrollPane, BorderLayout.CENTER);
        add(panelButton, BorderLayout.SOUTH);

        loadTable();

        btnTambah.addActionListener(e -> {
            new FormBarangDialog(conn, this).setVisible(true);
        });

        btnHapus.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int id = Integer.parseInt(table.getValueAt(row, 0).toString());
            try {
                PreparedStatement st = conn.prepareStatement("DELETE FROM barang WHERE id_barang=?");
                st.setInt(1, id);
                st.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Barang berhasil dihapus!");
                loadTable();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Gagal menghapus barang!");
            }
        });
    }

    void loadTable() {
        try {
            model.setRowCount(0);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM barang");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_barang"),
                        rs.getString("nama_barang"),
                        rs.getInt("stok"),
                        rs.getInt("harga_ins"),
                        rs.getInt("harga_user")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class PaketPanel extends JPanel {
    JTable tablePaket;
    DefaultTableModel modelPaket;
    Connection conn;

    public PaketPanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());

        modelPaket = new DefaultTableModel(
                new String[]{"ID", "Nama Paket", "Harga Ins", "Harga User"}, 0
        );

        tablePaket = new JTable(modelPaket);
        JScrollPane scrollPane = new JScrollPane(tablePaket);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Daftar Paket"));

        JButton btnTambah = new JButton("Tambah Paket");
        btnTambah.addActionListener(e -> {
            new PaketDialog(SwingUtilities.getWindowAncestor(this), conn, this).setVisible(true);
        });

        JButton btnHapus = new JButton("Hapus Paket");
        btnHapus.addActionListener(e -> hapusPaket());

        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelButton.add(btnTambah);
        panelButton.add(btnHapus);

        add(scrollPane, BorderLayout.CENTER);
        add(panelButton, BorderLayout.SOUTH);

        loadPaket();
    }

    public void loadPaket() {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id_paket, nama_paket, harga_ins, harga_user FROM paket")) {

            modelPaket.setRowCount(0); // kosongkan
            while (rs.next()) {
                modelPaket.addRow(new Object[]{
                        rs.getInt("id_paket"),
                        rs.getString("nama_paket"),
                        rs.getInt("harga_ins"),
                        rs.getInt("harga_user")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hapusPaket() {
        int row = tablePaket.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "❌ Pilih paket yang mau dihapus!");
            return;
        }
        int idPaket = Integer.parseInt(modelPaket.getValueAt(row, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(
                this, "Yakin mau hapus paket ini beserta isinya?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Hapus detail dulu
                PreparedStatement delDetail = conn.prepareStatement(
                        "DELETE FROM paket_detail WHERE id_paket=?"
                );
                delDetail.setInt(1, idPaket);
                delDetail.executeUpdate();

                // Hapus paket
                PreparedStatement delPaket = conn.prepareStatement(
                        "DELETE FROM paket WHERE id_paket=?"
                );
                delPaket.setInt(1, idPaket);
                delPaket.executeUpdate();

                JOptionPane.showMessageDialog(this, "✅ Paket berhasil dihapus!");
                loadPaket();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Gagal hapus paket!");
            }
        }
    }
}
class PembelianPanel extends JPanel {
    JTable tableKeranjang;
    DefaultTableModel modelKeranjang;
    JTextField totalHargaField, fieldJumlah;
    JTextField fieldNamaCustomer, fieldKelurahan, fieldKotaKabupaten, fieldNoTelp;
    JComboBox<String> comboBarang, comboPaket;
    JCheckBox cbInstaller, cbUser;
    JButton btnTambahKeranjang, btnSimpan, btnHapusKeranjang, btnPrint;

    Map<String, Integer> barangMap = new HashMap<>();
    Map<String, Integer> paketMap = new HashMap<>();
    List<CartItem> keranjang = new ArrayList<>();
    int totalHarga = 0;

    Connection conn;

    public PembelianPanel(Connection conn) {
        this.conn = conn;
        setLayout(new BorderLayout());
        initComponents();
        loadComboData();
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Data Customer & Pembelian"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Fields customer
        fieldNamaCustomer = new JTextField(20);
        fieldKelurahan = new JTextField(20);
        fieldKotaKabupaten = new JTextField(20);
        fieldNoTelp = new JTextField(15);

        comboBarang = new JComboBox<>();
        comboPaket = new JComboBox<>();
        fieldJumlah = new JTextField(10);
        cbInstaller = new JCheckBox("Installer");
        cbUser = new JCheckBox("User"); cbUser.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(cbInstaller); group.add(cbUser);

        btnTambahKeranjang = new JButton("+ Keranjang");
        btnSimpan = new JButton("Simpan");
        btnHapusKeranjang = new JButton("Hapus Keranjang");
        btnPrint = new JButton("Print Detail");

        int row = 0;
        addRow(formPanel, gbc, row++, "Nama Customer", fieldNamaCustomer);
        addRow(formPanel, gbc, row++, "Kelurahan", fieldKelurahan);
        addRow(formPanel, gbc, row++, "Kota/Kabupaten", fieldKotaKabupaten);
        addRow(formPanel, gbc, row++, "No. Telp", fieldNoTelp);

        addRow(formPanel, gbc, row++, "Barang", comboBarang);
        addRow(formPanel, gbc, row++, "Jumlah Barang", fieldJumlah);
        addRow(formPanel, gbc, row++, "Paket", comboPaket);

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Tipe"), gbc);
        JPanel tipePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tipePanel.add(cbInstaller); tipePanel.add(cbUser);
        gbc.gridx = 1; formPanel.add(tipePanel, gbc); row++;

        JPanel panelBtn = new JPanel();
        panelBtn.add(btnTambahKeranjang); panelBtn.add(btnHapusKeranjang); panelBtn.add(btnSimpan); panelBtn.add(btnPrint);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(panelBtn, gbc); row++;

        totalHargaField = new JTextField("0", 10); totalHargaField.setEditable(false);
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.add(new JLabel("Total Harga:")); totalPanel.add(totalHargaField);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(totalPanel, gbc); row++;

        modelKeranjang = new DefaultTableModel(new String[]{"Barang/Paket","Jumlah","Harga Satuan","Subtotal"}, 0);
        tableKeranjang = new JTable(modelKeranjang); JScrollPane scrollKeranjang = new JScrollPane(tableKeranjang);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formPanel, scrollKeranjang);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        btnTambahKeranjang.addActionListener(e -> tambahKeKeranjang());
        btnHapusKeranjang.addActionListener(e -> hapusKeranjang());
        comboPaket.addActionListener(e -> tambahPaketKeKeranjang());
        btnSimpan.addActionListener(e -> simpanPembelian());
        btnPrint.addActionListener(e -> printDetail());
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; panel.add(comp, gbc);
    }

    void tambahKeKeranjang() {
        if (comboBarang.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this,"❗ Pilih barang dahulu!");
            return;
        }
        try {
            String namaBarang = comboBarang.getSelectedItem().toString();
            int idBarang = barangMap.get(namaBarang);
            int jumlah = Integer.parseInt(fieldJumlah.getText());
            boolean isInstaller = cbInstaller.isSelected();

            PreparedStatement st = conn.prepareStatement(
                    "SELECT harga_ins, harga_user FROM barang WHERE id_barang=?");
            st.setInt(1, idBarang); ResultSet rs = st.executeQuery();
            if (rs.next()) {
                int harga = isInstaller ? rs.getInt("harga_ins") : rs.getInt("harga_user");
                int subtotal = harga * jumlah;
                keranjang.add(new CartItem(idBarang, namaBarang, jumlah, harga));
                modelKeranjang.addRow(new Object[]{namaBarang + " ("+(isInstaller?"Installer":"User")+")", jumlah, harga, subtotal});
                totalHarga += subtotal; totalHargaField.setText(String.valueOf(totalHarga));
                fieldJumlah.setText("");
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this,"❌ Gagal menambahkan ke keranjang!"); }
    }

    void tambahPaketKeKeranjang() {
        if (comboPaket.getSelectedIndex() <= 0) return;
        try {
            String namaPaket = comboPaket.getSelectedItem().toString();
            int idPaket = paketMap.get(namaPaket); boolean isInstaller = cbInstaller.isSelected();
            PreparedStatement st = conn.prepareStatement(
                    "SELECT harga_ins, harga_user FROM paket WHERE id_paket=?");
            st.setInt(1, idPaket); ResultSet rs = st.executeQuery();
            if (rs.next()) {
                int harga = isInstaller ? rs.getInt("harga_ins") : rs.getInt("harga_user");
                keranjang.add(new CartItem(idPaket, namaPaket, 1, harga));
                modelKeranjang.addRow(new Object[]{namaPaket+" ("+(isInstaller?"Installer":"User")+")", 1, harga, harga});
                totalHarga += harga; totalHargaField.setText(String.valueOf(totalHarga));
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this,"❌ Gagal menambahkan paket!"); }
    }

    void hapusKeranjang() {
        int row = tableKeranjang.getSelectedRow();
        if (row == -1) return;
        int subtotal = (int) modelKeranjang.getValueAt(row, 3); totalHarga -= subtotal;
        totalHargaField.setText(String.valueOf(totalHarga));
        modelKeranjang.removeRow(row); keranjang.remove(row);
    }

    void simpanPembelian() {
        System.out.println("=== MULAI SIMPAN PEMBELIAN ===");
        try {
            conn.setAutoCommit(false); // Pakai transaction agar lebih aman

            // ✅ Simpan customer baru
            int idCustomer = -1;
            try {
                System.out.println("Menyimpan customer baru...");
                PreparedStatement stCustomer = conn.prepareStatement(
                        "INSERT INTO customer(nama, kelurahan, kota_kabupaten, no_telp) VALUES(?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                stCustomer.setString(1, fieldNamaCustomer.getText());
                stCustomer.setString(2, fieldKelurahan.getText());
                stCustomer.setString(3, fieldKotaKabupaten.getText());
                stCustomer.setString(4, fieldNoTelp.getText());
                stCustomer.executeUpdate();
                ResultSet rsKey = stCustomer.getGeneratedKeys();
                if (rsKey.next()) {
                    idCustomer = rsKey.getInt(1);
                    System.out.println("ID Customer baru = " + idCustomer);
                } else {
                    throw new SQLException("❌ Gagal mendapatkan idCustomer baru.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new Exception("Error simpan customer: " + ex.getMessage());
            }

            int total = Integer.parseInt(totalHargaField.getText());
            int idPembelian = -1;

            // ✅ Simpan pembelian
            try {
                System.out.println("Menyimpan pembelian baru (total = " + total + ")");
                PreparedStatement stPembelian = conn.prepareStatement(
                        "INSERT INTO pembelian(id_customer, total_harga, tanggal) VALUES (?, ?, NOW())",
                        Statement.RETURN_GENERATED_KEYS
                );
                stPembelian.setInt(1, idCustomer);
                stPembelian.setInt(2, total);
                stPembelian.executeUpdate();
                ResultSet rsKey2 = stPembelian.getGeneratedKeys();
                if (rsKey2.next()) {
                    idPembelian = rsKey2.getInt(1);
                    System.out.println("ID Pembelian baru = " + idPembelian);
                } else {
                    throw new SQLException("❌ Gagal mendapatkan idPembelian baru.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new Exception("Error simpan pembelian: " + ex.getMessage());
            }

            // ✅ Simpan detail pembelian
            for (int row = 0; row < modelKeranjang.getRowCount(); row++) {
                String labelBarang = modelKeranjang.getValueAt(row, 0).toString();
                int jumlah = (int) modelKeranjang.getValueAt(row, 1);
                int hargaSatuan = (int) modelKeranjang.getValueAt(row, 2);
                int subtotal = (int) modelKeranjang.getValueAt(row, 3);
                String plainName = labelBarang.replaceAll("\\s*\\(.*\\)$", "");

                System.out.println(String.format(
                        "Row %d: label=%s plainName=%s jumlah=%d hargaSatuan=%d subtotal=%d",
                        row, labelBarang, plainName, jumlah, hargaSatuan, subtotal
                ));

                Integer idBarangOrPaket = barangMap.containsKey(plainName)
                        ? barangMap.get(plainName)
                        : paketMap.get(plainName);
                if (idBarangOrPaket == null) {
                    throw new SQLException("❌ Tidak ada id untuk plainName: " + plainName);
                }

                try {
                    System.out.println("Menyimpan detail pembelian id_barang=" + idBarangOrPaket);
                    PreparedStatement stDetail = conn.prepareStatement(
                            "INSERT INTO pembelian_detail(id_pembelian, id_barang, jumlah, harga_satuan, subtotal) VALUES (?,?,?,?,?)"
                    );
                    stDetail.setInt(1, idPembelian);
                    stDetail.setInt(2, idBarangOrPaket);
                    stDetail.setInt(3, jumlah);
                    stDetail.setInt(4, hargaSatuan);
                    stDetail.setInt(5, subtotal);
                    stDetail.executeUpdate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new Exception("Error simpan detail pembelian: " + ex.getMessage());
                }

                // ✅ Jika barang biasa => update stok
                if (barangMap.containsKey(plainName)) {
                    try {
                        System.out.println("Mengurangi stok barang id=" + idBarangOrPaket + " sebesar " + jumlah);
                        PreparedStatement upStok = conn.prepareStatement(
                                "UPDATE barang SET stok = stok - ? WHERE id_barang=?"
                        );
                        upStok.setInt(1, jumlah); upStok.setInt(2, idBarangOrPaket); upStok.executeUpdate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new Exception("Error update stok: " + ex.getMessage());
                    }
                } else {
                    // ✅ Kalau paket, ambil semua barang di paket_detail
                    try {
                        System.out.println("Mengurangi stok barang di paket id_paket=" + idBarangOrPaket);
                        PreparedStatement stPaketDetail = conn.prepareStatement(
                                "SELECT id_barang, jumlah_barang FROM paket_detail WHERE id_paket=?"
                        );
                        stPaketDetail.setInt(1, idBarangOrPaket); ResultSet rsPD = stPaketDetail.executeQuery();
                        while (rsPD.next()) {
                            int idBarangPaket = rsPD.getInt("id_barang");
                            int jumlahBarang = rsPD.getInt("jumlah_barang") * jumlah;
                            System.out.println("-> Stok barang id=" + idBarangPaket + " dikurangi " + jumlahBarang);
                            PreparedStatement upStokPaket = conn.prepareStatement(
                                    "UPDATE barang SET stok = stok - ? WHERE id_barang=?"
                            );
                            upStokPaket.setInt(1, jumlahBarang); upStokPaket.setInt(2, idBarangPaket); upStokPaket.executeUpdate();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new Exception("Error update stok paket: " + ex.getMessage());
                    }
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this,"✅ Berhasil disimpan!");
            keranjang.clear();
            modelKeranjang.setRowCount(0);
            totalHargaField.setText("0");

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"❌ Gagal simpan pembelian!\n"+e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch(SQLException ignored){}
        }
    }


    void printDetail() {
        System.out.println("\n=== DETAIL KERANJANG ===");
        for (int row = 0; row < modelKeranjang.getRowCount(); row++) {
            System.out.printf(
                    "%s (%d) x %d = %d\n",
                    modelKeranjang.getValueAt(row,0),
                    modelKeranjang.getValueAt(row,2),
                    modelKeranjang.getValueAt(row,1),
                    modelKeranjang.getValueAt(row,3)
            );
        }
        System.out.println("Total: " + totalHargaField.getText());
    }

    void loadComboData() {
        try (Statement st = conn.createStatement()) {
            comboBarang.removeAllItems();
            comboBarang.addItem("-- Pilih Barang --"); barangMap.clear();
            ResultSet rs = st.executeQuery("SELECT id_barang,nama_barang FROM barang");
            while(rs.next()){
                barangMap.put(rs.getString("nama_barang"), rs.getInt("id_barang"));
                comboBarang.addItem(rs.getString("nama_barang"));
            }
            comboBarang.setSelectedIndex(0);

            comboPaket.removeAllItems();
            comboPaket.addItem("-- Pilih Paket --"); paketMap.clear();
            rs = st.executeQuery("SELECT id_paket,nama_paket FROM paket");
            while(rs.next()){
                paketMap.put(rs.getString("nama_paket"), rs.getInt("id_paket"));
                comboPaket.addItem(rs.getString("nama_paket"));
            }
            comboPaket.setSelectedIndex(0);
        } catch(Exception e){ e.printStackTrace(); }
    }

    class CartItem {
        int idBarang; String namaBarang; int jumlah; int hargaSatuan;
        CartItem(int idBarang, String namaBarang, int jumlah, int hargaSatuan) {
            this.idBarang=idBarang; this.namaBarang=namaBarang; this.jumlah=jumlah; this.hargaSatuan=hargaSatuan;
        }
    }
}
