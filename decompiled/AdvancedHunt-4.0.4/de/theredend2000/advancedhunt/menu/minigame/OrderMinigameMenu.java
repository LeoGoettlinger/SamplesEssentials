package de.theredend2000.advancedhunt.menu.minigame;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.lib.xseries.XSound;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OrderMinigameMenu extends MinigameMenu {
   private static final XMaterial[] ITEM_TYPES = new XMaterial[]{
      XMaterial.RED_WOOL,
      XMaterial.ORANGE_WOOL,
      XMaterial.YELLOW_WOOL,
      XMaterial.LIME_WOOL,
      XMaterial.BLUE_WOOL,
      XMaterial.PURPLE_WOOL,
      XMaterial.CYAN_WOOL,
      XMaterial.PINK_WOOL
   };
   private static final int TOTAL_SLOTS = 54;
   private final int maxRounds;
   private final int sequenceLength;
   private final int displayTime;
   private int round = 0;
   private final List<Integer> correctSequence = new ArrayList<>();
   private final List<Integer> playerAnswer = new ArrayList<>();
   private final List<Integer> answerSlots = new ArrayList<>();
   private final List<Integer> paletteSlots = new ArrayList<>();
   private final List<Integer> paletteTypes = new ArrayList<>();
   private boolean memorisePhase = true;
   private boolean inputPhase = false;

   public OrderMinigameMenu(Player player, Main plugin, Consumer<Boolean> onFinish) {
      super(player, plugin, onFinish);
      this.maxRounds = plugin.getConfig().getInt("hint.minigames.order.rounds", 3);
      this.sequenceLength = plugin.getConfig().getInt("hint.minigames.order.sequence-length", 4);
      this.displayTime = plugin.getConfig().getInt("hint.minigames.order.display-time", 60);
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.minigame.order.title", false);
   }

   @Override
   public int getSlots() {
      return 54;
   }

   @Override
   public void setMenuItems() {
      this.fillBackground(new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());
      this.scheduleTask(this::startRound, 10L);
   }

   private void startRound() {
      if (!this.finished) {
         this.round++;
         this.memorisePhase = true;
         this.inputPhase = false;
         this.correctSequence.clear();
         this.playerAnswer.clear();
         this.answerSlots.clear();
         this.paletteSlots.clear();
         this.paletteTypes.clear();
         this.fillBackground(new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());
         int len = Math.min(this.sequenceLength, ITEM_TYPES.length);
         int startCol = (9 - len) / 2;

         for (int i = 0; i < len; i++) {
            this.answerSlots.add(9 + startCol + i);
         }

         List<Integer> typePool = new ArrayList<>();

         for (int i = 0; i < ITEM_TYPES.length; i++) {
            typePool.add(i);
         }

         Collections.shuffle(typePool);
         List<Integer> usedTypes = typePool.subList(0, Math.min(len, typePool.size()));

         for (int i = 0; i < len; i++) {
            this.correctSequence.add(usedTypes.get((int)(Math.random() * usedTypes.size())));
         }

         for (int i = 0; i < len; i++) {
            int typeIdx = this.correctSequence.get(i);
            this.updateSlot(
               this.answerSlots.get(i),
               new ItemBuilder(ITEM_TYPES[typeIdx])
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.order.sequence.name", false, "%pos%", String.valueOf(i + 1)))
                  .build()
            );
         }

         List<Integer> palettePool = new ArrayList<>(this.correctSequence);
         Collections.shuffle(palettePool);
         int paletteStartCol = (9 - palettePool.size()) / 2;

         for (int i = 0; i < palettePool.size(); i++) {
            int slot = 36 + paletteStartCol + i;
            int typeIdx = palettePool.get(i);
            this.paletteSlots.add(slot);
            this.paletteTypes.add(typeIdx);
            this.updateSlot(slot, new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());
         }

         this.playerMenuUtility
            .sendMessage(
               this.plugin
                  .getMessageManager()
                  .getMessage("minigame.order.memorise", true, "%round%", String.valueOf(this.round), "%max%", String.valueOf(this.maxRounds))
            );
         this.scheduleTask(this::startInputPhase, this.displayTime);
      }
   }

   private void startInputPhase() {
      if (!this.finished) {
         this.memorisePhase = false;
         this.inputPhase = true;

         for (int slot : this.answerSlots) {
            this.updateSlot(
               slot,
               new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE)
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.order.empty.name", false))
                  .build()
            );
         }

         for (int i = 0; i < this.paletteSlots.size(); i++) {
            int typeIdx = this.paletteTypes.get(i);
            this.updateSlot(
               this.paletteSlots.get(i),
               new ItemBuilder(ITEM_TYPES[typeIdx])
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.order.palette.name", false))
                  .build()
            );
         }

         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.order.recreate", true));
      }
   }

   @Override
   public void handleMenu(InventoryClickEvent event) {
      if (this.inputPhase && !this.finished) {
         int slot = event.getSlot();
         int paletteIndex = this.paletteSlots.indexOf(slot);
         if (paletteIndex >= 0) {
            int typeIdx = this.paletteTypes.get(paletteIndex);
            int pos = this.playerAnswer.size();
            if (pos < this.correctSequence.size()) {
               this.updateSlot(
                  this.answerSlots.get(pos),
                  new ItemBuilder(ITEM_TYPES[typeIdx])
                     .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.order.placed.name", false, "%pos%", String.valueOf(pos + 1)))
                     .build()
               );
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0F, 1.0F + pos * 0.1F);
               this.playerAnswer.add(typeIdx);
               this.updateSlot(this.paletteSlots.get(paletteIndex), new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());
               if (typeIdx != this.correctSequence.get(pos)) {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.order.wrong", true));
                  this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_VILLAGER_NO.get(), 1.0F, 1.0F);
                  this.finish(false);
               } else {
                  if (this.playerAnswer.size() >= this.correctSequence.size()) {
                     this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 1.0F, 1.5F);
                     if (this.round >= this.maxRounds) {
                        this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0F, 1.0F);
                        this.scheduleTask(() -> this.finish(true), 20L);
                     } else {
                        this.scheduleTask(this::startRound, 40L);
                     }
                  }
               }
            }
         }
      }
   }
}
