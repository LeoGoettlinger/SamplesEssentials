package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DialogArmorStand {
   public static FakeText createDialogArmorStand(final Entity sourceEntity, String dialog, final Vector offset) {
      offset.add(getDisplacementVector(sourceEntity).subtract(new Vector(0, 1, 0)));
      final FakeText fakeText = VisualDisplay.generateFakeText(sourceEntity.getLocation().clone().add(offset), dialog, 30.0);
      if (fakeText == null) {
         return null;
      } else {
         (new BukkitRunnable() {
            int taskTimer = 0;

            public void run() {
               this.taskTimer++;
               if (this.taskTimer <= 15 && sourceEntity.isValid()) {
                  Location newLoc = sourceEntity.getLocation().clone().add(offset).add(new Vector(0.0, this.taskTimer * 0.05, 0.0));
                  fakeText.teleport(newLoc);
               } else {
                  fakeText.remove();
                  this.cancel();
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 2L);
         return fakeText;
      }
   }

   private static Vector getDisplacementVector(Entity sourceEntity) {
      double height = 2.3;
      if (sourceEntity instanceof LivingEntity) {
         height = ((LivingEntity)sourceEntity).getEyeHeight();
      }

      return new Vector(0.0, height, 0.0);
   }

   public static FakeText createDialogArmorStand(final LivingEntity sourceEntity, String dialog) {
      if (sourceEntity == null) {
         return null;
      } else {
         final FakeText fakeText = VisualDisplay.generateFakeText(sourceEntity.getLocation().clone().add(getDisplacementVector(sourceEntity)), dialog, 30.0);
         if (fakeText == null) {
            return null;
         } else {
            (new BukkitRunnable() {
               int taskTimer = 0;

               public void run() {
                  if (this.taskTimer <= 15 && sourceEntity.isValid()) {
                     fakeText.teleport(sourceEntity.getLocation().clone().add(DialogArmorStand.getDisplacementVector(sourceEntity)));
                     this.taskTimer++;
                  } else {
                     fakeText.remove();
                     this.cancel();
                  }
               }
            }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
            return fakeText;
         }
      }
   }
}
