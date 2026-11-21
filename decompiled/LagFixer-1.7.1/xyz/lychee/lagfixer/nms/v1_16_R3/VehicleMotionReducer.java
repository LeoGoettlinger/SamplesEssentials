package xyz.lychee.lagfixer.nms.v1_16_R3;

import java.util.IdentityHashMap;
import java.util.function.Function;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityBoat;
import net.minecraft.server.v1_16_R3.EntityMinecartChest;
import net.minecraft.server.v1_16_R3.EntityMinecartContainer;
import net.minecraft.server.v1_16_R3.EntityMinecartFurnace;
import net.minecraft.server.v1_16_R3.EntityMinecartHopper;
import net.minecraft.server.v1_16_R3.EntityMinecartRideable;
import net.minecraft.server.v1_16_R3.EntityMinecartTNT;
import net.minecraft.server.v1_16_R3.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

public class VehicleMotionReducer extends VehicleMotionReducerModule.NMS {
   private final IdentityHashMap<Class<? extends Entity>, Function<Entity, Entity>> vehicles = new IdentityHashMap<>(10);

   public VehicleMotionReducer(VehicleMotionReducerModule module) {
      super(module);
      this.vehicles.put(EntityBoat.class, e -> new VehicleWrapper.OBoat(this, (EntityBoat)e));
      this.vehicles.put(EntityMinecartChest.class, e -> new VehicleWrapper.OMinecartChest(this, (EntityMinecartChest)e));
      this.vehicles.put(EntityMinecartHopper.class, e -> new VehicleWrapper.OMinecartHopper(this, (EntityMinecartHopper)e));
      this.vehicles.put(EntityMinecartFurnace.class, e -> new VehicleWrapper.OMinecartFurnace(this, (EntityMinecartFurnace)e));
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
   public void onLoad(ChunkLoadEvent e) {
      if (this.getModule().canContinue(e.getWorld())) {
         for (org.bukkit.entity.Entity entity : e.getChunk().getEntities()) {
            this.optimize(entity);
         }
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
            original.getWorld().addEntity(newVehicle);
            original.die();
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
      to.setPosition(from.lastX, from.lastY, from.lastZ);
      to.lastX = from.lastX;
      to.lastY = from.lastY;
      to.lastZ = from.lastZ;
      float yaw = Location.normalizeYaw(from.yaw);
      to.yaw = yaw;
      to.setHeadRotation(yaw);
   }
}
