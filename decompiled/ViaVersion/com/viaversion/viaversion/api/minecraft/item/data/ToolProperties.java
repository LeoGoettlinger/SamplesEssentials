package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record ToolProperties(ToolRule[] rules, float defaultMiningSpeed, int damagePerBlock, boolean canDestroyBlocksInCreative) implements Rewritable {
   public static final Type<ToolProperties> TYPE1_20_5 = new Type<ToolProperties>(ToolProperties.class) {
      public ToolProperties read(ByteBuf buffer) {
         ToolRule[] rules = ToolRule.ARRAY_TYPE.read(buffer);
         float defaultMiningSpeed = buffer.readFloat();
         int damagePerBlock = Types.VAR_INT.readPrimitive(buffer);
         return new ToolProperties(rules, defaultMiningSpeed, damagePerBlock, true);
      }

      public void write(ByteBuf buffer, ToolProperties value) {
         ToolRule.ARRAY_TYPE.write(buffer, value.rules());
         buffer.writeFloat(value.defaultMiningSpeed());
         Types.VAR_INT.writePrimitive(buffer, value.damagePerBlock());
      }
   };
   public static final Type<ToolProperties> TYPE1_21_5 = new Type<ToolProperties>(ToolProperties.class) {
      public ToolProperties read(ByteBuf buffer) {
         ToolRule[] rules = ToolRule.ARRAY_TYPE.read(buffer);
         float defaultMiningSpeed = buffer.readFloat();
         int damagePerBlock = Types.VAR_INT.readPrimitive(buffer);
         boolean canDestroyBlocksInCreative = buffer.readBoolean();
         return new ToolProperties(rules, defaultMiningSpeed, damagePerBlock, canDestroyBlocksInCreative);
      }

      public void write(ByteBuf buffer, ToolProperties value) {
         ToolRule.ARRAY_TYPE.write(buffer, value.rules());
         buffer.writeFloat(value.defaultMiningSpeed());
         Types.VAR_INT.writePrimitive(buffer, value.damagePerBlock());
         buffer.writeBoolean(value.canDestroyBlocksInCreative());
      }

      public void write(Ops ops, ToolProperties value) {
         ops.writeMap(
            map -> map.write("rules", ToolRule.ARRAY_TYPE, value.rules())
               .writeOptional("default_mining_speed", Types.FLOAT, value.defaultMiningSpeed(), 1.0F)
               .writeOptional("damage_per_block", Types.INT, value.damagePerBlock(), 1)
               .writeOptional("can_destroy_blocks_in_creative", Types.BOOLEAN, value.canDestroyBlocksInCreative(), true)
         );
      }
   };

   public ToolProperties(ToolRule[] rules, float defaultMiningSpeed, int damagePerBlock) {
      this(rules, defaultMiningSpeed, damagePerBlock, false);
   }

   public ToolProperties rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      ToolRule[] rules = new ToolRule[this.rules.length];

      for (int i = 0; i < rules.length; i++) {
         rules[i] = this.rules[i].rewrite(connection, protocol, clientbound);
      }

      return new ToolProperties(rules, this.defaultMiningSpeed, this.damagePerBlock, this.canDestroyBlocksInCreative);
   }
}
