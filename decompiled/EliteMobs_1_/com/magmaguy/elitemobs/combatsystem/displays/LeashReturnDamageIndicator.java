package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class LeashReturnDamageIndicator {
   private static final int POPUP_DURATION_TICKS = 20;
   private static final Map<LeashReturnDamageIndicator.PopupKey, Long> popupExpiry = new HashMap<>();
   private static long nextExpirySweep;

   private LeashReturnDamageIndicator() {
   }

   public static void show(RegionalBossEntity regionalBossEntity, Player viewer) {
      if (MobCombatSettingsConfig.isDisplayDamageOnHit()) {
         if (regionalBossEntity != null && viewer != null && viewer.isOnline()) {
            LivingEntity livingEntity = regionalBossEntity.getUnsyncedLivingEntity();
            if (livingEntity != null && livingEntity.isValid()) {
               LeashReturnDamageIndicator.PopupKey popupKey = new LeashReturnDamageIndicator.PopupKey(livingEntity.getUniqueId(), viewer.getUniqueId());
               long now = System.currentTimeMillis();
               Long expiry = popupExpiry.get(popupKey);
               if (expiry == null || now >= expiry) {
                  popupExpiry.put(popupKey, now + 1000L);
                  Vector offset = new Vector(ThreadLocalRandom.current().nextDouble(-1.5, 1.5), 0.0, ThreadLocalRandom.current().nextDouble(-1.5, 1.5));
                  Location location = livingEntity.getLocation().clone().add(offset.getX(), livingEntity.getEyeHeight() + 0.3, offset.getZ());
                  CombatPopupManager.createImmunePopup(location, viewer);
               }
            }
         }
      }
   }

   public static void shutdown() {
      popupExpiry.clear();
      nextExpirySweep = 0L;
   }

   static void expire() {
      if (!popupExpiry.isEmpty()) {
         long now = System.currentTimeMillis();
         if (now >= nextExpirySweep) {
            nextExpirySweep = now + 1000L;
            popupExpiry.values().removeIf(expiryTime -> expiryTime <= now);
         }
      }
   }

   private record PopupKey(UUID bossUuid, UUID viewerUuid) {
   }
}
