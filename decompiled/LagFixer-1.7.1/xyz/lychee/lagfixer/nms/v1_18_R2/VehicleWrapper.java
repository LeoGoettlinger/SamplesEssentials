package xyz.lychee.lagfixer.nms.v1_18_R2;

import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
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
         super(EntityTypes.i, b.s);
         this.provider = provider;
      }

      public boolean h(@NotNull Entity entity) {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bn() {
         return this.provider.getModule().isBoat_collides();
      }

      public boolean bi() {
         return this.provider.getModule().isBoat_pushable();
      }
   }

   public static class OMinecart extends EntityMinecartRideable implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecart(VehicleMotionReducer provider, EntityMinecartRideable m) {
         super(EntityTypes.Z, m.s);
         this.provider = provider;
      }

      public boolean h(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bn() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bi() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartChest extends EntityMinecartChest implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartChest(VehicleMotionReducer provider, EntityMinecartChest mc) {
         super(EntityTypes.aa, mc.s);
         this.provider = provider;
      }

      public boolean h(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bn() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bi() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartFurnace extends EntityMinecartFurnace implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartFurnace(VehicleMotionReducer provider, EntityMinecartFurnace mf) {
         super(EntityTypes.ac, mf.s);
         this.provider = provider;
      }

      public boolean h(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bn() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bi() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartHopper extends EntityMinecartHopper implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartHopper(VehicleMotionReducer provider, EntityMinecartHopper mh) {
         super(EntityTypes.ad, mh.s);
         this.provider = provider;
      }

      public boolean h(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bn() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bi() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartSpawner extends EntityMinecartMobSpawner implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartSpawner(VehicleMotionReducer provider, EntityMinecartMobSpawner other) {
         super(EntityTypes.ae, other.s);
         this.provider = provider;
         Optional.of(other.x().e).flatMap(sd -> EntityTypes.a(sd.c())).ifPresent(type -> this.x().a(type));
      }

      public boolean h(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bn() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bi() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }

   public static class OMinecartTNT extends EntityMinecartTNT implements VehicleWrapper {
      private final VehicleMotionReducer provider;

      OMinecartTNT(VehicleMotionReducer provider, EntityMinecartTNT mt) {
         super(EntityTypes.af, mt.s);
         this.provider = provider;
      }

      public boolean h(@NotNull Entity entity) {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bn() {
         return this.provider.getModule().isMinecart_collides();
      }

      public boolean bi() {
         return this.provider.getModule().isMinecart_pushable();
      }
   }
}
