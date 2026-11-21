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

public class SliderPuzzleMinigameMenu extends MinigameMenu {
   private static final int[] GRID_INV_SLOTS = new int[]{12, 13, 14, 21, 22, 23, 30, 31, 32};
   private static final int GRID_SIZE = 3;
   private static final int[] SOLVED = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 0};
   private static final XMaterial[] TILE_MATERIALS = new XMaterial[]{
      XMaterial.RED_WOOL,
      XMaterial.ORANGE_WOOL,
      XMaterial.YELLOW_WOOL,
      XMaterial.LIME_WOOL,
      XMaterial.BLUE_WOOL,
      XMaterial.PURPLE_WOOL,
      XMaterial.CYAN_WOOL,
      XMaterial.PINK_WOOL
   };
   private final int shuffles;
   private final int[] board = new int[9];
   private int emptyPos;

   public SliderPuzzleMinigameMenu(Player player, Main plugin, Consumer<Boolean> onFinish) {
      super(player, plugin, onFinish);
      this.shuffles = plugin.getConfig().getInt("hint.minigames.sliderpuzzle.shuffles", 50);
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.minigame.sliderpuzzle.title", false);
   }

   @Override
   public int getSlots() {
      return 45;
   }

   @Override
   public void setMenuItems() {
      this.fillBackground(new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).hideTooltip(true).build());

      for (int i = 0; i < 9; i++) {
         this.board[i] = SOLVED[i];
      }

      this.emptyPos = 8;
      this.shuffleBoard();
      this.renderBoard();
      this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.sliderpuzzle.start", true));
   }

   private void shuffleBoard() {
      Random rng = new Random();
      int lastMoved = -1;

      for (int s = 0; s < this.shuffles; s++) {
         List<Integer> neighbours = this.getNeighbours(this.emptyPos);
         if (neighbours.size() > 1) {
            int prevEmpty = lastMoved;
            neighbours.removeIf(n -> n == prevEmpty);
         }

         int pick = neighbours.get(rng.nextInt(neighbours.size()));
         lastMoved = this.emptyPos;
         this.swap(this.emptyPos, pick);
         this.emptyPos = pick;
      }
   }

   private void renderBoard() {
      for (int pos = 0; pos < 9; pos++) {
         int invSlot = GRID_INV_SLOTS[pos];
         int tile = this.board[pos];
         if (tile == 0) {
            this.updateSlot(
               invSlot,
               new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE)
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.sliderpuzzle.empty.name", false))
                  .build()
            );
         } else {
            this.updateSlot(
               invSlot,
               new ItemBuilder(TILE_MATERIALS[tile - 1])
                  .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.sliderpuzzle.tile.name", false, "%number%", String.valueOf(tile)))
                  .build()
            );
         }
      }
   }

   @Override
   public void handleMenu(InventoryClickEvent event) {
      if (!this.finished) {
         int invSlot = event.getSlot();
         int gridPos = this.invSlotToGridPos(invSlot);
         if (gridPos >= 0) {
            if (!this.getNeighbours(this.emptyPos).contains(gridPos)) {
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.BLOCK_STONE_PLACE.get(), 0.5F, 0.5F);
            } else {
               this.swap(this.emptyPos, gridPos);
               int prevEmpty = this.emptyPos;
               this.emptyPos = gridPos;
               this.renderTile(prevEmpty);
               this.renderTile(this.emptyPos);
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.BLOCK_NOTE_BLOCK_HAT.get(), 1.0F, 1.5F);
               if (this.isSolved()) {
                  this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0F, 1.0F);
                  this.scheduleTask(() -> this.finish(true), 20L);
               }
            }
         }
      }
   }

   private void renderTile(int pos) {
      int invSlot = GRID_INV_SLOTS[pos];
      int tile = this.board[pos];
      if (tile == 0) {
         this.updateSlot(
            invSlot,
            new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE)
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.sliderpuzzle.empty.name", false))
               .build()
         );
      } else {
         this.updateSlot(
            invSlot,
            new ItemBuilder(TILE_MATERIALS[tile - 1])
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.minigame.sliderpuzzle.tile.name", false, "%number%", String.valueOf(tile)))
               .build()
         );
      }
   }

   private void swap(int a, int b) {
      int tmp = this.board[a];
      this.board[a] = this.board[b];
      this.board[b] = tmp;
   }

   private boolean isSolved() {
      for (int i = 0; i < 9; i++) {
         if (this.board[i] != SOLVED[i]) {
            return false;
         }
      }

      return true;
   }

   private List<Integer> getNeighbours(int pos) {
      List<Integer> result = new ArrayList<>();
      int row = pos / 3;
      int col = pos % 3;
      if (row > 0) {
         result.add(pos - 3);
      }

      if (row < 2) {
         result.add(pos + 3);
      }

      if (col > 0) {
         result.add(pos - 1);
      }

      if (col < 2) {
         result.add(pos + 1);
      }

      return result;
   }

   private int invSlotToGridPos(int invSlot) {
      for (int i = 0; i < GRID_INV_SLOTS.length; i++) {
         if (GRID_INV_SLOTS[i] == invSlot) {
            return i;
         }
      }

      return -1;
   }
}
