package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;

public interface FakeText {
   void setText(String var1);

   String getText();

   void setVisible(boolean var1);

   void displayTo(Player var1);

   void displayTo(UUID var1);

   void hideFrom(Player var1);

   void hideFrom(UUID var1);

   void teleport(Location var1);

   Location getLocation();

   void remove();

   boolean hasViewers();

   default void setTextOpacity(byte opacity) {
   }

   default void setScale(float scale) {
   }

   default void setBackgroundColor(Color color) {
   }

   default void setBackgroundColor(int argb) {
   }

   default void setShadowed(boolean shadow) {
   }

   default void setSeeThrough(boolean seeThrough) {
   }

   default void setBillboard(Billboard billboard) {
   }

   default void mountTo(Entity vehicle) {
   }

   default void dismount() {
   }

   default void attachTo(Entity vehicle) {
   }

   default void detach() {
   }

   default Entity getVehicle() {
      return null;
   }

   default boolean isAutoTracked() {
      return false;
   }

   public interface Builder {
      FakeText.Builder text(String var1);

      FakeText.Builder backgroundColor(Color var1);

      FakeText.Builder backgroundColor(int var1);

      FakeText.Builder textOpacity(byte var1);

      FakeText.Builder billboard(Billboard var1);

      FakeText.Builder alignment(FakeText.TextAlignment var1);

      FakeText.Builder shadow(boolean var1);

      FakeText.Builder seeThrough(boolean var1);

      FakeText.Builder lineWidth(int var1);

      FakeText.Builder viewRange(float var1);

      FakeText.Builder teleportDuration(int var1);

      FakeText.Builder scale(float var1);

      FakeText.Builder translation(float var1, float var2, float var3);

      FakeText.Builder viewerFilter(Predicate<Player> var1);

      FakeText build(Location var1);
   }

   public static enum TextAlignment {
      CENTER,
      LEFT,
      RIGHT;
   }
}
