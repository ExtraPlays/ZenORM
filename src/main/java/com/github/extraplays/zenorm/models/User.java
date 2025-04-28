package com.github.extraplays.zenorm.models;

import com.github.extraplays.zenorm.annotations.Column;
import com.github.extraplays.zenorm.annotations.Embedded;
import com.github.extraplays.zenorm.annotations.OneToMany;
import com.github.extraplays.zenorm.annotations.Table;
import lombok.Data;
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

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "bio", type = "VARCHAR(255)", nullable = true)
    private String bio;

    @Column(name = "age", type = "INTEGER", nullable = true)
    private Integer age;

    @OneToMany(targetEntity = Post.class, mappedBy = "userId")
    private List<Post> posts;

    public User(String uuid, String name, String email, String bio, int age) {
        this.email = email;
        this.identifier = new Identifier(uuid, name);
        this.bio = bio;
        this.age = age;
    }

}
