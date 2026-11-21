package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

final class EliteOverheadHealthDisplay {
   private static final String FULL_BAR = "▌";
   private static final String EMPTY_BAR = "▌";
   private static final String COLOR_FULL_HIGH = "&a";
   private static final String COLOR_FULL_MED = "&e";
   private static final String COLOR_FULL_LOW = "&c";
   private static final String COLOR_FULL_CRITICAL = "&4";
   private static final String COLOR_EMPTY = "&8";
   private static final int BARS_PER_ROW = 10;
   private static final double VIEW_DISTANCE = 30.0;
   private static final double ROW_HEIGHT = 0.22;
   private final EliteEntity eliteEntity;
   private final List<FakeText> healthBarDisplays = new ArrayList<>();
   private final Map<FakeText, Set<UUID>> audiences = new HashMap<>();
   private final double healthMultiplier;
   private FakeText numericDisplay;
   private long lastCombatTime;
   private boolean dirty = true;

   EliteOverheadHealthDisplay(EliteEntity eliteEntity) {
      this.eliteEntity = eliteEntity;
      this.lastCombatTime = System.currentTimeMillis();
      this.healthMultiplier = eliteEntity instanceof CustomBossEntity customBoss ? customBoss.getHealthMultiplier() : eliteEntity.getHealthMultiplier();
   }

   EliteEntity eliteEntity() {
      return this.eliteEntity;
   }

   boolean isValid() {
      return this.eliteEntity != null && this.eliteEntity.isValid();
   }

   void resetCombatTimer() {
      this.lastCombatTime = System.currentTimeMillis();
   }

   boolean hasTimedOut() {
      long timeoutMillis = MobCombatSettingsConfig.getCombatDisplayTimeoutSeconds() * 1000L;
      return System.currentTimeMillis() - this.lastCombatTime > timeoutMillis;
   }

   void markDirty() {
      this.dirty = true;
   }

   void refreshIfDirty() {
      if (this.dirty && this.isValid()) {
         this.dirty = false;
         double currentHealth = this.eliteEntity.getHealth();
         double maxHealth = this.eliteEntity.getMaxHealth();
         if (MobCombatSettingsConfig.isDisplayVisualHealthBars()) {
            this.createHealthBars(currentHealth, maxHealth);
         } else {
            while (!this.healthBarDisplays.isEmpty()) {
               this.removeText(this.healthBarDisplays.removeLast());
            }
         }

         if (MobCombatSettingsConfig.isDisplayNumericHealth()) {
            this.createNumericDisplay(currentHealth, maxHealth);
         } else if (this.numericDisplay != null) {
            this.removeText(this.numericDisplay);
            this.numericDisplay = null;
         }
      }
   }

   void updatePositions() {
      if (this.isValid()) {
         Location baseLocation = this.getBaseLocation();
         if (baseLocation != null) {
            for (int index = 0; index < this.healthBarDisplays.size(); index++) {
               FakeText display = this.healthBarDisplays.get(index);
               if (display != null) {
                  display.teleport(baseLocation.clone().add(0.0, index * 0.22, 0.0));
               }
            }

            if (this.numericDisplay != null) {
               int rows = MobCombatSettingsConfig.isDisplayVisualHealthBars() ? this.calculateBarLayout().rows() : 0;
               this.numericDisplay.teleport(baseLocation.clone().add(0.0, rows * 0.22, 0.0));
            }

            this.reconcileViewers(baseLocation);
         }
      }
   }

   void cleanup() {
      this.cleanupVisualDisplays();
   }

   private void createHealthBars(double currentHealth, double maxHealth) {
      Location baseLocation = this.getBaseLocation();
      if (baseLocation != null) {
         EliteOverheadHealthDisplay.BarLayout layout = this.calculateBarLayout();

         while (this.healthBarDisplays.size() > layout.rows()) {
            this.removeText(this.healthBarDisplays.removeLast());
         }

         double healthRatio = maxHealth <= 0.0 ? 0.0 : Math.max(0.0, Math.min(1.0, currentHealth / maxHealth));
         int filledBars = (int)Math.ceil(healthRatio * layout.totalBars());
         String filledColor = this.healthColor(healthRatio * 100.0);
         int barsRemaining = filledBars;
         int totalBarsRemaining = layout.totalBars();

         for (int row = 0; row < layout.rows(); row++) {
            int rowSize = Math.min(layout.barsPerRow(), totalBarsRemaining);
            int filledInRow = Math.min(rowSize, barsRemaining);
            StringBuilder text = new StringBuilder();

            for (int index = 0; index < filledInRow; index++) {
               text.append(filledColor).append("▌");
            }

            for (int index = filledInRow; index < rowSize; index++) {
               text.append("&8").append("▌");
            }

            String rendered = ChatColorConverter.convert(text.toString());
            if (row < this.healthBarDisplays.size()) {
               setTextIfChanged(this.healthBarDisplays.get(row), rendered);
            } else {
               FakeText display = this.createFakeText(baseLocation.clone().add(0.0, row * 0.22, 0.0), rendered);
               if (display != null) {
                  this.healthBarDisplays.add(display);
               }
            }

            barsRemaining -= filledInRow;
            totalBarsRemaining -= rowSize;
         }
      }
   }

