package de.theredend2000.advancedhunt.menu.minigame;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.lib.xseries.XSound;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.Random;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitTask;

public class ReactionMinigameMenu extends MinigameMenu {
   private int currentSlot;
   private int currentCount;
   private boolean clickedCorrectSlot;
   private int lastClicked;
   private final Random random = new Random();
   private BukkitTask failTask;
   private boolean active;
   private final int hintCount;
   private final int updateTime;

   public ReactionMinigameMenu(Player player, Main plugin, Consumer<Boolean> onFinish) {
      super(player, plugin, onFinish);
      this.hintCount = plugin.getConfig().getInt("hint.minigames.reaction.required-clicks", 5);
      this.updateTime = plugin.getConfig().getInt("hint.minigames.reaction.update-time", 20);
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.minigame.reaction.title", false);
   }

   @Override
   public int getSlots() {
      return 54;
   }

   @Override
   public void setMenuItems() {
      this.clickedCorrectSlot = true;
      this.currentCount = 0;
      this.lastClicked = -1;
      this.active = true;
      this.fillBackground(new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE).hideTooltip(true).build());
      this.scheduleTask(() -> this.updateFrame(false), 10L);
   }

   private void restartFailedTask() {
      if (this.failTask != null) {
         this.failTask.cancel();
         this.activeTasks.remove(this.failTask);
      }

      this.failTask = this.scheduleTask(() -> this.updateFrame(true), this.updateTime + 5);
   }

   private void updateFrame(boolean timeout) {
      if (this.active && !this.finished) {
         if (timeout) {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.reaction.timeout", true));
            this.finish(false);
         } else if (!this.clickedCorrectSlot) {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.reaction.wrong_click", true));
            this.finish(false);
         } else {
            if (this.currentSlot >= 0 && this.currentSlot < this.getSlots()) {
               this.updateSlot(this.currentSlot, new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE).hideTooltip(true).build());
            }

            this.currentSlot = this.getRandomSlot();
            this.updateSlot(
               this.currentSlot,
               new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE)
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.reaction.click.name", false))
                  .setLore(
                     this.plugin
                        .getMessageManager()
                        .getMessageList(
                           "gui.minigame.reaction.click.lore",
                           false,
                           "%current%",
                           String.valueOf(this.currentCount + 1),
                           "%required%",
                           String.valueOf(this.hintCount)
                        )
                  )
                  .build()
            );
            this.clickedCorrectSlot = false;
         }
      }
   }

   private int getRandomSlot() {
      int nextNum;
      do {
         nextNum = this.random.nextInt(this.getSlots());
      } while (nextNum == this.lastClicked);

      return nextNum;
   }

   @Override
   public void handleMenu(InventoryClickEvent event) {
      if (this.active && !this.finished) {
         if (event.getSlot() == this.currentSlot) {
            this.currentCount++;
            this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0F, 2.0F);
            if (this.currentCount >= this.hintCount) {
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0F, 1.0F);
               this.finish(true);
               return;
            }

            this.clickedCorrectSlot = true;
            this.lastClicked = this.currentSlot;
            this.scheduleTask(() -> this.updateFrame(false), 5L);
            this.restartFailedTask();
         } else {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.reaction.wrong_click", true));
            this.finish(false);
         }
      }
   }

   @Override
   protected void finish(boolean success) {
      this.active = false;
      super.finish(success);
   }
}
