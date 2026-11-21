package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class BossTrackingPage {
   private BossTrackingPage() {
   }

   protected static TextComponent[] bossTrackingPage(Player player) {
      TextComponent configTextComponent = new TextComponent();

      for (int i = 0; i < 3; i++) {
         if (PlayerStatusMenuConfig.getBossTrackerHoverLines()[i] != null) {
            TextComponent line = new TextComponent(PlayerStatusMenuConfig.getBossTrackerTextLines()[i] + "\n");
            if (!PlayerStatusMenuConfig.getBossTrackerHoverLines()[i].isEmpty()) {
               PlayerStatusScreen.setHoverText(line, PlayerStatusMenuConfig.getBossTrackerHoverLines()[i]);
            }

            if (!PlayerStatusMenuConfig.getBossTrackerCommandLines()[i].isEmpty()) {
               line.setClickEvent(new ClickEvent(Action.RUN_COMMAND, PlayerStatusMenuConfig.getBossTrackerCommandLines()[i]));
            }

            configTextComponent.addExtra(line);
         }
      }

      ArrayList<TextComponent> textComponents = new ArrayList<>();
      int counter = 0;
      HashSet<CustomBossEntity> tempSet = new HashSet<>(CustomBossEntity.getTrackableCustomBosses());
      tempSet.forEach(customBossEntity -> {
         if (!customBossEntity.exists()) {
            CustomBossEntity.getTrackableCustomBosses().remove(customBossEntity);
         }
      });

      for (CustomBossEntity customBossEntity : CustomBossEntity.getTrackableCustomBosses()) {
         try {
            textComponents.add(
               SpigotMessage.commandHoverMessage(
                  customBossEntity.getBossTrackingBar().bossBarMessage(player, customBossEntity.getCustomBossesConfigFields().getLocationMessage()) + "\n",
                  PlayerStatusMenuConfig.getOnBossTrackHover(),
                  "/elitemobs track boss " + customBossEntity.getEliteUUID()
               )
            );
            counter++;
         } catch (Exception var10) {
            Logger.warn("Failed to correctly get elements for boss tracking page!");
            var10.printStackTrace();
         }
      }

      TextComponent[] textComponent;
      if (counter == 0) {
         textComponent = new TextComponent[]{configTextComponent};
      } else {
         textComponent = new TextComponent[(int)Math.ceil(counter / 6.0)];
         int internalCounter = 0;
         textComponent[0] = configTextComponent;

         for (TextComponent text : textComponents) {
            int currentPage = (int)Math.floor(internalCounter / 6.0);
            if (textComponent[currentPage] == null) {
               textComponent[currentPage] = new TextComponent();
            }

            textComponent[currentPage].addExtra(text);
            internalCounter++;
         }
      }

      return textComponent;
   }

   protected static void bossTrackingPage(Player targetPlayer, Player requestingPlayer) {
      Inventory inventory = Bukkit.createInventory(requestingPlayer, 54, PlayerStatusMenuConfig.getBossTrackerChestMenuName());
      int counter = 0;
      Map<Integer, CustomBossEntity> bosses = new HashMap<>();

      for (CustomBossEntity customBossEntity : CustomBossEntity.getTrackableCustomBosses()) {
         if (counter >= 53) {
            break;
         }

         bosses.put(counter, customBossEntity);
         inventory.setItem(
            counter,
            ItemStackGenerator.generateItemStack(
               Material.ZOMBIE_HEAD,
               customBossEntity.getBossTrackingBar().bossBarMessage(targetPlayer, customBossEntity.getCustomBossesConfigFields().getLocationMessage()),
               Collections.singletonList(MobCombatSettingsConfig.getBossLocationMessage())
            )
         );
         counter++;
      }

      inventory.setItem(53, PlayerStatusMenuConfig.getBackItem());
      if (requestingPlayer.openInventory(inventory) != null) {
         StatusInventorySafety.protect(inventory);
         BossTrackingPage.BossTrackingPageEvents.pageInventories.put(inventory, bosses);
      }
   }

   public static class BossTrackingPageEvents implements Listener {
      private static final Map<Inventory, Map<Integer, CustomBossEntity>> pageInventories = new HashMap<>();

      public static void shutdown() {
         pageInventories.clear();
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onInventoryInteract(InventoryClickEvent event) {
         if (event.getSlot() >= 0) {
            Player player = ((Player)event.getWhoClicked()).getPlayer();
            Map<Integer, CustomBossEntity> bosses = pageInventories.get(event.getInventory());
            if (bosses != null) {
               event.setCancelled(true);
               if (event.getClickedInventory() == event.getView().getTopInventory()) {
                  CustomBossEntity boss = bosses.get(event.getSlot());
                  if (boss != null) {
                     player.closeInventory();
                     boss.getBossTrackingBar().addTrackingPlayer(player);
                  } else {
                     if (event.getSlot() == 53) {
                        player.closeInventory();
                        CoverPage.coverPage(player);
                     }
                  }
               }
            }
         }
      }

      @EventHandler
      public void onInventoryClose(InventoryCloseEvent event) {
         pageInventories.remove(event.getInventory());
      }
   }
}
