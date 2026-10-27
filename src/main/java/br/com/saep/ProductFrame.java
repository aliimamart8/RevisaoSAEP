package br.com.saep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Cadastro, busca, edicao e exclusao logica de produtos. */
public class ProductFrame extends JFrame {
    private final UsuarioSessao sessao;
    private final JTextField busca = new JTextField(24);
    private final DefaultTableModel modelo = Util.modelo("ID", "Codigo", "Produto", "Marca", "Modelo", "Ano", "Compatibilidade", "Saldo", "Estoque minimo");
    private final JTable tabela = new JTable(modelo);

    public ProductFrame(UsuarioSessao sessao) {
        this.sessao = sessao;
        setTitle("SAEP | Cadastro de Produto");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1120, 660);
        setMinimumSize(new Dimension(950, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Util.FUNDO);
        setLayout(new BorderLayout(12, 12));

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Color.WHITE);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JLabel titulo = new JLabel("Cadastro de Produto");
        titulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        titulo.setForeground(Util.TEXTO);
        cabecalho.add(titulo, BorderLayout.WEST);
        JLabel usuario = new JLabel("Responsavel: " + sessao.getNome());
        usuario.setForeground(new Color(100, 112, 126));
        cabecalho.add(usuario, BorderLayout.EAST);
        add(cabecalho, BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(8, 12));
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(2, 22, 20, 22));
        JPanel buscaPainel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buscaPainel.setOpaque(false);
        buscaPainel.add(new JLabel("Buscar por codigo, produto, marca ou modelo:"));
        busca.setPreferredSize(new Dimension(240, 34));
        buscaPainel.add(busca);
        javax.swing.JButton buscar = Util.botao("Buscar", Util.AZUL_CLARO);
        buscar.addActionListener(e -> carregarProdutos());
        buscaPainel.add(buscar);
        javax.swing.JButton limpar = Util.botao("Limpar", new Color(113, 123, 136));
        limpar.addActionListener(e -> { busca.setText(""); carregarProdutos(); });
        buscaPainel.add(limpar);
        corpo.add(buscaPainel, BorderLayout.NORTH);

        Util.prepararTabela(tabela);
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(222, 227, 234)));
        corpo.add(scroll, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        botoes.setOpaque(false);
        javax.swing.JButton novo = Util.botao("Novo produto", Util.VERDE);
        novo.addActionListener(e -> abrirFormulario(null));
        javax.swing.JButton editar = Util.botao("Editar selecionado", Util.AZUL_CLARO);
        editar.addActionListener(e -> editarSelecionado());
        javax.swing.JButton excluir = Util.botao("Excluir selecionado", Util.VERMELHO);
        excluir.addActionListener(e -> excluirSelecionado());
        javax.swing.JButton voltar = Util.botao("Voltar ao painel", new Color(113, 123, 136));
        voltar.addActionListener(e -> dispose());
        botoes.add(novo); botoes.add(editar); botoes.add(excluir); botoes.add(voltar);
        corpo.add(botoes, BorderLayout.SOUTH);
        add(corpo, BorderLayout.CENTER);
        getRootPane().setDefaultButton(buscar);
        carregarProdutos();
    }

    private void carregarProdutos() {
        modelo.setRowCount(0);
        String termo = "%" + busca.getText().trim() + "%";
        String sql = "SELECT id,codigo,nome,marca,modelo,ano_fabricacao,compatibilidade,quantidade,estoque_minimo " +
            "FROM produtos WHERE ativo=1 AND (codigo LIKE ? OR nome LIKE ? OR marca LIKE ? OR modelo LIKE ? OR compatibilidade LIKE ?) ORDER BY nome ASC";
        try (Connection con = Util.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i=1; i<=5; i++) ps.setString(i, termo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) modelo.addRow(new Object[] { rs.getInt("id"), rs.getString("codigo"), rs.getString("nome"),
                    rs.getString("marca"), rs.getString("modelo"), rs.getInt("ano_fabricacao"), rs.getString("compatibilidade"),
                    rs.getInt("quantidade"), rs.getInt("estoque_minimo") });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Nao foi possivel listar produtos: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer idSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela.", "Selecao necessaria", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return Integer.parseInt(tabela.getValueAt(tabela.convertRowIndexToModel(linha), 0).toString());
    }

    private void editarSelecionado() {
        Integer id = idSelecionado();
        if (id == null) return;
        int linha = tabela.convertRowIndexToModel(tabela.getSelectedRow());
        Object[] dados = new Object[9];
        for (int i=0; i<9; i++) dados[i] = modelo.getValueAt(linha, i);
        abrirFormulario(dados);
    }

    private void abrirFormulario(Object[] dados) {
        boolean edicao = dados != null;
        JTextField codigo = campo(edicao ? dados[1] : "");
        JTextField nome = campo(edicao ? dados[2] : "");
        JTextField marca = campo(edicao ? dados[3] : "");
        JTextField modeloCampo = campo(edicao ? dados[4] : "");
        JTextField ano = campo(edicao ? dados[5] : String.valueOf(java.time.Year.now().getValue()));
        JTextField compatibilidade = campo(edicao ? dados[6] : "");
        JTextField minimo = campo(edicao ? dados[8] : "5");
        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 8));
        formulario.setPreferredSize(new Dimension(460, 300));
        adicionarCampo(formulario, "Codigo/SKU *", codigo);
        adicionarCampo(formulario, "Nome da peca *", nome);
        adicionarCampo(formulario, "Marca *", marca);
        adicionarCampo(formulario, "Modelo da peca *", modeloCampo);
        adicionarCampo(formulario, "Ano de fabricacao *", ano);
        adicionarCampo(formulario, "Compatibilidade do veiculo *", compatibilidade);
        adicionarCampo(formulario, "Estoque minimo *", minimo);
        formulario.add(new JLabel("Saldo atual"));
        formulario.add(new JLabel(edicao ? String.valueOf(dados[7]) + " unidade(s) (alterado em Gestao de Estoque)" : "0 unidade(s) (lancado por movimentacao)"));
        int opcao = JOptionPane.showConfirmDialog(this, formulario, edicao ? "Editar produto" : "Cadastrar produto",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) return;

        String cod = codigo.getText().trim(), nom = nome.getText().trim(), mar = marca.getText().trim();
        String mod = modeloCampo.getText().trim(), comp = compatibilidade.getText().trim();
        int anoNum, minimoNum;
        if (cod.isEmpty() || nom.isEmpty() || mar.isEmpty() || mod.isEmpty() || comp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos obrigatorios marcados com *.", "Validacao", JOptionPane.WARNING_MESSAGE); return;
        }
        try { anoNum = Integer.parseInt(ano.getText().trim()); minimoNum = Integer.parseInt(minimo.getText().trim()); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Ano e estoque minimo devem ser numeros inteiros.", "Validacao", JOptionPane.WARNING_MESSAGE); return; }
        if (anoNum < 1886 || anoNum > 2100) { JOptionPane.showMessageDialog(this, "Informe um ano entre 1886 e 2100.", "Validacao", JOptionPane.WARNING_MESSAGE); return; }
        if (minimoNum < 0) { JOptionPane.showMessageDialog(this, "O estoque minimo nao pode ser negativo.", "Validacao", JOptionPane.WARNING_MESSAGE); return; }

        String sql = edicao ? "UPDATE produtos SET codigo=?,nome=?,marca=?,modelo=?,ano_fabricacao=?,compatibilidade=?,estoque_minimo=? WHERE id=? AND ativo=1"
                           : "INSERT INTO produtos (codigo,nome,marca,modelo,ano_fabricacao,compatibilidade,quantidade,estoque_minimo,ativo) VALUES (?,?,?,?,?,?,0,?,1)";
        try (Connection con = Util.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cod); ps.setString(2, nom); ps.setString(3, mar); ps.setString(4, mod);
            ps.setInt(5, anoNum); ps.setString(6, comp); ps.setInt(7, minimoNum);
            if (edicao) ps.setInt(8, Integer.parseInt(dados[0].toString()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, edicao ? "Produto atualizado com sucesso." : "Produto cadastrado com sucesso.", "Concluido", JOptionPane.INFORMATION_MESSAGE);
            carregarProdutos();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, Util.mensagemBanco(ex), "Erro ao salvar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField campo(Object valor) {
        JTextField f = new JTextField(valor == null ? "" : valor.toString(), 20);
        return f;
    }
    private void adicionarCampo(JPanel p, String rotulo, JTextField campo) {
        p.add(new JLabel(rotulo)); p.add(campo);
    }

    private void excluirSelecionado() {
        Integer id = idSelecionado();
        if (id == null) return;
        int resposta = JOptionPane.showConfirmDialog(this,
            "Deseja realmente excluir/desativar este produto? O historico de movimentacoes sera preservado.",
            "Confirmar exclusao", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta != JOptionPane.YES_OPTION) return;
        try (Connection con = Util.conectar(); PreparedStatement ps = con.prepareStatement("UPDATE produtos SET ativo=0 WHERE id=? AND ativo=1")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() > 0) JOptionPane.showMessageDialog(this, "Produto excluido da listagem ativa. Historico preservado.", "Concluido", JOptionPane.INFORMATION_MESSAGE);
            carregarProdutos();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, Util.mensagemBanco(ex), "Erro ao excluir", JOptionPane.ERROR_MESSAGE);
        }
    }
}