   private void createNumericDisplay(double currentHealth, double maxHealth) {
      double healthRatio = maxHealth <= 0.0 ? 0.0 : currentHealth / maxHealth;
      String color = this.healthColor(healthRatio * 100.0);
      String text = this.eliteEntity.isScaledCombat()
         ? color + "&l" + DisplayTextFormatter.percentage(healthRatio)
         : color
            + "&l"
            + DisplayTextFormatter.number(currentHealth)
            + MobCombatSettingsConfig.getHealthDisplaySeparator()
            + color
            + "&l"
            + DisplayTextFormatter.number(maxHealth);
      Location baseLocation = this.getBaseLocation();
      if (baseLocation != null) {
         int rows = MobCombatSettingsConfig.isDisplayVisualHealthBars() ? this.calculateBarLayout().rows() : 0;
         String rendered = ChatColorConverter.convert(text);
         if (this.numericDisplay == null) {
            this.numericDisplay = this.createFakeText(baseLocation.clone().add(0.0, rows * 0.22, 0.0), rendered);
         } else {
            setTextIfChanged(this.numericDisplay, rendered);
         }
      }
   }

   private EliteOverheadHealthDisplay.BarLayout calculateBarLayout() {
      if (MobCombatSettingsConfig.isUseFixedHealthBarSize() || this.healthMultiplier <= 0.0) {
         return new EliteOverheadHealthDisplay.BarLayout(10, 10, 1);
      } else if (this.healthMultiplier < 1.0) {
         int totalBars = Math.max(1, (int)Math.round(10.0 * this.healthMultiplier));
         return new EliteOverheadHealthDisplay.BarLayout(totalBars, totalBars, 1);
      } else if (this.healthMultiplier == 1.0) {
         return new EliteOverheadHealthDisplay.BarLayout(10, 10, 1);
      } else {
         double logScale = Math.log(this.healthMultiplier) / Math.log(20.0);
         int totalBars = Math.min((int)Math.ceil(10.0 + 10.0 * logScale), 20);
         return totalBars <= 10
            ? new EliteOverheadHealthDisplay.BarLayout(totalBars, totalBars, 1)
            : new EliteOverheadHealthDisplay.BarLayout(totalBars, (int)Math.ceil(totalBars / 2.0), 2);
      }
   }

   private String healthColor(double healthPercent) {
      if (healthPercent > 75.0) {
         return "&a";
      } else if (healthPercent > 50.0) {
         return "&e";
      } else {
         return healthPercent > 25.0 ? "&c" : "&4";
      }
   }

   private Location getBaseLocation() {
      if (!this.isValid()) {
         return null;
      } else {
         LivingEntity entity = this.eliteEntity.getLivingEntity();
         if (entity == null) {
            return null;
         } else {
            if (this.eliteEntity instanceof CustomBossEntity customBoss && customBoss.getCustomModel() != null) {
               Location nametagLocation = customBoss.getCustomModel().getNametagBoneLocation();
               if (nametagLocation != null) {
                  return nametagLocation.clone().subtract(0.0, 0.9, 0.0);
               }
            }

            return entity.getLocation().clone().add(0.0, entity.getEyeHeight() + 0.8, 0.0);
         }
      }
   }

   private FakeText createFakeText(Location location, String text) {
      if (location != null && location.getWorld() != null) {
         FakeText display = VisualDisplay.createStyledFakeText(location, text, Color.fromARGB(80, 0, 0, 0), true, 1.0F);
         if (display == null) {
            return null;
         } else {
            this.audiences.put(display, new HashSet<>());
            return display;
         }
      } else {
         return null;
      }
   }

   private static void setTextIfChanged(FakeText display, String text) {
      if (!text.equals(display.getText())) {
         display.setText(text);
      }
   }

   private void reconcileViewers(Location base) {
      if (base.getWorld() != null && !this.audiences.isEmpty()) {
         Map<UUID, Location> playerLocations = new HashMap<>();

         for (Entity entity : base.getWorld()
            .getNearbyEntities(base, 30.0, 30.0 + this.healthBarDisplays.size() * 0.22, 30.0, entityx -> entityx instanceof Player)) {
            Player player = (Player)entity;
            playerLocations.put(player.getUniqueId(), player.getLocation());
         }

         this.audiences.forEach((display, viewers) -> {
            Location location = display.getLocation();
            playerLocations.forEach((playerId, playerLocation) -> {
               if (playerLocation.distanceSquared(location) <= 900.0) {
                  if (viewers.add(playerId)) {
                     display.displayTo(playerId);
                  }
               } else if (viewers.remove(playerId)) {
                  display.hideFrom(playerId);
               }
            });
            viewers.removeIf(playerId -> {
               if (playerLocations.containsKey(playerId)) {
                  return false;
               } else {
                  display.hideFrom(playerId);
                  return true;
               }
            });
         });
      }
   }

   private void removeText(FakeText display) {
      this.audiences.remove(display);
      display.remove();
   }

   private void cleanupVisualDisplays() {
      if (this.numericDisplay != null) {
         this.removeText(this.numericDisplay);
         this.numericDisplay = null;
      }

      this.healthBarDisplays.forEach(display -> {
         if (display != null) {
            this.removeText(display);
         }
      });
      this.healthBarDisplays.clear();
   }

   private record BarLayout(int totalBars, int barsPerRow, int rows) {
   }
}
