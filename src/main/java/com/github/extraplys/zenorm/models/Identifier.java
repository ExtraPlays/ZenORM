package com.github.extraplys.zenorm.models;

import com.github.extraplys.zenorm.annotations.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Identifier {
    @Column(name = "uuid", type = "VARCHAR(36)", unique = true, nullable = false)
    private String uuid;
    @Column(name = "name", type = "VARCHAR(255)", nullable = false)
    private String name;
}
