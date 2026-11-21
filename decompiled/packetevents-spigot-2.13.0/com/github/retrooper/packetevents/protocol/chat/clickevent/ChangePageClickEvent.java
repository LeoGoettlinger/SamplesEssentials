package com.github.retrooper.packetevents.protocol.chat.clickevent;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ChangePageClickEvent implements ClickEvent {
   private final int page;

   @Obsolete
   public ChangePageClickEvent(String page) {
      this(Integer.parseInt(page));
   }

   public ChangePageClickEvent(int page) {
      this.page = page;
   }

   public static ChangePageClickEvent decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      int page = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5)
         ? compound.getNumberTagValueOrThrow("page").intValue()
         : Integer.parseInt(compound.getStringTagValueOrThrow("value"));
      return new ChangePageClickEvent(page);
   }

   public static void encode(NBTCompound compound, PacketWrapper<?> wrapper, ChangePageClickEvent clickEvent) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
         compound.setTag("page", new NBTInt(clickEvent.page));
      } else {
         compound.setTag("value", new NBTString(Integer.toString(clickEvent.page)));
      }
   }

   @Override
   public ClickEventAction<?> getAction() {
      return ClickEventActions.CHANGE_PAGE;
   }

   @Override
   public net.kyori.adventure.text.event.ClickEvent asAdventure() {
      return net.kyori.adventure.text.event.ClickEvent.changePage(this.page);
   }

   public int getPage() {
      return this.page;
   }
}
