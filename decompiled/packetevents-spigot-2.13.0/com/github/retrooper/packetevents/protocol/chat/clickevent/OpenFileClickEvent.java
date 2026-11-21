package com.github.retrooper.packetevents.protocol.chat.clickevent;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenFileClickEvent implements ClickEvent {
   private final String path;

   public OpenFileClickEvent(String path) {
      this.path = path;
   }

   public static OpenFileClickEvent decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      boolean v1215 = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5);
      String path = compound.getStringTagValueOrThrow(v1215 ? "path" : "value");
      return new OpenFileClickEvent(path);
   }

   public static void encode(NBTCompound compound, PacketWrapper<?> wrapper, OpenFileClickEvent clickEvent) {
      boolean v1215 = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5);
      compound.setTag(v1215 ? "path" : "value", new NBTString(clickEvent.path));
   }

   @Override
   public ClickEventAction<?> getAction() {
      return ClickEventActions.OPEN_FILE;
   }

   @Override
   public net.kyori.adventure.text.event.ClickEvent asAdventure() {
      return net.kyori.adventure.text.event.ClickEvent.openFile(this.path);
   }

   public String getPath() {
      return this.path;
   }
}
