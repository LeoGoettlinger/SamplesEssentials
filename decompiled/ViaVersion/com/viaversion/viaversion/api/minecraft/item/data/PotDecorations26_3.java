package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record PotDecorations26_3(@Nullable Item back, @Nullable Item left, @Nullable Item right, @Nullable Item front) implements Copyable, Rewritable {
   public PotDecorations26_3 rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      return new PotDecorations26_3(
         Rewritable.rewriteItem(connection, protocol, clientbound, this.back),
         Rewritable.rewriteItem(connection, protocol, clientbound, this.left),
         Rewritable.rewriteItem(connection, protocol, clientbound, this.right),
         Rewritable.rewriteItem(connection, protocol, clientbound, this.front)
      );
   }

   public PotDecorations26_3 copy() {
      return new PotDecorations26_3(
         this.back == null ? null : this.back.copy(),
         this.left == null ? null : this.left.copy(),
         this.right == null ? null : this.right.copy(),
         this.front == null ? null : this.front.copy()
      );
   }

   public static final class PotDecorationsType extends Type<PotDecorations26_3> {
      private final Type<Item> itemTemplateType;

      public PotDecorationsType(Type<Item> itemTemplateType) {
         super(PotDecorations26_3.class);
         this.itemTemplateType = itemTemplateType;
      }

      public void write(ByteBuf buffer, PotDecorations26_3 value) {
         this.itemTemplateType.write(buffer, value.back());
         this.itemTemplateType.write(buffer, value.left());
         this.itemTemplateType.write(buffer, value.right());
         this.itemTemplateType.write(buffer, value.front());
      }

      public PotDecorations26_3 read(ByteBuf buffer) {
         Item back = this.itemTemplateType.read(buffer);
         Item left = this.itemTemplateType.read(buffer);
         Item right = this.itemTemplateType.read(buffer);
         Item front = this.itemTemplateType.read(buffer);
         return new PotDecorations26_3(back, left, right, front);
      }

      public void write(Ops ops, PotDecorations26_3 value) {
         ops.writeMap(
            map -> map.writeOptional("back", this.itemTemplateType, value.back())
               .writeOptional("left", this.itemTemplateType, value.left())
               .writeOptional("right", this.itemTemplateType, value.right())
               .writeOptional("front", this.itemTemplateType, value.front())
         );
      }
   }
}
