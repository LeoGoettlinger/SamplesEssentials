package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.passenger;

import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_paper.CraftBukkitBridge;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.phys.Vec3D;

public class PassengerOffset {
   private static Field dimensionsField = null;

   private PassengerOffset() {
   }

   public static boolean setPassengerOffset(Entity entity, double offsetX, double offsetY, double offsetZ) {
      try {
         if (dimensionsField == null) {
            dimensionsField = findDimensionsField();
         }

         EntitySize currentDimensions = (EntitySize)dimensionsField.get(entity);
         EntityAttachments currentAttachments = currentDimensions.d();
         if (currentAttachments == null) {
            currentAttachments = EntityAttachments.a(currentDimensions.a(), currentDimensions.b());
         }

         Field mapField = EntityAttachments.class.getDeclaredField("attachments");
         mapField.setAccessible(true);
         Map<EntityAttachment, List<Vec3D>> existingMap = (Map<EntityAttachment, List<Vec3D>>)mapField.get(currentAttachments);
         HashMap<EntityAttachment, List<Vec3D>> newMap = new HashMap<>(existingMap);
         newMap.put(EntityAttachment.a, List.of(new Vec3D(offsetX, offsetY, offsetZ)));
         Constructor<EntityAttachments> ctor = EntityAttachments.class.getDeclaredConstructor(Map.class);
         ctor.setAccessible(true);
         EntityAttachments newAttachments = ctor.newInstance(newMap);
         EntitySize newDimensions = new EntitySize(currentDimensions.a(), currentDimensions.b(), currentDimensions.c(), newAttachments, currentDimensions.e());
         dimensionsField.set(entity, newDimensions);
         return true;
      } catch (Exception var15) {
         var15.printStackTrace();
         return false;
      }
   }

   private static Field findDimensionsField() throws NoSuchFieldException {
      String fieldName = CraftBukkitBridge.isPaper() ? "dimensions" : "bz";
      Field field = Entity.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
   }
}
