package com.github.extraplays.ZenORM;

import com.github.extraplays.ZenORM.database.annotations.*;
import com.github.extraplays.ZenORM.database.enums.ColumnType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Data
@Getter
@Setter
@Table(name = "player_teste")
public class PlayerData {

    @Id
    private int id;

    @Varchar(size = 36)
    private String uuid;

    @Column(type = ColumnType.BOOLEAN)
    @Default(value = "false")
    private boolean isBanned;

    @Column(type = ColumnType.TEXT)
    @Default(value = "cuelho")
    private String teste;

    public PlayerData() {}

    public PlayerData(String uuid, boolean isBanned) {
        this.uuid = uuid;
        this.isBanned = isBanned;
    }


}
