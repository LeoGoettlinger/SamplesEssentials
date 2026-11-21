package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record UseCooldown(float seconds, @Nullable String cooldownGroup) implements Rewritable {
   public static final Type<UseCooldown> TYPE = new Type<UseCooldown>(UseCooldown.class) {
      public UseCooldown read(ByteBuf buffer) {
         float seconds = buffer.readFloat();
         String cooldownGroup = Types.OPTIONAL_STRING.read(buffer);
         return new UseCooldown(seconds, cooldownGroup);
      }

      public void write(ByteBuf buffer, UseCooldown value) {
         buffer.writeFloat(value.seconds());
         Types.OPTIONAL_STRING.write(buffer, value.cooldownGroup());
      }

      public void write(Ops ops, UseCooldown value) {
         Key cooldownGroup = value.cooldownGroup != null ? Key.of(value.cooldownGroup) : null;
         ops.writeMap(map -> map.write("seconds", Types.FLOAT, value.seconds()).writeOptional("cooldown_group", Types.IDENTIFIER, cooldownGroup));
      }
   };

   public UseCooldown rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      if (this.cooldownGroup == null) {
         return this;
      } else {
         String mappedCooldownGroup = Rewritable.rewriteItem(protocol, clientbound, this.cooldownGroup);
         return new UseCooldown(this.seconds, mappedCooldownGroup);
      }
   }
}
