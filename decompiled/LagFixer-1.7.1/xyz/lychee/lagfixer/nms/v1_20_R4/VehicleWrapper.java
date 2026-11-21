package xyz.lychee.lagfixer.nms.v1_20_R4;

import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.EntityBoat;
import net.minecraft.world.entity.vehicle.EntityMinecartChest;
import net.minecraft.world.entity.vehicle.EntityMinecartFurnace;
import net.minecraft.world.entity.vehicle.EntityMinecartHopper;
import net.minecraft.world.entity.vehicle.EntityMinecartMobSpawner;
import net.minecraft.world.entity.vehicle.EntityMinecartRideable;
import net.minecraft.world.entity.vehicle.EntityMinecartTNT;
import org.jetbrains.annotations.NotNull;

public interface VehicleWrapper {
   public static class OBoat extends EntityBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OBoat(VehicleMotionReducer provider, EntityBoat b) {
         super(EntityTypes.k, b.dP());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bF() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bA() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestBoat extends ChestBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestBoat(VehicleMotionReducer provider, ChestBoat cb) {
         super(EntityTypes.r, cb.dP());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bF() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bA() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OMinecart extends EntityMinecartRideable implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecart(VehicleMotionReducer provider, EntityMinecartRideable m) {
         super(EntityTypes.ar, m.dP());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bF() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bA() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartChest extends EntityMinecartChest implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartChest(VehicleMotionReducer provider, EntityMinecartChest mc) {
         super(EntityTypes.s, mc.dP());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bF() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bA() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartFurnace extends EntityMinecartFurnace implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartFurnace(VehicleMotionReducer provider, EntityMinecartFurnace mf) {
         super(EntityTypes.S, mf.dP());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bF() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bA() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartHopper extends EntityMinecartHopper implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartHopper(VehicleMotionReducer provider, EntityMinecartHopper mh) {
         super(EntityTypes.aa, mh.dP());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bF() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bA() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartSpawner extends EntityMinecartMobSpawner implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartSpawner(VehicleMotionReducer provider, EntityMinecartMobSpawner other) {
         super(EntityTypes.aU, other.dP());
         this.provider = provider;
         Optional.ofNullable(other.B().f).flatMap(sd -> EntityTypes.a(sd.d())).ifPresent(type -> this.B().a(type, other.dP(), other.ah, this.dp()));
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bF() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bA() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartTNT extends EntityMinecartTNT implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartTNT(VehicleMotionReducer provider, EntityMinecartTNT mt) {
         super(EntityTypes.bd, mt.dP());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bF() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bA() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }
}
