package com.viaversion.viaversion.api.protocol.remapper;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.exception.InformativeException;

@FunctionalInterface
public interface ValueWriter<T> {
   void write(PacketWrapper var1, T var2) throws InformativeException;
}
