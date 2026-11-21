package com.viaversion.viaversion.protocol;

import com.viaversion.viaversion.api.protocol.ProtocolPathKey;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public record ProtocolPathKeyImpl(ProtocolVersion clientProtocolVersion, ProtocolVersion serverProtocolVersion) implements ProtocolPathKey {
}
