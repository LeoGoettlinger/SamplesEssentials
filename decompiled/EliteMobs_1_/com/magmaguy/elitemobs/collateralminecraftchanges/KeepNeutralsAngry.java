package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class KeepNeutralsAngry {
   private static final Map<UUID, BukkitTask> angryMobTasks = new HashMap<>();

   private KeepNeutralsAngry() {
   }

   public static void shutdown() {
      for (BukkitTask task : angryMobTasks.values()) {
         if (task != null && !task.isCancelled()) {
            task.cancel();
         }
      }

      angryMobTasks.clear();
   }

   public static void release(UUID bodyId) {
      BukkitTask task = angryMobTasks.remove(bodyId);
      if (task != null) {
         task.cancel();
      }
   }

   public static void showMeYouWarFace(final EliteEntity eliteEntity) {
      final EntityType entityType = eliteEntity.getLivingEntity().getType();
      final UUID entityUUID = eliteEntity.getLivingEntity().getUniqueId();
      if (!angryMobTasks.containsKey(entityUUID)) {
         BukkitTask task = (new BukkitRunnable() {
               public void run() {
                  if (eliteEntity.isValid()
                     && entityUUID.equals(eliteEntity.getLivingEntity().getUniqueId())
                     && entityType.equals(eliteEntity.getLivingEntity().getType())
                     && (!entityType.equals(EntityType.WOLF) || !((Wolf)eliteEntity.getLivingEntity()).isTamed())) {
                     if (!(eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomBossesConfigFields().isNeutral())) {
                        if (eliteEntity.getLivingEntity().getType().equals(EntityType.LLAMA)
                           || eliteEntity.getLivingEntity().getType().equals(EntityType.RABBIT)
                           || ((Mob)eliteEntity.getLivingEntity()).getTarget() == null) {
                           for (Player player : Bukkit.getOnlinePlayers()) {
                              if (!player.getGameMode().equals(GameMode.SPECTATOR)
                                 && !player.getGameMode().equals(GameMode.CREATIVE)
                                 && Objects.equals(player.getLocation().getWorld(), eliteEntity.getLocation().getWorld())
                                 && player.getLocation().distanceSquared(eliteEntity.getLocation())
                                    < Math.pow(AttributeManager.getAttributeBaseValue(eliteEntity.getLivingEntity(), "generic_follow_range"), 2.0)) {
                                 ((Mob)eliteEntity.getLivingEntity()).setTarget(player);
                                 return;
                              }
                           }

                           ((Mob)eliteEntity.getLivingEntity()).setTarget(null);
                        }
                     }
                  } else {
                     this.cancel();
                     KeepNeutralsAngry.angryMobTasks.remove(entityUUID);
                  }
               }
            })
            .runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
         angryMobTasks.put(entityUUID, task);
      }
   }
}
