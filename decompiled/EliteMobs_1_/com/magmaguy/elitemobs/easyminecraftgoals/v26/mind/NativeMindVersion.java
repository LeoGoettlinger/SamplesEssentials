package com.magmaguy.elitemobs.easyminecraftgoals.v26.mind;

import com.google.common.collect.ImmutableList;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.CraftBukkitBridge;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;

final class NativeMindVersion {
   private NativeMindVersion() {
   }

   static void advancePhysics(Mob mob, Vec3 input) {
      mob.travel(input);
   }

   static ServerLevel getServerLevel(Location location) {
      return CraftBukkitBridge.getServerLevel(location);
   }

   static LivingEntity getNMSLivingEntity(org.bukkit.entity.LivingEntity entity) {
      return CraftBukkitBridge.getNMSLivingEntity(entity);
   }

   static EntityType<?> entityType(String key) {
      return (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(key));
   }

   static void position(Mob mob, double x, double y, double z, float yaw, float pitch) {
      mob.snapTo(x, y, z, yaw, pitch);
   }

   static Brain<Mob> newBrain() {
      return new Brain();
   }

   static void addActivity(Brain<Mob> brain, Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super Mob>>> controls) {
      brain.addActivity(activity, controls, Set.of(), Set.of());
   }

   static void stopControl(MoveControl control) {
      control.setWait();
   }

   static boolean sunlightExposure(Mob mob) {
      boolean susceptible = mob.getType().builtInRegistryHolder().is(EntityTypeTags.BURN_IN_DAYLIGHT);
      return susceptible
         && (Boolean)mob.level().environmentAttributes().getValue(EnvironmentAttributes.MONSTERS_BURN, mob.position())
         && mob.getLightLevelDependentMagicValue() > 0.5F
         && !mob.isInWaterOrRain()
         && !mob.isInPowderSnow
         && !mob.wasInPowderSnow
         && mob.level().canSeeSky(BlockPos.containing(mob.getX(), mob.getEyeY(), mob.getZ()));
   }
}
