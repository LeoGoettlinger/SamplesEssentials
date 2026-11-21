package github.nighter.smartspawner.spawner.interactions.place;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.events.SpawnerPlaceEvent;
import github.nighter.smartspawner.api.events.SpawnerStackEvent;
import github.nighter.smartspawner.config.Config;
import github.nighter.smartspawner.extras.HopperService;
import github.nighter.smartspawner.hooks.protections.CheckStackBlock;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.spawner.config.SpawnerDisplayConfigurator;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.utils.SpawnerTypeChecker;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerPlaceListener implements Listener {
   private static final double PARTICLE_OFFSET = 0.5;
   private static final long PLACEMENT_COOLDOWN_MS = 100L;
   private final SmartSpawner plugin;
   private final MessageService messageService;
   private final SpawnerManager spawnerManager;
   private final Map<UUID, Long> lastPlacementTime = new ConcurrentHashMap<>();

   public SpawnerPlaceListener(SmartSpawner plugin) {
      this.plugin = plugin;
      this.messageService = plugin.getMessageService();
      this.spawnerManager = plugin.getSpawnerManager();
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Block block = event.getBlock();
      if (block.getType() == Material.SPAWNER) {
         Player player = event.getPlayer();
         ItemStack item = event.getItemInHand();
         ItemMeta meta = item.getItemMeta();
         if (!this.checkPlacementCooldown(player)) {
            event.setCancelled(true);
         } else if (meta instanceof BlockStateMeta blockMeta) {
            if (!CheckStackBlock.CanPlayerPlaceBlock(player, block.getLocation())) {
               event.setCancelled(true);
            } else {
               boolean isVanillaSpawner = SpawnerTypeChecker.isVanillaSpawner(item);
               if (!this.verifyPlayerInventory(player, item, isVanillaSpawner)) {
                  event.setCancelled(true);
               } else {
                  int stackSize = this.calculateStackSize(player, item, isVanillaSpawner);
                  EntityType storedEntityType = null;
                  Material itemSpawnerMaterial = null;
                  String configName = SpawnerTypeChecker.getConfigName(item);
                  if (blockMeta.hasBlockState() && blockMeta.getBlockState() instanceof CreatureSpawner) {
                     storedEntityType = ((CreatureSpawner)blockMeta.getBlockState()).getSpawnedType();
                     if (storedEntityType == EntityType.ITEM
                        && meta.getPersistentDataContainer().has(new NamespacedKey(this.plugin, "item_spawner_material"), PersistentDataType.STRING)) {
                        String materialName = (String)meta.getPersistentDataContainer()
                           .get(new NamespacedKey(this.plugin, "item_spawner_material"), PersistentDataType.STRING);
                        if (materialName != null) {
                           try {
                              itemSpawnerMaterial = Material.valueOf(materialName);
                           } catch (IllegalArgumentException var14) {
                              this.plugin.getLogger().warning("Invalid item spawner material: " + materialName);
                           }
                        }
                     }
                  }

                  if (SpawnerPlaceEvent.getHandlerList().getRegisteredListeners().length != 0) {
                     SpawnerPlaceEvent e = new SpawnerPlaceEvent(player, block.getLocation(), storedEntityType, stackSize);
                     Bukkit.getPluginManager().callEvent(e);
                     if (e.isCancelled()) {
                        event.setCancelled(true);
                        return;
                     }
                  }

                  if (stackSize > 1 && SpawnerStackEvent.getHandlerList().getRegisteredListeners().length != 0) {
                     SpawnerStackEvent stackEvent = new SpawnerStackEvent(
                        player, block.getLocation(), 1, stackSize, SpawnerStackEvent.StackSource.BLOCK_PLACE, storedEntityType
                     );
                     Bukkit.getPluginManager().callEvent(stackEvent);
                     if (stackEvent.isCancelled()) {
                        stackSize = 1;
                     }
                  }

                  if (!this.immediatelyConsumeItems(player, item, stackSize)) {
                     event.setCancelled(true);
                  } else {
                     this.handleSpawnerSetup(block, player, storedEntityType, isVanillaSpawner, stackSize, itemSpawnerMaterial, configName);
                  }
               }
            }
         } else {
            event.setCancelled(true);
         }
      }
   }

   private boolean checkPlacementCooldown(Player player) {
      long currentTime = System.currentTimeMillis();
      Long lastTime = this.lastPlacementTime.get(player.getUniqueId());
      if (lastTime != null && currentTime - lastTime < 100L) {
         return false;
      } else {
         this.lastPlacementTime.put(player.getUniqueId(), currentTime);
         return true;
      }
   }

   private boolean verifyPlayerInventory(Player player, ItemStack item, boolean isVanillaSpawner) {
      if (player.getGameMode() == GameMode.CREATIVE) {
         return true;
      } else if (isVanillaSpawner) {
         return item.getAmount() >= 1;
      } else if (player.isSneaking()) {
         int requiredAmount = item.getAmount();
         int totalItems = 0;

         for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.isSimilar(item)) {
               totalItems += invItem.getAmount();
            }
         }

         return totalItems >= requiredAmount;
      } else {
         return item.getAmount() >= 1;
      }
   }

   private boolean immediatelyConsumeItems(Player player, ItemStack item, int stackSize) {
      if (player.getGameMode() == GameMode.CREATIVE) {
         return true;
      } else if (stackSize <= 1) {
         return true;
      } else {
         ItemStack[] contents = player.getInventory().getContents();
         int remainingToConsume = stackSize;

         for (int i = 0; i < contents.length && remainingToConsume > 0; i++) {
            ItemStack slot = contents[i];
            if (slot != null && slot.isSimilar(item)) {
               int amountInSlot = slot.getAmount();
               int toRemove = Math.min(remainingToConsume, amountInSlot);
               if (toRemove >= amountInSlot) {
                  contents[i] = null;
               } else {
                  slot.setAmount(amountInSlot - toRemove);
               }

               remainingToConsume -= toRemove;
            }
         }

         if (remainingToConsume > 0) {
            return false;
         } else {
            player.getInventory().setContents(contents);
            player.updateInventory();
            return true;
         }
      }
   }

   private int calculateStackSize(Player player, ItemStack item, boolean isVanillaSpawner) {
      if (isVanillaSpawner) {
         return 1;
      } else {
         return player.isSneaking() && Config.get().isSneakPlaceEnabled()
            ? Math.min(item.getAmount(), this.plugin.getConfig().getInt("spawner_properties.default.max_stack_size", 10000))
            : 1;
      }
   }

   private void handleSpawnerSetup(
      Block block, Player player, EntityType entityType, boolean isVanillaSpawner, int stackSize, Material itemSpawnerMaterial, String configName
   ) {
      if (entityType != null && entityType != EntityType.UNKNOWN) {
         CreatureSpawner spawner = (CreatureSpawner)block.getState(false);
         if (isVanillaSpawner) {
            spawner.setSpawnedType(entityType);
            spawner.update(true, false);
         } else {
            Scheduler.runLocationTaskLater(block.getLocation(), () -> {
               if (block.getType() == Material.SPAWNER) {
                  CreatureSpawner delayedSpawner = (CreatureSpawner)block.getState(false);
                  if (entityType == EntityType.ITEM && itemSpawnerMaterial != null) {
                     SpawnerDisplayConfigurator.applyItem(this.plugin, delayedSpawner, configName, itemSpawnerMaterial);
                     delayedSpawner.update(true, false);
                     this.createSmartItemSpawner(block, player, itemSpawnerMaterial, configName, stackSize);
                  } else {
                     EntityType finalEntityType = this.getEntityType(entityType, delayedSpawner);
                     SpawnerDisplayConfigurator.applyMob(this.plugin, delayedSpawner, configName, finalEntityType);
                     delayedSpawner.update(true, false);
                     this.createSmartSpawner(block, player, finalEntityType, configName, stackSize);
                  }

                  this.setupHopperIntegration(block);
               }
            }, 2L);
         }
      }
   }

   private EntityType getEntityType(EntityType storedEntityType, CreatureSpawner placedSpawner) {
      EntityType entityType = storedEntityType;
      if (storedEntityType == null || storedEntityType == EntityType.UNKNOWN) {
         entityType = placedSpawner.getSpawnedType();
         placedSpawner.setSpawnedType(entityType);
         placedSpawner.update(true, false);
      }

      return entityType;
   }

   private void createSmartSpawner(Block block, Player player, EntityType entityType, String configName, int stackSize) {
      SpawnerData existingSpawner = this.spawnerManager.getSpawnerByLocation(block.getLocation());
      if (existingSpawner == null) {
         String spawnerId = UUID.randomUUID().toString().substring(0, 8);
         if (block.getState(false) instanceof CreatureSpawner spawner) {
            SpawnerDisplayConfigurator.applyMob(this.plugin, spawner, configName, entityType);
            spawner.update(true, false);
         }

         SpawnerData spawner = new SpawnerData(spawnerId, block.getLocation(), entityType, configName, this.plugin);
         spawner.setSpawnerActive(true);
         spawner.setStackSize(stackSize);
         spawner.updateLastInteractedPlayer(player.getName());
         this.spawnerManager.addSpawner(spawnerId, spawner);
         this.spawnerManager.queueSpawnerForSaving(spawnerId);
         if (Config.get().isSpawnerActivateParticlesEnabled()) {
            this.showActivationParticles(block);
         }

         this.messageService.sendMessage(player, "spawner_activated");
      }
   }

   private void createSmartItemSpawner(Block block, Player player, Material itemMaterial, String configName, int stackSize) {
      SpawnerData existingSpawner = this.spawnerManager.getSpawnerByLocation(block.getLocation());
      if (existingSpawner == null) {
         String spawnerId = UUID.randomUUID().toString().substring(0, 8);
         if (block.getState(false) instanceof CreatureSpawner spawner) {
            SpawnerDisplayConfigurator.applyItem(this.plugin, spawner, configName, itemMaterial);
            spawner.update(true, false);
         }

         SpawnerData spawner = new SpawnerData(spawnerId, block.getLocation(), itemMaterial, configName, this.plugin);
         spawner.setSpawnerActive(true);
         spawner.setStackSize(stackSize);
         spawner.updateLastInteractedPlayer(player.getName());
         this.spawnerManager.addSpawner(spawnerId, spawner);
         this.spawnerManager.queueSpawnerForSaving(spawnerId);
         if (Config.get().isSpawnerActivateParticlesEnabled()) {
            this.showActivationParticles(block);
         }

         this.messageService.sendMessage(player, "spawner_activated");
      }
   }

   private void showActivationParticles(Block block) {
      Scheduler.runLocationTask(block.getLocation(), () -> {
         Location particleLocation = block.getLocation().clone().add(0.5, 0.5, 0.5);
         block.getWorld().spawnParticle(Particle.WITCH, particleLocation, 50, 0.5, 0.5, 0.5, 0.0);
      });
   }

   private void setupHopperIntegration(Block block) {
      HopperService currentHopperService = this.plugin.getHopperService();
      if (currentHopperService != null && this.plugin.getHopperConfig().isHopperEnabled()) {
         Block blockBelow = block.getRelative(BlockFace.DOWN);
         if (blockBelow.getType() == Material.HOPPER) {
            currentHopperService.getTracker().tryAdd(blockBelow);
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.cleanupPlayer(event.getPlayer().getUniqueId());
   }

   public void cleanupPlayer(UUID playerId) {
      this.lastPlacementTime.remove(playerId);
   }
}
