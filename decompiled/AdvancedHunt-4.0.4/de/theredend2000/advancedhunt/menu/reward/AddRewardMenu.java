package de.theredend2000.advancedhunt.menu.reward;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardType;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AddRewardMenu extends Menu {
   private final RewardsMenu parentMenu;

   public AddRewardMenu(Player player, Main plugin, RewardsMenu parentMenu) {
      super(player, plugin);
      this.parentMenu = parentMenu;
      this.setPreviousMenu(parentMenu);
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.rewards.open_reward_option_menu.title", false);
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
      ItemStack addItem = new ItemBuilder(XMaterial.CHEST)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.add_item.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.add_item.lore", false))
         .build();
      this.addButton(10, addItem, e -> this.openAddItemRewardMenu());
      ItemStack addCommand = new ItemBuilder(XMaterial.PLAYER_HEAD)
         .setSkullTexture(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWI2Y2VlOGZkYTdlZjBiM2FlMGViMDU3OWQ1Njc2Y2UzNmFmN2VmYzU3NGQ4ODcyOGYzODk0ZjZiMTY2NTM4In19fQ=="
         )
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.add_command.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.add_command.lore", false))
         .build();
      this.addButton(12, addCommand, e -> this.promptAddCommandReward());
      ItemStack addChatMessage = new ItemBuilder(XMaterial.WRITABLE_BOOK)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.add_chat_message.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.add_chat_message.lore", false))
         .build();
      this.addButton(14, addChatMessage, e -> this.promptAddChatMessageReward());
      ItemStack addBroadcast = new ItemBuilder(XMaterial.BELL)
         .setDisplayName(this.plugin.getMessageManager().getMessage("gui.rewards.add_broadcast_message.name", false))
         .setLore(this.plugin.getMessageManager().getMessageList("gui.rewards.add_broadcast_message.lore", false))
         .build();
      this.addButton(16, addBroadcast, e -> this.promptAddBroadcastMessageReward());
      this.fillBackground(this.FILLER_GLASS);
   }

   public void openAddItemRewardMenu() {
      new AddItemRewardMenu(this.playerMenuUtility, this.plugin, this.parentMenu, this, false, -1).open();
   }

   public void promptAddChatMessageReward() {
      this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.prompt.chat_message"));
      this.plugin.getChatInputListener().requestInput(this.playerMenuUtility, message -> {
         if (message != null && !message.isEmpty()) {
            this.parentMenu.addReward(new Reward(RewardType.CHAT_MESSAGE, 100.0, null, null, message));
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.added"));
            this.parentMenu.open();
         } else {
            this.open();
         }
      });
   }

   public void promptAddBroadcastMessageReward() {
      this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.prompt.broadcast_message"));
      this.plugin.getChatInputListener().requestInput(this.playerMenuUtility, message -> {
         if (message != null && !message.isEmpty()) {
            this.parentMenu.addReward(new Reward(RewardType.CHAT_MESSAGE_BROADCAST, 100.0, null, null, message));
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.added"));
            this.parentMenu.open();
         } else {
            this.open();
         }
      });
   }

   public void promptAddCommandReward() {
      this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.prompt.command"));
      this.plugin.getChatInputListener().requestInput(this.playerMenuUtility, command -> {
         if (command != null && !command.isEmpty()) {
            if (this.plugin.getRewardManager().isCommandBlacklisted(command)) {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.rewards.command_blacklisted"));
               this.open();
            } else {
               this.parentMenu.addReward(new Reward(RewardType.COMMAND, 100.0, null, null, command));
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.added"));
               this.parentMenu.open();
            }
         } else {
            this.open();
         }
      });
   }
}
