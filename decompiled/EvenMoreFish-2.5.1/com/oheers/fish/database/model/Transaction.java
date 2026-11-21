package com.oheers.fish.database.model;

import java.sql.Timestamp;

public record Transaction(String id, int userId, Timestamp timestamp) {
}
