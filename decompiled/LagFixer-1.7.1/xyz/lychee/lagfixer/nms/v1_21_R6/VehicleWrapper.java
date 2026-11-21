package xyz.lychee.lagfixer.nms.v1_21_R6;

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
         super(b.ax(), b.an(), () -> b.dU().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestBoat extends ChestBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestBoat(VehicleMotionReducer provider, ChestBoat cb) {
         super(cb.ax(), cb.an(), () -> cb.dU().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestRaft extends ChestRaft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestRaft(VehicleMotionReducer provider, ChestRaft cr) {
         super(EntityTypes.k, cr.an(), () -> cr.dU().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OMinecart extends EntityMinecartRideable implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecart(VehicleMotionReducer provider, EntityMinecartRideable m) {
         super(EntityTypes.aI, m.an());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartChest extends EntityMinecartChest implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartChest(VehicleMotionReducer provider, EntityMinecartChest mc) {
         super(EntityTypes.A, mc.an());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartFurnace extends EntityMinecartFurnace implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartFurnace(VehicleMotionReducer provider, EntityMinecartFurnace mf) {
         super(EntityTypes.af, mf.an());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartHopper extends EntityMinecartHopper implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartHopper(VehicleMotionReducer provider, EntityMinecartHopper mh) {
         super(EntityTypes.ao, mh.an());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartSpawner extends EntityMinecartMobSpawner implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartSpawner(VehicleMotionReducer provider, EntityMinecartMobSpawner other) {
         super(EntityTypes.br, other.an());
         this.provider = provider;
         Optional.ofNullable(other.e().m).flatMap(sd -> sd.d().a("id", EntityTypes.a)).ifPresent(type -> this.e().a(type, other.an(), other.ek(), this.dF()));
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartTNT extends EntityMinecartTNT implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartTNT(VehicleMotionReducer provider, EntityMinecartTNT mt) {
         super(EntityTypes.bC, mt.an());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class ORaft extends Raft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      ORaft(VehicleMotionReducer provider, Raft r) {
         super(EntityTypes.l, r.an(), () -> r.dU().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bU() {
         return this.provider.getModule().isBoat_pushable();
      }
   }
}
