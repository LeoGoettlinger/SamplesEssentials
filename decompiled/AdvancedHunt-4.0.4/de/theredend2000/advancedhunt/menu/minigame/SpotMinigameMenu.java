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
import org.bukkit.inventory.ItemStack;

public class SpotMinigameMenu extends MinigameMenu {
   private static final XMaterial[] ITEM_TYPES = new XMaterial[]{
      XMaterial.RED_WOOL,
      XMaterial.ORANGE_WOOL,
      XMaterial.YELLOW_WOOL,
      XMaterial.LIME_WOOL,
      XMaterial.BLUE_WOOL,
      XMaterial.PURPLE_WOOL,
      XMaterial.CYAN_WOOL,
      XMaterial.PINK_WOOL,
      XMaterial.MAGENTA_WOOL,
      XMaterial.WHITE_WOOL
   };
   private static final int TOTAL_SLOTS = 45;
   private final int itemCount;
   private final int displayTime;
   private final List<Integer> hiddenSlots = new ArrayList<>();
   private final List<Integer> foundSlots = new ArrayList<>();
   private boolean memorisePhase = true;

   public SpotMinigameMenu(Player player, Main plugin, Consumer<Boolean> onFinish) {
      super(player, plugin, onFinish);
      this.itemCount = plugin.getConfig().getInt("hint.minigames.spot.item-count", 5);
      this.displayTime = plugin.getConfig().getInt("hint.minigames.spot.display-time", 60);
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.minigame.spot.title", false);
   }

   @Override
   public int getSlots() {
      return 45;
   }

   @Override
   public void setMenuItems() {
      this.fillBackground(new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());
      this.startMemorisePhase();
   }

   private void startMemorisePhase() {
      this.memorisePhase = true;
      this.hiddenSlots.clear();
      this.foundSlots.clear();
      List<Integer> allSlots = new ArrayList<>();

      for (int i = 0; i < 45; i++) {
         allSlots.add(i);
      }

      Collections.shuffle(allSlots);
      List<XMaterial> pool = new ArrayList<>();

      for (XMaterial m : ITEM_TYPES) {
         pool.add(m);
      }

      Collections.shuffle(pool);
      int count = Math.min(this.itemCount, 45);

      for (int i = 0; i < count; i++) {
         int slot = allSlots.get(i);
         this.hiddenSlots.add(slot);
         XMaterial mat = pool.get(i % pool.size());
         this.updateSlot(slot, new ItemBuilder(mat).setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.spot.item.name", false)).build());
      }

      this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.spot.memorise", true));
      this.scheduleTask(this::startGuessPhase, this.displayTime);
   }

   private void startGuessPhase() {
      this.memorisePhase = false;
      ItemStack glass = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build();

      for (int i = 0; i < 45; i++) {
         this.updateSlot(i, glass);
      }

      this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.spot.find", true));
   }

   @Override
   public void handleMenu(InventoryClickEvent event) {
      if (!this.memorisePhase && !this.finished) {
         int slot = event.getSlot();
         if (slot >= 0 && slot < 45) {
            if (!this.foundSlots.contains(slot)) {
               if (this.hiddenSlots.contains(slot)) {
                  this.foundSlots.add(slot);
                  int idx = this.hiddenSlots.indexOf(slot);
                  XMaterial mat = ITEM_TYPES[idx % ITEM_TYPES.length];
                  this.updateSlot(
                     slot, new ItemBuilder(mat).setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.spot.item.found", false)).build()
                  );
                  this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0F, 2.0F);
                  if (this.foundSlots.size() >= this.hiddenSlots.size()) {
                     this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0F, 1.0F);
                     this.scheduleTask(() -> this.finish(true), 10L);
                  }
               } else {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.spot.wrong", true));
                  this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_VILLAGER_NO.get(), 1.0F, 1.0F);
                  this.finish(false);
               }
            }
         }
      }
   }
}
