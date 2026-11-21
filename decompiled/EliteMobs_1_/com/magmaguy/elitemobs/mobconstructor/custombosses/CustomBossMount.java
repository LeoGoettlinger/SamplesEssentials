package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventMountExploit;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomBossMount {
   private CustomBossMount() {
   }

   public static CustomBossEntity generateMount(CustomBossEntity rider) {
      String definition = rider.getCustomBossesConfigFields().getMountedEntity();
      if (definition == null) {
         return null;
      } else {
         LivingEntity riderBody = rider.getLivingEntity();
         if (riderBody != null && riderBody.isValid()) {
            EntityType nativeType;
            try {
               nativeType = EntityType.valueOf(definition);
            } catch (IllegalArgumentException var10) {
               return generateCustomMount(rider, riderBody, definition);
            }

            if (nativeType.getEntityClass() != null && LivingEntity.class.isAssignableFrom(nativeType.getEntityClass())) {
               LivingEntity mount = (LivingEntity)riderBody.getWorld().spawnEntity(riderBody.getLocation(), nativeType);
               rider.livingEntityMount = mount;
               boolean attached = false;

               Object var6;
               try {
                  if (rider.getLivingEntity() == riderBody) {
                     mount.setRemoveWhenFarAway(false);
                     attached = PreventMountExploit.addPassenger(mount, riderBody);
                     return null;
                  }

                  var6 = null;
               } finally {
                  if (!attached) {
                     if (rider.livingEntityMount == mount) {
                        rider.livingEntityMount = null;
                     }

                     mount.remove();
                  }
               }

               return (CustomBossEntity)var6;
            } else {
               Logger.warn("Mount " + definition + " for " + rider.getCustomBossesConfigFields().getFilename() + " is not a living entity.");
               return null;
            }
         } else {
            return null;
         }
      }
   }

   private static CustomBossEntity generateCustomMount(final CustomBossEntity rider, final LivingEntity riderBody, String definition) {
      CustomBossesConfigFields fields = CustomBossesConfig.getCustomBoss(definition);
      if (fields == null) {
         Logger.warn("Invalid mount " + definition + " for " + rider.getCustomBossesConfigFields().getFilename());
         return null;
      } else {
         final CustomBossEntity mount = new CustomBossEntity(fields);
         mount.setSpawnLocation(riderBody.getLocation());
         mount.setBypassesProtections(rider.getBypassesProtections());
         mount.setPersistent(false);
         mount.setMount(true);
         rider.customBossMount = mount;
         boolean queued = false;

         CustomBossEntity var6;
         try {
            mount.spawn(false);
            if (!mount.isValid() || rider.getLivingEntity() != riderBody || !riderBody.isValid()) {
               return null;
            }

            rider.mountAttachmentTask = (new BukkitRunnable() {
               public void run() {
                  if (rider.customBossMount != mount) {
                     mount.remove(RemovalReason.REINFORCEMENT_CULL);
                  } else {
                     rider.mountAttachmentTask = null;
                     boolean attached = false;

                     try {
                        if (!mount.isValid() || rider.getLivingEntity() != riderBody || !riderBody.isValid()) {
                           return;
                        }

                        if (mount.getCustomModel() != null) {
                           mount.getCustomModel().addPassenger(rider);
                           attached = true;
                        } else {
                           attached = PreventMountExploit.addPassenger(mount.getLivingEntity(), riderBody);
                        }
                     } finally {
                        if (!attached) {
                           CustomBossMount.removeCustomMount(rider, mount);
                        }
                     }
                  }
               }
            }).runTaskLater(MetadataHandler.PLUGIN, 5L);
            queued = true;
            var6 = mount;
         } finally {
            if (!queued) {
               removeCustomMount(rider, mount);
            }
         }

         return var6;
      }
   }

   private static void removeCustomMount(CustomBossEntity rider, CustomBossEntity mount) {
      if (rider.customBossMount == mount) {
         rider.customBossMount = null;
      }

      mount.remove(RemovalReason.REINFORCEMENT_CULL);
   }
}
