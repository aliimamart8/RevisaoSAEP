package br.com.saep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Painel principal e resumo do estoque. */
public class DashboardFrame extends JFrame {
    private final UsuarioSessao sessao;
    private final JLabel produtosValor = valorCard();
    private final JLabel alertasValor = valorCard();
    private final JLabel movimentosValor = valorCard();
    private final JLabel alertaTexto = new JLabel(" ");
    private boolean alertaInicialExibido = false;

    public DashboardFrame(UsuarioSessao sessao) {
        this.sessao = sessao;
        setTitle("SAEP | Painel principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 690);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Util.FUNDO);
        setLayout(new BorderLayout(0, 0));

        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(Util.AZUL);
        topo.setBorder(BorderFactory.createEmptyBorder(17, 24, 17, 24));
        JLabel titulo = new JLabel("SAEP  |  Controle de Estoque");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        topo.add(titulo, BorderLayout.WEST);
        JPanel usuarioBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        usuarioBox.setOpaque(false);
        JLabel usuarioLabel = new JLabel("Logado: " + sessao.getNome());
        usuarioLabel.setForeground(Color.WHITE);
        usuarioLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        usuarioBox.add(usuarioLabel);
        javax.swing.JButton sair = Util.botao("Sair", new Color(83, 103, 128));
        sair.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        usuarioBox.add(sair);
        topo.add(usuarioBox, BorderLayout.EAST);
        add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 22));
        centro.setBackground(Util.FUNDO);
        centro.setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));
        JPanel intro = new JPanel(new BorderLayout(0, 6));
        intro.setOpaque(false);
        JLabel boasVindas = new JLabel("Visao geral");
        boasVindas.setFont(new Font("SansSerif", Font.BOLD, 27));
        boasVindas.setForeground(Util.TEXTO);
        intro.add(boasVindas, BorderLayout.NORTH);
        JLabel subtitulo = new JLabel("Acompanhe o inventario e registre as movimentacoes das pecas.");
        subtitulo.setForeground(new Color(102, 112, 126));
        intro.add(subtitulo, BorderLayout.CENTER);
        centro.add(intro, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 18, 0));
        cards.setOpaque(false);
        cards.add(card("Produtos ativos", produtosValor, new Color(42, 93, 155)));
        cards.add(card("Produtos abaixo do minimo", alertasValor, new Color(184, 82, 48)));
        cards.add(card("Movimentacoes registradas", movimentosValor, new Color(35, 132, 91)));
        centro.add(cards, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(0, 18));
        inferior.setOpaque(false);
        alertaTexto.setForeground(Util.VERMELHO);
        alertaTexto.setFont(new Font("SansSerif", Font.BOLD, 13));
        inferior.add(alertaTexto, BorderLayout.NORTH);
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        acoes.setOpaque(false);
        javax.swing.JButton produtos = Util.botao("Cadastro de Produto", Util.AZUL_CLARO);
        produtos.addActionListener(e -> abrirProdutos());
        javax.swing.JButton estoque = Util.botao("Gestao de Estoque e Historico", Util.VERDE);
        estoque.addActionListener(e -> abrirEstoque());
        acoes.add(produtos); acoes.add(estoque);
        inferior.add(acoes, BorderLayout.CENTER);
        centro.add(inferior, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
        atualizarResumo();
    }

    private JLabel valorCard() {
        JLabel l = new JLabel("-", SwingConstants.LEFT);
        l.setForeground(Util.TEXTO);
        l.setFont(new Font("SansSerif", Font.BOLD, 34));
        return l;
    }

    private JPanel card(String titulo, JLabel valor, Color detalhe) {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(4, 0, 0, 0, detalhe),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("SansSerif", Font.BOLD, 13));
        t.setForeground(new Color(103, 113, 127));
        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private void atualizarResumo() {
        String baixo = "";
        try (Connection con = Util.conectar()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM produtos WHERE ativo=1"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) produtosValor.setText(rs.getString(1));
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM produtos WHERE ativo=1 AND quantidade < estoque_minimo"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) alertasValor.setText(rs.getString(1));
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM movimentacoes"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) movimentosValor.setText(rs.getString(1));
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT nome, quantidade, estoque_minimo FROM produtos WHERE ativo=1 AND quantidade < estoque_minimo ORDER BY nome LIMIT 5"); ResultSet rs = ps.executeQuery()) {
                StringBuilder b = new StringBuilder();
                while (rs.next()) {
                    if (b.length() > 0) b.append("  |  ");
                    b.append(rs.getString("nome")).append(" (saldo ").append(rs.getInt("quantidade"))
                     .append(" / minimo ").append(rs.getInt("estoque_minimo")).append(")");
                }
                baixo = b.toString();
            }
            if (baixo.isBlank()) alertaTexto.setText("Estoque em dia: nenhum produto abaixo do minimo.");
            else alertaTexto.setText("ALERTA DE ESTOQUE: " + baixo);
            if (!baixo.isBlank() && !alertaInicialExibido) {
                alertaInicialExibido = true;
                JOptionPane.showMessageDialog(this, "Existem produtos abaixo do estoque minimo:\n" + baixo,
                    "Alerta automatico de estoque", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Nao foi possivel carregar o resumo: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirProdutos() {
        ProductFrame frame = new ProductFrame(sessao);
        frame.addWindowListener(new WindowAdapter() { @Override public void windowClosed(WindowEvent e) { atualizarResumo(); } });
        frame.setVisible(true);
    }
    private void abrirEstoque() {
        StockFrame frame = new StockFrame(sessao);
        frame.addWindowListener(new WindowAdapter() { @Override public void windowClosed(WindowEvent e) { atualizarResumo(); } });
        frame.setVisible(true);
    }
}
