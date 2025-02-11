package com.github.extraplays.ZenORM;

import com.github.extraplays.ZenORM.database.DatabaseManager;
import com.github.extraplays.ZenORM.database.examples.PlayerData;
import com.github.extraplays.ZenORM.database.impl.async.AsyncProcessor;

public class Main {

    public static void main(String[] args) {

        DatabaseManager databaseManager = new DatabaseManager("localhost", "minecraft", "root", "", 3306);

        AsyncProcessor<PlayerData> dao = new AsyncProcessor<>(PlayerData.class, databaseManager.getDataSource());

    }

}
