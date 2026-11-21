package com.magmaguy.elitemobs.explosionregen;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteExplosionEvent;
import com.magmaguy.elitemobs.combatsystem.EliteProjectile;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.util.EntityMovementPath;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.EntityFinder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

public class Explosion {
   private static final HashSet<Explosion> explosions = new HashSet<>();
   private final Deque<BlockState> detonatedBlocks = new ArrayDeque<>();
   private BukkitTask task;
   private World world;
   private final int delayBeforeRegen = 1;
   private UUID worldUUID;

   public Explosion(List<BlockState> detonatedBlocks) {
      if (detonatedBlocks != null && !detonatedBlocks.isEmpty()) {
         Map<Explosion.BlockPosition, BlockState> unique = new LinkedHashMap<>();

         for (BlockState block : detonatedBlocks) {
            unique.putIfAbsent(Explosion.BlockPosition.of(block), block);
         }

         unique.values().stream().sorted(Comparator.comparingInt(BlockState::getY)).forEach(this.detonatedBlocks::addLast);
         this.world = this.detonatedBlocks.getFirst().getWorld();
         this.worldUUID = this.world.getUID();
         explosions.add(this);
         this.regenerate();
      }
   }

   public static void shutdown() {
      regenerateAllPendingBlocks();
   }

   public static void regenerateAllPendingBlocks() {
      for (Explosion explosion : List.copyOf(explosions)) {
         explosion.resetAllBlocks();
      }
   }

   public static void generateFakeExplosion(List<Block> blockList, Entity entity, PowersConfigFields powersConfigFields, Location explosionSourceLocation) {
      generateExplosion(blockList, entity, powersConfigFields, explosionSourceLocation);
   }

   public static void generateFakeExplosion(List<Block> blockList, Entity entity) {
      generateExplosion(blockList, entity, null, null);
   }

   private static void generateExplosion(EntityExplodeEvent event) {
      Explosion.Generation result = generateExplosion(event.blockList(), event.getEntity(), null, event.getEntity().getLocation());
      if (result == Explosion.Generation.CANCELLED) {
         event.setCancelled(true);
      } else if (result == Explosion.Generation.APPLIED) {
         event.blockList().clear();
      }
   }

   private static Explosion.Generation generateExplosion(List<Block> blockList, Entity entity, PowersConfigFields powersConfigFields, Location explosionSource) {
      if (!DefaultConfig.isDoExplosionRegen()) {
         return Explosion.Generation.BYPASS;
      } else if (EliteMobs.worldGuardIsEnabled && explosionSource != null && !WorldGuardFlagChecker.doExplosionRegenFlag(explosionSource)) {
         return Explosion.Generation.BYPASS;
      } else {
         Map<Explosion.BlockPosition, BlockState> captured = new LinkedHashMap<>();
         Set<Explosion.BlockPosition> visited = new HashSet<>();

         for (Block block : blockList) {
            if (!block.getType().isAir() && !block.getType().equals(Material.FIRE) && !block.isLiquid() && !EntityTracker.isTemporaryBlock(block)) {
               nearbyBlockScan(captured, visited, block.getState());
            }
         }

         ArrayList<BlockState> blockStates = new ArrayList<>(captured.values());
         Entity shooter = EntityFinder.filterRangedDamagers(entity);
         EliteEntity eliteEntity = null;
         if (shooter != null) {
            eliteEntity = EntityTracker.getEliteMobEntity(shooter);
         }

         EliteExplosionEvent eliteExplosionEvent = null;
         if (entity instanceof Projectile) {
            eliteExplosionEvent = new EliteExplosionEvent(
               eliteEntity,
               powersConfigFields = PowersConfig.getPower(EliteProjectile.readExplosivePower((Projectile)entity)),
               entity.getLocation(),
               blockStates
            );
         } else {
            eliteExplosionEvent = new EliteExplosionEvent(eliteEntity, powersConfigFields, entity.getLocation(), blockStates);
         }

         if (explosionSource != null) {
            eliteExplosionEvent.setExplosionSourceLocation(explosionSource);
         }

         Bukkit.getPluginManager().callEvent(eliteExplosionEvent);
         if (eliteExplosionEvent.isCancelled()) {
            return Explosion.Generation.CANCELLED;
         } else {
            Map<Explosion.BlockPosition, BlockState> approved = new LinkedHashMap<>();

            for (BlockState state : blockStates) {
               if (state != null
                  && state.getWorld().equals(entity.getWorld())
                  && !EntityTracker.isTemporaryBlock(state.getBlock())
                  && (DefaultConfig.isDoRegenerateContainers() || !(state instanceof Container))) {
                  approved.putIfAbsent(Explosion.BlockPosition.of(state), state);
               }
            }

            blockStates.clear();
            blockStates.addAll(approved.values());
            eliteExplosionEvent.visualExplosionEffect(powersConfigFields);

            for (BlockState blockState : blockStates) {
               BlockState live = blockState.getBlock().getState();
               if (live instanceof Chest chest) {
                  chest.getBlockInventory().clear();
               } else if (live instanceof Container container) {
                  container.getInventory().clear();
               }

               blockState.getBlock().setType(Material.AIR);
               blockState.getBlock().getState().update(true);
            }

            new Explosion(blockStates);
            return Explosion.Generation.APPLIED;
         }
      }
   }

