package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.powerstances.VisualItemInitializer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class CustomBossTrail {
   private final CustomBossEntity customBossEntity;
   private final ArrayList<BukkitTask> bukkitTasks = new ArrayList<>();
   private LivingEntity livingEntity;

   public CustomBossTrail(CustomBossEntity customBossEntity) {
      this.customBossEntity = customBossEntity;
      this.livingEntity = customBossEntity.getLivingEntity();
      this.startBossTrails();
   }

   private void startBossTrails() {
      if (this.customBossEntity.customBossesConfigFields.getTrails() != null) {
         for (String string : this.customBossEntity.customBossesConfigFields.getTrails()) {
            try {
               Particle particle = Particle.valueOf(string);
               if (particle.equals(Particle.BLOCK)) {
                  return;
               }

               this.doParticleTrail(particle);
            } catch (Exception var5) {
            }

            try {
               if (string.equals("LAVA")) {
                  return;
               }

               Material material = Material.valueOf(string);
               this.doItemTrail(material);
            } catch (Exception var4) {
            }
         }
      }
   }

   private void doParticleTrail(final Particle particle) {
      if (particle.getDataType() == Void.class) {
         this.bukkitTasks
            .add(
               (new BukkitRunnable() {
                     public void run() {
                        if (!CustomBossTrail.this.livingEntity.isValid()) {
                           this.cancel();
                        } else {
                           Location entityCenter = CustomBossTrail.this.livingEntity
                              .getLocation()
                              .clone()
                              .add(0.0, CustomBossTrail.this.livingEntity.getHeight() / 2.0, 0.0);
                           CustomBossTrail.this.livingEntity.getWorld().spawnParticle(particle, entityCenter, 1, 0.1, 0.1, 0.1, 0.05);
                        }
                     }
                  })
                  .runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L)
            );
      }
   }

   private void doItemTrail(final Material material) {
      this.bukkitTasks
         .add(
            (new BukkitRunnable() {
                  public void run() {
                     if (!CustomBossTrail.this.livingEntity.isValid()) {
                        this.cancel();
                     } else {
                        Location entityCenter = CustomBossTrail.this.livingEntity
                           .getLocation()
                           .clone()
                           .add(0.0, CustomBossTrail.this.livingEntity.getHeight() / 2.0, 0.0);
                        final Item item = VisualItemInitializer.initializeItem(
                           ItemStackGenerator.generateItemStack(material, "visualItem", List.of(ThreadLocalRandom.current().nextDouble() + "")), entityCenter
                        );
                        item.setVelocity(
                           new Vector(
                              ThreadLocalRandom.current().nextDouble() / 5.0 - 0.1,
                              ThreadLocalRandom.current().nextDouble() / 5.0 - 0.1,
                              ThreadLocalRandom.current().nextDouble() / 5.0 - 0.1
                           )
                        );
                        (new BukkitRunnable() {
                           public void run() {
                              item.remove();
                              EntityTracker.unregister(item, RemovalReason.EFFECT_TIMEOUT);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 20L);
                     }
                  }
               })
               .runTaskTimer(MetadataHandler.PLUGIN, 0L, 5L)
         );
   }

   public void terminateTrails() {
      for (BukkitTask bukkitTask : this.bukkitTasks) {
         bukkitTask.cancel();
      }

      this.bukkitTasks.clear();
   }

   public void restartTrails() {
      this.livingEntity = this.customBossEntity.getLivingEntity();
      this.startBossTrails();
   }
}
