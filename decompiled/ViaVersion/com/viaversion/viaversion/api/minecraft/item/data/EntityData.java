package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record EntityData(int type, CompoundTag tag) implements Rewritable, Copyable {
   public static final Type<EntityData> TYPE = new Type<EntityData>(EntityData.class) {
      public EntityData read(ByteBuf buffer) {
         int type = Types.VAR_INT.readPrimitive(buffer);
         CompoundTag tag = Types.COMPOUND_TAG.read(buffer);
         return new EntityData(type, tag);
      }

      public void write(ByteBuf buffer, EntityData value) {
         Types.VAR_INT.writePrimitive(buffer, value.type);
         Types.COMPOUND_TAG.write(buffer, value.tag);
      }

      public void write(Ops ops, EntityData data) {
         ops.writeMap(map -> map.write("id", Types.IDENTIFIER, ops.context().registryAccess().entity(data.type)).writeInlinedMap(Types.COMPOUND_TAG, data.tag));
      }
   };

   public EntityData rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      int mappedType = Rewritable.entityRewriteFunction(protocol, clientbound).applyAsInt(this.type);
      return new EntityData(mappedType, this.tag);
   }

   public EntityData copy() {
      return new EntityData(this.type, this.tag.copy());
   }
}
