package xyz.lychee.lagfixer.nms.v1_21_R5;

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
         super(b.ap(), b.ai(), () -> b.dM().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestBoat extends ChestBoat implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestBoat(VehicleMotionReducer provider, ChestBoat cb) {
         super(cb.ap(), cb.ai(), () -> cb.dM().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OChestRaft extends ChestRaft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OChestRaft(VehicleMotionReducer provider, ChestRaft cr) {
         super(EntityTypes.j, cr.ai(), () -> cr.dM().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OMinecart extends EntityMinecartRideable implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecart(VehicleMotionReducer provider, EntityMinecartRideable m) {
         super(EntityTypes.aF, m.ai());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartChest extends EntityMinecartChest implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartChest(VehicleMotionReducer provider, EntityMinecartChest mc) {
         super(EntityTypes.z, mc.ai());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartFurnace extends EntityMinecartFurnace implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartFurnace(VehicleMotionReducer provider, EntityMinecartFurnace mf) {
         super(EntityTypes.ad, mf.ai());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartHopper extends EntityMinecartHopper implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartHopper(VehicleMotionReducer provider, EntityMinecartHopper mh) {
         super(EntityTypes.am, mh.ai());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartSpawner extends EntityMinecartMobSpawner implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartSpawner(VehicleMotionReducer provider, EntityMinecartMobSpawner other) {
         super(EntityTypes.bo, other.ai());
         this.provider = provider;
         Optional.ofNullable(other.e().m).flatMap(sd -> sd.d().a("id", EntityTypes.a)).ifPresent(type -> this.e().a(type, other.ai(), other.ec(), this.dx()));
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartTNT extends EntityMinecartTNT implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartTNT(VehicleMotionReducer provider, EntityMinecartTNT mt) {
         super(EntityTypes.bz, mt.ai());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class ORaft extends Raft implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      ORaft(VehicleMotionReducer provider, Raft r) {
         super(EntityTypes.k, r.ai(), () -> r.dM().h());
         this.provider = provider;
      }

      public boolean i(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean j(Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bL() {
         return this.provider.getModule().isBoat_pushable();
      }
   }
}
