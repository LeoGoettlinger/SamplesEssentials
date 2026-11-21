package dev.cwhead.GravesX.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.compatibility.CompatibilitySoundEnum;
import dev.cwhead.GravesX.event.GraveExplodeEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockEntityExplodeListener implements Listener {
   private final Graves plugin;
   private long lastExplosionTime = 0L;

   public BlockEntityExplodeListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockExplode(BlockExplodeEvent event) {
      if (this.isWithinGlobalCooldown()) {
         this.plugin.debugMessage("[Explode Debug] BlockExplodeEvent skipped due to 20-tick global cooldown", 2);
      } else {
         this.plugin.debugMessage("[Explode Debug] BlockExplodeEvent fired, affected blocks: " + event.blockList().size(), 2);
         this.handleExplosion("BlockExplodeEvent", event.blockList(), null);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      if (this.isWithinGlobalCooldown()) {
         this.plugin.debugMessage("[Explode Debug] EntityExplodeEvent skipped due to 20-tick global cooldown", 2);
      } else {
         Entity entity = event.getEntity();
         this.plugin.debugMessage("[Explode Debug] EntityExplodeEvent fired by " + entity.getType() + ", affected blocks: " + event.blockList().size(), 2);
         this.handleExplosion("EntityExplodeEvent", event.blockList(), entity);
      }
   }

   private boolean isWithinGlobalCooldown() {
      long now = System.currentTimeMillis();
      if (now - this.lastExplosionTime < 1000L) {
         return true;
      } else {
         this.lastExplosionTime = now;
         return false;
      }
   }

   private void handleExplosion(String source, List<Block> affectedBlocks, Entity explodingEntity) {
      Iterator<Block> iterator = affectedBlocks.iterator();

      while (iterator.hasNext()) {
         Block block = iterator.next();
         Grave grave = this.plugin.getBlockManager().getGraveFromBlock(block);
         if (grave != null) {
            Location graveHeadLocation = grave.getLocationDeath();
            if (graveHeadLocation != null && graveHeadLocation.equals(block.getLocation()) && !this.shouldExplode(grave)) {
               this.plugin.debugMessage("[Explode Debug] " + source + ": protecting grave head block for grave " + grave.getUUID(), 2);
               iterator.remove();
            } else {
               this.plugin.debugMessage("[Explode Debug] " + source + ": processing explosion for grave " + grave.getUUID() + " at " + block.getLocation(), 2);
               this.handleGraveExplosion(iterator, block, grave, block.getLocation(), explodingEntity);
            }
         }
      }
   }

   private boolean shouldExplode(Grave grave) {
      return this.plugin.getConfigManager().getConfigSection("grave.explode", grave).getBoolean("grave.explode");
   }

   private void handleGraveExplosion(Iterator<Block> iterator, Block block, Grave grave, Location location, Entity explodingEntity) {
      GraveExplodeEvent modern = new GraveExplodeEvent(location, explodingEntity, grave);
      this.plugin.getServer().getPluginManager().callEvent(modern);
      com.ranull.graves.event.GraveExplodeEvent legacy = new com.ranull.graves.event.GraveExplodeEvent(location, explodingEntity, grave);
      this.plugin.getServer().getPluginManager().callEvent(legacy);
      if (!modern.isCancelled() && !modern.isAddon() && !legacy.isCancelled() && !legacy.isAddon()) {
         Location effectiveLoc = location;

         try {
            if (modern.hasLocation()) {
               effectiveLoc = modern.getLocation();
            } else {
               legacy.getLocation();
               effectiveLoc = legacy.getLocation();
            }
         } catch (Throwable var13) {
         }

         if (this.plugin.getConfigManager().getConfigSection("drop.looted-explosion-effect", grave).getBoolean("drop.looted-explosion-effect", false)) {
            try {
               Location deathLoc = grave.getLocationDeath();
               Objects.requireNonNull(deathLoc.getWorld()).spawnParticle(this.plugin.getVersionManager().getParticleForVersion("EXPLOSION"), deathLoc, 1);

               try {
                  deathLoc.getWorld().playSound(deathLoc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("ENTITY_GENERIC_EXPLODE")), 1.0F, 1.0F);
               } catch (Exception var11) {
                  deathLoc.getWorld().playSound(deathLoc, Objects.requireNonNull(CompatibilitySoundEnum.valueOf("EXPLODE")), 1.0F, 1.0F);
               }
            } catch (Exception var12) {
            }
         }

         if (this.plugin.getConfigManager().getConfigSection("drop.explode", grave).getBoolean("drop.explode")) {
            this.plugin.getGraveManager().breakGrave(effectiveLoc, grave);
         } else {
            this.plugin.getGraveManager().removeGrave(grave);
         }

         this.plugin.getGraveManager().closeGrave(grave);
         this.plugin.getGraveManager().playEffect("effect.loot", effectiveLoc, grave);
         String sourceName = explodingEntity != null ? explodingEntity.getType().name() : block.getType().name();
         this.plugin.getEntityManager().runCommands("event.command.explode", sourceName, effectiveLoc, grave);
         if (this.plugin.getConfigManager().getConfigSection("zombie.explode", grave).getBoolean("zombie.explode")) {
            this.plugin.getEntityManager().spawnZombie(effectiveLoc, grave);
         }
      } else {
         this.plugin.debugMessage("[Explode Debug] GraveExplodeEvent cancelled for grave " + grave.getUUID(), 2);
         iterator.remove();
      }
   }
}
