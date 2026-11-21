package com.viaversion.viaversion.api.minecraft;

public record ProfileKey(long expiresAt, byte[] publicKey, byte[] keySignature) {
}
