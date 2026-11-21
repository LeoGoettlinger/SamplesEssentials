package com.github.retrooper.packetevents.protocol.util;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface NbtMapDecoder<T> {
   T decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException;
}
