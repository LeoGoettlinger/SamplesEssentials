package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public class ItemDyeColor {
   private int rgb;
   @Obsolete
   private boolean showInTooltip;

   public ItemDyeColor(int rgb) {
      this(rgb, true);
   }

   @Obsolete
   public ItemDyeColor(int rgb, boolean showInTooltip) {
      this.rgb = rgb;
      this.showInTooltip = showInTooltip;
   }

   public static ItemDyeColor read(PacketWrapper<?> wrapper) {
      int rgb = wrapper.readInt();
      boolean showInTooltip = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5) || wrapper.readBoolean();
      return new ItemDyeColor(rgb, showInTooltip);
   }

   public static void write(PacketWrapper<?> wrapper, ItemDyeColor color) {
      wrapper.writeInt(color.rgb);
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_5)) {
         wrapper.writeBoolean(color.showInTooltip);
      }
   }

   public int getRgb() {
      return this.rgb;
   }

   public void setRgb(int rgb) {
      this.rgb = rgb;
   }

   @Obsolete
   public boolean isShowInTooltip() {
      return this.showInTooltip;
   }

   @Obsolete
   public void setShowInTooltip(boolean showInTooltip) {
      this.showInTooltip = showInTooltip;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ItemDyeColor)) {
         return false;
      } else {
         ItemDyeColor that = (ItemDyeColor)obj;
         return this.rgb != that.rgb ? false : this.showInTooltip == that.showInTooltip;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.rgb, this.showInTooltip);
   }
}
