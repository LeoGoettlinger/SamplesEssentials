package com.github.retrooper.packetevents.protocol.component.builtin;

import com.github.retrooper.packetevents.protocol.color.DyeColor;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;

public class CatCollarComponent {
   private DyeColor color;

   public CatCollarComponent(DyeColor color) {
      this.color = color;
   }

   public static CatCollarComponent read(PacketWrapper<?> wrapper) {
      DyeColor type = DyeColor.read(wrapper);
      return new CatCollarComponent(type);
   }

   public static void write(PacketWrapper<?> wrapper, CatCollarComponent component) {
      DyeColor.write(wrapper, component.color);
   }

   public DyeColor getDyeColor() {
      return this.color;
   }

   public void setDyeColor(DyeColor color) {
      this.color = color;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CatCollarComponent)) {
         return false;
      } else {
         CatCollarComponent that = (CatCollarComponent)obj;
         return this.color.equals(that.color);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.color);
   }
}
