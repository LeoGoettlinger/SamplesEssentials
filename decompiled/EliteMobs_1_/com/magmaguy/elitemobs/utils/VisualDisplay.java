package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import java.util.Collection;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.util.Consumer;

public class VisualDisplay {
   public static ArmorStand generateTemporaryArmorStand(Location location, final String customName) {
      ArmorStand visualArmorStand = (ArmorStand)location.getWorld().spawn(location, ArmorStand.class, new Consumer<ArmorStand>() {
         public void accept(ArmorStand armorStand) {
            armorStand.setVisible(false);
            armorStand.setMarker(true);
            armorStand.setCustomName(ChatColorConverter.convert(customName));
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setRemoveWhenFarAway(true);
            armorStand.setPersistent(false);
         }
      });
      EntityTracker.registerVisualEffects(visualArmorStand);
      return visualArmorStand;
   }

   public static FakeText generateFakeText(Location location, String text, double viewRange) {
      if (NMSManager.getAdapter() == null) {
         return null;
      } else if (location != null && location.getWorld() != null) {
         FakeText fakeText = NMSManager.getAdapter()
            .fakeTextBuilder()
            .text(ChatColorConverter.convert(text))
            .billboard(Billboard.CENTER)
            .shadow(false)
            .seeThrough(false)
            .build(location);

         for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= viewRange * viewRange) {
               fakeText.displayTo(player);
            }
         }

         return fakeText;
      } else {
         return null;
      }
   }

   public static FakeText generateFakeText(Location location, String text, Collection<Player> viewers) {
      if (NMSManager.getAdapter() == null) {
         return null;
      } else if (location != null && location.getWorld() != null) {
         FakeText fakeText = NMSManager.getAdapter()
            .fakeTextBuilder()
            .text(ChatColorConverter.convert(text))
            .billboard(Billboard.CENTER)
            .shadow(false)
            .seeThrough(false)
            .build(location);

         for (Player player : viewers) {
            fakeText.displayTo(player);
         }

         return fakeText;
      } else {
         return null;
      }
   }

   public static FakeText generateFakeText(Location location, String text, Player viewer) {
      if (NMSManager.getAdapter() == null) {
         return null;
      } else if (location != null && location.getWorld() != null) {
         FakeText fakeText = NMSManager.getAdapter()
            .fakeTextBuilder()
            .text(ChatColorConverter.convert(text))
            .billboard(Billboard.CENTER)
            .shadow(false)
            .seeThrough(false)
            .build(location);
         fakeText.displayTo(viewer);
         return fakeText;
      } else {
         return null;
      }
   }

   public static FakeText generateStyledFakeText(Location location, String text, Color backgroundColor, boolean shadowed, float scale, double viewRange) {
      if (NMSManager.getAdapter() == null) {
         return null;
      } else if (location != null && location.getWorld() != null) {
         FakeText.Builder builder = NMSManager.getAdapter()
            .fakeTextBuilder()
            .text(ChatColorConverter.convert(text))
            .billboard(Billboard.CENTER)
            .shadow(shadowed)
            .seeThrough(false);
         if (backgroundColor != null) {
            builder.backgroundColor(backgroundColor);
         }

         if (scale != 1.0F) {
            builder.scale(scale);
         }

         FakeText fakeText = builder.build(location);

         for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= viewRange * viewRange) {
               fakeText.displayTo(player);
            }
         }

         return fakeText;
      } else {
         return null;
      }
   }

   public static FakeText createStyledFakeText(Location location, String text, Color backgroundColor, boolean shadowed, float scale) {
      if (NMSManager.getAdapter() == null) {
         return null;
      } else if (location != null && location.getWorld() != null) {
         FakeText.Builder builder = NMSManager.getAdapter()
            .fakeTextBuilder()
            .text(ChatColorConverter.convert(text))
            .billboard(Billboard.CENTER)
            .shadow(shadowed)
            .seeThrough(false);
         if (backgroundColor != null) {
            builder.backgroundColor(backgroundColor);
         }

         if (scale != 1.0F) {
            builder.scale(scale);
         }

         return builder.build(location);
      } else {
         return null;
      }
   }
}
