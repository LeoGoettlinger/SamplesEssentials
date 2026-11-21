package com.github.retrooper.packetevents.protocol.chat.clickevent;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Obsolete;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Obsolete
public class TwitchUserInfoClickEvent implements ClickEvent {
   private final String value;

   public TwitchUserInfoClickEvent(String value) {
      this.value = value;
   }

   public static TwitchUserInfoClickEvent decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      String value = compound.getStringTagValueOrThrow("value");
      return new TwitchUserInfoClickEvent(value);
   }

   public static void encode(NBTCompound compound, PacketWrapper<?> wrapper, TwitchUserInfoClickEvent clickEvent) {
      compound.setTag("value", new NBTString(clickEvent.value));
   }

   @Override
   public ClickEventAction<?> getAction() {
      return ClickEventActions.TWITCH_USER_INFO;
   }

   @Override
   public net.kyori.adventure.text.event.ClickEvent asAdventure() {
      throw new UnsupportedOperationException();
   }

   public String getValue() {
      return this.value;
   }
}
