package xyz.lychee.lagfixer.nms.v26_1;

import java.util.IdentityHashMap;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.vehicle.ContainerEntity;
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
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftBoat;
import org.bukkit.craftbukkit.entity.CraftMinecart;
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
      this.vehicles.put(Boat.class, e -> new VehicleWrapper.OBoat(this, (Boat)e));
      this.vehicles.put(ChestBoat.class, e -> new VehicleWrapper.OChestBoat(this, (ChestBoat)e));
      this.vehicles.put(MinecartChest.class, e -> new VehicleWrapper.OMinecartChest(this, (MinecartChest)e));
      this.vehicles.put(MinecartHopper.class, e -> new VehicleWrapper.OMinecartHopper(this, (MinecartHopper)e));
      this.vehicles.put(MinecartFurnace.class, e -> new VehicleWrapper.OMinecartFurnace(this, (MinecartFurnace)e));
      this.vehicles.put(MinecartSpawner.class, e -> new VehicleWrapper.OMinecartSpawner(this, (MinecartSpawner)e));
      this.vehicles.put(MinecartTNT.class, e -> new VehicleWrapper.OMinecartTNT(this, (MinecartTNT)e));
      this.vehicles.put(Minecart.class, e -> new VehicleWrapper.OMinecart(this, (Minecart)e));
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
            original.level().addFreshEntity(newVehicle);
            original.remove(RemovalReason.UNLOADED_TO_CHUNK);
            return true;
         }
      }
   }

   private void copyItems(Entity from, Entity to) {
      if (from instanceof ContainerEntity fromContainer && to instanceof ContainerEntity toContainer) {
         for (int i = 0; i < fromContainer.getContainerSize(); i++) {
            ItemStack is = fromContainer.getItem(i);
            if (!is.isEmpty()) {
               toContainer.setItem(i, is.copyAndClear());
            }
         }

         fromContainer.clearContent();
      }
   }

   private void copyLocation(Entity from, Entity to) {
      to.setPos(from.xo, from.yo, from.zo);
      to.xo = from.xo;
      to.yo = from.yo;
      to.zo = from.zo;
      float yaw = Location.normalizeYaw(from.yRotO);
      to.setYRot(yaw);
      to.yRotO = yaw;
      to.setYHeadRot(yaw);
   }
}
