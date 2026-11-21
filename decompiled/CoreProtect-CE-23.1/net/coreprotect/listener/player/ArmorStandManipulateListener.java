package net.coreprotect.listener.player;

import java.sql.Connection;
import java.sql.Statement;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Database;
import net.coreprotect.database.lookup.ChestTransactionLookup;
import net.coreprotect.language.Phrase;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public final class ArmorStandManipulateListener extends Queue implements Listener {
   public static void inspectHangingTransactions(final Location location, final Player finalPlayer) {
      class BasicThread implements Runnable {
         @Override
         public void run() {
            if (!finalPlayer.hasPermission("coreprotect.inspect")) {
               Chat.sendMessage(finalPlayer, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
               ConfigHandler.inspecting.put(finalPlayer.getName(), false);
            } else if (ConfigHandler.converterRunning) {
               Chat.sendMessage(finalPlayer, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.UPGRADE_IN_PROGRESS));
            } else if (ConfigHandler.purgeRunning) {
               Chat.sendMessage(finalPlayer, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.PURGE_IN_PROGRESS));
            } else {
               if (ConfigHandler.lookupThrottle.get(finalPlayer.getName()) != null) {
                  Object[] lookupThrottle = ConfigHandler.lookupThrottle.get(finalPlayer.getName());
                  if ((Boolean)lookupThrottle[0] || System.currentTimeMillis() - (Long)lookupThrottle[1] < 100L) {
                     Chat.sendMessage(finalPlayer, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
                     return;
                  }
               }

               ConfigHandler.lookupThrottle.put(finalPlayer.getName(), new Object[]{true, System.currentTimeMillis()});

               try (Connection connection = Database.getConnection(true)) {
                  if (connection == null) {
                     Chat.sendMessage(finalPlayer, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
                  } else {
                     Statement statement = connection.createStatement();

                     for (String data : ChestTransactionLookup.performLookup(null, statement, location, finalPlayer, 1, 7, true)) {
                        Chat.sendComponent(finalPlayer, data);
                     }

                     statement.close();
                  }
               } catch (Exception var8) {
                  var8.printStackTrace();
               }

               ConfigHandler.lookupThrottle.put(finalPlayer.getName(), new Object[]{false, System.currentTimeMillis()});
            }
         }
      }

      Runnable runnable = new BasicThread();
      Thread thread = new Thread(runnable);
      thread.start();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
      Player player = event.getPlayer();
      ArmorStand armorStand = event.getRightClicked();
      EntityEquipment equipment = armorStand.getEquipment();
      ItemStack[] oldContents = ItemUtils.getArmorStandContents(equipment);
      ItemStack[] newContents = (ItemStack[])oldContents.clone();
      ItemStack item = event.getArmorStandItem();
      ItemStack playerItem = event.getPlayerItem();
      if (ConfigHandler.inspecting.get(player.getName()) != null && ConfigHandler.inspecting.get(player.getName())) {
         if (BlockGroup.CONTAINERS.contains(Material.ARMOR_STAND)) {
            inspectHangingTransactions(armorStand.getLocation(), player);
         }

         event.setCancelled(true);
      }

      if (!event.isCancelled()) {
         if (Config.getConfig(player.getWorld()).ITEM_TRANSACTIONS) {
            int slot = 0;
            byte var10;
            switch (event.getSlot()) {
               case LEGS:
                  var10 = 1;
                  break;
               case CHEST:
                  var10 = 2;
                  break;
               case HEAD:
                  var10 = 3;
                  break;
               case HAND:
                  var10 = 4;
                  break;
               case OFF_HAND:
                  var10 = 5;
                  break;
               default:
                  var10 = 0;
            }

            if (item.getType() != playerItem.getType()) {
               if (item.getType() != Material.AIR && playerItem.getType() == Material.AIR) {
                  oldContents[var10] = item.clone();
                  newContents[var10] = new ItemStack(Material.AIR);
                  PlayerInteractEntityListener.queueContainerSpecifiedItems(
                     player.getName(), Material.ARMOR_STAND, new Object[]{oldContents, newContents}, armorStand.getLocation(), false
                  );
               } else if (item.getType() == Material.AIR && playerItem.getType() != Material.AIR) {
                  oldContents[var10] = new ItemStack(Material.AIR);
                  newContents[var10] = playerItem.clone();
                  PlayerInteractEntityListener.queueContainerSpecifiedItems(
                     player.getName(), Material.ARMOR_STAND, new Object[]{oldContents, newContents}, armorStand.getLocation(), false
                  );
               } else if (item.getType() != Material.AIR && playerItem.getType() != Material.AIR) {
                  oldContents[var10] = item.clone();
                  newContents[var10] = playerItem.clone();
                  PlayerInteractEntityListener.queueContainerSpecifiedItems(
                     player.getName(), Material.ARMOR_STAND, new Object[]{oldContents, newContents}, armorStand.getLocation(), false
                  );
               }
            }
         }
      }
   }
}
