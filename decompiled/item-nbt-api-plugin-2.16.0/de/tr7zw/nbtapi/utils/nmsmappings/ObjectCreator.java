package de.tr7zw.nbtapi.utils.nmsmappings;

import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import java.lang.reflect.Constructor;
import java.util.logging.Level;

public enum ObjectCreator {
   NMS_NBTTAGCOMPOUND(null, null, ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz()),
   NMS_CUSTOMDATA(MinecraftVersion.MC1_20_R4, null, ClassWrapper.NMS_CUSTOMDATA.getClazz(), ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz()),
   NMS_BLOCKPOSITION(null, null, ClassWrapper.NMS_BLOCKPOSITION.getClazz(), int.class, int.class, int.class),
   NMS_COMPOUNDFROMITEM(
      MinecraftVersion.MC1_11_R1, MinecraftVersion.MC1_20_R3, ClassWrapper.NMS_ITEMSTACK.getClazz(), ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz()
   );

   private Constructor<?> construct;
   private Class<?> targetClass;

   private ObjectCreator(MinecraftVersion from, MinecraftVersion to, Class<?> clazz, Class<?>... args) {
      if (clazz != null) {
         if (from == null || MinecraftVersion.getVersion().getVersionId() >= from.getVersionId()) {
            if (to == null || MinecraftVersion.getVersion().getVersionId() <= to.getVersionId()) {
               try {
                  this.targetClass = clazz;
                  this.construct = clazz.getDeclaredConstructor(args);
                  this.construct.setAccessible(true);
               } catch (Exception var8) {
                  MinecraftVersion.getLogger().log(Level.SEVERE, "Unable to find the constructor for the class '" + clazz.getName() + "'", (Throwable)var8);
               }
            }
         }
      }
   }

   public Object getInstance(Object... args) {
      try {
         return this.construct.newInstance(args);
      } catch (Exception var3) {
         throw new NbtApiException("Exception while creating a new instance of '" + this.targetClass + "'", var3);
      }
   }
}
