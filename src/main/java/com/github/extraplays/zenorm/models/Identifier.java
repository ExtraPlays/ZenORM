package com.github.extraplays.zenorm.models;

import com.github.extraplays.zenorm.annotations.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Identifier {
    @Column(name = "uuid", type = "VARCHAR(36)", unique = true, nullable = false)
    private String uuid;
    @Column(name = "name", type = "VARCHAR(255)", nullable = false)
    private String name;
}
