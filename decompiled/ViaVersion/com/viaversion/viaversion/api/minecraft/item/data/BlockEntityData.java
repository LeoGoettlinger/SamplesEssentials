package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record BlockEntityData(int type, CompoundTag tag) implements Rewritable {
   public static final Type<BlockEntityData> TYPE = new Type<BlockEntityData>(BlockEntityData.class) {
      public BlockEntityData read(ByteBuf buffer) {
         int type = Types.VAR_INT.readPrimitive(buffer);
         CompoundTag tag = Types.COMPOUND_TAG.read(buffer);
         return new BlockEntityData(type, tag);
      }

      public void write(ByteBuf buffer, BlockEntityData value) {
         Types.VAR_INT.writePrimitive(buffer, value.type);
         Types.COMPOUND_TAG.write(buffer, value.tag);
      }

      public void write(Ops ops, BlockEntityData data) {
         ops.writeMap(
            map -> map.write("id", Types.IDENTIFIER, ops.context().registryAccess().blockEntity(data.type)).writeInlinedMap(Types.COMPOUND_TAG, data.tag)
         );
      }
   };

   public BlockEntityData rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      int mappedType = protocol.getMappingData().getBlockEntityMappings().getNewId(this.type);
      return new BlockEntityData(mappedType != -1 ? mappedType : 0, this.tag);
   }
}
