package net.thenextlvl.protect.listeners;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public final class EntityListener implements Listener {
   private final ProtectPlugin plugin;

   public EntityListener(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player player) {
         Area var4 = this.plugin.areaProvider().getArea(player);
         if (!event.getCause().equals(DamageCause.KILL)) {
            event.setCancelled(!var4.getFlag(this.plugin.flags.damage));
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onVehicleDestroy(VehicleDestroyEvent event) {
      if (event.getAttacker() != null) {
         event.setCancelled(!this.plugin.protectionService().canAttack(event.getAttacker(), event.getVehicle()));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntitySpawn(EntitySpawnEvent event) {
      if (event.getEntity().getEntitySpawnReason().equals(SpawnReason.NATURAL)) {
         Area area = this.plugin.areaProvider().getArea(event.getEntity());
         event.setCancelled(!area.getFlag(this.plugin.flags.naturalEntitySpawn));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onFoodLevelChange(FoodLevelChangeEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getEntity());
      event.setCancelled(event.getItem() == null && !area.getFlag(this.plugin.flags.hunger));
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityTarget(EntityTargetEvent event) {
      if (event.getTarget() != null) {
         event.setCancelled(!this.plugin.protectionService().canAttack(event.getEntity(), event.getTarget()));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getEntity());
      event.setCancelled(!this.plugin.protectionService().canAttack(event.getDamager(), event.getEntity()));
      this.plugin.failed(event.getDamager(), event, area, "area.failed.attack");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityKnockback(EntityKnockbackByEntityEvent event) {
      event.setCancelled(!this.plugin.protectionService().canKnockback(event.getHitBy(), event.getEntity()));
      if (event.isCancelled()) {
         this.plugin.failed(event.getHitBy(), event, this.plugin.areaProvider().getArea(event.getEntity()), "area.failed.attack");
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onInteract(PlayerInteractEntityEvent event) {
      event.setCancelled(!this.plugin.protectionService().canInteract(event.getPlayer(), event.getRightClicked()));
      this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(event.getRightClicked()), "area.failed.interact");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerShearEntity(PlayerShearEntityEvent event) {
      event.setCancelled(!this.plugin.protectionService().canShear(event.getPlayer(), event.getEntity()));
      this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(event.getEntity()), "area.failed.interact");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onHangingBreak(HangingBreakByEntityEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getEntity());
      event.setCancelled(!this.plugin.protectionService().canAttack(event.getRemover(), event.getEntity()));
      this.plugin.failed(event.getRemover(), area, "area.failed.attack");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onHangingPlace(HangingPlaceEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getEntity());
      event.setCancelled(!this.plugin.protectionService().canPlace(event.getPlayer(), area));
      this.plugin.failed(event.getPlayer(), event, area, "area.failed.place");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityPlace(EntityPlaceEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getEntity());
      event.setCancelled(!this.plugin.protectionService().canPlace(event.getPlayer(), area));
      this.plugin.failed(event.getPlayer(), event, area, "area.failed.place");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
      event.setCancelled(!this.plugin.protectionService().canInteract(event.getPlayer(), event.getRightClicked()));
      this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(event.getRightClicked()), "area.failed.interact");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onDrop(EntityDropItemEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getEntity());
      event.setCancelled(!area.getFlag(this.plugin.flags.entityItemDrop));
      this.plugin.failed(event.getEntity(), event, area, "area.failed.drop");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onDrop(PlayerDropItemEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getPlayer());
      event.setCancelled(!area.getFlag(this.plugin.flags.playerItemDrop));
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPickup(EntityPickupItemEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getItem());
      event.setCancelled(!area.getFlag(this.plugin.flags.entityItemPickup));
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onProjectileLaunch(ProjectileLaunchEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getEntity());
      event.setCancelled(!area.getFlag(this.plugin.flags.shoot));
      this.plugin.failed(event.getEntity(), event, area, "area.failed.shoot");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityBreakDoor(EntityBreakDoorEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getEntity());
      event.setCancelled(!area.getFlag(this.plugin.flags.entityBreakDoor));
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onSheepEatGrass(EntityChangeBlockEvent event) {
      if (event.getEntityType().equals(EntityType.SHEEP)) {
         Area area = this.plugin.areaProvider().getArea(event.getEntity());
         event.setCancelled(!area.getFlag(this.plugin.flags.sheepEatGrass));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityChangeBlock(EntityChangeBlockEvent event) {
      if (event.getEntityType().equals(EntityType.FALLING_BLOCK)) {
         if (!event.getTo().isAir()) {
            Location origin = event.getEntity().getOrigin();
            Area from = origin != null ? this.plugin.areaProvider().getArea(origin) : null;
            Area area = this.plugin.areaProvider().getArea(event.getEntity());
            if (from == null || !from.canInteract(area)) {
               event.setCancelled(true);
            }
         }
      }
   }
}
