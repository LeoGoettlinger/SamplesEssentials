package com.github.retrooper.packetevents.protocol.world.attributes.timelines;

import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.util.NbtCodec;
import com.github.retrooper.packetevents.protocol.util.NbtCodecException;
import com.github.retrooper.packetevents.protocol.util.NbtCodecs;
import com.github.retrooper.packetevents.protocol.util.NbtMapCodec;
import com.github.retrooper.packetevents.util.Either;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TimeMarkerInfo {
   private static final NbtCodec<TimeMarkerInfo> FULL_CODEC = (new NbtMapCodec<TimeMarkerInfo>() {
      public TimeMarkerInfo decode(NBTCompound tag, PacketWrapper<?> wrapper) throws NbtCodecException {
         int ticks = tag.getNumberTagValueOrThrow("ticks").intValue();
         boolean showInCommands = tag.getBooleanOr("show_in_commands", false);
         return new TimeMarkerInfo(ticks, showInCommands);
      }

      public void encode(NBTCompound tag, PacketWrapper<?> wrapper, TimeMarkerInfo value) throws NbtCodecException {
         tag.setTag("ticks", new NBTInt(value.ticks));
         if (value.showInCommands) {
            tag.setTag("show_in_commands", new NBTByte(true));
         }
      }
   }).codec();
   public static final NbtCodec<TimeMarkerInfo> CODEC = NbtCodecs.either(NbtCodecs.INT, FULL_CODEC)
      .apply(
         either -> either.map(TimeMarkerInfo::new, Function.identity()), info -> info.showInCommands ? Either.createRight(info) : Either.createLeft(info.ticks)
      );
   private final int ticks;
   private final boolean showInCommands;

   public TimeMarkerInfo(int ticks) {
      this(ticks, false);
   }

   public TimeMarkerInfo(int ticks, boolean showInCommands) {
      this.ticks = ticks;
      this.showInCommands = showInCommands;
   }

   public int getTicks() {
      return this.ticks;
   }

   public boolean isShowInCommands() {
      return this.showInCommands;
   }
}
