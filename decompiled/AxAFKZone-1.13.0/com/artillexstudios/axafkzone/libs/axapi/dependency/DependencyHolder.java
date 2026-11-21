package com.artillexstudios.axafkzone.libs.axapi.dependency;

public record DependencyHolder<T>(T dependency, boolean isDefault) {
}
