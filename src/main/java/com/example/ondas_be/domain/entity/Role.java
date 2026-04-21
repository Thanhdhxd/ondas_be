package com.example.ondas_be.domain.entity;

public enum Role {

    USER,
    CONTENT_MANAGER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
