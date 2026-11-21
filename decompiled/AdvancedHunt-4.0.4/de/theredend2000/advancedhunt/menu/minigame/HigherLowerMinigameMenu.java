package de.theredend2000.advancedhunt.menu.minigame;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.lib.xseries.XSound;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.Random;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class HigherLowerMinigameMenu extends MinigameMenu {
   private static final int TOTAL_SLOTS = 54;
   private static final int BUTTON_START = 9;
   private static final int BUTTON_END = 35;
   private static final int HINT_BAR_START = 36;
   private static final int FEEDBACK_START = 45;
   private final int maxNumber;
   private final int maxGuesses;
   private final Random random = new Random();
   private int secretNumber;
   private int guessesLeft;
   private int lowerBound;
   private int upperBound;

   public HigherLowerMinigameMenu(Player player, Main plugin, Consumer<Boolean> onFinish) {
      super(player, plugin, onFinish);
      this.maxNumber = Math.min(plugin.getConfig().getInt("hint.minigames.higherlower.max-number", 15), 27);
      this.maxGuesses = plugin.getConfig().getInt("hint.minigames.higherlower.max-guesses", 5);
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.minigame.higherlower.title", false);
   }

   @Override
   public int getSlots() {
      return 54;
   }

   @Override
   public void setMenuItems() {
      this.secretNumber = this.random.nextInt(this.maxNumber) + 1;
      this.guessesLeft = this.maxGuesses;
      this.lowerBound = 1;
      this.upperBound = this.maxNumber;
      this.fillBackground(new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());
      this.renderNumberButtons();
      this.renderHintBar();
      this.renderFeedback(null);
      this.playerMenuUtility
         .sendMessage(
            this.plugin
               .getMessageManager()
               .getMessage("minigame.higherlower.start", true, "%max%", String.valueOf(this.maxNumber), "%guesses%", String.valueOf(this.maxGuesses))
         );
   }

   private void renderNumberButtons() {
      for (int n = 1; n <= this.maxNumber; n++) {
         int slot = 9 + (n - 1);
         boolean eliminated = n < this.lowerBound || n > this.upperBound;
         if (eliminated) {
            this.updateSlot(
               slot,
               new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE)
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.higherlower.number.eliminated", false, "%n%", String.valueOf(n)))
                  .build()
            );
         } else {
            this.updateSlot(
               slot,
               new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE)
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.higherlower.number.active", false, "%n%", String.valueOf(n)))
                  .build()
            );
         }
      }

      for (int slot = 9 + this.maxNumber; slot <= 35; slot++) {
         this.updateSlot(slot, new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());
      }
   }

   private void renderHintBar() {
      for (int i = 0; i < 9; i++) {
         int slot = 36 + i;
         if (i < this.guessesLeft) {
            this.updateSlot(
               slot,
               new ItemBuilder(XMaterial.YELLOW_STAINED_GLASS_PANE)
                  .setDisplayName(
                     this.plugin
                        .getMessageManager()
                        .getMessage("gui.minigame.higherlower.guesses.remaining", false, "%left%", String.valueOf(this.guessesLeft))
                  )
                  .build()
            );
         } else {
            this.updateSlot(slot, new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());
         }
      }
   }

   private void renderFeedback(String direction) {
      for (int i = 0; i < 9; i++) {
         this.updateSlot(45 + i, new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());
      }

      if (direction == null) {
         this.updateSlot(
            49,
            new ItemBuilder(XMaterial.PAPER)
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.higherlower.feedback.neutral", false))
               .build()
         );
      } else if ("higher".equals(direction)) {
         for (int i = 2; i <= 6; i++) {
            this.updateSlot(
               45 + i,
               new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE)
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.higherlower.feedback.higher", false))
                  .build()
            );
         }
      } else if ("lower".equals(direction)) {
         for (int i = 2; i <= 6; i++) {
            this.updateSlot(
               45 + i,
               new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE)
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.higherlower.feedback.lower", false))
                  .build()
            );
         }
      }
   }

   @Override
   public void handleMenu(InventoryClickEvent event) {
      if (!this.finished) {
         int slot = event.getSlot();
         if (slot >= 9 && slot <= 9 + this.maxNumber - 1) {
            int guess = slot - 9 + 1;
            if (guess < this.lowerBound || guess > this.upperBound) {
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.BLOCK_STONE_PLACE.get(), 0.5F, 0.5F);
            } else if (guess == this.secretNumber) {
               this.playerMenuUtility
                  .sendMessage(this.plugin.getMessageManager().getMessage("minigame.higherlower.correct", true, "%n%", String.valueOf(this.secretNumber)));
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0F, 1.0F);
               this.scheduleTask(() -> this.finish(true), 20L);
            } else {
               this.guessesLeft--;
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_VILLAGER_NO.get(), 1.0F, 1.0F);
               String direction;
               if (guess < this.secretNumber) {
                  this.lowerBound = guess + 1;
                  direction = "higher";
                  this.playerMenuUtility
                     .sendMessage(this.plugin.getMessageManager().getMessage("minigame.higherlower.higher", true, "%left%", String.valueOf(this.guessesLeft)));
               } else {
                  this.upperBound = guess - 1;
                  direction = "lower";
                  this.playerMenuUtility
                     .sendMessage(this.plugin.getMessageManager().getMessage("minigame.higherlower.lower", true, "%left%", String.valueOf(this.guessesLeft)));
               }

               this.renderNumberButtons();
               this.renderFeedback(direction);
               this.renderHintBar();
               if (this.guessesLeft <= 0) {
                  this.playerMenuUtility
                     .sendMessage(this.plugin.getMessageManager().getMessage("minigame.higherlower.lose", true, "%n%", String.valueOf(this.secretNumber)));
                  this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_VILLAGER_NO.get(), 1.0F, 0.5F);
                  this.scheduleTask(() -> this.finish(false), 40L);
               }
            }
         }
      }
   }
}
