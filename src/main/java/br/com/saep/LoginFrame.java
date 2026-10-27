package br.com.saep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Tela de autenticacao de usuarios. */
public class LoginFrame extends JFrame {
    private final JTextField campoUsuario = new JTextField(20);
    private final JPasswordField campoSenha = new JPasswordField(20);

    public LoginFrame() {
        setTitle("SAEP | Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(790, 490);
        setMinimumSize(new Dimension(700, 440));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Util.FUNDO);

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setBackground(Util.AZUL);
        lateral.setPreferredSize(new Dimension(290, 490));
        lateral.setBorder(BorderFactory.createEmptyBorder(45, 28, 35, 28));
        JLabel marca = new JLabel("SAEP", SwingConstants.LEFT);
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font("SansSerif", Font.BOLD, 36));
        JLabel descricao = new JLabel("Controle de estoque\nde autopecas", SwingConstants.LEFT);
        descricao.setForeground(new Color(227, 235, 245));
        descricao.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lateral.add(marca, BorderLayout.NORTH);
        lateral.add(descricao, BorderLayout.CENTER);
        JLabel rodape = new JLabel("Inventario com rastreabilidade");
        rodape.setForeground(new Color(193, 209, 228));
        lateral.add(rodape, BorderLayout.SOUTH);
        add(lateral, BorderLayout.WEST);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createEmptyBorder(30, 42, 30, 42));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 8, 0);
        JLabel titulo = new JLabel("Bem-vindo(a)");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(Util.TEXTO);
        formulario.add(titulo, c);
        c.gridy++; c.insets = new Insets(0, 0, 22, 0);
        JLabel subtitulo = new JLabel("Entre com suas credenciais para continuar.");
        subtitulo.setForeground(new Color(105, 115, 128));
        formulario.add(subtitulo, c);
        c.gridwidth = 1; c.gridy++; c.insets = new Insets(0, 0, 7, 12);
        formulario.add(new JLabel("Usuario"), c);
        c.gridx = 1; c.insets = new Insets(0, 0, 7, 0);
        formulario.add(new JLabel(""), c);
        c.gridx = 0; c.gridy++; c.gridwidth = 2; c.insets = new Insets(0, 0, 16, 0);
        campoUsuario.setPreferredSize(new Dimension(300, 36));
        campoUsuario.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formulario.add(campoUsuario, c);
        c.gridy++; c.insets = new Insets(0, 0, 7, 0);
        formulario.add(new JLabel("Senha"), c);
        c.gridy++; c.insets = new Insets(0, 0, 20, 0);
        campoSenha.setPreferredSize(new Dimension(300, 36));
        campoSenha.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formulario.add(campoSenha, c);
        c.gridy++; c.insets = new Insets(0, 0, 12, 0);
        javax.swing.JButton entrar = Util.botao("Entrar no sistema", Util.AZUL_CLARO);
        entrar.setPreferredSize(new Dimension(300, 42));
        entrar.addActionListener(e -> autenticar());
        formulario.add(entrar, c);
        c.gridy++; c.insets = new Insets(2, 0, 0, 0);
        JLabel dica = new JLabel("Acesso demonstrativo: admin / admin123");
        dica.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dica.setForeground(new Color(115, 123, 134));
        formulario.add(dica, c);
        add(formulario, BorderLayout.CENTER);
        getRootPane().setDefaultButton(entrar);
    }

    private void autenticar() {
        String usuario = campoUsuario.getText().trim();
        String senha = new String(campoSenha.getPassword());
        if (usuario.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe usuario e senha.", "Dados obrigatorios", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sql = "SELECT id, nome, usuario, senha_hash, ativo FROM usuarios WHERE usuario = ? LIMIT 1";
        try (Connection con = Util.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Usuario nao encontrado.", "Falha de autenticacao", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (rs.getInt("ativo") != 1) {
                    JOptionPane.showMessageDialog(this, "Este usuario esta desativado. Procure o administrador.", "Acesso negado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Util.sha256(senha).equalsIgnoreCase(rs.getString("senha_hash"))) {
                    JOptionPane.showMessageDialog(this, "Senha incorreta. Tente novamente.", "Falha de autenticacao", JOptionPane.ERROR_MESSAGE);
                    campoSenha.setText("");
                    return;
                }
                UsuarioSessao sessao = new UsuarioSessao(rs.getInt("id"), rs.getString("nome"), rs.getString("usuario"));
                new DashboardFrame(sessao).setVisible(true);
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Nao foi possivel conectar ao banco de dados. Verifique se o MySQL esta iniciado, se o script saep_db.sql foi executado e se SAEP_DB_USER/SAEP_DB_PASSWORD estao corretos.\n\nDetalhe: " + ex.getMessage(),
                "Erro de conexao", JOptionPane.ERROR_MESSAGE);
        }
    }
}
