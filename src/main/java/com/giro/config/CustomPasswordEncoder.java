package com.giro.config;

import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence plainTextPassword) {
        return CryptPassword.encrypt(plainTextPassword.toString(), "123456789");
    }

    @Override
    public boolean matches(CharSequence plainTextPassword, String passwordInDatabase) {
        String encrypted = this.encode(plainTextPassword.toString());
        return encrypted.equals(passwordInDatabase);
    }

}
