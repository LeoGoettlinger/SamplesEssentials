package github.nighter.smartspawner.spawner.gui.main;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.events.SpawnerExpClaimEvent;
import github.nighter.smartspawner.api.gui.GuiLayoutType;
import github.nighter.smartspawner.config.Config;
import github.nighter.smartspawner.hooks.rpg.AuraSkillsIntegration;
import github.nighter.smartspawner.language.LanguageManager;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import github.nighter.smartspawner.spawner.gui.sell.SpawnerSellConfirmUI;
import github.nighter.smartspawner.spawner.gui.storage.SpawnerStorageUI;
import github.nighter.smartspawner.spawner.gui.synchronization.SpawnerGuiViewManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

public class SpawnerMenuAction implements Listener {
   private static final Set<Material> SPAWNER_INFO_MATERIALS = Set.of(
      Material.PLAYER_HEAD,
      Material.SPAWNER,
      Material.ZOMBIE_HEAD,
      Material.SKELETON_SKULL,
      Material.WITHER_SKELETON_SKULL,
      Material.CREEPER_HEAD,
      Material.PIGLIN_HEAD,
      Material.DRAGON_HEAD
   );
   private final SmartSpawner plugin;
   private final SpawnerMenuUI spawnerMenuUI;
   private final SpawnerStorageUI spawnerStorageUI;
   private final SpawnerGuiViewManager spawnerGuiViewManager;
   private final LanguageManager languageManager;
   private final MessageService messageService;
   private AuraSkillsIntegration auraSkills;

   public SpawnerMenuAction(SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerMenuUI = plugin.getSpawnerMenuUI();
      this.spawnerStorageUI = plugin.getSpawnerStorageUI();
      this.spawnerGuiViewManager = plugin.getSpawnerGuiViewManager();
      this.languageManager = plugin.getLanguageManager();
      this.messageService = plugin.getMessageService();
      this.auraSkills = plugin.getIntegrationManager().getAuraSkillsIntegration();
   }

   public void reload() {
      this.auraSkills = this.plugin.getIntegrationManager().getAuraSkillsIntegration();
   }

