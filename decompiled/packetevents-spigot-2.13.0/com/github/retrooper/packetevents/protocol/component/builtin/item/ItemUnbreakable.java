package com.github.retrooper.packetevents.protocol.component.builtin.item;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus.Obsolete;

public class ItemUnbreakable {
   @Obsolete
   private boolean showInTooltip;

   public ItemUnbreakable() {
      this(true);
   }

   @Obsolete
   public ItemUnbreakable(boolean showInTooltip) {
      this.showInTooltip = showInTooltip;
   }

   public static ItemUnbreakable read(PacketWrapper<?> wrapper) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
         return new ItemUnbreakable();
      } else {
         boolean showInTooltip = wrapper.readBoolean();
         return new ItemUnbreakable(showInTooltip);
      }
   }

   public static void write(PacketWrapper<?> wrapper, ItemUnbreakable value) {
      if (wrapper.getServerVersion().isOlderThan(ServerVersion.V_1_21_5)) {
         wrapper.writeBoolean(value.showInTooltip);
      }
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
      if (!(obj instanceof ItemUnbreakable)) {
         return false;
      } else {
         ItemUnbreakable that = (ItemUnbreakable)obj;
         return this.showInTooltip == that.showInTooltip;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.showInTooltip);
   }
}
