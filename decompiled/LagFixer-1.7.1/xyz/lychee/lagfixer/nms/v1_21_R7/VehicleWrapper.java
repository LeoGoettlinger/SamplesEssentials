package xyz.lychee.lagfixer.nms.v1_21_R7;

import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.EntityBoat;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.entity.vehicle.minecart.EntityMinecartChest;
import net.minecraft.world.entity.vehicle.minecart.EntityMinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.EntityMinecartHopper;
import net.minecraft.world.entity.vehicle.minecart.EntityMinecartMobSpawner;
import net.minecraft.world.entity.vehicle.minecart.EntityMinecartRideable;
import net.minecraft.world.entity.vehicle.minecart.EntityMinecartTNT;
import org.jetbrains.annotations.NotNull;

public interface VehicleWrapper {
   public static class OBoat extends EntityBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OBoat(VehicleMotionReducer provider, EntityBoat b) {
         super(b.ay(), b.ao(), () -> b.dZ().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestBoat extends ChestBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestBoat(VehicleMotionReducer provider, ChestBoat cb) {
         super(cb.ay(), cb.ao(), () -> cb.dZ().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestRaft extends ChestRaft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestRaft(VehicleMotionReducer provider, ChestRaft cr) {
         super(EntityTypes.k, cr.ao(), () -> cr.dZ().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OMinecart extends EntityMinecartRideable implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecart(VehicleMotionReducer provider, EntityMinecartRideable m) {
         super(EntityTypes.aJ, m.ao());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartChest extends EntityMinecartChest implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartChest(VehicleMotionReducer provider, EntityMinecartChest mc) {
         super(EntityTypes.B, mc.ao());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartFurnace extends EntityMinecartFurnace implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartFurnace(VehicleMotionReducer provider, EntityMinecartFurnace mf) {
         super(EntityTypes.ag, mf.ao());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartHopper extends EntityMinecartHopper implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartHopper(VehicleMotionReducer provider, EntityMinecartHopper mh) {
         super(EntityTypes.ap, mh.ao());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartSpawner extends EntityMinecartMobSpawner implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartSpawner(VehicleMotionReducer provider, EntityMinecartMobSpawner other) {
         super(EntityTypes.bu, other.ao());
         this.provider = provider;
         Optional.ofNullable(other.e().m).flatMap(sd -> sd.d().a("id", EntityTypes.a)).ifPresent(type -> this.e().a(type, other.ao(), other.ep(), this.dK()));
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartTNT extends EntityMinecartTNT implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartTNT(VehicleMotionReducer provider, EntityMinecartTNT mt) {
         super(EntityTypes.bF, mt.ao());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class ORaft extends Raft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      ORaft(VehicleMotionReducer provider, Raft r) {
         super(EntityTypes.l, r.ao(), () -> r.dZ().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bY() {
         return this.provider.getModule().isBoat_pushable();
      }
   }
}
