package com.viaversion.viaversion.api.minecraft;

import java.util.UUID;

public record PlayerMessageSignature(UUID uuid, byte[] signatureBytes) {
}