   @EventHandler
   public void onMenuClick(InventoryClickEvent event) {
      if (event.getWhoClicked() instanceof Player player) {
         if (event.getInventory().getHolder(false) instanceof SpawnerMenuHolder holder) {
            event.setCancelled(true);
            SpawnerData var10 = holder.getSpawnerData();
            if (event.getClickedInventory() != null && event.getClickedInventory().getHolder(false) instanceof SpawnerMenuHolder) {
               ItemStack clickedItem = event.getCurrentItem();
               if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                  int slot = event.getRawSlot();
                  String clickType = this.getClickTypeString(event.getClick());
                  if (!this.handleLayoutAction(holder.getLayout(), player, var10, slot, clickType)) {
                     Material itemType = clickedItem.getType();
                     if (itemType == Material.CHEST) {
                        if (!this.plugin.getGuiButtonInteractionService().tryUseAntiSpam(player)) {
                           return;
                        }

                        this.handleStorageClick(player, var10);
                     } else if (SPAWNER_INFO_MATERIALS.contains(itemType)) {
                        this.handleSpawnerInfoClick(player, var10, event.getClick());
                     } else if (itemType == Material.EXPERIENCE_BOTTLE) {
                        this.handleExpBottleClick(player, var10, false);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean handleLayoutAction(GuiLayout layout, Player player, SpawnerData spawner, int slot, String clickType) {
      if (layout == null) {
         return false;
      } else {
         Optional<GuiButton> buttonOpt = layout.getButtonAtSlot(slot);
         if (buttonOpt.isEmpty()) {
            return false;
         } else {
            GuiButton button = buttonOpt.get();
            String action = button.getActionWithFallback(clickType);
            if (action == null || action.isEmpty()) {
               return true;
            } else if (action.equals("none")) {
               return true;
            } else {
               switch (action) {
                  case "open_storage":
                     if (!this.plugin.getGuiButtonInteractionService().tryUse(player, GuiLayoutType.MAIN_GUI, button)) {
                        return true;
                     }

                     this.plugin.getGuiButtonInteractionService().playNavigateSound(player, button, clickType);
                     this.handleStorageClick(player, spawner, false);
                     return true;
                  case "sell_and_exp":
                     if (!this.plugin.getGuiButtonInteractionService().tryUse(player, GuiLayoutType.MAIN_GUI, button)) {
                        return true;
                     } else {
                        if (this.plugin.hasSellIntegration() && player.hasPermission("smartspawner.sellall")) {
                           if (spawner.getVirtualInventory().getUsedSlots() == 0) {
                              boolean success;
                              if (spawner.getSpawnerExp() > 0L) {
                                 success = this.tryCollectExpForPlayer(player, spawner);
                              } else {
                                 this.messageService.sendMessage(player, "spawner_storage_empty");
                                 success = false;
                              }

                              this.playActionResult(player, button, clickType, success);
                              return true;
                           }

                           this.plugin
                              .getSpawnerSellConfirmUI()
                              .openSellConfirmGui(player, spawner, SpawnerSellConfirmUI.PreviousGui.MAIN_MENU, true, button, clickType);
                           return true;
                        }

                        this.messageService.sendMessage(player, "no_permission");
                        this.plugin.getGuiButtonInteractionService().playFailSound(player, button, clickType);
                        return true;
                     }
                  case "sell_all":
                     if (!this.plugin.getGuiButtonInteractionService().tryUse(player, GuiLayoutType.MAIN_GUI, button)) {
                        return true;
                     } else {
                        if (this.plugin.hasSellIntegration() && player.hasPermission("smartspawner.sellall")) {
                           this.handleSellAllItems(player, spawner, false, button, clickType);
                           return true;
                        }

                        this.messageService.sendMessage(player, "no_permission");
                        this.plugin.getGuiButtonInteractionService().playFailSound(player, button, clickType);
                        return true;
                     }
                  case "collect_exp":
                     if (!this.plugin.getGuiButtonInteractionService().tryUse(player, GuiLayoutType.MAIN_GUI, button)) {
                        return true;
                     }

                     this.playActionResult(player, button, clickType, this.handleExpBottleAcceptedClick(player, spawner, false));
                     return true;
                  default:
                     return false;
               }
            }
         }
      }
   }

   private String getClickTypeString(ClickType clickType) {
      return switch (clickType) {
         case LEFT -> "left_click";
         case RIGHT -> "right_click";
         case SHIFT_LEFT -> "shift_left_click";
         case SHIFT_RIGHT -> "shift_right_click";
         default -> "left_click";
      };
   }

   public void handleStorageClick(Player player, SpawnerData spawner) {
      this.handleStorageClick(player, spawner, true);
   }

   private void handleStorageClick(Player player, SpawnerData spawner, boolean playSound) {
      if (this.plugin.getSpawnerStorageAction().isStorageInUseByOther(player, spawner)) {
         this.messageService.sendMessage(player, "storage_in_use");
      } else {
         Inventory pageInventory = this.spawnerStorageUI.createStorageInventory(player, spawner, 1, -1);
         if (playSound) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F);
         }

         player.openInventory(pageInventory);
      }
   }

   private void handleSpawnerInfoClick(Player player, SpawnerData spawner, ClickType clickType) {
      if (this.plugin.getGuiButtonInteractionService().tryUseAntiSpam(player)) {
         boolean hasShopIntegration = this.plugin.hasSellIntegration() && player.hasPermission("smartspawner.sellall");
         if (hasShopIntegration && clickType == ClickType.LEFT) {
            this.handleExpBottleAcceptedClick(player, spawner, true);
            this.handleSellAllItems(player, spawner, true);
         }
      }
   }

   private void handleSellAllItems(Player player, SpawnerData spawner, boolean playSound) {
      this.handleSellAllItems(player, spawner, playSound, null, "click");
   }

   private void handleSellAllItems(Player player, SpawnerData spawner, boolean playSound, GuiButton sourceButton, String sourceClickType) {
      if (this.plugin.hasSellIntegration()) {
         if (!player.hasPermission("smartspawner.sellall")) {
            this.messageService.sendMessage(player, "no_permission");
         } else if (spawner.getVirtualInventory().getUsedSlots() == 0) {
            this.messageService.sendMessage(player, "spawner_storage_empty");
            if (sourceButton != null) {
               this.plugin.getGuiButtonInteractionService().playFailSound(player, sourceButton, sourceClickType);
            }
         } else {
            if (playSound) {
               player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
            }

            this.plugin
               .getSpawnerSellConfirmUI()
               .openSellConfirmGui(player, spawner, SpawnerSellConfirmUI.PreviousGui.MAIN_MENU, false, sourceButton, sourceClickType);
         }
      }
   }

   public void collectExpForPlayer(Player player, SpawnerData spawner) {
      this.tryCollectExpForPlayer(player, spawner);
   }

   public boolean tryCollectExpForPlayer(Player player, SpawnerData spawner) {
      long exp = spawner.getSpawnerExp();
      if (exp <= 0L) {
         this.messageService.sendMessage(player, "no_exp");
         return false;
      } else {
         long initialExp = exp;
         long expUsedForMending = 0L;
         if (Config.get().isAllowExpMending()) {
            expUsedForMending = this.applyMendingFromExp(player, exp);
            exp -= expUsedForMending;
         }

         if (this.auraSkills != null) {
            this.giveAuraSkillsXp(player, spawner, initialExp);
         }

         if (exp > 0L) {
            if (SpawnerExpClaimEvent.getHandlerList().getRegisteredListeners().length != 0) {
               SpawnerExpClaimEvent expClaimEvent = new SpawnerExpClaimEvent(player, spawner.getSpawnerLocation(), exp);
               Bukkit.getPluginManager().callEvent(expClaimEvent);
               if (expClaimEvent.isCancelled()) {
                  return false;
               }

               if (exp != expClaimEvent.getExpAmount()) {
                  exp = expClaimEvent.getExpAmount();
               }
            }

            this.givePlayerExpInChunks(player, exp);
         }

         spawner.setSpawnerExp(0L);
         this.plugin.getSpawnerManager().markSpawnerModified(spawner.getSpawnerId());
         this.spawnerGuiViewManager.updateSpawnerMenuViewers(spawner);
         if (spawner.getSpawnerExp() < spawner.getMaxStoredExp() && spawner.getIsAtCapacity()) {
            spawner.setIsAtCapacity(false);
         }

         this.sendExpCollectionMessage(player, initialExp, expUsedForMending);
         return true;
      }
   }

   public void handleExpBottleClick(Player player, SpawnerData spawner, boolean isSell) {
      if (this.plugin.getGuiButtonInteractionService().tryUseAntiSpam(player)) {
         this.handleExpBottleAcceptedClick(player, spawner, isSell);
      }
   }

   public boolean handleExpBottleAcceptedClick(Player player, SpawnerData spawner, boolean isSell) {
      long exp = spawner.getSpawnerExp();
      if (exp <= 0L && !isSell) {
         this.messageService.sendMessage(player, "no_exp");
         return false;
      } else {
         long initialExp = exp;
         long expUsedForMending = 0L;
         if (Config.get().isAllowExpMending()) {
            expUsedForMending = this.applyMendingFromExp(player, exp);
            exp -= expUsedForMending;
         }

         if (this.auraSkills != null) {
            this.giveAuraSkillsXp(player, spawner, initialExp);
         }

         if (exp > 0L) {
            if (SpawnerExpClaimEvent.getHandlerList().getRegisteredListeners().length != 0) {
               SpawnerExpClaimEvent expClaimEvent = new SpawnerExpClaimEvent(player, spawner.getSpawnerLocation(), exp);
               Bukkit.getPluginManager().callEvent(expClaimEvent);
               if (expClaimEvent.isCancelled()) {
                  return false;
               }

               if (exp != expClaimEvent.getExpAmount()) {
                  exp = expClaimEvent.getExpAmount();
               }
            }

            this.givePlayerExpInChunks(player, exp);
         }

         spawner.setSpawnerExp(0L);
         this.plugin.getSpawnerManager().markSpawnerModified(spawner.getSpawnerId());
         this.spawnerMenuUI.openSpawnerMenu(player, spawner, true);
         this.spawnerGuiViewManager.updateSpawnerMenuViewers(spawner);
         if (spawner.getSpawnerExp() < spawner.getMaxStoredExp() && spawner.getIsAtCapacity()) {
            spawner.setIsAtCapacity(false);
         }

         this.sendExpCollectionMessage(player, initialExp, expUsedForMending);
         return initialExp > 0L;
      }
   }

   private void playActionResult(Player player, GuiButton button, String clickType, boolean success) {
      if (success) {
         this.plugin.getGuiButtonInteractionService().playSuccessSound(player, button, clickType);
      } else {
         this.plugin.getGuiButtonInteractionService().playFailSound(player, button, clickType);
      }
   }

   private long applyMendingFromExp(Player player, long availableExp) {
      if (availableExp <= 0L) {
         return 0L;
      } else {
         long expUsed = 0L;
         PlayerInventory inventory = player.getInventory();

         for (ItemStack item : Arrays.asList(
            inventory.getItemInMainHand(),
            inventory.getItemInOffHand(),
            inventory.getHelmet(),
            inventory.getChestplate(),
            inventory.getLeggings(),
            inventory.getBoots()
         )) {
            if (availableExp <= 0L) {
               break;
            }

            if (item != null
               && item.getType() != Material.AIR
               && item.getEnchantments().containsKey(Enchantment.MENDING)
               && item.getItemMeta() instanceof Damageable damageable
               && damageable.getDamage() > 0) {
               int damage = damageable.getDamage();
               long durabilityToRepair = Math.min((long)damage, availableExp * 2L);
               long expNeeded = (durabilityToRepair + 1L) / 2L;
               if (expNeeded > 0L) {
                  long actualExpUsed = Math.min(expNeeded, availableExp);
                  long actualRepair = actualExpUsed * 2L;
                  int newDamage = (int)Math.max(0L, damage - actualRepair);
                  Damageable meta = (Damageable)item.getItemMeta();
                  meta.setDamage(newDamage);
                  item.setItemMeta(meta);
                  availableExp -= actualExpUsed;
                  expUsed += actualExpUsed;
                  player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5F, 1.0F);
                  player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0.0, 1.0, 0.0), 5);
               }
            }
         }

         return expUsed;
      }
   }

   public long[] collectExpSilently(Player player, SpawnerData spawner) {
      long exp = spawner.getSpawnerExp();
      if (exp <= 0L) {
         return new long[]{0L, 0L};
      } else {
         long initialExp = exp;
         long expUsedForMending = 0L;
         if (Config.get().isAllowExpMending()) {
            expUsedForMending = this.applyMendingFromExp(player, exp);
            exp -= expUsedForMending;
         }

         if (this.auraSkills != null) {
            this.giveAuraSkillsXp(player, spawner, initialExp);
         }

         if (exp > 0L) {
            if (SpawnerExpClaimEvent.getHandlerList().getRegisteredListeners().length != 0) {
               SpawnerExpClaimEvent expClaimEvent = new SpawnerExpClaimEvent(player, spawner.getSpawnerLocation(), exp);
               Bukkit.getPluginManager().callEvent(expClaimEvent);
               if (expClaimEvent.isCancelled()) {
                  return new long[]{0L, 0L};
               }

               if (exp != expClaimEvent.getExpAmount()) {
                  exp = expClaimEvent.getExpAmount();
               }
            }

            this.givePlayerExpInChunks(player, exp);
         }

         spawner.setSpawnerExp(0L);
         this.plugin.getSpawnerManager().markSpawnerModified(spawner.getSpawnerId());
         this.spawnerGuiViewManager.updateSpawnerMenuViewers(spawner);
         if (spawner.getSpawnerExp() < spawner.getMaxStoredExp() && spawner.getIsAtCapacity()) {
            spawner.setIsAtCapacity(false);
         }

         return new long[]{initialExp, expUsedForMending};
      }
   }

   private void sendExpCollectionMessage(Player player, long totalExp, long mendingExp) {
      Map<String, String> placeholders = new HashMap<>();
      if (mendingExp > 0L) {
         long remainingExp = totalExp - mendingExp;
         placeholders.put("exp_mending", this.languageManager.formatNumber(mendingExp));
         placeholders.put("exp", this.languageManager.formatNumber(remainingExp));
         this.messageService.sendMessage(player, "exp_collected_with_mending", placeholders);
      } else if (totalExp > 0L) {
         placeholders.put("exp", this.plugin.getLanguageManager().formatNumber(totalExp));
         this.messageService.sendMessage(player, "exp_collected", placeholders);
      }
   }

   private void giveAuraSkillsXp(Player player, SpawnerData spawner, long totalExp) {
      try {
         if (this.auraSkills == null || !this.auraSkills.isEnabled()) {
            return;
         }

         EntityType entityType = spawner.getEntityType();
         if (entityType == null) {
            return;
         }

         this.auraSkills.giveSkillXp(player, entityType, this.clampLongToInt(totalExp));
      } catch (Exception var6) {
         this.plugin.getLogger().warning("Error giving AuraSkills XP: " + var6.getMessage());
      }
   }

   private void givePlayerExpInChunks(Player player, long totalExp) {
      long remaining = totalExp;

      while (remaining > 0L) {
         int grant = this.clampLongToInt(Math.min(remaining, 2147483647L));
         player.giveExp(grant);
         remaining -= grant;
      }
   }

   private int clampLongToInt(long value) {
      return value <= 0L ? 0 : (int)Math.min(value, 2147483647L);
   }
}
