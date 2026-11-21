package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;

public class ItemLore {
   public static final ItemLore EMPTY = new ItemLore(Collections.emptyList());
   private List<Component> lines;

   public ItemLore(List<Component> lines) {
      this.lines = lines;
   }

   public static ItemLore read(PacketWrapper<?> wrapper) {
      List<Component> lines = wrapper.readList(PacketWrapper::readComponent);
      return new ItemLore(lines);
   }

   public static void write(PacketWrapper<?> wrapper, ItemLore lore) {
      wrapper.writeList(lore.lines, PacketWrapper::writeComponent);
   }

   public void addLine(Component line) {
      this.lines.add(line);
   }

   public List<Component> getLines() {
      return this.lines;
   }

   public void setLines(List<Component> lines) {
      this.lines = lines;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ItemLore)) {
         return false;
      } else {
         ItemLore itemLore = (ItemLore)obj;
         return this.lines.equals(itemLore.lines);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.lines);
   }

   @Override
   public String toString() {
      return "ItemLore{lines=" + this.lines + '}';
   }
}
