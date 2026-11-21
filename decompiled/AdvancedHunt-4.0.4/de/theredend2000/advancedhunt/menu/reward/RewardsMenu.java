package de.theredend2000.advancedhunt.menu.reward;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.CollectionRewardHolder;
import de.theredend2000.advancedhunt.model.PresetRewardHolder;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardHolder;
import de.theredend2000.advancedhunt.model.RewardPresetType;
import de.theredend2000.advancedhunt.model.RewardType;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.model.TreasureRewardHolder;
import de.theredend2000.advancedhunt.platform.PlatformAccess;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import de.theredend2000.advancedhunt.util.ItemSerializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RewardsMenu extends PagedMenu {
   private RewardsMenu.QuickActionMode quickActionMode = RewardsMenu.QuickActionMode.DELETE;
   private final RewardHolder holder;
   private final List<Reward> rewards;
   private RewardHolder alternateHolder;

   public RewardsMenu(Player player, Main plugin, RewardHolder holder) {
      super(player, plugin);
      this.holder = holder;
      this.rewards = holder.getRewards() != null ? new ArrayList<>(holder.getRewards()) : new ArrayList<>();
      this.maxItemsPerPage = 28;
   }

   public RewardsMenu setAlternateContext(RewardHolder alternateHolder) {
      this.alternateHolder = alternateHolder;
      return this;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage(this.holder.getRewardsTitleKey(), false);
   }

   @Override
   public int getSlots() {
      return 54;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void setMenuItems() {
      this.index = 0;
      this.addMenuBorder();
      if (this.rewards.isEmpty()) {
         this.addStaticItem(
            22,
            this.getWarningIcon(
               this.plugin.getMessageManager().getMessage("gui.rewards.no_rewards.name", false),
               this.plugin.getMessageManager().getMessageList("gui.rewards.no_rewards.lore", false)
            )
         );
      } else {
         this.addPagedButtons(this.rewards.size());
         int startIndex = this.page * this.maxItemsPerPage;
         int endIndex = Math.min(startIndex + this.maxItemsPerPage, this.rewards.size());
         this.hasNextPage = endIndex < this.rewards.size();

         for (int i = startIndex; i < endIndex; i++) {
            Reward reward = this.rewards.get(i);
            int rewardIndex = i;
            ItemStack displayItem = this.createRewardDisplay(reward, i + 1);
            this.addPagedItem(this.index++, displayItem, e -> this.handleRewardClick(e, rewardIndex));
         }
      }
   }

   private int getTotalPages() {
      return this.rewards.isEmpty() ? 1 : (int)Math.ceil((double)this.rewards.size() / this.maxItemsPerPage);
   }

   @Override
   public void addMenuBorder() {
      super.addMenuBorder();
      if (this.holder instanceof TreasureRewardHolder) {
         Treasure treasure = ((TreasureRewardHolder)this.holder).getTreasure();
         this.addButton(
            0,
            new ItemBuilder(XMaterial.PLAYER_HEAD)
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.treasure_info.name", false))
               .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.treasure_info.lore", false, "%uuid%", treasure.getId().toString()))
               .setSkullTexture(
                  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0MzlkMmUzMDZiMjI1NTE2YWE5YTZkMDA3YTdlNzVlZGQyZDUwMTVkMTEzYjQyZjQ0YmU2MmE1MTdlNTc0ZiJ9fX0="
               )
               .build(),
            e -> {
               this.playerMenuUtility.closeInventory();
               this.showTreasureInfo(this.playerMenuUtility, treasure);
            }
         );
      }

      this.addButton(
         53,
         new ItemBuilder(XMaterial.HOPPER)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.open_reward_option_menu.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.open_reward_option_menu.lore", false))
            .build(),
         e -> new AddRewardMenu(this.playerMenuUtility, this.plugin, this).open()
      );
      RewardsMenu.QuickActionMode mode = this.getQuickActionMode();
      this.addButton(
         52,
         new ItemBuilder(XMaterial.LEVER)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.quick_mode.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList(
                     "gui.rewards.quick_mode.lore", false, "%mode%", this.getQuickActionModeDisplay(mode), "%action%", this.getQuickActionActionDisplay(mode)
                  )
            )
            .build(),
         e -> {
            this.setQuickActionMode(this.getNextQuickActionMode(this.getQuickActionMode()));
            this.updateQuickActionDisplay();
         }
      );
      RewardPresetType presetType = this.holder.getRewardsTitleKey().contains("collection") ? RewardPresetType.COLLECTION : RewardPresetType.TREASURE;
      boolean isCollection = presetType == RewardPresetType.COLLECTION;
      String presetTypeName = this.plugin.getMessageManager().getMessage(isCollection ? "common.collection" : "common.treasure", false);
      if (!(this.holder instanceof PresetRewardHolder)) {
         this.addButton(
            45,
            new ItemBuilder(XMaterial.WRITABLE_BOOK)
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.save_preset.name", false, "%Type%", presetTypeName))
               .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.save_preset.lore", false, "%type%", presetTypeName))
               .build(),
            e -> {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.preset.prompt_name", "%type%", presetTypeName));
               this.plugin
                  .getChatInputListener()
                  .requestInput(
                     this.playerMenuUtility,
                     presetName -> {
                        if (presetName != null && !presetName.trim().isEmpty()) {
                           String trimmed = presetName.trim();
                           if (this.plugin.getRewardPresetManager().hasPresetName(presetType, trimmed)) {
                              this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.preset.duplicate_name"));
                              this.open();
                           } else {
                              this.plugin
                                 .getRewardPresetManager()
                                 .createPreset(presetType, trimmed, new ArrayList<>(this.rewards))
                                 .thenAccept(
                                    success -> Bukkit.getScheduler()
                                       .runTask(
                                          this.plugin,
                                          () -> {
                                             if (!success) {
                                                this.playerMenuUtility
                                                   .sendMessage(
                                                      this.plugin.getMessageManager().getMessage("error.preset.save_failed", "%type%", presetTypeName)
                                                   );
                                                this.open();
                                             } else {
                                                this.playerMenuUtility
                                                   .sendMessage(
                                                      this.plugin
                                                         .getMessageManager()
                                                         .getMessage("feedback.preset.saved", "%type%", presetTypeName, "%name%", trimmed)
                                                   );
                                                this.open();
                                             }
                                          }
                                       )
                                 );
                           }
                        } else {
                           this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.preset.invalid_name"));
                           this.open();
                        }
                     }
                  );
            }
         );
         this.addButton(
            46,
            new ItemBuilder(XMaterial.WRITTEN_BOOK)
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.load_preset.name", false, "%Type%", presetTypeName))
               .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.load_preset.lore", false, "%type%", presetTypeName))
               .build(),
            e -> {
               Collection collectionContext = null;
               if (this.holder instanceof CollectionRewardHolder) {
                  collectionContext = ((CollectionRewardHolder)this.holder).getCollection();
               } else if (this.alternateHolder instanceof CollectionRewardHolder) {
                  collectionContext = ((CollectionRewardHolder)this.alternateHolder).getCollection();
               }

               new RewardPresetListMenu(
                     this.playerMenuUtility,
                     this.plugin,
                     presetType,
                     selected -> {
                        this.rewards.clear();
                        this.rewards.addAll(selected.getRewards());
                        this.holder.saveRewards(new ArrayList<>(this.rewards));
                        this.playerMenuUtility
                           .sendMessage(
                              this.plugin.getMessageManager().getMessage("feedback.preset.loaded", "%type%", presetTypeName, "%name%", selected.getName())
                           );
                        Bukkit.getScheduler().runTask(this.plugin, this::open);
                     },
                     collectionContext
                  )
                  .setPreviousMenu(this)
                  .open();
            }
         );
      }

      if (this.alternateHolder != null) {
         Material icon = isCollection ? XMaterial.ENDER_CHEST.get() : XMaterial.CHEST.get();
         String switchKey = isCollection ? "gui.rewards.switch_to_treasure" : "gui.rewards.switch_to_collection";
         this.addButton(
            8,
            new ItemBuilder(icon)
               .setDisplayName(this.plugin.getMessageManager().getMessage(switchKey + ".name", false))
               .setLore(this.plugin.getMessageManager().getMessageList(switchKey + ".lore", false))
               .build(),
            e -> {
               RewardsMenu newMenu = new RewardsMenu(this.playerMenuUtility, this.plugin, this.alternateHolder).setAlternateContext(this.holder);
               newMenu.open();
            }
         );
      }
   }

   private ItemStack createRewardDisplay(Reward reward, int rewardNumber) {
      String chanceLore = this.plugin.getMessageManager().getMessage("gui.rewards.chance_lore", false, "%chance%", this.formatChance(reward.getChance()));
      if (reward.getType() == RewardType.ITEM) {
         return this.createItemRewardDisplay(reward, chanceLore, rewardNumber);
      } else if (reward.getType() == RewardType.COMMAND) {
         return this.createCommandRewardDisplay(reward, chanceLore, rewardNumber);
      } else {
         return reward.getType() == RewardType.CHAT_MESSAGE
            ? this.createChatMessageRewardDisplay(reward, chanceLore, rewardNumber)
            : this.createBroadcastMessageRewardDisplay(reward, chanceLore, rewardNumber);
      }
   }

   private ItemStack createItemRewardDisplay(Reward reward, String chanceLore, int rewardNumber) {
      ItemStack item = ItemSerializer.deserialize(reward.getValue());
      if (item != null && item.getType() != XMaterial.AIR.get()) {
         ItemBuilder builder = new ItemBuilder(item.clone());
         List<String> lore = new ArrayList<>();
         if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            lore.addAll(item.getItemMeta().getLore());
         }

         lore.add("");
         lore.add(ChatColor.DARK_GRAY + "─────────────");
         lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.reward_number", false, "%number%", String.valueOf(rewardNumber)));
         lore.add(chanceLore);
         if (item.getAmount() > 1) {
            lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.amount_lore", false, "%amount%", String.valueOf(item.getAmount())));
         }

         if (reward.getMessage() != null && !reward.getMessage().isEmpty()) {
            String messageValue = reward.getMessage().length() > 30 ? reward.getMessage().substring(0, 27) + "..." : reward.getMessage();
            lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.message_lore", false, "%message%", messageValue));
         }

         if (reward.getBroadcast() != null && !reward.getBroadcast().isEmpty()) {
            String broadcastValue = reward.getBroadcast().length() > 30 ? reward.getBroadcast().substring(0, 27) + "..." : reward.getBroadcast();
            lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.broadcast_lore", false, "%broadcast%", broadcastValue));
         }

         lore.add("");
         lore.add(
            this.plugin
               .getMessageManager()
               .getMessage("gui.rewards.click_to_edit", false, "%action%", this.getQuickActionActionDisplay(this.getQuickActionMode()))
         );
         return builder.setLore(lore).build();
      } else {
         return new ItemBuilder(XMaterial.BARRIER)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.invalid_item.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.invalid_item.lore", false, "%chance%", this.formatChance(reward.getChance())))
            .build();
      }
   }

   private ItemStack createCommandRewardDisplay(Reward reward, String chanceLore, int rewardNumber) {
      String command = reward.getValue();
      String displayCommand = command.length() > 40 ? command.substring(0, 37) + "..." : command;
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.command_label", false));
      lore.add(ChatColor.WHITE + "/" + displayCommand);
      if (command.length() > 40) {
         lore.add("");
         lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.truncated.command", false));
      }

      lore.add("");
      lore.add(ChatColor.DARK_GRAY + "─────────────");
      lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.reward_number", false, "%number%", String.valueOf(rewardNumber)));
      lore.add(chanceLore);
      if (reward.getMessage() != null && !reward.getMessage().isEmpty()) {
         String messageValue = reward.getMessage().length() > 30 ? reward.getMessage().substring(0, 27) + "..." : reward.getMessage();
         lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.message_lore", false, "%message%", messageValue));
      }

      if (reward.getBroadcast() != null && !reward.getBroadcast().isEmpty()) {
         String broadcastValue = reward.getBroadcast().length() > 30 ? reward.getBroadcast().substring(0, 27) + "..." : reward.getBroadcast();
         lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.broadcast_lore", false, "%broadcast%", broadcastValue));
      }

      lore.add("");
      lore.add(
         this.plugin
            .getMessageManager()
            .getMessage("gui.rewards.click_to_edit", false, "%action%", this.getQuickActionActionDisplay(this.getQuickActionMode()))
      );
      return new ItemBuilder(XMaterial.PLAYER_HEAD)
         .setSkullTexture(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWI2Y2VlOGZkYTdlZjBiM2FlMGViMDU3OWQ1Njc2Y2UzNmFmN2VmYzU3NGQ4ODcyOGYzODk0ZjZiMTY2NTM4In19fQ=="
         )
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.command_name", false))
         .setLore(lore)
         .build();
   }

   private void handleRewardClick(InventoryClickEvent e, int rewardIndex) {
      if (rewardIndex < this.rewards.size()) {
         ClickType clickType = e.getClick();
         if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
            e.setCancelled(true);
            Reward reward = this.rewards.get(rewardIndex);
            RewardsMenu.QuickActionMode mode = this.getQuickActionMode();
            if (mode == RewardsMenu.QuickActionMode.DELETE) {
               this.deleteReward(rewardIndex);
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.deleted"));
               int totalPages = this.getTotalPages();
               if (this.page > totalPages - 1) {
                  this.page = Math.max(0, totalPages - 1);
               }

               this.refresh();
               return;
            }

            if (mode == RewardsMenu.QuickActionMode.GET_INSTANCE) {
               this.giveRewardInstance(reward);
               return;
            }
         }

         Reward rewardx = this.rewards.get(rewardIndex);
         new RewardActionMenu(this.playerMenuUtility, this.plugin, this, rewardx, rewardIndex).setPreviousMenu(this).open();
      }
   }

   private RewardsMenu.QuickActionMode getQuickActionMode() {
      return this.quickActionMode;
   }

   private void setQuickActionMode(RewardsMenu.QuickActionMode mode) {
      this.quickActionMode = mode;
   }

   private RewardsMenu.QuickActionMode getNextQuickActionMode(RewardsMenu.QuickActionMode current) {
      return current == RewardsMenu.QuickActionMode.DELETE ? RewardsMenu.QuickActionMode.GET_INSTANCE : RewardsMenu.QuickActionMode.DELETE;
   }

   private String getQuickActionModeDisplay(RewardsMenu.QuickActionMode mode) {
      return mode == RewardsMenu.QuickActionMode.GET_INSTANCE
         ? this.plugin.getMessageManager().getMessage("gui.rewards.quick_mode.mode_get_instance", false)
         : this.plugin.getMessageManager().getMessage("gui.rewards.quick_mode.mode_delete", false);
   }

   private String getQuickActionActionDisplay(RewardsMenu.QuickActionMode mode) {
      return mode == RewardsMenu.QuickActionMode.GET_INSTANCE
         ? this.plugin.getMessageManager().getMessage("gui.rewards.quick_action.get_instance", false)
         : this.plugin.getMessageManager().getMessage("gui.rewards.quick_action.delete", false);
   }

   private void updateQuickActionDisplay() {
      RewardsMenu.QuickActionMode mode = this.getQuickActionMode();
      ItemStack leverIcon = new ItemBuilder(XMaterial.LEVER)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.quick_mode.name", false))
         .setLore(
            this.plugin
               .getMessageManager()
               .getMessageList(
                  "gui.rewards.quick_mode.lore", false, "%mode%", this.getQuickActionModeDisplay(mode), "%action%", this.getQuickActionActionDisplay(mode)
               )
         )
         .build();
      this.updateSlot(52, leverIcon);
      int startIndex = this.page * this.maxItemsPerPage;
      int endIndex = Math.min(startIndex + this.maxItemsPerPage, this.rewards.size());

      for (int i = startIndex; i < endIndex; i++) {
         Reward reward = this.rewards.get(i);
         int displayIndex = i - startIndex;
         int slot = this.getSlotForPagedIndex(displayIndex);
         ItemStack updatedDisplay = this.createRewardDisplay(reward, i + 1);
         this.updateSlot(slot, updatedDisplay);
      }
   }

   private void giveRewardInstance(Reward reward) {
      if (reward.getType() == RewardType.ITEM) {
         ItemStack item = ItemSerializer.deserialize(reward.getValue());
         if (item != null && item.getType() != XMaterial.AIR.get()) {
            ItemStack toGive = item.clone();
            Map<Integer, ItemStack> leftover = this.playerMenuUtility.getInventory().addItem(new ItemStack[]{toGive});
            if (!leftover.isEmpty()) {
               leftover.values().forEach(stack -> this.playerMenuUtility.getWorld().dropItemNaturally(this.playerMenuUtility.getLocation(), stack));
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.instance_dropped"));
            } else {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.instance_given"));
            }
         } else {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.instance_invalid"));
         }
      } else {
         if (reward.getType() == RewardType.COMMAND) {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.command_preview", "%command%", reward.getValue()));
         } else if (reward.getType() == RewardType.CHAT_MESSAGE) {
            this.playerMenuUtility
               .sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.chat_message_preview", "%message%", reward.getValue()));
         } else if (reward.getType() == RewardType.CHAT_MESSAGE_BROADCAST) {
            this.playerMenuUtility
               .sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.broadcast_message_preview", "%message%", reward.getValue()));
         }
      }
   }

   private ItemStack createBroadcastMessageRewardDisplay(Reward reward, String chanceLore, int rewardNumber) {
      String message = reward.getValue();
      String displayMessage = message.length() > 40 ? message.substring(0, 37) + "..." : message;
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.broadcast_message_label", false));
      lore.add(ChatColor.WHITE + displayMessage);
      if (message.length() > 40) {
         lore.add("");
         lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.truncated.message", false));
      }

      lore.add("");
      lore.add(ChatColor.DARK_GRAY + "─────────────");
      lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.reward_number", false, "%number%", String.valueOf(rewardNumber)));
      lore.add(chanceLore);
      lore.add("");
      lore.add(
         this.plugin
            .getMessageManager()
            .getMessage("gui.rewards.click_to_edit", false, "%action%", this.getQuickActionActionDisplay(this.getQuickActionMode()))
      );
      return new ItemBuilder(XMaterial.BELL)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.broadcast_message_name", false))
         .setLore(lore)
         .build();
   }

   private ItemStack createChatMessageRewardDisplay(Reward reward, String chanceLore, int rewardNumber) {
      String message = reward.getValue();
      String displayMessage = message.length() > 40 ? message.substring(0, 37) + "..." : message;
      List<String> lore = new ArrayList<>();
      lore.add("");
      lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.chat_message_label", false));
      lore.add(ChatColor.WHITE + displayMessage);
      if (message.length() > 40) {
         lore.add("");
         lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.truncated.message", false));
      }

      lore.add("");
      lore.add(ChatColor.DARK_GRAY + "─────────────");
      lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.reward_number", false, "%number%", String.valueOf(rewardNumber)));
      lore.add(chanceLore);
      lore.add("");
      lore.add(
         this.plugin
            .getMessageManager()
            .getMessage("gui.rewards.click_to_edit", false, "%action%", this.getQuickActionActionDisplay(this.getQuickActionMode()))
      );
      return new ItemBuilder(XMaterial.WRITABLE_BOOK)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.chat_message_name", false))
         .setLore(lore)
         .build();
   }

   public void promptAddCommandReward() {
      this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.prompt.command"));
      this.plugin.getChatInputListener().requestInput(this.playerMenuUtility, command -> {
         if (command != null && !command.isEmpty()) {
            if (this.plugin.getRewardManager().isCommandBlacklisted(command)) {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.rewards.command_blacklisted"));
               this.open();
            } else {
               this.addReward(new Reward(RewardType.COMMAND, 100.0, null, null, command));
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.added"));
               this.open();
            }
         } else {
            this.open();
         }
      });
   }

   public void updateRewardChance(int rewardIndex, double newChance) {
      if (rewardIndex >= 0 && rewardIndex < this.rewards.size()) {
         Reward oldReward = this.rewards.get(rewardIndex);
         Reward newReward = new Reward(oldReward.getType(), newChance, oldReward.getMessage(), oldReward.getBroadcast(), oldReward.getValue());
         this.rewards.set(rewardIndex, newReward);
         this.saveRewards();
      }
   }

   public void updateRewardMessage(int rewardIndex, String message) {
      if (rewardIndex >= 0 && rewardIndex < this.rewards.size()) {
         Reward oldReward = this.rewards.get(rewardIndex);
         Reward newReward = new Reward(oldReward.getType(), oldReward.getChance(), message, oldReward.getBroadcast(), oldReward.getValue());
         this.rewards.set(rewardIndex, newReward);
         this.saveRewards();
      }
   }

   public void updateRewardBroadcast(int rewardIndex, String broadcast) {
      if (rewardIndex >= 0 && rewardIndex < this.rewards.size()) {
         Reward oldReward = this.rewards.get(rewardIndex);
         Reward newReward = new Reward(oldReward.getType(), oldReward.getChance(), oldReward.getMessage(), broadcast, oldReward.getValue());
         this.rewards.set(rewardIndex, newReward);
         this.saveRewards();
      }
   }

   public void updateRewardValue(int rewardIndex, String value) {
      if (rewardIndex >= 0 && rewardIndex < this.rewards.size()) {
         Reward oldReward = this.rewards.get(rewardIndex);
         Reward newReward = new Reward(oldReward.getType(), oldReward.getChance(), oldReward.getMessage(), oldReward.getBroadcast(), value);
         this.rewards.set(rewardIndex, newReward);
         this.saveRewards();
      }
   }

   public void deleteReward(int rewardIndex) {
      if (rewardIndex >= 0 && rewardIndex < this.rewards.size()) {
         this.rewards.remove(rewardIndex);
         this.saveRewards();
      }
   }

   public void addReward(Reward reward) {
      this.rewards.add(reward);
      this.saveRewards();
   }

   private void saveRewards() {
      this.holder.saveRewards(new ArrayList<>(this.rewards));
   }

   private String formatChance(double chance) {
      return chance == (int)chance ? String.valueOf((int)chance) : String.format("%.3f", chance);
   }

   private void showTreasureInfo(Player player, Treasure treasure) {
      String uuid = treasure.getId().toString();
      String material = treasure.getMaterial();
      String blockStateFull = treasure.getBlockState() != null ? treasure.getBlockState() : "";
      String blockStateDisplay = blockStateFull.length() > 40 ? blockStateFull.substring(0, 40) + "..." : blockStateFull;
      String nbtFull = treasure.getNbtData() != null ? treasure.getNbtData() : "-";
      String nbtDisplay = nbtFull.length() > 60 ? nbtFull.substring(0, 60) + "..." : nbtFull;
      Location loc = treasure.getLocation();
      int rewardsCount = treasure.getRewards().size();
      String locationStr = loc != null
         ? this.plugin
            .getMessageManager()
            .getMessage(
               "gui.rewards.treasure_info.info.location",
               false,
               "%x%",
               String.valueOf(loc.getBlockX()),
               "%y%",
               String.valueOf(loc.getBlockY()),
               "%z%",
               String.valueOf(loc.getBlockZ()),
               "%world%",
               loc.getWorld() != null ? loc.getWorld().getName() : "?"
            )
         : "-";
      String uuidLineTemplate = this.plugin.getMessageManager().getMessage("gui.rewards.treasure_info.info.uuid", false, "%uuid%", uuid);
      String nbtLineTemplate = this.plugin.getMessageManager().getMessage("gui.rewards.treasure_info.info.nbt", false, "%nbtData%", nbtDisplay);
      String blockStateLineTemplate = this.plugin
         .getMessageManager()
         .getMessage("gui.rewards.treasure_info.info.block_states_line", false, "%block_states%", blockStateDisplay);
      String hoverUuid = this.plugin.getMessageManager().getMessage("gui.rewards.treasure_info.info.click_to_copy_uuid", false);
      String hoverNbt = this.plugin.getMessageManager().getMessage("gui.rewards.treasure_info.info.click_to_copy_nbt", false);
      String hoverBlockState = this.plugin.getMessageManager().getMessage("gui.rewards.treasure_info.info.click_to_copy_blockstate", false);
      String hoverAll = this.plugin.getMessageManager().getMessage("gui.rewards.treasure_info.info.click_to_copy_all", false);
      String copyAll = "UUID: "
         + uuid
         + " | Material: "
         + material
         + (!blockStateFull.isEmpty() ? " | BlockState: " + blockStateFull : "")
         + (!nbtFull.equals("-") ? " | NBT: " + nbtFull : "")
         + " | Location: "
         + locationStr
         + " | Rewards: "
         + rewardsCount;

      for (String line : this.plugin
         .getMessageManager()
         .getMessageList(
            "gui.rewards.treasure_info.info.lines",
            false,
            "%uuid%",
            uuidLineTemplate,
            "%count%",
            String.valueOf(rewardsCount),
            "%block%",
            material,
            "%blockStateLine%",
            blockStateLineTemplate,
            "%nbtData%",
            nbtLineTemplate,
            "%location%",
            locationStr
         )) {
         if (line.contains(uuid)) {
            PlatformAccess.get().sendClickableCopyText(player, line, uuid, hoverUuid);
         } else if (!nbtFull.equals("-") && line.contains(nbtDisplay.replace("...", "").substring(0, Math.min(20, nbtDisplay.length())))) {
            PlatformAccess.get().sendClickableCopyText(player, line, nbtFull, hoverNbt);
         } else if (!blockStateFull.isEmpty() && line.contains(blockStateDisplay.replace("...", "").substring(0, Math.min(20, blockStateDisplay.length())))) {
            PlatformAccess.get().sendClickableCopyText(player, line, blockStateFull, hoverBlockState);
         } else if (!line.contains("%copyall%") && !line.contains("[COPY ALL DATA]")) {
            player.sendMessage(line);
         } else {
            PlatformAccess.get().sendClickableCopyText(player, line, copyAll, hoverAll);
         }
      }
   }

   private static enum QuickActionMode {
      DELETE,
      GET_INSTANCE;
   }
}
