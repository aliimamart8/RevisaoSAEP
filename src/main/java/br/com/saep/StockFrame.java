package br.com.saep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Entrada/saida de estoque e consulta de historico de movimentacoes. */
public class StockFrame extends JFrame {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final UsuarioSessao sessao;
    private final JComboBox<ProdutoItem> produto = new JComboBox<>();
    private final JComboBox<String> tipo = new JComboBox<>(new String[] {"ENTRADA", "SAIDA"});
    private final JTextField quantidade = new JTextField("1", 8);
    private final JTextField data = new JTextField(LocalDateTime.now().format(FORMATO), 16);
    private final JTextField observacao = new JTextField(24);
    private final JLabel alerta = new JLabel(" ");
    private final DefaultTableModel modelo = Util.modelo("ID", "Codigo", "Produto", "Tipo", "Quantidade", "Data/hora", "Responsavel", "Observacao");
    private final JTable tabela = new JTable(modelo);

    private static class ProdutoItem {
        final int id; final String codigo; final String nome; final int quantidade; final int minimo;
        ProdutoItem(int id, String codigo, String nome, int quantidade, int minimo) {
            this.id=id; this.codigo=codigo; this.nome=nome; this.quantidade=quantidade; this.minimo=minimo;
        }
        @Override public String toString() { return codigo + " - " + nome + " (saldo: " + quantidade + ")"; }
    }

