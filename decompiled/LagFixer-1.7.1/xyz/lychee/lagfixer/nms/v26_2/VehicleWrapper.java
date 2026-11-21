package xyz.lychee.lagfixer.nms.v26_2;

import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.entity.vehicle.minecart.MinecartSpawner;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import org.jetbrains.annotations.NotNull;

public interface VehicleWrapper {
   public static class OBoat extends Boat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OBoat(VehicleMotionReducer provider, Boat b) {
         super(b.getType(), b.level(), () -> b.getPickResult().getItem());
         this.provider = provider;
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestBoat extends ChestBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestBoat(VehicleMotionReducer provider, ChestBoat cb) {
         super(cb.getType(), cb.level(), () -> cb.getPickResult().getItem());
         this.provider = provider;
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestRaft extends ChestRaft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestRaft(VehicleMotionReducer provider, ChestRaft cr) {
         super(EntityTypes.BAMBOO_CHEST_RAFT, cr.level(), () -> cr.getPickResult().getItem());
         this.provider = provider;
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OMinecart extends Minecart implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecart(VehicleMotionReducer provider, Minecart m) {
         super(EntityTypes.MINECART, m.level());
         this.provider = provider;
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartChest extends MinecartChest implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartChest(VehicleMotionReducer provider, MinecartChest mc) {
         super(EntityTypes.CHEST_MINECART, mc.level());
         this.provider = provider;
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartFurnace extends MinecartFurnace implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartFurnace(VehicleMotionReducer provider, MinecartFurnace mf) {
         super(EntityTypes.FURNACE_MINECART, mf.level());
         this.provider = provider;
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartHopper extends MinecartHopper implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartHopper(VehicleMotionReducer provider, MinecartHopper mh) {
         super(EntityTypes.HOPPER_MINECART, mh.level());
         this.provider = provider;
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartSpawner extends MinecartSpawner implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartSpawner(VehicleMotionReducer provider, MinecartSpawner other) {
         super(EntityTypes.SPAWNER_MINECART, other.level());
         this.provider = provider;
         Optional.ofNullable(other.getSpawner().nextSpawnData)
            .flatMap(sd -> sd.entityToSpawn().read("id", EntityType.CODEC))
            .ifPresent(type -> this.getSpawner().setEntityId(type, other.level(), other.getRandom(), this.blockPosition()));
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartTNT extends MinecartTNT implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartTNT(VehicleMotionReducer provider, MinecartTNT mt) {
         super(EntityTypes.TNT_MINECART, mt.level());
         this.provider = provider;
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class ORaft extends Raft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      ORaft(VehicleMotionReducer provider, Raft r) {
         super(EntityTypes.BAMBOO_RAFT, r.level(), () -> r.getPickResult().getItem());
         this.provider = provider;
      }

      public boolean canCollideWith(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean canBeCollidedWith(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean isPushable() {
         return this.provider.getModule().isBoat_pushable();
      }
   }
}
