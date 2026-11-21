package xyz.lychee.lagfixer.nms.v1_17_R1;

import java.util.IdentityHashMap;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.vehicle.EntityBoat;
import net.minecraft.world.entity.vehicle.EntityMinecartChest;
import net.minecraft.world.entity.vehicle.EntityMinecartContainer;
import net.minecraft.world.entity.vehicle.EntityMinecartFurnace;
import net.minecraft.world.entity.vehicle.EntityMinecartHopper;
import net.minecraft.world.entity.vehicle.EntityMinecartMobSpawner;
import net.minecraft.world.entity.vehicle.EntityMinecartRideable;
import net.minecraft.world.entity.vehicle.EntityMinecartTNT;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.EntitiesLoadEvent;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

public class VehicleMotionReducer extends VehicleMotionReducerModule.NMS {
   private final IdentityHashMap<Class<? extends Entity>, Function<Entity, Entity>> vehicles = new IdentityHashMap<>(10);

   public VehicleMotionReducer(VehicleMotionReducerModule module) {
      super(module);
      this.vehicles.put(EntityBoat.class, e -> new VehicleWrapper.OBoat(this, (EntityBoat)e));
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
            newVehicle.setSilent(silent);
            this.copyLocation(original, newVehicle);
            this.copyItems(original, newVehicle);
            original.t.addEntity(newVehicle);
            original.a(RemovalReason.c);
            return true;
         }
      }
   }

   private void copyItems(Entity from, Entity to) {
      if (from instanceof EntityMinecartContainer fromContainer && to instanceof EntityMinecartContainer toContainer) {
         for (int i = 0; i < fromContainer.getSize(); i++) {
            ItemStack is = fromContainer.getItem(i);
            if (!is.isEmpty()) {
               toContainer.setItem(i, is.cloneItemStack());
            }
         }

         fromContainer.clear();
      }
   }

   private void copyLocation(Entity from, Entity to) {
      to.setPosition(from.u, from.v, from.w);
      to.u = from.u;
      to.v = from.v;
      to.w = from.w;
      float yaw = Location.normalizeYaw(from.x);
      to.setYRot(yaw);
      to.x = yaw;
      to.setHeadRotation(yaw);
   }
}
