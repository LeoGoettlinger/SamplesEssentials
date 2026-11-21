package de.theredend2000.advancedhunt.menu.minigame;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.lib.xseries.XSound;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitTask;

public class MemoryMinigameMenu extends MinigameMenu {
   private final List<Integer> sequence;
   private final List<Integer> playerSequence;
   private final Random random;
   private int round;
   private boolean showingSequence;
   private final int maxRounds;
   private final int displayTime;
   private final int[] GAME_SLOTS = new int[]{11, 13, 15, 29, 31, 33};
   private final XMaterial[] COLORS = new XMaterial[]{
      XMaterial.RED_WOOL, XMaterial.ORANGE_WOOL, XMaterial.YELLOW_WOOL, XMaterial.LIME_WOOL, XMaterial.BLUE_WOOL, XMaterial.PURPLE_WOOL
   };

   public MemoryMinigameMenu(Player player, Main plugin, Consumer<Boolean> onFinish) {
      super(player, plugin, onFinish);
      this.sequence = new ArrayList<>();
      this.playerSequence = new ArrayList<>();
      this.random = new Random();
      this.round = 0;
      this.maxRounds = plugin.getConfig().getInt("hint.minigames.memory.rounds", 5);
      this.displayTime = plugin.getConfig().getInt("hint.minigames.memory.display-time", 20);
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.minigame.memory.title", false);
   }

   @Override
   public int getSlots() {
      return 45;
   }

   @Override
   public void setMenuItems() {
      this.fillBackground(new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());

      for (int i = 0; i < this.GAME_SLOTS.length; i++) {
         this.updateSlot(
            this.GAME_SLOTS[i],
            new ItemBuilder(this.COLORS[i])
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.memory.button.name", false, "%number%", String.valueOf(i + 1)))
               .build()
         );
      }

      this.startRound();
   }

   private void startRound() {
      this.round++;
      this.playerSequence.clear();
      this.showingSequence = true;
      this.sequence.add(this.GAME_SLOTS[this.random.nextInt(this.GAME_SLOTS.length)]);
      this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.memory.watch"));
      int[] index = new int[]{0};
      BukkitTask[] task = new BukkitTask[1];
      task[0] = this.scheduleTaskTimer(() -> {
         if (this.finished) {
            if (task[0] != null) {
               task[0].cancel();
            }
         } else if (index[0] >= this.sequence.size()) {
            this.showingSequence = false;
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.memory.repeat"));
            if (task[0] != null) {
               task[0].cancel();
            }
         } else {
            int slot = this.sequence.get(index[0]);
            this.flashSlot(slot);
            index[0]++;
         }
      }, this.displayTime, this.displayTime);
   }

   private void flashSlot(int slot) {
      int colorIndex = -1;

      for (int i = 0; i < this.GAME_SLOTS.length; i++) {
         if (this.GAME_SLOTS[i] == slot) {
            colorIndex = i;
            break;
         }
      }

      if (colorIndex != -1) {
         this.updateSlot(
            slot,
            new ItemBuilder(XMaterial.WHITE_WOOL)
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.memory.button.name", false, "%number%", String.valueOf(colorIndex + 1)))
               .build()
         );
         this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0F, 1.0F + colorIndex * 0.2F);
         int finalColorIndex = colorIndex;
         this.scheduleTask(
            () -> {
               if (!this.finished) {
                  this.updateSlot(
                     slot,
                     new ItemBuilder(this.COLORS[finalColorIndex])
                        .setDisplayName(
                           this.plugin
                              .getMessageManager()
                              .getMessage("gui.minigame.memory.button.name", false, "%number%", String.valueOf(finalColorIndex + 1))
                        )
                        .build()
                  );
               }
            },
            10L
         );
      }
   }

   @Override
   public void handleMenu(InventoryClickEvent event) {
      if (!this.showingSequence && !this.finished) {
         int slot = event.getSlot();
         boolean isGameSlot = false;

         for (int s : this.GAME_SLOTS) {
            if (s == slot) {
               isGameSlot = true;
               break;
            }
         }

         if (isGameSlot) {
            this.flashSlot(slot);
            this.playerSequence.add(slot);
            int currentIndex = this.playerSequence.size() - 1;
            if (this.playerSequence.get(currentIndex).equals(this.sequence.get(currentIndex))) {
               if (this.playerSequence.size() == this.sequence.size()) {
                  if (this.round >= this.maxRounds) {
                     this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0F, 1.0F);
                     this.finish(true);
                  } else {
                     this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 1.0F, 1.0F);
                     this.scheduleTask(this::startRound, 30L);
                  }
               }
            } else {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.memory.wrong", true));
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_VILLAGER_NO.get(), 1.0F, 1.0F);
               this.finish(false);
            }
         }
      }
   }
}
