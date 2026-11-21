package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ItemSwingAnimation {
   private static final ItemSwingAnimation.Type[] TYPES = ItemSwingAnimation.Type.values();
   private ItemSwingAnimation.Type type;
   private int duration;

   public ItemSwingAnimation(ItemSwingAnimation.Type type, int duration) {
      this.type = type;
      this.duration = duration;
   }

   public static ItemSwingAnimation read(PacketWrapper<?> wrapper) {
      ItemSwingAnimation.Type type = wrapper.readEnum(TYPES, ItemSwingAnimation.Type.NONE);
      int duration = wrapper.readVarInt();
      return new ItemSwingAnimation(type, duration);
   }

   public static void write(PacketWrapper<?> wrapper, ItemSwingAnimation component) {
      wrapper.writeEnum(component.type);
      wrapper.writeVarInt(component.duration);
   }

   public ItemSwingAnimation.Type getType() {
      return this.type;
   }

   public void setType(ItemSwingAnimation.Type type) {
      this.type = type;
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int duration) {
      this.duration = duration;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         ItemSwingAnimation that = (ItemSwingAnimation)obj;
         return this.duration != that.duration ? false : this.type == that.type;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.duration);
   }

   public static enum Type {
      NONE,
      WHACK,
      STAB;
   }
}
