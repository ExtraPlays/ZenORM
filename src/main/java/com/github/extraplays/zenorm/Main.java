package com.github.extraplays.zenorm;

import com.github.extraplays.zenorm.entity.TableGenerator;
import com.github.extraplays.zenorm.models.Post;
import com.github.extraplays.zenorm.models.User;
import com.github.extraplays.zenorm.providers.MySQLProvider;
import com.github.extraplays.zenorm.repository.AsyncRepository;

import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

        MySQLProvider provider = new MySQLProvider("localhost", 3306, "teste", "root", "root");
        OrmManager orm = new OrmManager(provider);

        TableGenerator.migrateTable(User.class, provider, provider.getDialect());
        TableGenerator.migrateTable(Post.class, provider, provider.getDialect());

        AsyncRepository<User> userRepository = orm.getRepository(User.class);
        AsyncRepository<Post> postRepository = orm.getRepository(Post.class);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite o email do usuário:");
        String email = scanner.nextLine();

        Optional<User> user = orm.query(User.class).where("email", "=", email).findOne();

        if (user.isPresent()) {
            System.out.println("Usuário encontrado: " + user.get());
            orm.shutdown();
        } else {
            System.out.println("Criando novo usuário com email: " + email);

            User newUser = new User(UUID.randomUUID().toString(), "John Doe 2", email, "teste", 25);

            userRepository.saveAsync(newUser)
                .thenRun(() -> {
                    System.out.println("Usuario salvo com sucesso: " + email);
                    orm.shutdown();
                }).exceptionally(ex -> {
                    System.err.println("Erro ao salvar o usuário: " + ex.getMessage());
                    orm.shutdown();
                    return null;
                }).join();
        }

    }

}