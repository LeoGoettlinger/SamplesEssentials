package github.nighter.smartspawner.spawner.interactions.destroy;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.events.SpawnerPlayerBreakEvent;
import github.nighter.smartspawner.config.Config;
import github.nighter.smartspawner.extras.HopperService;
import github.nighter.smartspawner.hooks.protections.CheckBreakBlock;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.spawner.config.SpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.gui.main.SpawnerMenuAction;
import github.nighter.smartspawner.spawner.gui.synchronization.SpawnerGuiViewManager;
import github.nighter.smartspawner.spawner.item.SpawnerItemFactory;
import github.nighter.smartspawner.spawner.lootgen.SpawnerRangeChecker;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.sell.SpawnerSellManager;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class SpawnerBreakListener implements Listener {
   private static final int MAX_STACK_SIZE = 64;
   private static final String DROP_CHANCE_BYPASS_PERMISSION = "smartspawner.break.bypassdropchance";
   private final SpawnerBreakListener.BreakPluginContext plugin;
   private final MessageService messageService;
   private final SpawnerManager spawnerManager;
   private final SpawnerItemFactory spawnerItemFactory;
   private Config breakConfig;

   public SpawnerBreakListener(SmartSpawner plugin) {
      this(new SpawnerBreakListener.SmartSpawnerBreakPluginContext(plugin));
   }

   SpawnerBreakListener(SpawnerBreakListener.BreakPluginContext plugin) {
      this.plugin = plugin;
      this.messageService = plugin.getMessageService();
      this.spawnerManager = plugin.getSpawnerManager();
      this.spawnerItemFactory = plugin.getSpawnerItemFactory();
      this.loadConfig();
   }

   public void loadConfig() {
      this.breakConfig = Config.get();
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onSpawnerBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      Location location = block.getLocation();
      if (block.getType() == Material.SPAWNER) {
         SpawnerRemovalService removalService = this.plugin.getSpawnerRemovalService();
         if (removalService != null && removalService.isRemovalPending(location)) {
            event.setCancelled(true);
            this.messageService.sendMessage(player, "action_in_progress");
         } else if (!CheckBreakBlock.CanPlayerBreakBlock(player, location)) {
            event.setCancelled(true);
         } else if (!this.breakConfig.isBreakEnabled()) {
            event.setCancelled(true);
         } else {
            SpawnerData spawner = this.spawnerManager.getSpawnerByLocation(location);
            if (!player.hasPermission("smartspawner.break")) {
               event.setCancelled(true);
               this.messageService.sendMessage(player, "break_no_permission");
            } else if (!this.breakConfig.isNaturalBreakable() && spawner == null) {
               block.setType(Material.AIR);
               event.setCancelled(true);
            } else {
               boolean breakHandled;
               if (spawner != null) {
                  breakHandled = this.handleSmartSpawnerBreak(block, spawner, player);
               } else {
                  CreatureSpawner creatureSpawner = (CreatureSpawner)block.getState(false);
                  if (this.callAPIEvent(player, block.getLocation(), 1, creatureSpawner.getSpawnedType())) {
                     event.setCancelled(true);
                     return;
                  }

                  breakHandled = this.handleVanillaSpawnerBreak(block, creatureSpawner, player);
               }

               event.setCancelled(true);
               if (breakHandled) {
                  this.cleanupAssociatedHopper(block);
               }
            }
         }
      }
   }

   private boolean handleSmartSpawnerBreak(Block block, SpawnerData spawner, Player player) {
      Location location = block.getLocation();
      ItemStack tool = player.getInventory().getItemInMainHand();
      if (!this.validateBreakConditions(player, tool, spawner)) {
         return false;
      } else {
         SpawnerData currentSpawner = this.spawnerManager.getSpawnerByLocation(location);
         if (currentSpawner == null || !currentSpawner.getSpawnerId().equals(spawner.getSpawnerId())) {
            return false;
         } else if (currentSpawner.isSelling()) {
            this.messageService.sendMessage(player, "action_in_progress");
            return false;
         } else {
            boolean wantsStackBreak = player.isSneaking() && currentSpawner.getStackSize() > 1;
            boolean bypassDropChance = this.hasDropChanceBypass(player);
            if (wantsStackBreak && this.breakConfig.isSneakBreakEnabled() && !bypassDropChance && this.hasSmartSpawnerDropChance(currentSpawner)) {
               this.messageService.sendMessage(player, "sneak_break_blocked");
               return false;
            } else {
               currentSpawner.updateLastInteractedPlayer(player.getName());
               this.plugin.getSpawnerGuiViewManager().closeAllViewersInventory(currentSpawner);
               SpawnerBreakListener.SpawnerBreakResult result = this.processDrops(
                  player, location, currentSpawner, wantsStackBreak && this.breakConfig.isSneakBreakEnabled(), bypassDropChance
               );
               if (!result.isSuccess()) {
                  return false;
               } else {
                  if (result.isFullyRemoved()) {
                     boolean cleanupDeferred = this.maybeAutoSellAndClaimExp(
                        player, currentSpawner, () -> this.applyBreakResult(block, currentSpawner, player, result)
                     );
                     if (!cleanupDeferred) {
                        this.applyBreakResult(block, currentSpawner, player, result);
                     }
                  } else {
                     this.applyBreakResult(block, currentSpawner, player, result);
                  }

                  if (player.getGameMode() != GameMode.CREATIVE) {
                     this.reduceDurability(tool, player, result.getDurabilityLoss());
                  }

                  return true;
               }
            }
         }
      }
   }

   private boolean handleVanillaSpawnerBreak(Block block, CreatureSpawner creatureSpawner, Player player) {
      Location location = block.getLocation();
      ItemStack tool = player.getInventory().getItemInMainHand();
      if (!this.validateBreakConditions(player, tool, null)) {
         return false;
      } else if (block.getType() != Material.SPAWNER) {
         return false;
      } else {
         EntityType entityType = creatureSpawner.getSpawnedType();
         ItemStack spawnerItem;
         if (this.breakConfig.isConvertNaturalToSmartSpawner()) {
            spawnerItem = this.spawnerItemFactory.createSmartSpawnerItem(entityType);
         } else {
            spawnerItem = this.spawnerItemFactory.createVanillaSpawnerItem(entityType);
         }

         World world = location.getWorld();
         if (world == null) {
            return false;
         } else {
            block.setType(Material.AIR);
            if (!this.hasDropChanceBypass(player) && !this.shouldDropSpawner(this.getNaturalSpawnerDropChance(entityType))) {
               this.messageService.sendMessage(player, "drop_failed");
            } else if (this.breakConfig.isDirectToInventory()) {
               this.giveSpawnersToPlayer(player, 1, spawnerItem);
               player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.2F);
            } else {
               world.dropItemNaturally(this.findSafeDropLocation(block, player), spawnerItem);
            }

            this.reduceDurability(tool, player, this.breakConfig.getDurabilityLoss());
            return true;
         }
      }
   }

   private boolean validateBreakConditions(Player player, ItemStack tool, SpawnerData spawner) {
      if (player.getGameMode() == GameMode.CREATIVE) {
         return true;
      } else if (!player.hasPermission("smartspawner.break")) {
         this.messageService.sendMessage(player, "break_no_permission");
         return false;
      } else if (!this.isValidTool(tool)) {
         this.messageService.sendMessage(player, "break_tool_required");
         return false;
      } else if (this.breakConfig.isSilkTouchRequired() && tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) < this.breakConfig.getSilkTouchLevel()) {
         this.messageService.sendMessage(player, "break_silk_touch");
         return false;
      } else {
         return true;
      }
   }

   SpawnerBreakListener.SpawnerBreakResult processDrops(Player player, Location location, SpawnerData spawner, boolean isCrouching, boolean bypassDropChance) {
      int currentStackSize = spawner.getStackSize();
      World world = location.getWorld();
      if (world == null) {
         return new SpawnerBreakListener.SpawnerBreakResult(false, 0, 0, this.breakConfig.getDurabilityLoss(), false, new ItemStack(Material.SPAWNER));
      } else {
         ItemStack template;
         if (spawner.isItemSpawner()) {
            template = this.spawnerItemFactory.createItemSpawnerItem(spawner.getConfigName(), spawner.getSpawnedItemMaterial(), 1);
         } else {
            template = this.spawnerItemFactory.createSmartSpawnerItem(spawner.getConfigName(), spawner.getEntityType(), 1);
         }

         int newStackSize = currentStackSize;
         int dropAmount;
         boolean shouldDeleteSpawner;
         if (isCrouching) {
            if (currentStackSize <= 64) {
               dropAmount = currentStackSize;
               shouldDeleteSpawner = true;
            } else {
               dropAmount = 64;
               shouldDeleteSpawner = false;
               newStackSize = currentStackSize - 64;
            }
         } else {
            dropAmount = 1;
            shouldDeleteSpawner = currentStackSize <= 1;
            if (!shouldDeleteSpawner) {
               newStackSize = currentStackSize - 1;
            }
         }

         if (this.callAPIEvent(player, location, dropAmount, spawner.getEntityType())) {
            return new SpawnerBreakListener.SpawnerBreakResult(false, dropAmount, 0, 0, false, template);
         } else {
            if (!shouldDeleteSpawner) {
               spawner.setStackSize(newStackSize);
            }

            int actualDropAmount = bypassDropChance ? dropAmount : this.rollDroppedAmount(dropAmount, this.getSmartSpawnerDropChance(spawner));
            return new SpawnerBreakListener.SpawnerBreakResult(
               true, actualDropAmount, dropAmount, this.breakConfig.getDurabilityLoss(), shouldDeleteSpawner, template
            );
         }
      }
   }

   void applyBreakResult(Block spawnerBlock, SpawnerData spawner, Player player, SpawnerBreakListener.SpawnerBreakResult result) {
      if (result.isFullyRemoved()) {
         this.cleanupSpawner(spawnerBlock, spawner);
      } else {
         this.spawnerManager.markSpawnerModified(spawner.getSpawnerId());
      }

      if (this.breakConfig.isDirectToInventory()) {
         if (result.getDroppedAmount() <= 0) {
            this.messageService.sendMessage(player, "drop_failed");
         } else {
            this.giveSpawnersToPlayer(player, result.getDroppedAmount(), result.getDropTemplate());
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.2F);
         }
      } else {
         World world = spawnerBlock.getWorld();
         if (world != null) {
            if (result.getDroppedAmount() <= 0) {
               this.messageService.sendMessage(player, "drop_failed");
            } else {
               ItemStack dropItem = result.getDropTemplate().clone();
               dropItem.setAmount(result.getDroppedAmount());
               world.dropItemNaturally(this.findSafeDropLocation(spawnerBlock, player), dropItem);
            }
         }
      }
   }

   private double getSmartSpawnerDropChance(SpawnerData spawner) {
      SpawnerSettingsConfig settingsConfig = this.plugin.getSpawnerSettingsConfig();
      return settingsConfig == null ? 100.0 : settingsConfig.getSpawnerDropChance(spawner.getConfigName(), spawner.getEntityType());
   }

   private boolean hasSmartSpawnerDropChance(SpawnerData spawner) {
      SpawnerSettingsConfig settingsConfig = this.plugin.getSpawnerSettingsConfig();
      return settingsConfig != null && settingsConfig.hasSpawnerDropChance(spawner.getConfigName(), spawner.getEntityType());
   }

   private boolean hasDropChanceBypass(Player player) {
      return player.hasPermission("smartspawner.break.bypassdropchance");
   }

   private double getNaturalSpawnerDropChance(EntityType entityType) {
      return this.breakConfig.getNaturalSpawnerDropChance(entityType);
   }

   private boolean shouldDropSpawner(double dropChance) {
      if (dropChance >= 100.0) {
         return true;
      } else {
         return dropChance <= 0.0 ? false : ThreadLocalRandom.current().nextDouble(100.0) < dropChance;
      }
   }

   private int rollDroppedAmount(int amount, double dropChance) {
      if (amount > 0 && !(dropChance <= 0.0)) {
         if (dropChance >= 100.0) {
            return amount;
         } else {
            int droppedAmount = 0;
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int i = 0; i < amount; i++) {
               if (random.nextDouble(100.0) < dropChance) {
                  droppedAmount++;
               }
            }

            return droppedAmount;
         }
      } else {
         return 0;
      }
   }

   private boolean callAPIEvent(Player player, Location location, int dropAmount, EntityType entityType) {
      if (SpawnerPlayerBreakEvent.getHandlerList().getRegisteredListeners().length != 0) {
         SpawnerPlayerBreakEvent e = new SpawnerPlayerBreakEvent(player, location, dropAmount, entityType);
         Bukkit.getPluginManager().callEvent(e);
         return e.isCancelled();
      } else {
         return false;
      }
   }

   private void reduceDurability(ItemStack tool, Player player, int durabilityLoss) {
      if (tool.getType().getMaxDurability() != 0) {
         ItemMeta meta = tool.getItemMeta();
         if (!meta.isUnbreakable()) {
            if (meta instanceof Damageable damageable) {
               int currentDurability = damageable.getDamage();
               int newDurability = currentDurability + durabilityLoss;
               if (newDurability >= tool.getType().getMaxDurability()) {
                  player.getInventory().setItemInMainHand(null);
                  player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
               } else {
                  damageable.setDamage(newDurability);
                  tool.setItemMeta(meta);
               }
            }
         }
      }
   }

   private void cleanupSpawner(Block block, SpawnerData spawner) {
      spawner.getSpawnerStop().set(true);
      block.setType(Material.AIR);
      String spawnerId = spawner.getSpawnerId();
      this.plugin.getRangeChecker().deactivateSpawner(spawner);
      this.spawnerManager.removeSpawner(spawnerId);
      this.spawnerManager.markSpawnerDeleted(spawnerId);
   }

   private Location findSafeDropLocation(Block block, Player player) {
      BlockFace[] priority = new BlockFace[]{BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP};

      for (BlockFace face : priority) {
         Block neighbor = block.getRelative(face);
         if (this.isSafeItemDropSpace(neighbor)) {
            return neighbor.getLocation().toCenterLocation();
         }
      }

      return player.getLocation().toCenterLocation();
   }

   private boolean isSafeItemDropSpace(Block block) {
      return block.getType().isAir();
   }

   private boolean isValidTool(ItemStack tool) {
      return tool == null ? false : this.breakConfig.isRequiredTool(tool.getType());
   }

   boolean maybeAutoSellAndClaimExp(Player player, SpawnerData spawner, Runnable onSellComplete) {
      if (!this.breakConfig.isSellAndXpBreak()) {
         return false;
      } else {
         SpawnerMenuAction menuAction = this.plugin.getSpawnerMenuAction();
         if (menuAction != null && spawner.getSpawnerExp() > 0L) {
            menuAction.collectExpForPlayer(player, spawner);
         }

         if (!this.plugin.hasSellIntegration() || !player.hasPermission("smartspawner.sellall")) {
            return false;
         } else if (spawner.getVirtualInventory().getUsedSlots() > 0) {
            this.plugin.getSpawnerSellManager().sellAllItems(player, spawner, onSellComplete);
            return true;
         } else {
            return false;
         }
      }
   }

   private void giveSpawnersToPlayer(Player player, int amount, ItemStack template) {
      int MAX_STACK_SIZE = 64;
      ItemStack itemToGive = template.clone();
      itemToGive.setAmount(Math.min(amount, 64));
      Map<Integer, ItemStack> failedItems = player.getInventory().addItem(new ItemStack[]{itemToGive});
      if (!failedItems.isEmpty()) {
         for (ItemStack failedItem : failedItems.values()) {
            player.getWorld().dropItemNaturally(player.getLocation().toCenterLocation(), failedItem);
         }

         this.messageService.sendMessage(player, "inventory_full");
      }

      player.updateInventory();
   }

   @EventHandler
   public void onSpawnerDamage(BlockDamageEvent event) {
      Block block = event.getBlock();
      Player player = event.getPlayer();
      if (block.getType() == Material.SPAWNER) {
         if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool.getType() != Material.AIR) {
               if (this.isValidTool(tool)) {
                  if (this.breakConfig.isSilkTouchRequired() && tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) < this.breakConfig.getSilkTouchLevel()) {
                     this.messageService.sendMessage(player, "break_silk_touch");
                     return;
                  }

                  if (!player.hasPermission("smartspawner.break")) {
                     this.messageService.sendMessage(player, "break_no_permission");
                  }
               } else {
                  this.messageService.sendMessage(player, "break_tool_required");
               }
            }
         }
      }
   }

   public void cleanupAssociatedHopper(Block block) {
      HopperService currentHopperService = this.plugin.getHopperService();
      if (currentHopperService != null) {
         currentHopperService.getTracker().removeBelowSpawner(block);
      }
   }

   interface BreakPluginContext {
      MessageService getMessageService();

      SpawnerManager getSpawnerManager();

      HopperService getHopperService();

      SpawnerItemFactory getSpawnerItemFactory();

      SpawnerGuiViewManager getSpawnerGuiViewManager();

      SpawnerSettingsConfig getSpawnerSettingsConfig();

      boolean hasSellIntegration();

      SpawnerMenuAction getSpawnerMenuAction();

      SpawnerSellManager getSpawnerSellManager();

      SpawnerRangeChecker getRangeChecker();

      SpawnerRemovalService getSpawnerRemovalService();
   }

   private static final class SmartSpawnerBreakPluginContext implements SpawnerBreakListener.BreakPluginContext {
      private final SmartSpawner plugin;

      private SmartSpawnerBreakPluginContext(SmartSpawner plugin) {
         this.plugin = plugin;
      }

      @Override
      public MessageService getMessageService() {
         return this.plugin.getMessageService();
      }

      @Override
      public SpawnerManager getSpawnerManager() {
         return this.plugin.getSpawnerManager();
      }

      @Override
      public HopperService getHopperService() {
         return this.plugin.getHopperService();
      }

      @Override
      public SpawnerItemFactory getSpawnerItemFactory() {
         return this.plugin.getSpawnerItemFactory();
      }

      @Override
      public SpawnerGuiViewManager getSpawnerGuiViewManager() {
         return this.plugin.getSpawnerGuiViewManager();
      }

      @Override
      public SpawnerSettingsConfig getSpawnerSettingsConfig() {
         return this.plugin.getSpawnerSettingsConfig();
      }

      @Override
      public boolean hasSellIntegration() {
         return this.plugin.hasSellIntegration();
      }

      @Override
      public SpawnerMenuAction getSpawnerMenuAction() {
         return this.plugin.getSpawnerMenuAction();
      }

      @Override
      public SpawnerSellManager getSpawnerSellManager() {
         return this.plugin.getSpawnerSellManager();
      }

      @Override
      public SpawnerRangeChecker getRangeChecker() {
         return this.plugin.getRangeChecker();
      }

      @Override
      public SpawnerRemovalService getSpawnerRemovalService() {
         return this.plugin.getSpawnerRemovalService();
      }
   }

   static class SpawnerBreakResult {
      private final boolean success;
      private final int droppedAmount;
      private final int removedAmount;
      private final int baseDurabilityLoss;
      private final boolean fullyRemoved;
      private final ItemStack dropTemplate;

      public SpawnerBreakResult(boolean success, int droppedAmount, int removedAmount, int baseDurabilityLoss, boolean fullyRemoved, ItemStack dropTemplate) {
         this.success = success;
         this.droppedAmount = droppedAmount;
         this.removedAmount = removedAmount;
         this.baseDurabilityLoss = baseDurabilityLoss;
         this.fullyRemoved = fullyRemoved;
         this.dropTemplate = dropTemplate.clone();
      }

      public int getDurabilityLoss() {
         return this.removedAmount * this.baseDurabilityLoss;
      }

      @Generated
      public boolean isSuccess() {
         return this.success;
      }

      @Generated
      public int getDroppedAmount() {
         return this.droppedAmount;
      }

      @Generated
      public boolean isFullyRemoved() {
         return this.fullyRemoved;
      }

      @Generated
      public ItemStack getDropTemplate() {
         return this.dropTemplate;
      }
   }
}
