package de.theredend2000.advancedhunt.menu.reward;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.common.ConfirmationMenu;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardType;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import de.theredend2000.advancedhunt.util.ItemSerializer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RewardActionMenu extends Menu {
   private final RewardsMenu parentMenu;
   private final Reward reward;
   private final int rewardIndex;

   public RewardActionMenu(Player player, Main plugin, RewardsMenu parentMenu, Reward reward, int rewardIndex) {
      super(player, plugin);
      this.parentMenu = parentMenu;
      this.reward = reward;
      this.rewardIndex = rewardIndex;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.rewards.editor.title", false, "%number%", String.valueOf(this.rewardIndex + 1));
   }

   @Override
   public int getSlots() {
      return 27;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void setMenuItems() {
      this.fillBorders(this.FILLER_GLASS);
      ItemStack rewardDisplay = this.createRewardPreview();
      this.addStaticItem(4, rewardDisplay);
      ItemStack editChance = new ItemBuilder(XMaterial.CLOCK)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.editor.edit_chance.name", false))
         .setLore(
            this.plugin
               .getMessageManager()
               .getMessageList("gui.rewards.editor.edit_chance.lore", false, "%chance%", this.formatChance(this.reward.getChance()))
         )
         .build();
      this.addButton(this.canHaveMessage(this.reward.getType()) ? 11 : 12, editChance, e -> this.editChance());
      String displayValue;
      if (this.reward.getType() != RewardType.ITEM) {
         displayValue = this.truncate(this.reward.getValue(), 30);
      } else {
         displayValue = ItemSerializer.deserialize(this.reward.getValue()).getType().name();
      }

      String displayMessage = this.truncate(this.formatMessage(this.reward.getMessage()), 30);
      String displayBroadcast = this.truncate(this.formatMessage(this.reward.getBroadcast()), 30);
      ItemStack message = new ItemBuilder(XMaterial.WRITABLE_BOOK)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.editor.edit_message.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.editor.edit_message.lore", false, "%message%", displayMessage))
         .build();
      this.addButton(this.canHaveMessage(this.reward.getType()) ? 14 : 26, message, this::editMessage);
      ItemStack broadcast = new ItemBuilder(XMaterial.BELL)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.editor.edit_broadcast.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.editor.edit_broadcast.lore", false, "%broadcast%", displayBroadcast))
         .build();
      this.addButton(this.canHaveMessage(this.reward.getType()) ? 15 : 26, broadcast, this::editBroadcast);
      ItemStack value = new ItemBuilder(XMaterial.CAULDRON)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.editor.edit_value.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.editor.edit_value.lore", false, "%value%", displayValue))
         .build();
      this.addButton(this.canHaveMessage(this.reward.getType()) ? 12 : 14, value, e -> this.editValue());
      ItemStack delete = new ItemBuilder(XMaterial.RED_CONCRETE)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.editor.delete.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.editor.delete.lore", false))
         .build();
      this.addButton(26, delete, e -> this.confirmDelete());
      this.fillBackground(this.FILLER_GLASS);
   }

   private ItemStack createRewardPreview() {
      if (this.reward.getType() == RewardType.ITEM) {
         ItemStack item = ItemSerializer.deserialize(this.reward.getValue());
         if (item != null && item.getType() != XMaterial.AIR.get()) {
            ItemBuilder builder = new ItemBuilder(item.clone());
            List<String> lore = new ArrayList<>();
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
               lore.addAll(item.getItemMeta().getLore());
            }

            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "─────────────");
            lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.chance_lore", false, "%chance%", this.formatChance(this.reward.getChance())));
            String messageValue = null;
            if (this.reward.getMessage() != null) {
               messageValue = this.reward.getMessage().length() > 30 ? this.reward.getMessage().substring(0, 27) + "..." : this.reward.getMessage();
            }

            lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.message_lore", false, "%message%", this.formatMessage(messageValue)));
            String broadcastValue = null;
            if (this.reward.getBroadcast() != null) {
               broadcastValue = this.reward.getBroadcast().length() > 30 ? this.reward.getBroadcast().substring(0, 27) + "..." : this.reward.getBroadcast();
            }

            lore.add(this.plugin.getMessageManager().getMessage("gui.rewards.broadcast_lore", false, "%broadcast%", this.formatMessage(broadcastValue)));
            return builder.setLore(lore).build();
         }
      }

      if (this.reward.getType() == RewardType.COMMAND) {
         String displayValue = "/" + (this.reward.getValue().length() > 30 ? this.reward.getValue().substring(0, 27) + "..." : this.reward.getValue());
         List<String> lorex = new ArrayList<>();
         lorex.add("");
         lorex.add(this.plugin.getMessageManager().getMessage("gui.rewards.command_label", false));
         lorex.add(ChatColor.WHITE + displayValue);
         lorex.add("");
         lorex.add(ChatColor.DARK_GRAY + "─────────────");
         lorex.add(this.plugin.getMessageManager().getMessage("gui.rewards.chance_lore", false, "%chance%", this.formatChance(this.reward.getChance())));
         String messageValuex = null;
         if (this.reward.getMessage() != null) {
            messageValuex = this.reward.getMessage().length() > 30 ? this.reward.getMessage().substring(0, 27) + "..." : this.reward.getMessage();
         }

         lorex.add(this.plugin.getMessageManager().getMessage("gui.rewards.message_lore", false, "%message%", this.formatMessage(messageValuex)));
         String broadcastValue = null;
         if (this.reward.getBroadcast() != null) {
            broadcastValue = this.reward.getBroadcast().length() > 30 ? this.reward.getBroadcast().substring(0, 27) + "..." : this.reward.getBroadcast();
         }

         lorex.add(this.plugin.getMessageManager().getMessage("gui.rewards.broadcast_lore", false, "%broadcast%", this.formatMessage(broadcastValue)));
         return new ItemBuilder(XMaterial.PLAYER_HEAD)
            .setSkullTexture(
               "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWI2Y2VlOGZkYTdlZjBiM2FlMGViMDU3OWQ1Njc2Y2UzNmFmN2VmYzU3NGQ4ODcyOGYzODk0ZjZiMTY2NTM4In19fQ=="
            )
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.command_name", false))
            .setLore(lorex)
            .build();
      } else if (this.reward.getType() == RewardType.CHAT_MESSAGE) {
         String displayValuex = this.reward.getValue().length() > 30 ? this.reward.getValue().substring(0, 27) + "..." : this.reward.getValue();
         return new ItemBuilder(XMaterial.WRITABLE_BOOK)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.chat_message_name", false))
            .setLore(
               "",
               this.plugin.getMessageManager().getMessage("gui.rewards.chat_message_label", false),
               ChatColor.WHITE + displayValuex,
               "",
               ChatColor.DARK_GRAY + "─────────────",
               this.plugin.getMessageManager().getMessage("gui.rewards.chance_lore", false, "%chance%", this.formatChance(this.reward.getChance()))
            )
            .build();
      } else if (this.reward.getType() == RewardType.CHAT_MESSAGE_BROADCAST) {
         String displayValuex = this.reward.getValue().length() > 30 ? this.reward.getValue().substring(0, 27) + "..." : this.reward.getValue();
         return new ItemBuilder(Material.BELL)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.broadcast_message_name", false))
            .setLore(
               "",
               this.plugin.getMessageManager().getMessage("gui.rewards.broadcast_message_label", false),
               ChatColor.WHITE + displayValuex,
               "",
               ChatColor.DARK_GRAY + "─────────────",
               this.plugin.getMessageManager().getMessage("gui.rewards.chance_lore", false, "%chance%", this.formatChance(this.reward.getChance()))
            )
            .build();
      } else {
         return new ItemBuilder(Material.BARRIER)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.invalid_item.name", false))
            .setLore(
               this.plugin.getMessageManager().getMessageList("gui.rewards.invalid_item.lore", false, "%chance%", this.formatChance(this.reward.getChance()))
            )
            .build();
      }
   }

   private void editChance() {
      this.playerMenuUtility
         .sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.prompt.new_chance", "%current%", this.formatChance(this.reward.getChance())));
      this.plugin
         .getChatInputListener()
         .requestInput(
            this.playerMenuUtility,
            chanceStr -> {
               try {
                  double chance = Double.parseDouble(chanceStr);
                  if (chance < 0.0 || chance > 100.0) {
                     this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.rewards.invalid_chance"));
                     this.parentMenu.open();
                     return;
                  }

                  this.parentMenu.updateRewardChance(this.rewardIndex, chance);
                  this.playerMenuUtility
                     .sendMessage(
                        this.plugin
                           .getMessageManager()
                           .getMessage("feedback.rewards.updated", "%type%", this.plugin.getMessageManager().getMessage("common.chance", false))
                     );
                  this.parentMenu.open();
               } catch (NumberFormatException var4) {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.rewards.invalid_chance"));
                  this.parentMenu.open();
               }
            }
         );
   }

   private void editMessage(InventoryClickEvent e) {
      ClickType clickType = e.getClick();
      if (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT) {
         this.playerMenuUtility
            .sendMessage(
               this.plugin.getMessageManager().getMessage("feedback.rewards.prompt.new_message", "%current%", this.formatMessage(this.reward.getMessage()))
            );
         this.plugin
            .getChatInputListener()
            .requestInput(
               this.playerMenuUtility,
               message -> {
                  this.parentMenu.updateRewardMessage(this.rewardIndex, message);
                  this.playerMenuUtility
                     .sendMessage(
                        this.plugin
                           .getMessageManager()
                           .getMessage("feedback.rewards.updated", "%type%", this.plugin.getMessageManager().getMessage("common.message", false))
                     );
                  this.parentMenu.open();
               }
            );
      } else if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
         this.parentMenu.updateRewardMessage(this.rewardIndex, null);
         this.playerMenuUtility
            .sendMessage(
               this.plugin
                  .getMessageManager()
                  .getMessage("feedback.rewards.reset", "%type%", this.plugin.getMessageManager().getMessage("common.message", false))
            );
         this.parentMenu.open();
      }
   }

   private void editBroadcast(InventoryClickEvent e) {
      ClickType clickType = e.getClick();
      if (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT) {
         this.playerMenuUtility
            .sendMessage(
               this.plugin.getMessageManager().getMessage("feedback.rewards.prompt.new_broadcast", "%current%", this.formatMessage(this.reward.getBroadcast()))
            );
         this.plugin
            .getChatInputListener()
            .requestInput(
               this.playerMenuUtility,
               message -> {
                  this.parentMenu.updateRewardBroadcast(this.rewardIndex, message);
                  this.playerMenuUtility
                     .sendMessage(
                        this.plugin
                           .getMessageManager()
                           .getMessage("feedback.rewards.updated", "%type%", this.plugin.getMessageManager().getMessage("common.broadcast", false))
                     );
                  this.parentMenu.open();
               }
            );
      } else if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
         this.parentMenu.updateRewardBroadcast(this.rewardIndex, null);
         this.playerMenuUtility
            .sendMessage(
               this.plugin
                  .getMessageManager()
                  .getMessage("feedback.rewards.reset", "%type%", this.plugin.getMessageManager().getMessage("common.broadcast", false))
            );
         this.parentMenu.open();
      }
   }

   private void editValue() {
      if (this.reward.getType() != RewardType.ITEM) {
         this.playerMenuUtility
            .sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.prompt.new_value", "%current%", this.reward.getValue()));
         this.plugin
            .getChatInputListener()
            .requestInput(
               this.playerMenuUtility,
               value -> {
                  this.parentMenu.updateRewardValue(this.rewardIndex, value);
                  this.playerMenuUtility
                     .sendMessage(
                        this.plugin
                           .getMessageManager()
                           .getMessage("feedback.rewards.updated", "%type%", this.plugin.getMessageManager().getMessage("common.value", false))
                     );
                  this.parentMenu.open();
               }
            );
      } else {
         new AddItemRewardMenu(this.playerMenuUtility, this.plugin, this.parentMenu, this, true, this.rewardIndex).open();
      }
   }

   private void confirmDelete() {
      String title = this.plugin.getMessageManager().getMessage("gui.rewards.editor.confirm_delete.title", false);
      ConfirmationMenu confirmMenu = new ConfirmationMenu(this.playerMenuUtility, this.plugin, title, e -> {
         this.parentMenu.deleteReward(this.rewardIndex);
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.deleted"));
         this.parentMenu.open();
      }, e -> this.open());
      confirmMenu.open();
   }

   private String formatChance(double chance) {
      return chance == (int)chance ? String.valueOf((int)chance) : String.format("%.3f", chance);
   }

   private String formatMessage(String message) {
      return message == null ? this.plugin.getMessageManager().getMessage("gui.common.none", false) : message;
   }

   private boolean canHaveMessage(RewardType rewardType) {
      return rewardType == RewardType.COMMAND || rewardType == RewardType.ITEM;
   }

   private String truncate(String text, int maxLength) {
      return text != null && text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
   }
}
