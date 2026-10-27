package br.com.saep;

/** Dados do usuario autenticado durante a sessao. */
public final class UsuarioSessao {
    private final int id;
    private final String nome;
    private final String usuario;

    public UsuarioSessao(int id, String nome, String usuario) {
        this.id = id;
        this.nome = nome;
        this.usuario = usuario;
    }
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getUsuario() { return usuario; }
}