    public StockFrame(UsuarioSessao sessao) {
        this.sessao = sessao;
        setTitle("SAEP | Gestao de Estoque");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1160, 720);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Util.FUNDO);
        setLayout(new BorderLayout(10, 10));

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Color.WHITE);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel titulo = new JLabel("Gestao de Estoque");
        titulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        titulo.setForeground(Util.TEXTO);
        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(new JLabel("Responsavel: " + sessao.getNome()), BorderLayout.EAST);
        add(cabecalho, BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(8, 12));
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(0, 22, 20, 22));
        JPanel topo = new JPanel(new BorderLayout(0, 10));
        topo.setOpaque(false);
        alerta.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        alerta.setForeground(Util.VERMELHO);
        topo.add(alerta, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 226, 234)), "Registrar movimentacao"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8); c.anchor = GridBagConstraints.WEST;
        addField(formulario, c, 0, 0, "Produto", produto);
        addField(formulario, c, 2, 0, "Movimento", tipo);
        addField(formulario, c, 0, 1, "Quantidade *", quantidade);
        addField(formulario, c, 2, 1, "Data e hora (AAAA-MM-DD HH:MM) *", data);
        addField(formulario, c, 0, 2, "Observacao", observacao);
        javax.swing.JButton registrar = Util.botao("Registrar movimentacao", Util.VERDE);
        registrar.addActionListener(e -> registrarMovimentacao());
        c.gridx=2; c.gridy=2; c.gridwidth=2; c.fill=GridBagConstraints.NONE; c.weightx=0;
        formulario.add(registrar, c);
        topo.add(formulario, BorderLayout.CENTER);
        corpo.add(topo, BorderLayout.NORTH);

        JPanel historico = new JPanel(new BorderLayout(0, 8));
        historico.setOpaque(false);
        JLabel histTitulo = new JLabel("Historico completo de movimentacoes");
        histTitulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        histTitulo.setForeground(Util.TEXTO);
        historico.add(histTitulo, BorderLayout.NORTH);
        Util.prepararTabela(tabela);
        historico.add(new JScrollPane(tabela), BorderLayout.CENTER);
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rodape.setOpaque(false);
        javax.swing.JButton atualizar = Util.botao("Atualizar", Util.AZUL_CLARO);
        atualizar.addActionListener(e -> atualizarTudo());
        javax.swing.JButton voltar = Util.botao("Voltar ao painel", new Color(113, 123, 136));
        voltar.addActionListener(e -> dispose());
        rodape.add(atualizar); rodape.add(voltar);
        historico.add(rodape, BorderLayout.SOUTH);
        corpo.add(historico, BorderLayout.CENTER);
        add(corpo, BorderLayout.CENTER);
        atualizarTudo();
    }

    private void addField(JPanel p, GridBagConstraints original, int x, int y, String nome, java.awt.Component campo) {
        GridBagConstraints c1 = (GridBagConstraints) original.clone();
        c1.gridx=x; c1.gridy=y; c1.gridwidth=1; c1.fill=GridBagConstraints.NONE; c1.weightx=0;
        p.add(new JLabel(nome), c1);
        GridBagConstraints c2 = (GridBagConstraints) original.clone();
        c2.gridx=x+1; c2.gridy=y; c2.gridwidth=1; c2.fill=GridBagConstraints.HORIZONTAL; c2.weightx=1;
        p.add(campo, c2);
    }

    private void atualizarTudo() {
        carregarProdutos();
        carregarHistorico();
        atualizarAlerta();
    }

    private void carregarProdutos() {
        ProdutoItem atual = (ProdutoItem) produto.getSelectedItem();
        produto.removeAllItems();
        try (Connection con = Util.conectar(); PreparedStatement ps = con.prepareStatement(
                "SELECT id,codigo,nome,quantidade,estoque_minimo FROM produtos WHERE ativo=1 ORDER BY nome ASC"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProdutoItem item = new ProdutoItem(rs.getInt("id"), rs.getString("codigo"), rs.getString("nome"),
                    rs.getInt("quantidade"), rs.getInt("estoque_minimo"));
                produto.addItem(item);
                if (atual != null && atual.id == item.id) produto.setSelectedItem(item);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Nao foi possivel carregar os produtos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarHistorico() {
        modelo.setRowCount(0);
        String sql = "SELECT m.id,p.codigo,p.nome AS produto_nome,m.tipo,m.quantidade,DATE_FORMAT(m.data_movimentacao,'%d/%m/%Y %H:%i') AS data_fmt,u.nome AS responsavel,COALESCE(m.observacao,'') AS observacao " +
            "FROM movimentacoes m JOIN produtos p ON p.id=m.produto_id JOIN usuarios u ON u.id=m.usuario_id " +
            "ORDER BY m.data_movimentacao DESC,m.id DESC LIMIT 500";
        try (Connection con = Util.conectar(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) modelo.addRow(new Object[] {rs.getInt("id"), rs.getString("codigo"), rs.getString("produto_nome"),
                rs.getString("tipo"), rs.getInt("quantidade"), rs.getString("data_fmt"), rs.getString("responsavel"), rs.getString("observacao")});
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Nao foi possivel carregar o historico: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarAlerta() {
        try (Connection con = Util.conectar(); PreparedStatement ps = con.prepareStatement(
                "SELECT nome,quantidade,estoque_minimo FROM produtos WHERE ativo=1 AND quantidade < estoque_minimo ORDER BY nome"); ResultSet rs = ps.executeQuery()) {
            StringBuilder b = new StringBuilder();
            while (rs.next()) {
                if (b.length() > 0) b.append(" | ");
                b.append(rs.getString("nome")).append(": saldo ").append(rs.getInt("quantidade"))
                 .append(" / minimo ").append(rs.getInt("estoque_minimo"));
            }
            if (b.length() == 0) { alerta.setText("Estoque em dia: nenhum produto abaixo do minimo."); alerta.setForeground(Util.VERDE); }
            else { alerta.setText("ALERTA AUTOMATICO: " + b); alerta.setForeground(Util.VERMELHO); }
        } catch (SQLException ex) {
            alerta.setText("Nao foi possivel verificar os alertas de estoque.");
        }
    }

    private void registrarMovimentacao() {
        ProdutoItem item = (ProdutoItem) produto.getSelectedItem();
        if (item == null) { JOptionPane.showMessageDialog(this, "Cadastre pelo menos um produto ativo.", "Sem produtos", JOptionPane.WARNING_MESSAGE); return; }
        int qtd;
        LocalDateTime dataMov;
        try { qtd = Integer.parseInt(quantidade.getText().trim()); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "A quantidade deve ser um numero inteiro positivo.", "Validacao", JOptionPane.WARNING_MESSAGE); return; }
        if (qtd <= 0) { JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que zero.", "Validacao", JOptionPane.WARNING_MESSAGE); return; }
        try { dataMov = LocalDateTime.parse(data.getText().trim(), FORMATO); }
        catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(this, "Use o formato AAAA-MM-DD HH:MM, por exemplo 2026-10-27 14:30.", "Data invalida", JOptionPane.WARNING_MESSAGE); return; }
        String movimento = (String) tipo.getSelectedItem();
        String obs = observacao.getText().trim();
        try (Connection con = Util.conectar()) {
            con.setAutoCommit(false);
            try {
                int saldo; int minimo; String nome;
                try (PreparedStatement ps = con.prepareStatement("SELECT nome,quantidade,estoque_minimo FROM produtos WHERE id=? AND ativo=1 FOR UPDATE")) {
                    ps.setInt(1, item.id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { con.rollback(); JOptionPane.showMessageDialog(this, "O produto nao esta mais ativo.", "Operacao cancelada", JOptionPane.WARNING_MESSAGE); return; }
                        nome = rs.getString("nome"); saldo = rs.getInt("quantidade"); minimo = rs.getInt("estoque_minimo");
                    }
                }
                int novoSaldo = movimento.equals("ENTRADA") ? saldo + qtd : saldo - qtd;
                if (movimento.equals("SAIDA") && qtd > saldo) {
                    con.rollback();
                    JOptionPane.showMessageDialog(this, "Saida nao permitida: saldo atual " + saldo + " e quantidade solicitada " + qtd + ".", "Estoque insuficiente", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE produtos SET quantidade=? WHERE id=?")) {
                    ps.setInt(1, novoSaldo); ps.setInt(2, item.id); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO movimentacoes (produto_id,usuario_id,tipo,quantidade,data_movimentacao,observacao) VALUES (?,?,?,?,?,?)")) {
                    ps.setInt(1, item.id); ps.setInt(2, sessao.getId()); ps.setString(3, movimento); ps.setInt(4, qtd);
                    ps.setTimestamp(5, Timestamp.valueOf(dataMov)); ps.setString(6, obs.isBlank() ? null : obs); ps.executeUpdate();
                }
                con.commit();
                quantidade.setText("1"); observacao.setText("");
                atualizarTudo();
                String mensagem = (movimento.equals("ENTRADA") ? "Entrada" : "Saida") + " registrada para " + nome + ". Novo saldo: " + novoSaldo + ".";
                JOptionPane.showMessageDialog(this, mensagem, "Movimentacao concluida", JOptionPane.INFORMATION_MESSAGE);
                if (novoSaldo < minimo) JOptionPane.showMessageDialog(this,
                    "ALERTA: o produto " + nome + " ficou abaixo do estoque minimo. Saldo: " + novoSaldo + "; minimo: " + minimo + ".",
                    "Alerta de estoque minimo", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                con.rollback();
                if (ex instanceof SQLException) JOptionPane.showMessageDialog(this, Util.mensagemBanco((SQLException) ex), "Erro na movimentacao", JOptionPane.ERROR_MESSAGE);
                else JOptionPane.showMessageDialog(this, "Nao foi possivel registrar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } finally { con.setAutoCommit(true); }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro de conexao: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
