# SAEP - Controle de Estoque de Autopecas

Projeto desktop em Java Swing preparado a partir do caderno de prova SAEP. Ele cobre login, painel principal, cadastro e pesquisa de produtos, entrada/saida de estoque, alertas de estoque minimo e historico com responsavel e data.

## 1. Requisitos

- JDK 21 (ou superior compativel com o codigo).
- MySQL Community Server 8.4 LTS.
- Apache Maven 3.9 ou superior.
- Windows 10/11, Linux ou macOS com interface grafica. A documentacao de infraestrutura considera Windows 11 64 bits.
- MySQL Workbench e uma IDE Java (IntelliJ IDEA, Eclipse, Apache NetBeans ou VS Code) sao recomendados.

## 2. Preparar o banco de dados

1. Inicie o servico do MySQL.
2. Abra `banco-de-dados/saep_db.sql` no MySQL Workbench.
3. Execute o script completo. Ele cria e popula o banco `saep_db`.
4. O script recria as tabelas demonstrativas existentes com o mesmo nome; use-o em um banco de testes, pois a execucao apaga os dados anteriores dessas tabelas.

## 3. Configurar conexao

Por padrao, a aplicacao tenta conectar a `jdbc:mysql://localhost:3306/saep_db` com usuario `root` e senha vazia. Ajuste seu ambiente de acordo com a instalacao do MySQL. Para nao editar o fonte, defina as variaveis de ambiente:

- `SAEP_DB_URL`: URL JDBC, por exemplo `jdbc:mysql://localhost:3306/saep_db?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true`
- `SAEP_DB_USER`: usuario do banco
- `SAEP_DB_PASSWORD`: senha do usuario do banco

Exemplo de PowerShell (troque pelos dados locais):

```powershell
$env:SAEP_DB_USER = "root"
$env:SAEP_DB_PASSWORD = "sua_senha"
$env:SAEP_DB_URL = "jdbc:mysql://localhost:3306/saep_db?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true"
```

## 4. Executar

Abra o terminal nesta pasta e execute:

```bash
mvn clean package
mvn exec:java -Dexec.mainClass="br.com.saep.App"
```

Se o plugin `exec` nao estiver disponivel, execute a classe `br.com.saep.App` pela IDE depois de importar o projeto como projeto Maven. A primeira compilacao precisa baixar o conector JDBC do Maven Central e requer internet.

## 5. Usuarios de demonstracao

| Usuario | Senha |
|---|---|
| `admin` | `admin123` |
| `maria` | `maria123` |
| `joao` | `joao123` |

Essas credenciais sao exclusivamente para demonstracao academica. Altere-as antes de qualquer uso real. O prototipo compara hash SHA-256 para evitar guardar a senha em texto puro, mas SHA-256 simples nao substitui um algoritmo de armazenamento de senhas como BCrypt ou Argon2id.

## 6. Regras importantes implementadas

- A quantidade do estoque nao pode ficar negativa; uma saida maior que o saldo e recusada.
- Toda movimentacao salva tipo, quantidade, data/hora, usuario responsavel e observacao opcional.
- Produto com quantidade menor que o minimo aparece em alerta no painel e na tela de estoque.
- A exclusao de produto e logica (desativacao) para preservar o historico. Produtos desativados saem das listas de cadastro e movimentacao, mas continuam no historico antigo.
- Para preservar a rastreabilidade, a tela de cadastro nao altera diretamente o saldo; use Gestao de Estoque.

## 7. Estrutura

- `src/main/java/br/com/saep/`: codigo-fonte Java.
- `banco-de-dados/saep_db.sql`: criacao e populacao do MySQL.
- `documentacao/`: requisitos funcionais, casos de teste, infraestrutura e DER.

## 8. Observacoes

Trata-se de um prototipo educacional. Antes de uso em producao, implemente politica de senha forte com BCrypt/Argon2id, permissao por perfil, logs de auditoria, backups, configuracao segura de credenciais, testes automatizados e monitoramento.
