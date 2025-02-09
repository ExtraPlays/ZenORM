# ZenORM

ZenORM é um ORM (Object-Relational Mapping) para MySQL, desenvolvido em Java, utilizando HikariCP para gerenciamento de conexões, annotations para mapeamento de entidades e uma interface simples e intuitiva para manipulação de dados.

## Características

- **Gerenciamento de Conexão** com HikariCP
- **Annotations para Mapeamento de Tabelas** (@Table, @Column, @Id, etc.)
- **Suporte a Tipos de Dados** como INT, BIGINT, VARCHAR, BOOLEAN, TIMESTAMP, etc.
- **CRUD Completo** (Create, Read, Update, Delete)
- **Sistema Modular** e de fácil extensão

## Exemplo de Uso

### Definição de uma Entidade

```java

@Data
@Getter
@Setter
@NoArgsConstructor
@Table(name = "player_data")
public class PlayerData {

    @Column(name = "id", type = ColumnType.INT, primary = true, autoIncrement = true)
    private int id;

    @Column(name = "uuid", type = ColumnType.VARCHAR, length = 36, unique = true)
    private String uuid;

    @Column(name = "created_at", type = ColumnType.TIMESTAMP, defaultValue = "CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "last_login", type = ColumnType.TIMESTAMP, defaultValue = "CURRENT_TIMESTAMP")
    private Timestamp lastLogin;

    public PlayerData(String uuid, Timestamp createdAt, Timestamp lastLogin) {
        this.uuid = uuid;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }
}
```

### Utilizando o ORM para Operações no Banco de Dados

```java
Processor<PlayerData> dao = new Processor<>(PlayerData.class);

// Inserindo um novo jogador
PlayerData player = new PlayerData("123e4567-e89b-12d3-a456-426614174000", null, null);
dao.insert(player);

// Buscando um jogador
Optional<PlayerData> foundPlayer = dao.find("uuid", "123e4567-e89b-12d3-a456-426614174000");
foundPlayer.ifPresent(p -> System.out.println("Player encontrado: " + p.getUuid()));

// Atualizando dados do jogador
player.setLastLogin(new Timestamp(System.currentTimeMillis()));
dao.save(player);

// Removendo um jogador
dao.delete(player);
```

## Melhorias Futuras

- **Suporte a Relacionamentos** (OneToMany, ManyToOne)
- **Suporte a Outras Bases de Dados** (PostgreSQL, SQLite)
- **Sistema de Cache e Otimização de Queries**

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

