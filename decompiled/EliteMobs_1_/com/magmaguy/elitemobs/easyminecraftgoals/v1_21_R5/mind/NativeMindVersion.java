package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R5.mind;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.control.ControllerMove;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

final class NativeMindVersion {
   private static final Field MOVE_OPERATION = moveOperation();
   private static final Object WAIT = Arrays.stream(MOVE_OPERATION.getType().getEnumConstants())
      .filter(value -> ((Enum)value).name().equals("WAIT"))
      .findFirst()
      .orElseThrow();

   private static Field moveOperation() {
      for (Field field : ControllerMove.class.getDeclaredFields()) {
         if (field.getType().isEnum()) {
            field.setAccessible(true);
            return field;
         }
      }

      throw new ExceptionInInitializerError("MoveControl operation is unavailable");
   }

   private NativeMindVersion() {
   }

   static void advancePhysics(EntityInsentient mob, Vec3D input) {
      mob.k(input);
   }

   static WorldServer getServerLevel(Location location) {
      return ((CraftWorld)location.getWorld()).getHandle();
   }

   static EntityLiving getNMSLivingEntity(LivingEntity entity) {
      return ((CraftLivingEntity)entity).getHandle();
   }

   static EntityTypes<?> entityType(String key) {
      return (EntityTypes<?>)BuiltInRegistries.f.a(MinecraftKey.a(key));
   }

   static void position(EntityInsentient mob, double x, double y, double z, float yaw, float pitch) {
      mob.b(x, y, z, yaw, pitch);
   }

   static BehaviorController<EntityInsentient> newBrain() {
      return new BehaviorController(List.of(), List.of(), ImmutableList.of(), () -> BehaviorController.b(List.of(), List.of()));
   }

   static void addActivity(
      BehaviorController<EntityInsentient> brain,
      Activity activity,
      ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super EntityInsentient>>> controls
   ) {
      brain.a(activity, controls);
   }

   static void stopControl(ControllerMove control) {
      try {
         MOVE_OPERATION.set(control, WAIT);
      } catch (IllegalAccessException var2) {
         throw new IllegalStateException("Could not stop native movement", var2);
      }
   }

   static boolean sunlightExposure(EntityInsentient mob) {
      String var2 = mob.getBukkitEntity().getType().name();

      boolean susceptible = switch (var2) {
         case "ZOMBIE", "ZOMBIE_VILLAGER", "DROWNED", "SKELETON", "STRAY", "BOGGED", "PHANTOM" -> true;
         default -> false;
      };
      return susceptible && mob.ai().W() && mob.bB() > 0.5F && !mob.bo() && !mob.aI && !mob.aJ && mob.ai().h(BlockPosition.a(mob.dC(), mob.dG(), mob.dI()));
   }
}
