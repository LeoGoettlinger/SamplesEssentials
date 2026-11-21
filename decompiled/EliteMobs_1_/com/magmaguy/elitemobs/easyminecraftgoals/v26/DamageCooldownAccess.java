package com.magmaguy.elitemobs.easyminecraftgoals.v26;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.world.entity.LivingEntity;

final class DamageCooldownAccess {
   private final MethodHandle getter;
   private final MethodHandle setter;

   private DamageCooldownAccess(MethodHandle getter, MethodHandle setter) {
      this.getter = getter;
      this.setter = setter;
   }

   static DamageCooldownAccess current() {
      return DamageCooldownAccess.NativeAccess.INSTANCE;
   }

   static DamageCooldownAccess resolve(Class<?> livingEntityType) {
      Field field;
      try {
         field = livingEntityType.getDeclaredField("damageCooldownTime");
      } catch (NoSuchFieldException var5) {
         return null;
      }

      int modifiers = field.getModifiers();
      if (field.getType() == int.class && Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
         try {
            return new DamageCooldownAccess(
               MethodHandles.lookup().unreflectGetter(field).asType(MethodType.methodType(int.class, Object.class)),
               MethodHandles.lookup().unreflectSetter(field).asType(MethodType.methodType(void.class, Object.class, int.class))
            );
         } catch (RuntimeException | IllegalAccessException var4) {
            throw new IllegalStateException("Unable to access the native hit cooldown field: " + field, var4);
         }
      } else {
         throw new IllegalStateException("Unexpected native hit cooldown field: " + field);
      }
   }

   int get(Object entity) {
      try {
         return (int)this.getter.invokeExact((Object)entity);
      } catch (Throwable var3) {
         throw new IllegalStateException("Unable to read the native hit cooldown", var3);
      }
   }

   void set(Object entity, int ticks) {
      try {
         this.setter.invokeExact((Object)entity, (int)ticks);
      } catch (Throwable var4) {
         throw new IllegalStateException("Unable to write the native hit cooldown", var4);
      }
   }

   private static final class NativeAccess {
      private static final DamageCooldownAccess INSTANCE = DamageCooldownAccess.resolve(LivingEntity.class);
   }
}
