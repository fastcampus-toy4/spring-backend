package com.toy4.jeommechu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private Integer age;
    private String gender;
    // + getters/setters
}
