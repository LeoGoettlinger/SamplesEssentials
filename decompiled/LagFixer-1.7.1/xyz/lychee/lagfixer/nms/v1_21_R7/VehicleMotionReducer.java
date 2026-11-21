package xyz.lychee.lagfixer.nms.v1_21_R7;

import java.util.IdentityHashMap;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.vehicle.ContainerEntity;
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
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.EntitiesLoadEvent;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

public class VehicleMotionReducer extends VehicleMotionReducerModule.NMS {
   private final IdentityHashMap<Class<? extends Entity>, Function<Entity, Entity>> vehicles = new IdentityHashMap<>(10);

   public VehicleMotionReducer(VehicleMotionReducerModule module) {
      super(module);
      this.vehicles.put(Raft.class, e -> new VehicleWrapper.ORaft(this, (Raft)e));
      this.vehicles.put(ChestRaft.class, e -> new VehicleWrapper.OChestRaft(this, (ChestRaft)e));
      this.vehicles.put(EntityBoat.class, e -> new VehicleWrapper.OBoat(this, (EntityBoat)e));
      this.vehicles.put(ChestBoat.class, e -> new VehicleWrapper.OChestBoat(this, (ChestBoat)e));
      this.vehicles.put(EntityMinecartChest.class, e -> new VehicleWrapper.OMinecartChest(this, (EntityMinecartChest)e));
      this.vehicles.put(EntityMinecartHopper.class, e -> new VehicleWrapper.OMinecartHopper(this, (EntityMinecartHopper)e));
      this.vehicles.put(EntityMinecartFurnace.class, e -> new VehicleWrapper.OMinecartFurnace(this, (EntityMinecartFurnace)e));
      this.vehicles.put(EntityMinecartMobSpawner.class, e -> new VehicleWrapper.OMinecartSpawner(this, (EntityMinecartMobSpawner)e));
      this.vehicles.put(EntityMinecartTNT.class, e -> new VehicleWrapper.OMinecartTNT(this, (EntityMinecartTNT)e));
      this.vehicles.put(EntityMinecartRideable.class, e -> new VehicleWrapper.OMinecart(this, (EntityMinecartRideable)e));
   }

   @Override
   public boolean optimize(org.bukkit.entity.Entity vehicle) {
      if (vehicle instanceof CraftBoat boat) {
         return !this.getModule().isBoat() ? false : this.processEntity(boat.getHandle(), this.getModule().isBoat_silent());
      } else if (vehicle instanceof CraftMinecart minecart) {
         return !this.getModule().isMinecart() ? false : this.processEntity(minecart.getHandle(), this.getModule().isMinecart_silent());
      } else {
         return false;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onLoad(EntitiesLoadEvent e) {
      if (this.getModule().canContinue(e.getWorld())) {
         e.getEntities().forEach(this::optimize);
      }
   }

   private boolean processEntity(Entity original, boolean silent) {
      if (original instanceof VehicleWrapper) {
         return false;
      } else {
         Function<Entity, Entity> factory = this.vehicles.get(original.getClass());
         if (factory == null) {
            return false;
         } else {
            Entity newVehicle = factory.apply(original);
            newVehicle.f(silent);
            this.copyLocation(original, newVehicle);
            this.copyItems(original, newVehicle);
            original.ao().b(newVehicle);
            original.a(RemovalReason.c);
            return true;
         }
      }
   }

   private void copyItems(Entity from, Entity to) {
      if (from instanceof ContainerEntity fromContainer && to instanceof ContainerEntity toContainer) {
         for (int i = 0; i < fromContainer.b(); i++) {
            ItemStack is = fromContainer.a(i);
            if (!is.f()) {
               toContainer.a(i, is.g());
            }
         }

         fromContainer.a();
      }
   }

   private void copyLocation(Entity from, Entity to) {
      to.a_(from.Y, from.Z, from.aa);
      to.Y = from.Y;
      to.Z = from.Z;
      to.aa = from.aa;
      float yaw = Location.normalizeYaw(from.ab);
      to.v(yaw);
      to.ab = yaw;
      to.r(yaw);
   }
}
