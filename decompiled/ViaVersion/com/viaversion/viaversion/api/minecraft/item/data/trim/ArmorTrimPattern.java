package com.viaversion.viaversion.api.minecraft.item.data.trim;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record ArmorTrimPattern(String assetName, int itemId, Tag description, boolean decal) implements Copyable, Rewritable {
   public static final HolderType<ArmorTrimPattern> TYPE1_20_5 = new HolderType<ArmorTrimPattern>() {
      public ArmorTrimPattern readDirect(ByteBuf buffer) {
         String assetName = Types.STRING.read(buffer);
         int itemId = Types.VAR_INT.readPrimitive(buffer);
         Tag description = Types.TAG.read(buffer);
         boolean decal = buffer.readBoolean();
         return new ArmorTrimPattern(assetName, itemId, description, decal);
      }

      public void writeDirect(ByteBuf buffer, ArmorTrimPattern value) {
         Types.STRING.write(buffer, value.assetName());
         Types.VAR_INT.writePrimitive(buffer, value.itemId());
         Types.TAG.write(buffer, value.description());
         buffer.writeBoolean(value.decal());
      }
   };
   public static final HolderType<ArmorTrimPattern> TYPE1_21_5 = new HolderType<ArmorTrimPattern>() {
      public ArmorTrimPattern readDirect(ByteBuf buffer) {
         String assetName = Types.STRING.read(buffer);
         Tag description = Types.TAG.read(buffer);
         boolean decal = buffer.readBoolean();
         return new ArmorTrimPattern(assetName, description, decal);
      }

      public void writeDirect(ByteBuf buffer, ArmorTrimPattern value) {
         Types.STRING.write(buffer, value.assetName());
         Types.TAG.write(buffer, value.description());
         buffer.writeBoolean(value.decal());
      }

      public void writeDirect(Ops ops, ArmorTrimPattern object) {
         ops.writeMap(
            map -> map.write("asset_id", Types.IDENTIFIER, Key.of(object.assetName()))
               .write("description", Types.TEXT_COMPONENT_TAG, object.description())
               .write("decal", Types.BOOLEAN, object.decal())
         );
      }

      @Override
      protected Key identifier(Ops ops, int id) {
         return ops.context().registryAccess().registryKey("trim_pattern", id);
      }
   };

   public ArmorTrimPattern(String assetName, Tag description, boolean decal) {
      this(assetName, 0, description, decal);
   }

   public ArmorTrimPattern rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      return new ArmorTrimPattern(this.assetName, Rewritable.rewriteItem(protocol, clientbound, this.itemId), this.description, this.decal);
   }

   public ArmorTrimPattern copy() {
      return new ArmorTrimPattern(this.assetName, this.itemId, this.description.copy(), this.decal);
   }
}
