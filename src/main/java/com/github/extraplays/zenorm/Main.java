package com.github.extraplays.zenorm;

import com.github.extraplays.zenorm.entity.TableGenerator;
import com.github.extraplays.zenorm.models.Post;
import com.github.extraplays.zenorm.models.User;
import com.github.extraplays.zenorm.providers.MySQLProvider;
import com.github.extraplays.zenorm.repository.AsyncRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {

        MySQLProvider provider = new MySQLProvider("localhost", 3306, "minecraft", "root", "root");

        OrmManager orm = new OrmManager(provider);
        TableGenerator.migrateTable(User.class, provider, provider.getDialect());
        TableGenerator.migrateTable(Post.class, provider, provider.getDialect());

        AsyncRepository<User> userRepository = orm.getRepository(User.class);
        AsyncRepository<Post> postRepository = orm.getRepository(Post.class);

        List<User> users = orm.query(User.class)
            .where("age", ">", "18")
            .findAll();

        if (users.isEmpty()) {
            System.out.println("Nenhum usuário encontrado.");
        } else {
            users.forEach(user -> {
                System.out.println(user.toString() + " \n");
            });
        }

        orm.shutdown();

    }

}