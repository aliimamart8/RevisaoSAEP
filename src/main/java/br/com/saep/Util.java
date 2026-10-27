package br.com.saep;

import java.awt.Color;
import java.awt.Font;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Conexao com banco, hash de demonstracao e padroes visuais compartilhados. */
public final class Util {
    public static final Color AZUL = new Color(27, 54, 93);
    public static final Color AZUL_CLARO = new Color(42, 93, 155);
    public static final Color FUNDO = new Color(245, 247, 250);
    public static final Color TEXTO = new Color(42, 52, 65);
    public static final Color VERDE = new Color(35, 132, 91);
    public static final Color VERMELHO = new Color(176, 54, 54);

    private Util() { }

    public static Connection conectar() throws SQLException {
        String url = System.getenv().getOrDefault("SAEP_DB_URL",
            "jdbc:mysql://localhost:3306/saep_db?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true");
        String usuario = System.getenv().getOrDefault("SAEP_DB_USER", "root");
        String senha = System.getenv().getOrDefault("SAEP_DB_PASSWORD", "");
        return DriverManager.getConnection(url, usuario, senha);
    }

    public static String sha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(valor.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nao esta disponivel nesta JVM.", e);
        }
    }

    public static JButton botao(String texto, Color cor) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(cor);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        b.setOpaque(true);
        return b;
    }

    public static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    public static void prepararTabela(JTable tabela) {
        tabela.setRowHeight(27);
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabela.getTableHeader().setBackground(new Color(231, 237, 245));
        tabela.getTableHeader().setForeground(TEXTO);
        tabela.setSelectionBackground(new Color(215, 230, 248));
        tabela.setSelectionForeground(TEXTO);
        tabela.setFillsViewportHeight(true);
        tabela.setAutoCreateRowSorter(true);
    }

    public static String mensagemBanco(SQLException e) {
        String msg = e.getMessage() == null ? "erro sem detalhes" : e.getMessage();
        if (msg.toLowerCase().contains("duplicate") || msg.toLowerCase().contains("uk_"))
            return "Ja existe um registro com esse codigo ou usuario. Confira os campos unicos.";
        return "Falha no banco de dados: " + msg;
    }
}
