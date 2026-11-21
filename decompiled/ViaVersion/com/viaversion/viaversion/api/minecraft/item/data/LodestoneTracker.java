package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.GlobalBlockPosition;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record LodestoneTracker(@Nullable GlobalBlockPosition position, boolean tracked) {
   public static final Type<LodestoneTracker> TYPE = new Type<LodestoneTracker>(LodestoneTracker.class) {
      public LodestoneTracker read(ByteBuf buffer) {
         GlobalBlockPosition position = Types.OPTIONAL_GLOBAL_POSITION.read(buffer);
         boolean tracked = buffer.readBoolean();
         return new LodestoneTracker(position, tracked);
      }

      public void write(ByteBuf buffer, LodestoneTracker value) {
         Types.OPTIONAL_GLOBAL_POSITION.write(buffer, value.position);
         buffer.writeBoolean(value.tracked);
      }

      public void write(Ops ops, LodestoneTracker value) {
         ops.writeMap(map -> map.writeOptional("target", Types.GLOBAL_POSITION, value.position).writeOptional("tracked", Types.BOOLEAN, value.tracked, true));
      }
   };
}
