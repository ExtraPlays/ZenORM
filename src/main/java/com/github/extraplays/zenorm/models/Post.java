package com.github.extraplays.zenorm.models;

import com.github.extraplays.zenorm.annotations.Column;
import com.github.extraplays.zenorm.annotations.ManyToOne;
import com.github.extraplays.zenorm.annotations.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "posts")
public class Post {

    @Column(name = "id", type = "INTEGER", primaryKey = true, autoIncrement = true)
    private int id;

    @Column(name = "content", type = "TEXT")
    private String content;

    @ManyToOne
    @Column(name = "user_id", type = "INTEGER", nullable = false)
    private int userId;

    public Post(String content, int userId) {
        this.content = content;
        this.userId = userId;
    }
}