   private static void nearbyBlockScan(Map<Explosion.BlockPosition, BlockState> states, Set<Explosion.BlockPosition> visited, BlockState start) {
      Deque<BlockState> pending = new ArrayDeque<>();
      pending.add(start);

      while (!pending.isEmpty()) {
         BlockState state = pending.removeFirst();
         Explosion.BlockPosition key = Explosion.BlockPosition.of(state);
         if (visited.add(key)
            && !EntityTracker.isTemporaryBlock(state.getBlock())
            && (DefaultConfig.isDoRegenerateContainers() || !(state instanceof Container))) {
            states.putIfAbsent(key, state);

            for (int x = -1; x <= 1; x++) {
               for (int y = -1; y <= 1; y++) {
                  for (int z = -1; z <= 1; z++) {
                     if (x != 0 || y != 0 || z != 0) {
                        Explosion.BlockPosition neighbor = new Explosion.BlockPosition(key.world, key.x + x, key.y + y, key.z + z);
                        if (!visited.contains(neighbor)) {
                           BlockState adjacent = state.getBlock().getRelative(x, y, z).getState();
                           if (isCodependentBlock(adjacent, y)) {
                              pending.addLast(adjacent);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isCodependentBlock(BlockState blockState, int y) {
      if (y == 1) {
         switch (blockState.getType()) {
            case SUGAR_CANE:
            case STRUCTURE_BLOCK:
            case TALL_GRASS:
            case BLACK_CARPET:
            case BLUE_CARPET:
            case BROWN_CARPET:
            case CYAN_CARPET:
            case GRAY_CARPET:
            case GREEN_CARPET:
            case LIGHT_BLUE_CARPET:
            case LIGHT_GRAY_CARPET:
            case LIME_CARPET:
            case MAGENTA_CARPET:
            case ORANGE_CARPET:
            case CARVED_PUMPKIN:
            case PINK_CARPET:
            case PURPLE_CARPET:
            case RED_CARPET:
            case WHITE_CARPET:
            case YELLOW_CARPET:
            case DARK_OAK_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case CRIMSON_DOOR:
            case IRON_DOOR:
            case JUNGLE_DOOR:
            case OAK_DOOR:
            case SPRUCE_DOOR:
            case WARPED_DOOR:
            case SUNFLOWER:
            case WHEAT:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case DANDELION:
            case NETHER_WART:
            case REDSTONE:
            case COMPARATOR:
            case REPEATER:
            case TORCH:
            case REDSTONE_TORCH:
               return true;
         }
      }

      if (y == -1 && blockState.getType() == Material.VINE) {
         return true;
      } else {
         switch (blockState.getType()) {
            case PAINTING:
            case LADDER:
            case LANTERN:
            case VINE:
            case SOUL_LANTERN:
            case TRIPWIRE:
            case TRIPWIRE_HOOK:
            case REDSTONE_WALL_TORCH:
            case WALL_TORCH:
            case ACACIA_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case CRIMSON_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
            case IRON_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case OAK_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
            case WARPED_TRAPDOOR:
            case COCOA_BEANS:
               return true;
            default:
               return false;
         }
      }
   }

   public void resetAllBlocks() {
      while (!this.detonatedBlocks.isEmpty()) {
         if (!this.fullBlockRestore(this.detonatedBlocks.getFirst())) {
            return;
         }

         this.detonatedBlocks.removeFirst();
      }

      this.discard();
   }

   public void regenerate() {
      if (this.task == null && !this.detonatedBlocks.isEmpty()) {
         this.task = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
            if (Bukkit.getWorld(this.worldUUID) != this.world) {
               this.task.cancel();
               this.task = null;
               Logger.warn("Explosion recovery suspended: original world " + this.worldUUID + " is unavailable.");
            } else if (this.detonatedBlocks.isEmpty()) {
               this.discard();
            } else {
               if (this.fullBlockRestore(this.detonatedBlocks.getFirst())) {
                  this.detonatedBlocks.removeFirst();
               }

               if (this.detonatedBlocks.isEmpty()) {
                  this.discard();
               }
            }
         }, 1200L, 1L);
      }
   }

   private boolean fullBlockRestore(BlockState state) {
      World restorationWorld = this.world;
      if (restorationWorld != null && Bukkit.getWorld(this.worldUUID) == restorationWorld) {
         BoundingBox restoredVolume = new BoundingBox(state.getX(), state.getY(), state.getZ(), state.getX() + 1, state.getY() + 1, state.getZ() + 1);

         for (Entity entity : restorationWorld.getNearbyEntities(restoredVolume)) {
            Location destination = entity.getLocation().clone().add(0.0, restoredVolume.getMaxY() - entity.getBoundingBox().getMinY(), 0.0);
            if (!EntityMovementPath.isClear(entity, destination) || !entity.teleport(destination)) {
               return false;
            }
         }

         return this.world == restorationWorld
               && Bukkit.getWorld(this.worldUUID) == restorationWorld
               && restorationWorld.getNearbyEntities(restoredVolume).isEmpty()
            ? state.update(true, false)
            : false;
      } else {
         return false;
      }
   }

   private void discard() {
      if (this.task != null) {
         this.task.cancel();
      }

      this.task = null;
      this.detonatedBlocks.clear();
      explosions.remove(this);
      this.world = null;
   }

   public static void discardForWorld(UUID worldId) {
      for (Explosion explosion : List.copyOf(explosions)) {
         if (worldId.equals(explosion.worldUUID)) {
            explosion.discard();
         }
      }
   }

   private record BlockPosition(UUID world, int x, int y, int z) {
      static Explosion.BlockPosition of(BlockState state) {
         return new Explosion.BlockPosition(state.getWorld().getUID(), state.getX(), state.getY(), state.getZ());
      }
   }

   public static class ExplosionEvent implements Listener {
      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGHEST
      )
      public void onWorldUnload(WorldUnloadEvent event) {
         UUID worldId = event.getWorld().getUID();
         if (!DungeonInstance.isWorldRetiring(worldId)) {
            for (Explosion explosion : List.copyOf(Explosion.explosions)) {
               if (worldId.equals(explosion.worldUUID)) {
                  try {
                     explosion.resetAllBlocks();
                  } catch (RuntimeException var6) {
                     Logger.warn("Could not finish explosion recovery before world unload: " + var6.getMessage());
                  }

                  if (!explosion.detonatedBlocks.isEmpty()) {
                     event.setCancelled(true);
                     Logger.warn("World unload postponed until pending explosion blocks can be restored.");
                  }
               }
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGHEST
      )
      public void entityExplodeEvent(EntityExplodeEvent event) {
         Entity entity = event.getEntity();
         if (entity instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter) {
            entity = shooter;
         }

         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(entity);
         if (eliteEntity != null) {
            Explosion.generateExplosion(event);
         } else {
            if (EntityTracker.isProjectileEntity(event.getEntity())) {
               Explosion.generateExplosion(event);
            }

            if (event.getEntity().getPersistentDataContainer().has(new NamespacedKey(MetadataHandler.PLUGIN, "eliteCrystal"), PersistentDataType.STRING)) {
               Explosion.generateExplosion(event);
            }
         }
      }
   }

   private static enum Generation {
      BYPASS,
      CANCELLED,
      APPLIED;
   }
}
