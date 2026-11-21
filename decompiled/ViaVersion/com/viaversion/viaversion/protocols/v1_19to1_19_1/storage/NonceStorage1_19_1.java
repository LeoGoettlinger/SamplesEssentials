package com.viaversion.viaversion.protocols.v1_19to1_19_1.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import org.checkerframework.checker.nullness.qual.Nullable;

public record NonceStorage1_19_1(@Nullable byte[] nonce) implements StorableObject {
}
