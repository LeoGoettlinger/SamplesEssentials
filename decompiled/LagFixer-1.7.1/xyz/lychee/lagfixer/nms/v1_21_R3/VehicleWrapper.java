package xyz.lychee.lagfixer.nms.v1_21_R3;

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
         super(b.aq(), b.dV(), () -> b.dI().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestBoat extends ChestBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestBoat(VehicleMotionReducer provider, ChestBoat cb) {
         super(cb.aq(), cb.dV(), () -> cb.dI().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestRaft extends ChestRaft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestRaft(VehicleMotionReducer provider, ChestRaft cr) {
         super(EntityTypes.i, cr.dV(), () -> cr.dI().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OMinecart extends EntityMinecartRideable implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecart(VehicleMotionReducer provider, EntityMinecartRideable m) {
         super(EntityTypes.aD, m.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartChest extends EntityMinecartChest implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartChest(VehicleMotionReducer provider, EntityMinecartChest mc) {
         super(EntityTypes.y, mc.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartFurnace extends EntityMinecartFurnace implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartFurnace(VehicleMotionReducer provider, EntityMinecartFurnace mf) {
         super(EntityTypes.ac, mf.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartHopper extends EntityMinecartHopper implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartHopper(VehicleMotionReducer provider, EntityMinecartHopper mh) {
         super(EntityTypes.ak, mh.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartSpawner extends EntityMinecartMobSpawner implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartSpawner(VehicleMotionReducer provider, EntityMinecartMobSpawner other) {
         super(EntityTypes.bl, other.dV());
         this.provider = provider;
         Optional.ofNullable(other.v().f).flatMap(sd -> EntityTypes.a(sd.d())).ifPresent(type -> this.v().a(type, other.dV(), other.dY(), this.dv()));
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartTNT extends EntityMinecartTNT implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartTNT(VehicleMotionReducer provider, EntityMinecartTNT mt) {
         super(EntityTypes.bw, mt.dV());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class ORaft extends Raft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      ORaft(VehicleMotionReducer provider, Raft r) {
         super(EntityTypes.j, r.dV(), () -> r.dI().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bN() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bI() {
         return this.provider.getModule().isBoat_pushable();
      }
   }
}
