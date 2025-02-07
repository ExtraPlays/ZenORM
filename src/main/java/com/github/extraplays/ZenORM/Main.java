package com.github.extraplays.ZenORM;

import com.github.extraplays.ZenORM.database.DatabaseManager;
import com.github.extraplays.ZenORM.database.impl.Processor;
import com.github.extraplays.ZenORM.database.impl.TableGenerator;

import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        DatabaseManager databaseManager = new DatabaseManager("localhost", "minecraft", "root", "", 3306);

        TableGenerator.createTable(PlayerData.class);
        Processor<PlayerData> playerDataDAO = new Processor<>(PlayerData.class);
        Optional<PlayerData> playerData = playerDataDAO.findWhere("uuid", "123");

        playerData.ifPresent(data -> System.out.println(data.toString()));

        databaseManager.close();

    }

}
