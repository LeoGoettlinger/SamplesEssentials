package github.nighter.smartspawner.spawner.interactions.stack;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.events.SpawnerStackEvent;
import github.nighter.smartspawner.config.Config;
import github.nighter.smartspawner.hooks.protections.CheckStackBlock;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.spawner.config.ItemSpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.config.SpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.utils.SpawnerTypeChecker;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class SpawnerStackHandler {
   private static final long STACK_COOLDOWN = 250L;
   private final SmartSpawner plugin;
   private final MessageService messageService;
   private final SpawnerManager spawnerManager;
   private final Map<UUID, Long> lastStackTime;
   private final Map<Location, UUID> stackLocks;

   public SpawnerStackHandler(SmartSpawner plugin) {
      this.plugin = plugin;
      this.messageService = plugin.getMessageService();
      this.spawnerManager = plugin.getSpawnerManager();
      this.lastStackTime = new ConcurrentHashMap<>();
      this.stackLocks = new ConcurrentHashMap<>();
      this.startCleanupTask();
   }

   private void startCleanupTask() {
      Scheduler.runTaskTimer(() -> {
         long now = System.currentTimeMillis();
         this.lastStackTime.entrySet().removeIf(entry -> now - entry.getValue() > 10000L);
         this.stackLocks.entrySet().removeIf(entry -> this.plugin.getServer().getPlayer(entry.getValue()) == null);
      }, 200L, 200L);
   }

   public void handleSpawnerStacking(Player player, Block block, SpawnerData spawnerData, ItemStack itemInHand) {
      if (spawnerData.isSelling()) {
         this.messageService.sendMessage(player, "action_in_progress");
      } else if (!this.isOnCooldown(player)) {
         if (this.acquireStackLock(player, block.getLocation())) {
            try {
               boolean stackAll = player.isSneaking() && Config.get().isSneakStackEnabled();
               this.handleSpawnerStack(player, spawnerData, itemInHand, stackAll);
            } finally {
               this.releaseStackLock(block.getLocation());
               this.updateLastStackTime(player);
            }
         }
      }
   }

   private boolean isOnCooldown(Player player) {
      long lastTime = this.lastStackTime.getOrDefault(player.getUniqueId(), 0L);
      return System.currentTimeMillis() - lastTime < 250L;
   }

   private void updateLastStackTime(Player player) {
      this.lastStackTime.put(player.getUniqueId(), System.currentTimeMillis());
   }

   private boolean acquireStackLock(Player player, Location location) {
      return this.stackLocks.putIfAbsent(location, player.getUniqueId()) == null;
   }

   private void releaseStackLock(Location location) {
      this.stackLocks.remove(location);
   }

   public boolean handleSpawnerStack(Player player, SpawnerData targetSpawner, ItemStack itemInHand, boolean stackAll) {
      if (itemInHand.getType() != Material.SPAWNER) {
         return false;
      } else {
         Location location = targetSpawner.getSpawnerLocation();
         if (!this.hasStackPermissions(player, location)) {
            return false;
         } else if (SpawnerTypeChecker.isVanillaSpawner(itemInHand)) {
            this.messageService.sendMessage(player, "spawner_different");
            return false;
         } else {
            boolean isItemSpawnerItem = SpawnerTypeChecker.isItemSpawner(itemInHand);
            boolean isTargetItemSpawner = targetSpawner.isItemSpawner();
            if (isItemSpawnerItem != isTargetItemSpawner) {
               this.messageService.sendMessage(player, "spawner_different");
               return false;
            } else {
               String handConfigName = SpawnerTypeChecker.getConfigName(itemInHand);
               if (handConfigName != null && !this.currentName(handConfigName, targetSpawner).equals(targetSpawner.getConfigName())) {
                  this.messageService.sendMessage(player, "spawner_different");
                  return false;
               } else {
                  if (isItemSpawnerItem && isTargetItemSpawner) {
                     Material handItemMaterial = SpawnerTypeChecker.getItemSpawnerMaterial(itemInHand);
                     Material targetItemMaterial = targetSpawner.getSpawnedItemMaterial();
                     if (handItemMaterial == null || targetItemMaterial == null || handItemMaterial != targetItemMaterial) {
                        this.messageService.sendMessage(player, "spawner_different");
                        return false;
                     }
                  } else {
                     Optional<EntityType> handEntityTypeOpt = this.getEntityTypeFromItem(itemInHand);
                     if (!handEntityTypeOpt.isPresent()) {
                        this.messageService.sendMessage(player, "spawner_different");
                        return false;
                     }

                     EntityType handEntityType = handEntityTypeOpt.get();
                     EntityType targetEntityType = targetSpawner.getEntityType();
                     if (handEntityType != targetEntityType) {
                        this.messageService.sendMessage(player, "spawner_different");
                        return false;
                     }
                  }

                  int maxStackSize = targetSpawner.getMaxStackSize();
                  int currentStack = targetSpawner.getStackSize();
                  Map<String, String> placeholders = new HashMap<>();
                  placeholders.put("max", String.valueOf(maxStackSize));
                  if (currentStack >= maxStackSize) {
                     this.messageService.sendMessage(player, "spawner_stack_full", placeholders);
                     return false;
                  } else {
                     return this.processStackAddition(player, targetSpawner, itemInHand, stackAll, currentStack, maxStackSize);
                  }
               }
            }
         }
      }
   }

   private boolean hasStackPermissions(Player player, Location location) {
      if (!CheckStackBlock.CanPlayerPlaceBlock(player, location)) {
         return false;
      } else if (!player.hasPermission("smartspawner.stack")) {
         this.messageService.sendMessage(player, "no_permission");
         return false;
      } else {
         return true;
      }
   }

   public Optional<EntityType> getEntityTypeFromItem(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta == null) {
         return Optional.empty();
      } else {
         if (meta instanceof BlockStateMeta blockMeta && blockMeta.hasBlockState() && blockMeta.getBlockState() instanceof CreatureSpawner handSpawner) {
            EntityType entityType = handSpawner.getSpawnedType();
            if (entityType != null) {
               return Optional.of(entityType);
            }
         }

         return Optional.empty();
      }
   }

   private boolean processStackAddition(Player player, SpawnerData targetSpawner, ItemStack itemInHand, boolean stackAll, int currentStack, int maxStackSize) {
      int itemAmount = itemInHand.getAmount();
      int spaceLeft = maxStackSize - currentStack;
      int amountToStack = stackAll ? Math.min(spaceLeft, itemAmount) : 1;
      int newStack = currentStack + amountToStack;
      if (SpawnerStackEvent.getHandlerList().getRegisteredListeners().length != 0) {
         SpawnerStackEvent e = new SpawnerStackEvent(
            player, targetSpawner.getSpawnerLocation(), currentStack, newStack, SpawnerStackEvent.StackSource.PLACE, targetSpawner.getEntityType()
         );
         Bukkit.getPluginManager().callEvent(e);
         if (e.isCancelled()) {
            return false;
         }
      }

      targetSpawner.setStackSize(newStack);
      this.spawnerManager.markSpawnerModified(targetSpawner.getSpawnerId());
      if (targetSpawner.getIsAtCapacity()) {
         targetSpawner.setIsAtCapacity(false);
      }

      targetSpawner.updateLastInteractedPlayer(player.getName());
      this.updatePlayerInventory(player, itemInHand, amountToStack);
      this.showStackAnimation(targetSpawner, newStack, player);
      return true;
   }

   private void updatePlayerInventory(Player player, ItemStack itemInHand, int amountUsed) {
      if (player.getGameMode() != GameMode.CREATIVE) {
         int remainingAmount = itemInHand.getAmount() - amountUsed;
         if (remainingAmount <= 0) {
            player.getInventory().setItemInMainHand(null);
         } else {
            itemInHand.setAmount(remainingAmount);
         }
      }
   }

   private void showStackAnimation(SpawnerData spawner, int newStack, Player player) {
      if (Config.get().isSpawnerStackParticlesEnabled()) {
         Location loc = spawner.getSpawnerLocation();
         World world = loc.getWorld();
         if (world != null) {
            Scheduler.runLocationTask(loc, () -> world.spawnParticle(Particle.HAPPY_VILLAGER, loc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 0.0));
         }
      }

      Map<String, String> placeholders = new HashMap<>();
      placeholders.put("amount", String.valueOf(newStack));
      this.messageService.sendMessage(player, "spawner_stack_success", placeholders);
   }

   private String currentName(String storedName, SpawnerData target) {
      if (target.isItemSpawner()) {
         ItemSpawnerSettingsConfig.ItemDefinition definition = this.plugin.getItemSpawnerSettingsConfig().resolve(storedName);
         return definition != null && definition.material() == target.getSpawnedItemMaterial() ? definition.name() : storedName;
      } else {
         SpawnerSettingsConfig.MobDefinition definition = this.plugin.getSpawnerSettingsConfig().resolve(storedName);
         return definition != null && definition.entityType() == target.getEntityType() ? definition.name() : storedName;
      }
   }
}
