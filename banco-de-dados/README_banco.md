# Resumo do banco de dados

- `usuarios`: identidade de login, hash da senha, status ativo e data de criacao.
- `produtos`: codigo unico, caracteristicas da peca, saldo, estoque minimo e status ativo.
- `movimentacoes`: historico ligado a um produto e ao usuario responsavel, com tipo, quantidade, data/hora e observacao.

Relacionamentos:
- `usuarios (1) -> (N) movimentacoes`
- `produtos (1) -> (N) movimentacoes`

A exclusao do produto e logica (`ativo = 0`) para preservar o registro das movimentacoes anteriores.
