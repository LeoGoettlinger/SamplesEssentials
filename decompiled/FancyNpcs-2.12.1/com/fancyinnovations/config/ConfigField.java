package com.fancyinnovations.config;

public record ConfigField<T>(String path, String description, boolean forRemoval, T defaultValue, boolean forceDefault, Class<T> type) {
}
