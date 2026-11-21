package xyz.lychee.lagfixer.modules;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.objects.ISupportNms;

public class LagShieldModule extends AbstractModule implements Runnable, Listener {
   private final TreeMap<Double, Integer> dynamic_view_distance_tps = new TreeMap<>();
   private final TreeMap<Double, Integer> dynamic_simulation_distance_tps = new TreeMap<>();
   private final TreeMap<Double, Integer> dynamic_tick_speed_tps = new TreeMap<>();
   private int locks = 0;
   private BukkitTask task;
   private double entitySpawn_tps;
   private double tickHopper_tps;
   private double redstone_tps;
   private double projectiles_tps;
   private double leavesDecay_tps;
   private double mobAi_tps;
   private double liquidFlow_tps;
   private double explosions_tps;
   private double fireworks_tps;
   private boolean entitySpawn;
   private boolean tickHopper;
   private boolean redstone;
   private boolean projectiles;
   private boolean leavesDecay;
   private boolean mobAi;
   private boolean liquidFlow;
   private boolean explosions;
   private boolean fireworks;
   private boolean dynamic_view_distance;
   private boolean dynamic_simulation_distance;
   private boolean dynamic_tick_speed;

   public LagShieldModule(LagFixer plugin, ModuleManager manager) {
      super(
         plugin,
         manager,
         AbstractModule.Impact.HIGH,
         "LagShield",
         new String[]{
            "Monitors server load and adjusts settings during latency spikes.",
            "Addresses fluctuations in server performance to mitigate delays and lag.",
            "Dynamically adjusts settings, disables unnecessary features, and optimizes resources.",
            "Ensures smooth gameplay by minimizing the impact of performance fluctuations."
         },
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZjY2ZlNTA5NmEzMzViOWFiNzhhYjRmNzc4YWU0OTlmNGNjYWI0ZTJjOTVmYTM0OTIyN2ZkMDYwNzU5YmFhZiJ9fX0="
      );
   }

   @Override
   public void run() {
      SupportManager support = SupportManager.getInstance();
      ISupportNms nms = support.getNms();
      double tps = support.getResourceMonitor().getTps();
      boolean oldMobAi = this.mobAi;
      this.entitySpawn = tps < this.entitySpawn_tps;
      this.tickHopper = tps < this.tickHopper_tps;
      this.redstone = tps < this.redstone_tps;
      this.projectiles = tps < this.projectiles_tps;
      this.leavesDecay = tps < this.leavesDecay_tps;
      this.mobAi = tps < this.mobAi_tps;
      this.liquidFlow = tps < this.liquidFlow_tps;
      this.explosions = tps < this.explosions_tps;
      this.fireworks = tps < this.fireworks_tps;
      if (this.mobAi) {
         for (World w : this.getAllowedWorlds()) {
            for (LivingEntity le : w.getLivingEntities()) {
               nms.setEntityAi(le, false);
            }
         }
      } else if (oldMobAi) {
         for (World w : this.getAllowedWorlds()) {
            for (LivingEntity le : w.getLivingEntities()) {
               nms.setEntityAi(le, true);
            }
         }
      }

      if (this.dynamic_view_distance) {
         Integer viewDistance = this.getThreshold(this.dynamic_view_distance_tps, tps);
         if (viewDistance != null) {
            for (World w : this.getAllowedWorlds()) {
               nms.setViewDistance(w, viewDistance);
            }
         }
      }

      if (this.dynamic_simulation_distance) {
         Integer simulationDistance = this.getThreshold(this.dynamic_simulation_distance_tps, tps);
         if (simulationDistance != null) {
            for (World w : this.getAllowedWorlds()) {
               nms.setSimulationDistance(w, simulationDistance);
            }
         }
      }

      if (this.dynamic_tick_speed) {
         Integer tickSpeed = this.getThreshold(this.dynamic_tick_speed_tps, tps);
         if (tickSpeed != null) {
            for (World w : this.getAllowedWorlds()) {
               w.setGameRule(GameRule.RANDOM_TICK_SPEED, tickSpeed);
            }
         }
      }
   }

