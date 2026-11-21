package com.github.retrooper.packetevents.protocol.chat.clickevent;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CopyToClipboardClickEvent implements ClickEvent {
   private final String value;

   public CopyToClipboardClickEvent(String value) {
      this.value = value;
   }

   public static CopyToClipboardClickEvent decode(NBTCompound compound, PacketWrapper<?> wrapper) {
      String value = compound.getStringTagValueOrThrow("value");
      return new CopyToClipboardClickEvent(value);
   }

   public static void encode(NBTCompound compound, PacketWrapper<?> wrapper, CopyToClipboardClickEvent clickEvent) {
      compound.setTag("value", new NBTString(clickEvent.value));
   }

   @Override
   public ClickEventAction<?> getAction() {
      return ClickEventActions.COPY_TO_CLIPBOARD;
   }

   @Override
   public net.kyori.adventure.text.event.ClickEvent asAdventure() {
      return net.kyori.adventure.text.event.ClickEvent.copyToClipboard(this.value);
   }

   public String getValue() {
      return this.value;
   }
}
