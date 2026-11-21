package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.misc.HolderSetType;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record ToolRule(HolderSet blocks, @Nullable Float speed, @Nullable Boolean correctForDrops) implements Rewritable {
   public static final Type<ToolRule> TYPE = new Type<ToolRule>(ToolRule.class) {
      public ToolRule read(ByteBuf buffer) {
         HolderSet blocks = Types.HOLDER_SET.read(buffer);
         Float speed = Types.OPTIONAL_FLOAT.read(buffer);
         Boolean correctForDrops = Types.OPTIONAL_BOOLEAN.read(buffer);
         return new ToolRule(blocks, speed, correctForDrops);
      }

      public void write(ByteBuf buffer, ToolRule value) {
         Types.HOLDER_SET.write(buffer, value.blocks);
         Types.OPTIONAL_FLOAT.write(buffer, value.speed);
         Types.OPTIONAL_BOOLEAN.write(buffer, value.correctForDrops);
      }

      public void write(Ops ops, ToolRule value) {
         ops.writeMap(
            map -> map.write("blocks", new HolderSetType(MappingData.MappingType.BLOCK), value.blocks)
               .writeOptional("speed", Types.FLOAT, value.speed)
               .writeOptional("correct_for_drops", Types.BOOLEAN, value.correctForDrops)
         );
      }
   };
   public static final Type<ToolRule[]> ARRAY_TYPE = new ArrayType<>(TYPE);

   public ToolRule rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      return this.blocks.hasIds()
         ? new ToolRule(this.blocks.rewrite(Rewritable.blockRewriteFunction(protocol, clientbound)), this.speed, this.correctForDrops)
         : this;
   }
}