   private Integer getThreshold(TreeMap<Double, Integer> map, double tps) {
      if (map.isEmpty()) {
         return null;
      } else {
         Entry<Double, Integer> entry = map.ceilingEntry(tps);
         if (entry != null) {
            return entry.getValue();
         } else {
            entry = map.lastEntry();
            return entry != null ? entry.getValue() : null;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onRedstone(BlockRedstoneEvent e) {
      if (e.getNewCurrent() != 0 && this.redstone && this.canContinue(e.getBlock().getWorld())) {
         e.setNewCurrent(0);
         this.locks++;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSpawn(VehicleCreateEvent e) {
      if (this.entitySpawn && this.canContinue(e.getVehicle().getWorld())) {
         e.setCancelled(true);
         this.locks++;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSpawn(CreatureSpawnEvent e) {
      if (this.entitySpawn && this.canContinue(e.getEntity().getWorld())) {
         e.setCancelled(true);
         this.locks++;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onLaunch(ProjectileLaunchEvent e) {
      if (this.projectiles && this.canContinue(e.getEntity().getWorld())) {
         e.setCancelled(true);
         this.locks++;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onHopper(InventoryMoveItemEvent e) {
      if (e.getSource().getType() == InventoryType.HOPPER && this.tickHopper) {
         Location loc = e.getSource().getLocation();
         if (loc != null && !this.canContinue(loc.getWorld())) {
            return;
         }

         e.setCancelled(true);
         this.locks++;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onDecay(LeavesDecayEvent e) {
      if (this.leavesDecay && this.canContinue(e.getBlock().getWorld())) {
         e.setCancelled(true);
         this.locks++;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockFromTo(BlockFromToEvent e) {
      if (this.liquidFlow && e.getBlock().isLiquid() && this.canContinue(e.getBlock().getWorld())) {
         e.setCancelled(true);
         this.locks++;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onExplosion(BlockExplodeEvent e) {
      if (this.explosions && this.canContinue(e.getBlock().getWorld())) {
         e.setCancelled(true);
         this.locks++;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onFirework(FireworkExplodeEvent e) {
      if (this.fireworks && this.canContinue(e.getEntity().getWorld())) {
         e.setCancelled(true);
         this.locks++;
      }
   }

   public void loadThreshold(Map<Double, Integer> map, String key) {
      map.clear();

      for (String threshold : this.getSection().getStringList(key)) {
         try {
            String[] split = threshold.split(":");
            map.put(Double.parseDouble(split[0]), Integer.parseInt(split[1]));
         } catch (Exception var6) {
         }
      }
   }

   @Override
   public void load() throws Exception {
      Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
      this.task = SupportManager.getInstance().getFork().runTimer(false, this, 1L, 1L, TimeUnit.MINUTES);
   }

   @Override
   public boolean loadConfig() {
      this.entitySpawn_tps = this.getSection().getDouble("tps_threshold.entity_spawn");
      this.tickHopper_tps = this.getSection().getDouble("tps_threshold.tick_hopper");
      this.redstone_tps = this.getSection().getDouble("tps_threshold.redstone");
      this.projectiles_tps = this.getSection().getDouble("tps_threshold.projectiles");
      this.leavesDecay_tps = this.getSection().getDouble("tps_threshold.leaves_decay");
      this.mobAi_tps = this.getSection().getDouble("tps_threshold.mobai");
      this.liquidFlow_tps = this.getSection().getDouble("tps_threshold.liquid_flow");
      this.explosions_tps = this.getSection().getDouble("tps_threshold.explosions");
      this.fireworks_tps = this.getSection().getDouble("tps_threshold.fireworks");
      this.dynamic_view_distance = this.getSection().getBoolean("dynamic_view_distance.enabled");
      if (this.dynamic_view_distance) {
         this.loadThreshold(this.dynamic_view_distance_tps, "dynamic_view_distance.tps_thresholds");
      }

      this.dynamic_simulation_distance = this.getSection().getBoolean("dynamic_simulation_distance.enabled");
      if (this.dynamic_simulation_distance) {
         this.loadThreshold(this.dynamic_simulation_distance_tps, "dynamic_simulation_distance.tps_thresholds");
      }

      this.dynamic_tick_speed = this.getSection().getBoolean("dynamic_tick_speed.enabled");
      if (this.dynamic_tick_speed) {
         this.loadThreshold(this.dynamic_tick_speed_tps, "dynamic_tick_speed.tps_thresholds");
      }

      return true;
   }

   @Override
   public void disable() {
      HandlerList.unregisterAll(this);
      if (this.task != null) {
         this.task.cancel();
      }
   }

   @Generated
   public TreeMap<Double, Integer> getDynamic_view_distance_tps() {
      return this.dynamic_view_distance_tps;
   }

   @Generated
   public TreeMap<Double, Integer> getDynamic_simulation_distance_tps() {
      return this.dynamic_simulation_distance_tps;
   }

   @Generated
   public TreeMap<Double, Integer> getDynamic_tick_speed_tps() {
      return this.dynamic_tick_speed_tps;
   }

   @Generated
   public int getLocks() {
      return this.locks;
   }

   @Generated
   public BukkitTask getTask() {
      return this.task;
   }

   @Generated
   public double getEntitySpawn_tps() {
      return this.entitySpawn_tps;
   }

   @Generated
   public double getTickHopper_tps() {
      return this.tickHopper_tps;
   }

   @Generated
   public double getRedstone_tps() {
      return this.redstone_tps;
   }

   @Generated
   public double getProjectiles_tps() {
      return this.projectiles_tps;
   }

   @Generated
   public double getLeavesDecay_tps() {
      return this.leavesDecay_tps;
   }

   @Generated
   public double getMobAi_tps() {
      return this.mobAi_tps;
   }

   @Generated
   public double getLiquidFlow_tps() {
      return this.liquidFlow_tps;
   }

   @Generated
   public double getExplosions_tps() {
      return this.explosions_tps;
   }

   @Generated
   public double getFireworks_tps() {
      return this.fireworks_tps;
   }

   @Generated
   public boolean isEntitySpawn() {
      return this.entitySpawn;
   }

   @Generated
   public boolean isTickHopper() {
      return this.tickHopper;
   }

   @Generated
   public boolean isRedstone() {
      return this.redstone;
   }

   @Generated
   public boolean isProjectiles() {
      return this.projectiles;
   }

   @Generated
   public boolean isLeavesDecay() {
      return this.leavesDecay;
   }

   @Generated
   public boolean isMobAi() {
      return this.mobAi;
   }

   @Generated
   public boolean isLiquidFlow() {
      return this.liquidFlow;
   }

   @Generated
   public boolean isExplosions() {
      return this.explosions;
   }

   @Generated
   public boolean isFireworks() {
      return this.fireworks;
   }

   @Generated
   public boolean isDynamic_view_distance() {
      return this.dynamic_view_distance;
   }

   @Generated
   public boolean isDynamic_simulation_distance() {
      return this.dynamic_simulation_distance;
   }

   @Generated
   public boolean isDynamic_tick_speed() {
      return this.dynamic_tick_speed;
   }
}
