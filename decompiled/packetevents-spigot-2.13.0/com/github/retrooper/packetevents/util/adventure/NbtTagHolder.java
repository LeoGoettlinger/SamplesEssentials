package com.github.retrooper.packetevents.util.adventure;

import com.github.retrooper.packetevents.protocol.nbt.NBT;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.util.Codec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class NbtTagHolder implements BinaryTagHolder {
   private final NBT tag;

   public NbtTagHolder(NBT tag) {
      this.tag = tag;
   }

   @Override
   public String string() {
      return AdventureNbtUtil.toString(this.tag);
   }

   @Override
   public <T, DX extends Exception> T get(Codec<T, String, DX, ?> codec) throws DX {
      return codec.decode(this.string());
   }

   public NBT getTag() {
      return this.tag;
   }
}
