import java.awt.BorderLayout;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

public class AppLaporanPresensi extends JFrame {

    // ================= DATABASE =================
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/db_presensi?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // ================= FILE JASPER =================
    private static final String JASPER_PATH = "report/presensi.jasper";

    private JasperPrint jasperPrint;

    public AppLaporanPresensi() {
        setTitle("Laporan Presensi Karyawan");
        setSize(450, 120);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();

        JButton btnGenerate = new JButton("Generate");
        JButton btnPreview  = new JButton("Preview");
        JButton btnExport   = new JButton("Export PDF");
        JButton btnExit     = new JButton("Keluar");

        panel.add(btnGenerate);
        panel.add(btnPreview);
        panel.add(btnExport);
        panel.add(btnExit);

        add(panel, BorderLayout.CENTER);

        // ====== ACTION ======
        btnGenerate.addActionListener(e -> {
            try {
                jasperPrint = buildReport();
                JOptionPane.showMessageDialog(this,
                        "Laporan berhasil dibuat");
            } catch (Exception ex) {
                showError(ex);
            }
        });

        btnPreview.addActionListener(e -> {
            if (jasperPrint == null) {
                JOptionPane.showMessageDialog(this,
                        "Klik Generate dulu");
                return;
            }
            JasperViewer.viewReport(jasperPrint, false);
        });

        btnExport.addActionListener(e -> {
            if (jasperPrint == null) {
                JOptionPane.showMessageDialog(this,
                        "Klik Generate dulu");
                return;
            }
            try {
                new File("output").mkdirs();
                JasperExportManager.exportReportToPdfFile(
                        jasperPrint,
                        "output/laporan_presensi.pdf"
                );
                JOptionPane.showMessageDialog(this,
                        "PDF berhasil dibuat di folder output/");
            } catch (Exception ex) {
                showError(ex);
            }
        });

        btnExit.addActionListener(e -> dispose());
    }

    // ================= BUILD REPORT =================
    private JasperPrint buildReport() throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");

        File file = new File(JASPER_PATH);
        if (!file.exists()) {
            throw new RuntimeException(
                "File presensi.jasper tidak ditemukan di:\n" +
                file.getAbsolutePath()
            );
        }

        JasperReport report =
                (JasperReport) JRLoader.loadObject(file);

        Map<String, Object> params = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASS)) {

            return JasperFillManager.fillReport(
                    report, params, conn
            );
        }
    }

    // ================= ERROR =================
    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new AppLaporanPresensi().setVisible(true)
        );
    }
}
