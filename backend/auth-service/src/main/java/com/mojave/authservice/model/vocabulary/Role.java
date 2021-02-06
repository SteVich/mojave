package com.mojave.authservice.model.vocabulary;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum Role {

    NOT_DEFINED("Not defined"),
    PM("Project manager"),
    DEV("Developer"),
    QA("Quality assurance");

    String value;
}
