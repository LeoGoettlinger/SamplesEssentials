package com.artillexstudios.axtrade.libs.axapi.packetentity.meta.serializer;

public record EntityDataAccessor<T>(int id, EntityDataSerializer<T> serializers) {
}
