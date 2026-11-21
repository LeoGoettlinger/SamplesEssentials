package xyz.lychee.lagfixer.nms.v1_21_R4;

import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.ChestRaft;
import net.minecraft.world.entity.vehicle.EntityBoat;
import net.minecraft.world.entity.vehicle.EntityMinecartChest;
import net.minecraft.world.entity.vehicle.EntityMinecartFurnace;
import net.minecraft.world.entity.vehicle.EntityMinecartHopper;
import net.minecraft.world.entity.vehicle.EntityMinecartMobSpawner;
import net.minecraft.world.entity.vehicle.EntityMinecartRideable;
import net.minecraft.world.entity.vehicle.EntityMinecartTNT;
import net.minecraft.world.entity.vehicle.Raft;
import org.jetbrains.annotations.NotNull;

public interface VehicleWrapper {
   public static class OBoat extends EntityBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OBoat(VehicleMotionReducer provider, EntityBoat b) {
         super(b.an(), b.dV(), () -> b.dI().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestBoat extends ChestBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestBoat(VehicleMotionReducer provider, ChestBoat cb) {
         super(cb.an(), cb.dV(), () -> cb.dI().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestRaft extends ChestRaft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestRaft(VehicleMotionReducer provider, ChestRaft cr) {
         super(EntityTypes.j, cr.dV(), () -> cr.dI().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OMinecart extends EntityMinecartRideable implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecart(VehicleMotionReducer provider, EntityMinecartRideable m) {
         super(EntityTypes.aE, m.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartChest extends EntityMinecartChest implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartChest(VehicleMotionReducer provider, EntityMinecartChest mc) {
         super(EntityTypes.z, mc.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartFurnace extends EntityMinecartFurnace implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartFurnace(VehicleMotionReducer provider, EntityMinecartFurnace mf) {
         super(EntityTypes.ad, mf.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartHopper extends EntityMinecartHopper implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartHopper(VehicleMotionReducer provider, EntityMinecartHopper mh) {
         super(EntityTypes.al, mh.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartSpawner extends EntityMinecartMobSpawner implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartSpawner(VehicleMotionReducer provider, EntityMinecartMobSpawner other) {
         super(EntityTypes.bn, other.dV());
         this.provider = provider;
         Optional.ofNullable(other.e().l).flatMap(sd -> sd.d().a("id", EntityTypes.a)).ifPresent(type -> this.e().a(type, other.dV(), other.dY(), this.dv()));
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartTNT extends EntityMinecartTNT implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartTNT(VehicleMotionReducer provider, EntityMinecartTNT mt) {
         super(EntityTypes.by, mt.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class ORaft extends Raft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      ORaft(VehicleMotionReducer provider, Raft r) {
         super(EntityTypes.k, r.dV(), () -> r.dI().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bG() {
         return this.provider.getModule().isBoat_pushable();
      }
   }
}
