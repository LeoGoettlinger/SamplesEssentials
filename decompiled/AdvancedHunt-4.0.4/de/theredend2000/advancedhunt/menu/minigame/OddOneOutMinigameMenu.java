package de.theredend2000.advancedhunt.menu.minigame;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.lib.xseries.XSound;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class OddOneOutMinigameMenu extends MinigameMenu {
   private static final XMaterial[][] DIFFICULTY_GROUPS = new XMaterial[][]{
      {
            XMaterial.RED_WOOL,
            XMaterial.BLUE_WOOL,
            XMaterial.YELLOW_WOOL,
            XMaterial.GREEN_WOOL,
            XMaterial.BLACK_WOOL,
            XMaterial.ORANGE_WOOL,
            XMaterial.PURPLE_WOOL,
            XMaterial.WHITE_WOOL
      },
      {
            XMaterial.RED_CONCRETE,
            XMaterial.BLUE_CONCRETE,
            XMaterial.YELLOW_CONCRETE,
            XMaterial.GREEN_CONCRETE,
            XMaterial.BLACK_CONCRETE,
            XMaterial.ORANGE_CONCRETE,
            XMaterial.PURPLE_CONCRETE,
            XMaterial.WHITE_CONCRETE
      },
      {
            XMaterial.CYAN_WOOL,
            XMaterial.LIGHT_BLUE_WOOL,
            XMaterial.LIME_WOOL,
            XMaterial.GREEN_WOOL,
            XMaterial.MAGENTA_WOOL,
            XMaterial.PINK_WOOL,
            XMaterial.PURPLE_WOOL,
            XMaterial.BLUE_WOOL
      },
      {
            XMaterial.CYAN_CONCRETE,
            XMaterial.LIGHT_BLUE_CONCRETE,
            XMaterial.LIME_CONCRETE,
            XMaterial.GREEN_CONCRETE,
            XMaterial.MAGENTA_CONCRETE,
            XMaterial.PINK_CONCRETE,
            XMaterial.PURPLE_CONCRETE,
            XMaterial.BLUE_CONCRETE
      },
      {XMaterial.STONE, XMaterial.COBBLESTONE, XMaterial.ANDESITE, XMaterial.DIORITE, XMaterial.GRANITE, XMaterial.DEEPSLATE, XMaterial.TUFF, XMaterial.CALCITE},
      {
            XMaterial.OAK_PLANKS,
            XMaterial.BIRCH_PLANKS,
            XMaterial.SPRUCE_PLANKS,
            XMaterial.JUNGLE_PLANKS,
            XMaterial.ACACIA_PLANKS,
            XMaterial.DARK_OAK_PLANKS,
            XMaterial.MANGROVE_PLANKS,
            XMaterial.CHERRY_PLANKS
      },
      {
            XMaterial.TERRACOTTA,
            XMaterial.WHITE_TERRACOTTA,
            XMaterial.LIGHT_GRAY_TERRACOTTA,
            XMaterial.GRAY_TERRACOTTA,
            XMaterial.BROWN_TERRACOTTA,
            XMaterial.ORANGE_TERRACOTTA,
            XMaterial.YELLOW_TERRACOTTA,
            XMaterial.RED_TERRACOTTA
      },
      {
            XMaterial.WHITE_GLAZED_TERRACOTTA,
            XMaterial.LIGHT_GRAY_GLAZED_TERRACOTTA,
            XMaterial.GRAY_GLAZED_TERRACOTTA,
            XMaterial.BROWN_GLAZED_TERRACOTTA,
            XMaterial.ORANGE_GLAZED_TERRACOTTA,
            XMaterial.YELLOW_GLAZED_TERRACOTTA,
            XMaterial.GREEN_GLAZED_TERRACOTTA,
            XMaterial.CYAN_GLAZED_TERRACOTTA
      },
      {
            XMaterial.WHITE_WOOL,
            XMaterial.LIGHT_GRAY_WOOL,
            XMaterial.GRAY_WOOL,
            XMaterial.WHITE_CONCRETE,
            XMaterial.LIGHT_GRAY_CONCRETE,
            XMaterial.GRAY_CONCRETE,
            XMaterial.QUARTZ_BLOCK,
            XMaterial.SNOW_BLOCK
      },
      {
            XMaterial.POLISHED_ANDESITE,
            XMaterial.POLISHED_DIORITE,
            XMaterial.POLISHED_GRANITE,
            XMaterial.POLISHED_DEEPSLATE,
            XMaterial.SMOOTH_STONE,
            XMaterial.SMOOTH_QUARTZ,
            XMaterial.CHISELED_STONE_BRICKS,
            XMaterial.STONE_BRICKS
      }
   };
   private static final int MAX_DIFFICULTY = DIFFICULTY_GROUPS.length;
   private static final int TOTAL_SLOTS = 54;
   private final int maxRounds;
   private final int baseTime;
   private final int startDifficulty;
   private final Random random = new Random();
   private int round = 0;
   private int currentDifficulty;
   private int roundsOnDifficulty = 0;
   private int oddSlot = -1;
   private boolean waitingForClick = false;
   private XMaterial currentMain = null;
   private XMaterial currentOdd = null;
   private BukkitTask countdownTask = null;

   public OddOneOutMinigameMenu(Player player, Main plugin, Consumer<Boolean> onFinish) {
      super(player, plugin, onFinish);
      this.maxRounds = plugin.getConfig().getInt("hint.minigames.oddoneout.rounds", 10);
      this.baseTime = plugin.getConfig().getInt("hint.minigames.oddoneout.base-time", 200);
      int raw = plugin.getConfig().getInt("hint.minigames.oddoneout.difficulty", 4);
      this.startDifficulty = Math.max(1, Math.min(MAX_DIFFICULTY, raw));
      this.currentDifficulty = this.startDifficulty;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.minigame.oddoneout.title", false);
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

   private void updateDifficulty() {
      if (this.currentDifficulty >= MAX_DIFFICULTY) {
         this.roundsOnDifficulty++;
      } else {
         double chance = this.roundsOnDifficulty / (this.roundsOnDifficulty + 2.0);
         if (this.random.nextDouble() < chance) {
            this.currentDifficulty++;
            this.roundsOnDifficulty = 0;
         } else {
            this.roundsOnDifficulty++;
         }
      }
   }

   private void startRound() {
      if (!this.finished) {
         this.round++;
         if (this.round > 1) {
            this.updateDifficulty();
         }

         this.waitingForClick = false;
         XMaterial[] group = DIFFICULTY_GROUPS[this.currentDifficulty - 1];
         List<XMaterial> pool = new ArrayList<>();

         for (XMaterial m : group) {
            pool.add(m);
         }

         Collections.shuffle(pool, this.random);
         this.currentMain = pool.get(0);
         this.currentOdd = pool.get(1);
         this.oddSlot = this.random.nextInt(54);
         this.playerMenuUtility
            .sendMessage(
               this.plugin
                  .getMessageManager()
                  .getMessage(
                     "minigame.oddoneout.find",
                     true,
                     "%round%",
                     String.valueOf(this.round),
                     "%max%",
                     String.valueOf(this.maxRounds),
                     "%difficulty%",
                     String.valueOf(this.currentDifficulty)
                  )
            );
         this.waitingForClick = true;
         this.startCountdown(this.baseTime);
      }
   }

   private void startCountdown(long totalTicks) {
      this.cancelCountdown();
      int[] secondsLeft = new int[]{(int)Math.ceil(totalTicks / 20.0)};
      this.renderAllItems(secondsLeft[0]);
      this.countdownTask = this.scheduleTaskTimer(() -> {
         if (this.waitingForClick && !this.finished) {
            secondsLeft[0]--;
            if (secondsLeft[0] <= 0) {
               this.cancelCountdown();
               if (!this.waitingForClick || this.finished) {
                  return;
               }

               this.waitingForClick = false;
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.oddoneout.timeout", true));
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_VILLAGER_NO.get(), 1.0F, 1.0F);
               this.finish(false);
            } else {
               this.renderAllItems(secondsLeft[0]);
            }
         } else {
            this.cancelCountdown();
         }
      }, 20L, 20L);
   }

   private void cancelCountdown() {
      if (this.countdownTask != null && !this.countdownTask.isCancelled()) {
         this.countdownTask.cancel();
      }

      this.countdownTask = null;
   }

   private void renderAllItems(int seconds) {
      String timeName = this.plugin.getMessageManager().getMessage("gui.minigame.oddoneout.timer", false, "%seconds%", String.valueOf(seconds));
      ItemStack mainItem = new ItemBuilder(this.currentMain).setDisplayName(timeName).build();
      ItemStack oddItem = new ItemBuilder(this.currentOdd).setDisplayName(timeName).build();

      for (int i = 0; i < 54; i++) {
         this.updateSlot(i, i == this.oddSlot ? oddItem : mainItem);
      }
   }

   @Override
   public void handleMenu(InventoryClickEvent event) {
      if (this.waitingForClick && !this.finished) {
         int slot = event.getSlot();
         if (slot == this.oddSlot) {
            this.waitingForClick = false;
            this.cancelCountdown();
            this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0F, 2.0F);
            if (this.round >= this.maxRounds) {
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_PLAYER_LEVELUP.get(), 1.0F, 1.0F);
               this.scheduleTask(() -> this.finish(true), 10L);
            } else {
               this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 1.0F, 1.0F);
               this.scheduleTask(this::startRound, 20L);
            }
         } else {
            this.waitingForClick = false;
            this.cancelCountdown();
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("minigame.oddoneout.wrong", true));
            this.playerMenuUtility.playSound(this.playerMenuUtility.getLocation(), XSound.ENTITY_VILLAGER_NO.get(), 1.0F, 1.0F);
            this.finish(false);
         }
      }
   }
}
