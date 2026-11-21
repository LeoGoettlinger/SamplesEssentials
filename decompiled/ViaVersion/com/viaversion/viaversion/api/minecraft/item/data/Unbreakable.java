package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public record Unbreakable(boolean showInTooltip) {
   public static final Type<Unbreakable> TYPE = new Type<Unbreakable>(Unbreakable.class) {
      public Unbreakable read(ByteBuf buffer) {
         return new Unbreakable(buffer.readBoolean());
      }

      public void write(ByteBuf buffer, Unbreakable value) {
         buffer.writeBoolean(value.showInTooltip());
      }
   };
}
