package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.PlayerStatsTracker;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathMessageByEliteMob implements Listener {
   private static final WeakHashMap<EntityDamageEvent, WeakReference<EliteEntity>> eliteHits = new WeakHashMap<>();

   public static void shutdown() {
      eliteHits.clear();
   }

   public static void recordHit(EntityDamageEvent hit, EliteEntity source) {
      eliteHits.put(hit, new WeakReference<>(source));
   }

   public static String initializeDeathMessage(Player player, EliteEntity eliteEntity) {
      String deathMessage = "";
      if (eliteEntity instanceof CustomBossEntity customBossEntity
         && customBossEntity.getCustomBossesConfigFields().getOnKillMessage() != null
         && !customBossEntity.getCustomBossesConfigFields().getOnKillMessage().isEmpty()) {
         return deathMessageSender(
            customBossEntity.getCustomBossesConfigFields()
               .getOnKillMessage()
               .replace("$playerName", player.getName())
               .replace("$player", player.getDisplayName()),
            player,
            customBossEntity.getLivingEntity()
         );
      } else {
         if (MobPropertiesConfig.getMobProperties().containsKey(eliteEntity.getLivingEntity().getType())) {
            deathMessage = deathMessageSender(
               MobPropertiesConfig.getMobProperties()
                  .get(eliteEntity.getLivingEntity().getType())
                  .getDeathMessages()
                  .get(
                     ThreadLocalRandom.current()
                        .nextInt(MobPropertiesConfig.getMobProperties().get(eliteEntity.getLivingEntity().getType()).getDeathMessages().size())
                  ),
               player,
               eliteEntity.getLivingEntity()
            );
         }

         return deathMessage;
      }
   }

   private static String deathMessageSender(String deathMessage, Player player, LivingEntity livingEntity) {
      return deathMessagePlaceholderConversion(deathMessage, player, livingEntity);
   }

   private static String deathMessagePlaceholderConversion(String deathMessage, Player player, LivingEntity livingEntity) {
      String livingEntityName = EntityTracker.getEliteMobEntity(livingEntity).getName();
      deathMessage = deathMessage.replace("$playerName", player.getName());
      deathMessage = deathMessage.replace("$player", player.getDisplayName());
      deathMessage = deathMessage.replace("$entity", livingEntityName);
      return ChatColorConverter.convert("&7" + deathMessage);
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      EntityDamageEvent cause = event.getEntity().getLastDamageCause();
      WeakReference<EliteEntity> reference = eliteHits.remove(cause);
      EliteEntity source = reference == null ? null : reference.get();
      if (source != null && cause != null && !cause.isCancelled() && source.getLivingEntity() != null) {
         String message = initializeDeathMessage(event.getEntity(), source);
         if (message != null && !message.isBlank()) {
            event.setDeathMessage(message);
         }

         PlayerStatsTracker.registerPlayerDeath(event.getEntity());
      }
   }
}
