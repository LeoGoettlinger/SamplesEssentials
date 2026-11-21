package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.CrashFix;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EliteExplosionEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final EliteEntity eliteEntity;
   private final List<BlockState> blockStates;
   private final PowersConfigFields powersConfigFields;
   private Location explosionSourceLocation;
   private boolean isCancelled = false;

   public EliteExplosionEvent(EliteEntity eliteEntity, PowersConfigFields powersConfigFields, Location explosionSourceLocation, List<BlockState> blockStates) {
      this.eliteEntity = eliteEntity;
      this.explosionSourceLocation = explosionSourceLocation;
      this.powersConfigFields = powersConfigFields;
      this.blockStates = blockStates;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public void setExplosionSourceLocation(Location explosionSourceLocation) {
      this.explosionSourceLocation = explosionSourceLocation;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean b) {
      this.isCancelled = b;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public void visualExplosionEffect(PowersConfigFields powersConfigFields) {
      EliteExplosionEvent.VisualExplosionEffectType visualExplosionEffectType;
      if (powersConfigFields == null) {
         visualExplosionEffectType = EliteExplosionEvent.VisualExplosionEffectType.NORMAL;
      } else if (powersConfigFields.getFilename().equals("ender_dragon_empowered_lightning.yml")) {
         visualExplosionEffectType = EliteExplosionEvent.VisualExplosionEffectType.ASCEND;
      } else if (powersConfigFields.getFilename().equalsIgnoreCase("ender_dragon_tornado.yml")) {
         visualExplosionEffectType = EliteExplosionEvent.VisualExplosionEffectType.HIGH_POWER;
      } else {
         visualExplosionEffectType = EliteExplosionEvent.VisualExplosionEffectType.NORMAL;
      }

      for (BlockState blockState : this.blockStates) {
         final FallingBlock fallingBlock = blockState.getBlock()
            .getWorld()
            .spawnFallingBlock(blockState.getLocation().add(new Vector(0.0, 0.2, 0.0)), blockState.getBlockData());
         fallingBlock.setDropItem(false);
         fallingBlock.setHurtEntities(true);
         switch (visualExplosionEffectType) {
            case NORMAL:
            default:
               fallingBlock.setVelocity(fallingBlock.getLocation().clone().subtract(this.explosionSourceLocation).toVector().normalize().setY(1).multiply(0.5));
               break;
            case HIGH_POWER:
               fallingBlock.setVelocity(fallingBlock.getLocation().clone().subtract(this.explosionSourceLocation).toVector().normalize().setY(2).multiply(0.9));
               break;
            case ASCEND:
               fallingBlock.setVelocity(
                  fallingBlock.getLocation()
                     .clone()
                     .subtract(this.explosionSourceLocation)
                     .toVector()
                     .normalize()
                     .setY(1)
                     .normalize()
                     .multiply(ThreadLocalRandom.current().nextDouble())
               );
               fallingBlock.setGravity(false);
               fallingBlock.setGlowing(true);
         }

         (new BukkitRunnable() {
            public void run() {
               if (fallingBlock.isValid()) {
                  fallingBlock.remove();
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, 80L);
         CrashFix.registerVisualFallingBlock(fallingBlock);
      }
   }

   @Generated
   public EliteEntity getEliteEntity() {
      return this.eliteEntity;
   }

   @Generated
   public List<BlockState> getBlockStates() {
      return this.blockStates;
   }

   @Generated
   public PowersConfigFields getPowersConfigFields() {
      return this.powersConfigFields;
   }

   @Generated
   public Location getExplosionSourceLocation() {
      return this.explosionSourceLocation;
   }

   public static class EliteExplosionEvents implements Listener {
      @EventHandler(
         ignoreCancelled = true
      )
      public void onBlockDrop(EntityChangeBlockEvent event) {
         if (event.getEntity().getType().equals(EntityType.FALLING_BLOCK)) {
            if (CrashFix.isVisualFallingBlock(event.getEntity())) {
               event.setCancelled(true);
               event.getEntity().remove();
            }
         }
      }
   }

   public static enum VisualExplosionEffectType {
      NORMAL,
      HIGH_POWER,
      ASCEND;
   }
}
