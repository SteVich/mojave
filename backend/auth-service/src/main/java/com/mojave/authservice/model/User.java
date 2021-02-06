package com.mojave.authservice.model;

import com.mojave.authservice.model.vocabulary.Role;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    Long id;
    String username;
    String email;
    String password;
    Role role;
    boolean active;
}
