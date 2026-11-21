package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

final class XpPopupManager {
   private static final int DURATION_TICKS = 50;
   private static final Color[] GRADIENT_COLORS = new Color[]{
      new Color(255, 107, 0), new Color(255, 215, 0), new Color(255, 255, 0), new Color(255, 215, 0), new Color(255, 107, 0)
   };
   private static final List<XpPopupManager.XpPopup> activePopups = new ArrayList<>();

   private XpPopupManager() {
   }

   static void create(Location location, Player player, long xpAmount) {
      if (location != null && location.getWorld() != null && player != null && player.isOnline()) {
         Vector offset = new Vector(
            ThreadLocalRandom.current().nextDouble(-0.5, 0.5),
            ThreadLocalRandom.current().nextDouble(0.5, 1.0),
            ThreadLocalRandom.current().nextDouble(-0.5, 0.5)
         );
         Location popupLocation = location.clone().add(offset);
         String text = MobCombatSettingsConfig.getXpPopupFormat().replace("$amount", DisplayTextFormatter.number(xpAmount));
         String initialText = ChatColorConverter.convert("<gradient:#FF6B00:#FFD700:#FFFF00>" + text + "</gradient>");
         FakeText display = VisualDisplay.createStyledFakeText(popupLocation, initialText, org.bukkit.Color.fromARGB(120, 80, 60, 0), true, 0.85F);
         if (display != null) {
            display.displayTo(player);
            activePopups.add(new XpPopupManager.XpPopup(display, popupLocation, text, 0.85F));
         }
      }
   }

   static void update() {
      Iterator<XpPopupManager.XpPopup> iterator = activePopups.iterator();

      while (iterator.hasNext()) {
         XpPopupManager.XpPopup popup = iterator.next();
         boolean keep = false;

         try {
            keep = popup.update();
         } catch (RuntimeException var10) {
            MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Discarding a failed XP popup", (Throwable)var10);
         }

         if (!keep) {
            try {
               popup.cleanup();
            } catch (RuntimeException var8) {
               MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to clean an XP popup", (Throwable)var8);
            } finally {
               iterator.remove();
            }
         }
      }
   }

   static void shutdown() {
      activePopups.forEach(popup -> {
         try {
            popup.cleanup();
         } catch (RuntimeException var2) {
            MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to clean an XP popup during shutdown", (Throwable)var2);
         }
      });
      activePopups.clear();
   }

   private static String shiftedGradient(String text, float shift) {
      if (text != null && !text.isEmpty()) {
         StringBuilder result = new StringBuilder();
         int colorCount = GRADIENT_COLORS.length;

         for (int index = 0; index < text.length(); index++) {
            float position = ((float)index / text.length() + shift) % 1.0F;
            float scaledPosition = position * (colorCount - 1);
            int firstIndex = (int)scaledPosition;
            int secondIndex = (firstIndex + 1) % colorCount;
            Color color = interpolate(GRADIENT_COLORS[firstIndex], GRADIENT_COLORS[secondIndex], scaledPosition - firstIndex);
            String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());

            try {
               result.append(ChatColor.of(hex)).append(text.charAt(index));
            } catch (IllegalArgumentException var12) {
               result.append(org.bukkit.ChatColor.GOLD).append(text.charAt(index));
            }
         }

         return result.toString();
      } else {
         return "";
      }
   }

   private static Color interpolate(Color first, Color second, float ratio) {
      int red = (int)(first.getRed() + ratio * (second.getRed() - first.getRed()));
      int green = (int)(first.getGreen() + ratio * (second.getGreen() - first.getGreen()));
      int blue = (int)(first.getBlue() + ratio * (second.getBlue() - first.getBlue()));
      return new Color(red, green, blue);
   }

   private static final class XpPopup {
      private final FakeText display;
      private final Location startLocation;
      private final String text;
      private final float baseScale;
      private final String[] gradientFrames = new String[20];
      private int ticksAlive;

      private XpPopup(FakeText display, Location startLocation, String text, float baseScale) {
         this.display = display;
         this.startLocation = startLocation.clone();
         this.text = text;
         this.baseScale = baseScale;
      }

      private boolean update() {
         this.ticksAlive++;
         if (this.ticksAlive < 50 && this.display != null) {
            float progress = this.ticksAlive / 50.0F;
            this.display.teleport(this.startLocation.clone().add(0.0, progress * 1.2, 0.0));
            int frame = this.ticksAlive % this.gradientFrames.length;
            if (this.gradientFrames[frame] == null) {
               this.gradientFrames[frame] = XpPopupManager.shiftedGradient(this.text, frame / 20.0F);
            }

            this.display.setText(this.gradientFrames[frame]);
            float pulse = (float)Math.sin(this.ticksAlive * 0.15) * 0.1F;
            float scaleMultiplier;
            if (progress < 0.1F) {
               scaleMultiplier = 1.0F + progress / 0.1F * 0.2F;
            } else if (progress > 0.8F) {
               scaleMultiplier = 1.2F - (progress - 0.8F) / 0.2F * 0.4F;
            } else {
               scaleMultiplier = 1.2F + pulse;
            }

            this.display.setScale(this.baseScale * scaleMultiplier);
            if (progress > 0.75F) {
               this.display.setTextOpacity((byte)((1.0F - (progress - 0.75F) / 0.25F) * 255.0F));
            }

            return true;
         } else {
            return false;
         }
      }

      private void cleanup() {
         if (this.display != null) {
            this.display.remove();
         }
      }
   }
}
