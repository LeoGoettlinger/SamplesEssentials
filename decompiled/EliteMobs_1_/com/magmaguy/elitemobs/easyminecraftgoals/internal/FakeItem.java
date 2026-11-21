package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemStack;

public interface FakeItem {
   void setItemStack(ItemStack var1);

   ItemStack getItemStack();

   void setVisible(boolean var1);

   void displayTo(Player var1);

   void displayTo(UUID var1);

   void hideFrom(Player var1);

   void hideFrom(UUID var1);

   void teleport(Location var1);

   Location getLocation();

   void remove();

   boolean hasViewers();

   default void setScale(float scale) {
   }

   default void setBillboard(Billboard billboard) {
   }

   default void setGlowing(boolean glowing) {
   }

   default void setCustomName(String name) {
   }

   default void setCustomNameVisible(boolean visible) {
   }

   default void setYawRotation(float yawDegrees) {
   }

   public interface Builder {
      FakeItem.Builder itemStack(ItemStack var1);

      FakeItem.Builder billboard(Billboard var1);

      FakeItem.Builder scale(float var1);

      FakeItem.Builder viewRange(float var1);

      FakeItem.Builder glowing(boolean var1);

      FakeItem.Builder customName(String var1);

      FakeItem.Builder customNameVisible(boolean var1);

      FakeItem build(Location var1);
   }
}
