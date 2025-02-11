package com.github.extraplays.ZenORM.database.examples;

import com.github.extraplays.ZenORM.database.annotations.*;
import com.github.extraplays.ZenORM.database.enums.ColumnType;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;


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