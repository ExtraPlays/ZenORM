package com.github.extraplays.zenorm.models;

import com.github.extraplays.zenorm.annotations.Column;
import com.github.extraplays.zenorm.annotations.Embedded;
import com.github.extraplays.zenorm.annotations.OneToMany;
import com.github.extraplays.zenorm.annotations.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Column(name = "id", type = "INTEGER", primaryKey = true, autoIncrement = true)
    private int id;

    @Embedded
    private Identifier identifier;

    @Column(name = "username", type = "VARCHAR(255)", unique = true, nullable = false)
    private String username;

    @Column(name = "email", type = "VARCHAR(255)", unique = true, nullable = false)
    private String email;

    @OneToMany(targetEntity = Post.class, mappedBy = "userId")
    private List<Post> posts;

    public User(String uuid, String name, String username, String email) {
        this.username = username;
        this.email = email;
        this.identifier = new Identifier(uuid, name);
    }

}
