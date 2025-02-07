# ZenORM

ZenORM é um ORM (Object-Relational Mapping) para MySQL, desenvolvido em Java, utilizando HikariCP para gerenciamento de conexões, annotations para mapeamento de entidades e uma interface simples e intuitiva para manipulação de dados.

## Características

- **Gerenciamento de Conexão** com HikariCP
- **Annotations para Mapeamento de Tabelas** (@Table, @Column, @Id, etc.)
- **Suporte a Tipos de Dados** como INT, BIGINT, VARCHAR, BOOLEAN, TIMESTAMP, etc.
- **Valores Padrão com `@Default`** para colunas automáticas
- **CRUD Completo** (Create, Read, Update, Delete)
- **Sistema Modular** e de fácil extensão

## Instalação

Adicione a dependência do **MySQL Connector** no `build.gradle`:

```gradle
dependencies {
    implementation 'mysql:mysql-connector-java:8.0.33'
}
```

Ou no `pom.xml` (Maven):

```xml
<dependencies>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
</dependencies>
```

## Configuração do Banco de Dados

Crie uma instância do **DatabaseManager** para gerenciar a conexão com o MySQL:

```java
DatabaseManager dbManager = new DatabaseManager("localhost", "database_name", "root", "password", 3306);
```

## Exemplo de Uso

### Definição de uma Entidade

```java
@Table(name = "player_teste")
public class PlayerData {

    @Id
    private int id;

    @Varchar(size = 36)
    private String uuid;

    @Column(type = ColumnType.BOOLEAN)
    @Default(value = "false")
    private boolean isBanned;

    @Column(type = ColumnType.TIMESTAMP)
    @Default(value = "CURRENT_TIMESTAMP")
    private Timestamp created_at;

    @Column(type = ColumnType.TIMESTAMP)
    @Default(value = "CURRENT_TIMESTAMP")
    private Timestamp updated_at;

    public PlayerData() {}

    public PlayerData(String uuid, boolean isBanned) {
        this.uuid = uuid;
        this.isBanned = isBanned;
        this.created_at = new Timestamp(System.currentTimeMillis());
        this.updated_at = new Timestamp(System.currentTimeMillis());
    }
}
```

### Criando a Tabela no Banco de Dados

```java
TableGenerator.createTable(PlayerData.class);
```

### Utilizando o ORM para Operações no Banco de Dados

```java
Processor<PlayerData> dao = new Processor<>(PlayerData.class);

// Inserindo um novo jogador
PlayerData player = new PlayerData("123e4567-e89b-12d3-a456-426614174000", false);
dao.insert(player);

// Buscando um jogador
Optional<PlayerData> foundPlayer = dao.findWhere("uuid", "123e4567-e89b-12d3-a456-426614174000");
foundPlayer.ifPresent(p -> System.out.println("Player encontrado: " + p.getUuid()));

// Atualizando dados do jogador
player.setIsBanned(true);
dao.update(player);

// Removendo um jogador
dao.delete(player);
```

## Melhorias Futuras

- **Suporte a Relacionamentos** (OneToMany, ManyToOne)
- **Suporte a Outras Bases de Dados** (PostgreSQL, SQLite)
- **Sistema de Cache e Otimização de Queries**

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

