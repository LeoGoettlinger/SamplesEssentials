package de.theredend2000.advancedhunt.menu.reward;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.common.SingleItemInputMenu;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardType;
import de.theredend2000.advancedhunt.util.ItemSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AddItemRewardMenu extends SingleItemInputMenu {
   private final RewardsMenu parentMenu;
   private final Menu lastMenu;
   private final boolean edit;
   private final int rewardIndex;

   public AddItemRewardMenu(Player player, Main plugin, RewardsMenu parentMenu, Menu lastMenu, boolean edit, int rewardIndex) {
      super(player, plugin, lastMenu, true);
      this.parentMenu = parentMenu;
      this.lastMenu = lastMenu;
      this.edit = edit;
      this.rewardIndex = rewardIndex;
   }

   @Override
   protected String getGuiKeyBase() {
      return "gui.rewards.add.item";
   }

   @Override
   protected String getNoItemErrorKey() {
      return "error.rewards.add.item.no_item";
   }

   @Override
   protected void onConfirm(ItemStack itemSnapshot) {
      if (this.edit) {
         String serialized = ItemSerializer.serialize(itemSnapshot);
         this.parentMenu.updateRewardValue(this.rewardIndex, serialized);
         this.playerMenuUtility
            .sendMessage(
               this.plugin
                  .getMessageManager()
                  .getMessage("feedback.rewards.updated", "%type%", this.plugin.getMessageManager().getMessage("common.value", false))
            );
         this.parentMenu.open();
      } else {
         String serialized = ItemSerializer.serialize(itemSnapshot);
         Reward reward = new Reward(RewardType.ITEM, 100.0, null, null, serialized);
         this.parentMenu.addReward(reward);
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.rewards.added"));
         this.parentMenu.open();
      }
   }

   @Override
   protected void onCancel() {
      this.lastMenu.open();
   }
}
