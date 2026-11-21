package xyz.lychee.lagfixer.modules;

import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.PlayerInventory;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

public class VehicleMotionReducerModule extends AbstractModule implements Listener {
   private VehicleMotionReducerModule.NMS vehicleMotionReducer;
   private boolean force_load;
   private boolean minecart;
   private boolean minecart_collides;
   private boolean minecart_pushable;
   private boolean minecart_silent;
   private boolean boat;
   private boolean boat_collides;
   private boolean boat_pushable;
   private boolean boat_silent;

   public VehicleMotionReducerModule(LagFixer plugin, ModuleManager manager) {
      super(
         plugin,
         manager,
         AbstractModule.Impact.LOW,
         "VehicleMotionReducer",
         new String[]{
            "Optimizes all vehicles such as Boats and Minecarts.",
            "Removes minecarts with chests spawned in mineshafts.",
            "Particularly useful when minecarts are frequently used on the server.",
            "Enhances server performance by optimizing vehicle mechanics and removing unnecessary entities."
         },
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJjMjA1MGVjYTBlZmRkMDMxZTY1OGI5OTZjMjM5YmY3ZGEzYWVmODY1NjEyMzY3ZWQ5ZDg5NWFlN2EwZGE5ZiJ9fX0="
      );
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityPlace(EntityPlaceEvent event) {
      Entity entity = event.getEntity();
      if (this.canContinue(entity.getWorld()) && entity instanceof Vehicle vehicle) {
         Player player = event.getPlayer();
         boolean cancel = this.vehicleMotionReducer.optimize(vehicle);
         if (cancel) {
            event.setCancelled(true);
            if (player != null && player.getGameMode() != GameMode.CREATIVE) {
               PlayerInventory inventory = player.getInventory();
               String mainHand = inventory.getItemInMainHand().getType().name().toLowerCase();
               String offHand = inventory.getItemInOffHand().getType().name().toLowerCase();
               if (mainHand.contains("boat") || mainHand.contains("minecart") || mainHand.contains("raft")) {
                  inventory.setItemInMainHand(null);
               } else if (offHand.contains("boat") || offHand.contains("minecart") || offHand.contains("raft")) {
                  inventory.setItemInOffHand(null);
               }
            }
         }
      }
   }

   public boolean isEnabled(Entity ent) {
      return ent instanceof Minecart && this.minecart || ent instanceof Boat && this.boat;
   }

   @Override
   public void load() {
      Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
      Bukkit.getPluginManager().registerEvents(this.vehicleMotionReducer, this.getPlugin());
      if (this.force_load) {
         this.getAllowedWorlds()
            .forEach(
               w -> SupportManager.getInstance()
                  .getFork()
                  .runNow(
                     false,
                     new Location(w, 0.0, 100.0, 0.0),
                     () -> w.getLivingEntities().stream().filter(this::isEnabled).forEach(ent -> this.vehicleMotionReducer.optimize(ent))
                  )
            );
      }
   }

   @Override
   public boolean loadConfig() {
      this.vehicleMotionReducer = ReflectionUtils.createInstance("VehicleMotionReducer", this);
      this.force_load = this.getSection().getBoolean("force_load");
      this.minecart = this.getSection().getBoolean("minecart.enabled");
      if (this.minecart) {
         this.minecart_collides = this.getSection().getBoolean("minecart.collides");
         this.minecart_pushable = this.getSection().getBoolean("minecart.pushable");
         this.minecart_silent = this.getSection().getBoolean("minecart.silent");
      }

      this.boat = this.getSection().getBoolean("boat.enabled");
      if (this.boat) {
         this.boat_collides = this.getSection().getBoolean("boat.collides");
         this.boat_pushable = this.getSection().getBoolean("boat.pushable");
         this.boat_silent = this.getSection().getBoolean("boat.silent");
      }

      return this.vehicleMotionReducer != null;
   }

   @Override
   public void disable() {
      HandlerList.unregisterAll(this);
      if (this.vehicleMotionReducer != null) {
         HandlerList.unregisterAll(this.vehicleMotionReducer);
      }
   }

   @Generated
   public VehicleMotionReducerModule.NMS getVehicleMotionReducer() {
      return this.vehicleMotionReducer;
   }

   @Generated
   public boolean isForce_load() {
      return this.force_load;
   }

   @Generated
   public boolean isMinecart() {
      return this.minecart;
   }

   @Generated
   public boolean isMinecart_collides() {
      return this.minecart_collides;
   }

   @Generated
   public boolean isMinecart_pushable() {
      return this.minecart_pushable;
   }

   @Generated
   public boolean isMinecart_silent() {
      return this.minecart_silent;
   }

   @Generated
   public boolean isBoat() {
      return this.boat;
   }

   @Generated
   public boolean isBoat_collides() {
      return this.boat_collides;
   }

   @Generated
   public boolean isBoat_pushable() {
      return this.boat_pushable;
   }

   @Generated
   public boolean isBoat_silent() {
      return this.boat_silent;
   }

   public abstract static class NMS implements Listener {
      private final VehicleMotionReducerModule module;

      public NMS(VehicleMotionReducerModule module) {
         this.module = module;
      }

      public abstract boolean optimize(Entity var1);

      @Generated
      public VehicleMotionReducerModule getModule() {
         return this.module;
      }
   }
}
