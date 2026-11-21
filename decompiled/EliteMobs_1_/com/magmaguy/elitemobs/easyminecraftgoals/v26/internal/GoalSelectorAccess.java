package com.magmaguy.elitemobs.easyminecraftgoals.v26.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;

public final class GoalSelectorAccess {
   private static final MethodHandle ACCESSOR = resolveAccessor();

   private GoalSelectorAccess() {
   }

   public static GoalSelector get(Mob mob) {
      try {
         return (GoalSelector)ACCESSOR.invokeExact((Mob)mob);
      } catch (Throwable var2) {
         throw new IllegalStateException("Unable to access the Mob goal selector on this server version.", var2);
      }
   }

   private static MethodHandle resolveAccessor() {
      try {
         try {
            return MethodHandles.lookup().unreflect(Mob.class.getMethod("getGoalSelector")).asType(MethodType.methodType(GoalSelector.class, Mob.class));
         } catch (NoSuchMethodException var4) {
            for (Class<?> currentClass = Mob.class; currentClass != null; currentClass = currentClass.getSuperclass()) {
               try {
                  Field field = currentClass.getDeclaredField("goalSelector");
                  field.setAccessible(true);
                  return MethodHandles.lookup().unreflectGetter(field).asType(MethodType.methodType(GoalSelector.class, Mob.class));
               } catch (NoSuchFieldException var3) {
               }
            }

            throw new NoSuchFieldException("Mob.goalSelector");
         }
      } catch (RuntimeException | ReflectiveOperationException var5) {
         throw new IllegalStateException("Unable to resolve the Mob goal selector on this server version.", var5);
      }
   }
}
