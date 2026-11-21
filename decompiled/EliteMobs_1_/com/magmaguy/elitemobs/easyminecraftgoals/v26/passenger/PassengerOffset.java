package com.magmaguy.elitemobs.easyminecraftgoals.v26.passenger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;

public class PassengerOffset {
   private static Field dimensionsField = null;
   private static boolean dimensionsFieldLookupTried = false;

   private PassengerOffset() {
   }

   public static boolean setPassengerOffset(Entity entity, double offsetX, double offsetY, double offsetZ) {
      if (!dimensionsFieldLookupTried) {
         dimensionsFieldLookupTried = true;
         dimensionsField = findDimensionsField();
      }

      if (dimensionsField == null) {
         return false;
      } else {
         try {
            EntityDimensions currentDimensions = (EntityDimensions)dimensionsField.get(entity);
            EntityAttachments currentAttachments = currentDimensions.attachments();
            if (currentAttachments == null) {
               currentAttachments = EntityAttachments.createDefault(currentDimensions.width(), currentDimensions.height());
            }

            Field mapField = EntityAttachments.class.getDeclaredField("attachments");
            mapField.setAccessible(true);
            Map<EntityAttachment, List<Vec3>> existingMap = (Map<EntityAttachment, List<Vec3>>)mapField.get(currentAttachments);
            HashMap<EntityAttachment, List<Vec3>> newMap = new HashMap<>(existingMap);
            newMap.put(EntityAttachment.PASSENGER, List.of(new Vec3(offsetX, offsetY, offsetZ)));
            Constructor<EntityAttachments> ctor = EntityAttachments.class.getDeclaredConstructor(Map.class);
            ctor.setAccessible(true);
            EntityAttachments newAttachments = ctor.newInstance(newMap);
            EntityDimensions newDimensions = new EntityDimensions(
               currentDimensions.width(), currentDimensions.height(), currentDimensions.eyeHeight(), newAttachments, currentDimensions.fixed()
            );
            dimensionsField.set(entity, newDimensions);
            return true;
         } catch (Exception var15) {
            var15.printStackTrace();
            return false;
         }
      }
   }

   private static Field findDimensionsField() {
      for (Field f : Entity.class.getDeclaredFields()) {
         if (f.getType() == EntityDimensions.class) {
            f.setAccessible(true);
            return f;
         }
      }

      new RuntimeException("PassengerOffset: no EntityDimensions field on Entity.class; passenger offsets disabled this session").printStackTrace();
      return null;
   }
}
