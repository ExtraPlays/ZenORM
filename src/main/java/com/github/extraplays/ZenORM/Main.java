package com.github.extraplays.ZenORM;

import com.github.extraplays.ZenORM.database.DatabaseManager;
import com.github.extraplays.ZenORM.database.examples.PlayerData;
import com.github.extraplays.ZenORM.database.impl.Processor;

import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        DatabaseManager databaseManager = new DatabaseManager("localhost", "minecraft", "root", "", 3306);

        Processor<PlayerData> playerDataDAO = new Processor<>(PlayerData.class, databaseManager.getDataSource());
        Optional<PlayerData> playerData = playerDataDAO.find("uuid", "123");

        playerData.ifPresent(data -> System.out.println(data.toString()));

    }

}
