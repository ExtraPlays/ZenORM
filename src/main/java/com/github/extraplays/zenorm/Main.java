package com.github.extraplays.zenorm;

import com.github.extraplays.zenorm.entity.TableGenerator;
import com.github.extraplays.zenorm.models.Post;
import com.github.extraplays.zenorm.models.User;
import com.github.extraplays.zenorm.providers.MySQLProvider;
import com.github.extraplays.zenorm.repository.AsyncRepository;

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

        userRepository.findOneAsync("username = ?", "uchih4")
            .thenCompose(optionalUser -> {
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();

                    System.out.println("Usuário encontrado: " + user.getIdentifier().getName());
                    System.out.println("Quantidade de posts: " + (user.getPosts() != null ? user.getPosts().size() : 0));

                    if (user.getPosts() != null) {
                        for (Post post : user.getPosts()) {
                            System.out.println("Post ID: " + post.getId() + " | Conteúdo: " + post.getContent());
                        }
                    }

                    return CompletableFuture.completedFuture(null);
                } else {
                    System.out.println("Usuário não encontrado.");

                    User newUser = new User(UUID.randomUUID().toString(), "uchih4", "uchih4", "uchih4@gmail.com");
                    return userRepository.saveAsync(newUser);
                }
            })
            .thenRun(orm::shutdown)
            .exceptionally(ex -> {
                ex.printStackTrace();
                orm.shutdown();
                return null;
            })
            .join();
    }

}