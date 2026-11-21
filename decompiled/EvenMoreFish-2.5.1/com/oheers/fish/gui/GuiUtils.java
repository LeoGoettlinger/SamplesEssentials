package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.selling.SellHelper;
import com.oheers.fish.commands.main.MainCommand;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.gui.guis.BaitsGui;
import com.oheers.fish.gui.guis.FishJournalGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.messages.ConfigMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.evenmorefish.fish.libs.inventorygui.GuiElement;
import org.evenmorefish.fish.libs.inventorygui.InventoryGui;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GuiUtils {
   public static Map<String, BiConsumer<ConfigGui, GuiElement.Click>> getActionMap() {
      Map<String, BiConsumer<ConfigGui, GuiElement.Click>> newActionMap = new HashMap<>();
      newActionMap.put("full-exit", (gui, click) -> {
         if (gui != null) {
            gui.doRescue();
         }

         closeGui(click.getWhoClicked());
      });
      newActionMap.put("open-main-menu", (gui, click) -> {
         if (gui != null) {
            gui.doRescue();
         }

         new MainMenuGui(click.getWhoClicked()).open();
         clearHistory(click.getWhoClicked());
      });
      newActionMap.put("fish-toggle", (gui, click) -> {
         if (click.getWhoClicked() instanceof Player player) {
            EvenMoreFish.getInstance().getToggle().performFishToggle(player);
         }

         click.getGui().draw();
      });
      newActionMap.put("open-shop", (gui, click) -> {
         if (gui != null) {
            gui.doRescue();
         }

         if (click.getWhoClicked() instanceof Player player) {
            new SellGui(player, SellGui.SellState.NORMAL, null).open();
            clearHistory(click.getWhoClicked());
         }
      });
      newActionMap.put("show-command-help", (gui, click) -> {
         click.getWhoClicked().closeInventory();
         MainCommand.sendHelpMessage(click.getWhoClicked());
      });
      newActionMap.put("sell-inventory", (gui, click) -> {
         HumanEntity humanEntity = click.getWhoClicked();
         if (humanEntity instanceof Player player) {
            if (gui instanceof SellGui sellGui) {
               new SellGui(player, SellGui.SellState.CONFIRM, sellGui.getFishInventory()).open();
            } else {
               SellHelper.get().sell(click.getWhoClicked().getInventory(), player);
               closeGui(humanEntity);
            }
         }
      });
      newActionMap.put("sell-shop", (gui, click) -> {
         HumanEntity humanEntity = click.getWhoClicked();
         if (gui instanceof SellGui sellGui && humanEntity instanceof Player player) {
            new SellGui(player, SellGui.SellState.CONFIRM, sellGui.getFishInventory()).open();
         } else {
            FishUtils.sellInventoryGui(click.getGui(), click.getWhoClicked());
            closeGui(click.getWhoClicked());
         }
      });
      newActionMap.put("sell-inventory-confirm", (gui, click) -> {
         if (click.getWhoClicked() instanceof Player player) {
            SellHelper.get().sell(click.getWhoClicked().getInventory(), player);
            if (gui != null) {
               gui.doRescue();
            }

            closeGui(click.getWhoClicked());
         }
      });
      newActionMap.put("sell-shop-confirm", (gui, click) -> {
         FishUtils.sellInventoryGui(click.getGui(), click.getWhoClicked());
         if (gui != null) {
            gui.doRescue();
         }

         closeGui(click.getWhoClicked());
      });
      newActionMap.put("open-baits-menu", (gui, click) -> {
         if (gui != null) {
            gui.doRescue();
         }

         new BaitsGui(click.getWhoClicked()).open();
         clearHistory(click.getWhoClicked());
      });
      newActionMap.put("open-journal-menu", (gui, click) -> {
         if (!DatabaseUtil.isDatabaseOnline()) {
            ConfigMessage.JOURNAL_DISABLED.getMessage().send(click.getWhoClicked());
         } else {
            if (gui != null) {
               gui.doRescue();
            }

            if (click.getWhoClicked() instanceof Player player) {
               openJournalFromMainMenu(player, FishJournalGui::openAsync);
               clearHistory(click.getWhoClicked());
            }
         }
      });
      newActionMap.put("first-page", (gui, click) -> {});
      newActionMap.put("previous-page", (gui, click) -> {});
      newActionMap.put("next-page", (gui, click) -> {});
      newActionMap.put("last-page", (gui, click) -> {});
      return newActionMap;
   }

   private static void closeGui(HumanEntity human) {
      clearHistory(human);
      human.closeInventory();
   }

   private static void clearHistory(HumanEntity human) {
      InventoryGui.clearHistory(human);
   }

   static void openJournalFromMainMenu(@NonNull Player player, @NonNull GuiUtils.JournalOpener opener) {
      opener.open(player, null, null);
   }

   @FunctionalInterface
   interface JournalOpener {
      void open(@NonNull Player player, @Nullable Rarity rarity, @Nullable InventoryGui expectedOpenGui);
   }
}
