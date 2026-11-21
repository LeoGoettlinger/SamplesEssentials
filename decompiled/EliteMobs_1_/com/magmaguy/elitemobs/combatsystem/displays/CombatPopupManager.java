package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.items.EliteEnchantmentCatalog;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

final class CombatPopupManager {
   private static final int DURATION_TICKS = 20;
   private static final List<CombatPopupManager.Popup> activePopups = new ArrayList<>();

   private CombatPopupManager() {
   }

   static void update() {
      Iterator<CombatPopupManager.Popup> iterator = activePopups.iterator();

      while (iterator.hasNext()) {
         CombatPopupManager.Popup popup = iterator.next();
         boolean keep = false;

         try {
            keep = popup.update();
         } catch (RuntimeException var10) {
            MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Discarding a failed combat popup", (Throwable)var10);
         }

         if (!keep) {
            try {
               popup.cleanup();
            } catch (RuntimeException var8) {
               MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to clean a combat popup", (Throwable)var8);
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
            MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to clean a combat popup during shutdown", (Throwable)var2);
         }
      });
      activePopups.clear();
   }

   static void createDamagePopup(
      EliteEntity eliteEntity, double damage, boolean critical, double damageModifier, double classAbilityBonusDamage, Vector offset, Player player
   ) {
      if (MobCombatSettingsConfig.isDisplayDamageOnHit()) {
         LivingEntity entity = eliteEntity.getUnsyncedLivingEntity();
         if (entity != null && entity.isValid()) {
            Location baseLocation = entity.getLocation().clone();
            Location mobLocation = eliteEntity.getLocation();
            double eyeHeight = entity.getEyeHeight();
            baseLocation.add(offset.getX(), eyeHeight + offset.getY() + 0.3, offset.getZ());
            StringBuilder text = new StringBuilder();
            if (damageModifier < 1.0) {
               text.append(MobCombatSettingsConfig.getResistTextColor());
               if (mobLocation.getWorld() != null) {
                  mobLocation.getWorld().playSound(mobLocation, Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);
               }

               if (MobCombatSettingsConfig.isDoResistEffect() && player != null) {
                  HitEffectManager.createResistEffect(eliteEntity, player);
               }
            } else if (damageModifier > 1.0) {
               text.append(MobCombatSettingsConfig.getWeakTextColor());
               if (mobLocation.getWorld() != null) {
                  mobLocation.getWorld().playSound(mobLocation, Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
               }

               if (MobCombatSettingsConfig.isDoWeakEffect() && player != null) {
                  HitEffectManager.createWeakEffect(eliteEntity, player);
               }
            } else {
               text.append("&c");
            }

            if (critical) {
               text.append(EliteEnchantmentCatalog.text("elitemobs:critical_strikes", "criticalHitColor", "&5")).append("&l");
            }

            text.append(CombatPopupText.damageAmount(eliteEntity.isScaledCombat(), eliteEntity.getMaxHealth(), damage, classAbilityBonusDamage));
            create(
               baseLocation,
               ChatColorConverter.convert(text.toString()),
               critical ? CombatPopupManager.PopupType.CRITICAL : CombatPopupManager.PopupType.DAMAGE,
               critical ? 1.3F : 1.0F
            );
            if (damageModifier != 1.0) {
               Vector modifierOffset = offset.clone().subtract(new Vector(0.0, 0.3, 0.0));
               Location modifierLocation = entity.getLocation().clone();
               modifierLocation.add(modifierOffset.getX(), eyeHeight + modifierOffset.getY() + 0.3, modifierOffset.getZ());
               boolean resisted = damageModifier < 1.0;
               create(
                  modifierLocation,
                  resisted ? MobCombatSettingsConfig.getResistText() : MobCombatSettingsConfig.getWeakText(),
                  resisted ? CombatPopupManager.PopupType.RESIST : CombatPopupManager.PopupType.WEAK,
                  0.8F
               );
            }

            if (critical) {
               Vector criticalOffset = offset.clone().add(new Vector(0.0, 0.4, 0.0));
               Location criticalLocation = entity.getLocation().clone();
               criticalLocation.add(criticalOffset.getX(), eyeHeight + criticalOffset.getY() + 0.3, criticalOffset.getZ());
               create(
                  criticalLocation,
                  EliteEnchantmentCatalog.text("elitemobs:critical_strikes", "criticalHitPopup", "&5Critical Hit!"),
                  CombatPopupManager.PopupType.CRITICAL,
                  0.9F
               );
            }
         }
      }
   }

   static void createHealPopup(EliteEntity eliteEntity, double healAmount, boolean fullHeal) {
      if (MobCombatSettingsConfig.isDisplayDamageOnHit()) {
         LivingEntity entity = eliteEntity.getUnsyncedLivingEntity();
         if (entity != null && entity.isValid()) {
            Vector offset = new Vector(ThreadLocalRandom.current().nextDouble(-1.0, 1.0), 0.0, ThreadLocalRandom.current().nextDouble(-1.0, 1.0));
            Location location = entity.getLocation().clone();
            location.add(offset.getX(), entity.getEyeHeight() + 0.3, offset.getZ());
            String text = fullHeal
               ? MobCombatSettingsConfig.getFullHealMessage()
               : "&a" + MobCombatSettingsConfig.getHealPopupFormat().replace("$amount", DisplayTextFormatter.number(healAmount));
            create(location, ChatColorConverter.convert(text), CombatPopupManager.PopupType.HEAL, fullHeal ? 1.2F : 1.0F);
         }
      }
   }

   static void createImmunePopup(Location location, Player viewer) {
      if (location != null && location.getWorld() != null && viewer != null && viewer.isOnline()) {
         FakeText display = VisualDisplay.createStyledFakeText(
            location, MobCombatSettingsConfig.getInvulnerablePopupText(), Color.fromARGB(120, 0, 30, 80), true, 1.0F
         );
         if (display != null) {
            display.displayTo(viewer);
            activePopups.add(new CombatPopupManager.Popup(display, location, 1.0F));
         }
      }
   }

   private static void create(Location location, String text, CombatPopupManager.PopupType type, float scale) {
      if (location != null && location.getWorld() != null) {
         FakeText display = VisualDisplay.createStyledFakeText(location, text, backgroundColor(type), true, scale);
         if (display != null) {
            for (Player player : location.getWorld().getPlayers()) {
               if (player.getLocation().distanceSquared(location) <= 900.0) {
                  display.displayTo(player);
               }
            }

            activePopups.add(new CombatPopupManager.Popup(display, location, scale));
         }
      }
   }

   private static Color backgroundColor(CombatPopupManager.PopupType type) {
      return switch (type) {
         case DAMAGE -> Color.fromARGB(100, 50, 0, 0);
         case CRITICAL -> Color.fromARGB(120, 80, 0, 80);
         case HEAL -> Color.fromARGB(100, 0, 50, 0);
         case WEAK -> Color.fromARGB(100, 0, 0, 80);
         case RESIST -> Color.fromARGB(100, 80, 0, 0);
      };
   }

   private static final class Popup {
      private final FakeText display;
      private final Location startLocation;
      private final float baseScale;
      private int ticksAlive;

      private Popup(FakeText display, Location startLocation, float baseScale) {
         this.display = display;
         this.startLocation = startLocation.clone();
         this.baseScale = baseScale;
      }

      private boolean update() {
         this.ticksAlive++;
         if (this.ticksAlive < 20 && this.display != null) {
            float progress = this.ticksAlive / 20.0F;
            this.display.teleport(this.startLocation.clone().add(0.0, progress * 0.8, 0.0));
            float scaleMultiplier;
            if (progress < 0.2F) {
               scaleMultiplier = 1.0F + progress / 0.2F * 0.3F;
            } else if (progress > 0.7F) {
               scaleMultiplier = 1.3F - (progress - 0.7F) / 0.3F * 0.5F;
            } else {
               scaleMultiplier = 1.3F;
            }

            this.display.setScale(this.baseScale * scaleMultiplier);
            if (progress > 0.7F) {
               this.display.setTextOpacity((byte)((1.0F - (progress - 0.7F) / 0.3F) * 255.0F));
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

   private static enum PopupType {
      DAMAGE,
      CRITICAL,
      HEAL,
      WEAK,
      RESIST;
   }
}
