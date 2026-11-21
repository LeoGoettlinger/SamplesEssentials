package com.artillexstudios.axtrade.libs.axapi.dependency;

public record DependencyHolder<T>(T dependency, boolean isDefault) {
}
