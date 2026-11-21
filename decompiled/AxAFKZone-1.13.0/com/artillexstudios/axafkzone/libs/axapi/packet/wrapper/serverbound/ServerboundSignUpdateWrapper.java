package com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.serverbound;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketType;
import com.artillexstudios.axafkzone.libs.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.packet.data.SignTextSlot;
import com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axafkzone.libs.axapi.utils.Version;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.position.BlockPosition;
import java.util.Arrays;

public final class ServerboundSignUpdateWrapper extends PacketWrapper {
   private BlockPosition position;
   private boolean frontText;
   private String[] lines;

   public ServerboundSignUpdateWrapper(PacketEvent event) {
      super(event);
   }

   public BlockPosition position() {
      return this.position;
   }

   public void position(BlockPosition position) {
      this.position = position.immutable();
   }

   public boolean frontText() {
      return this.frontText;
   }

   public void frontText(boolean frontText) {
      this.frontText = frontText;
   }

   public String[] lines() {
      return this.lines;
   }

   public void lines(String[] lines) {
      this.lines = lines;
   }

   @Override
   public void write(FriendlyByteBuf out) {
      out.writeBlockPos(this.position);
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_1) && Version.getServerVersion().isOlderThan(Version.v26_3)) {
         out.writeBoolean(this.frontText);
      }

      for (int i = 0; i < 4; i++) {
         out.writeUTF(this.lines[i]);
      }

      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_3)) {
         out.writeEnum(SignTextSlot.get(this.frontText));
      }
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.position = buf.readBlockPosition();
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v1_20_1) && Version.getServerVersion().isOlderThan(Version.v26_3)) {
         this.frontText = buf.readBoolean();
      } else {
         this.frontText = true;
      }

      this.lines = new String[4];

      for (int i = 0; i < 4; i++) {
         this.lines[i] = buf.readUTF(384);
      }

      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_3)) {
         SignTextSlot signTextSlot = buf.readEnum(SignTextSlot.class);
         this.frontText = signTextSlot == SignTextSlot.FRONT;
      }

      if (FeatureFlags.DEBUG.get()) {
         LogUtils.debug("Read lines from sign: {}", Arrays.toString((Object[])this.lines));
      }
   }

   @Override
   public PacketType packetType() {
      return ServerboundPacketTypes.SIGN_UPDATE;
   }
}
