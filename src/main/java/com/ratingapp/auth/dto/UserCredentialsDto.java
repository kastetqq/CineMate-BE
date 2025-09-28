package com.ratingapp.auth.dto;

import lombok.Data;

@Data
public class UserCredentialsDto {
    private String username;
    private String email;
    private String password;
}
