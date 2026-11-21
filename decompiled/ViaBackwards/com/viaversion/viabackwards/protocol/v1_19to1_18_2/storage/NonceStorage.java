package com.viaversion.viabackwards.protocol.v1_19to1_18_2.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import org.checkerframework.checker.nullness.qual.Nullable;

public record NonceStorage(@Nullable byte[] nonce) implements StorableObject {
}
