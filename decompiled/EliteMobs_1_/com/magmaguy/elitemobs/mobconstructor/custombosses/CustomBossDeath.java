package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.ClassLootCoverage;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Round;
import com.magmaguy.elitemobs.ondeathcommands.OnDeathCommands;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class CustomBossDeath implements Listener {
   private static void doLoot(CustomBossEntity customBossEntity) {
      if (!customBossEntity.isTriggeredAntiExploit()) {
         ClassLootCoverage.dropLoot(customBossEntity);
         if (customBossEntity.customBossesConfigFields.getUniqueLootList() != null && !customBossEntity.customBossesConfigFields.getUniqueLootList().isEmpty()) {
            Set<Player> lockedOutPlayers = Set.of();
            if (customBossEntity instanceof InstancedBossEntity instancedBoss) {
               lockedOutPlayers = instancedBoss.getLockoutPlayers();
            }

            for (Player player : customBossEntity.getDamagers().keySet()) {
               if (!lockedOutPlayers.contains(player)) {
                  dropLoot(player, customBossEntity);
               }
            }

            for (Player playerx : PartyManager.expandSharedCreditParticipants(customBossEntity.getDamagers().keySet(), customBossEntity)) {
               if (!lockedOutPlayers.contains(playerx) && !customBossEntity.getDamagers().containsKey(playerx)) {
                  customBossEntity.customBossesConfigFields
                     .getCustomLootTable()
                     .bossDrop(playerx, customBossEntity.getLevel(), customBossEntity.getLocation(), customBossEntity, true);
               }
            }
         }
      }
   }

   public static void dropLoot(Player player, CustomBossEntity customBossEntity) {
      customBossEntity.customBossesConfigFields
         .getCustomLootTable()
         .bossDrop(player, customBossEntity.getLevel(), customBossEntity.getLocation(), customBossEntity);
   }

   private static void doDeathMessage(CustomBossEntity customBossEntity) {
      if (MobCombatSettingsConfig.isEnableDeathMessages()) {
         StringBuilder playersList = new StringBuilder();
         StringBuilder playersNameList = new StringBuilder();

         for (Player player : customBossEntity.getDamagers().keySet()) {
            if (playersList.length() == 0) {
               playersList.append(player.getDisplayName());
               playersNameList.append(player.getName());
            } else {
               playersList.append(", &f").append(player.getDisplayName());
               playersNameList.append(", ").append(player.getName());
            }
         }

         playersList = new StringBuilder(ChatColorConverter.convert(playersList.toString()));
         if (customBossEntity.hasDamagers()) {
            if (customBossEntity.customBossesConfigFields.getDeathMessages() != null && !customBossEntity.customBossesConfigFields.getDeathMessages().isEmpty()
               )
             {
               Player topDamager = null;
               Player secondDamager = null;
               Player thirdDamager = null;
               HashMap<Player, Double> sortedMap = sortByComparator(customBossEntity.getDamagers(), false);
               Iterator<Player> sortedMapIterator = sortedMap.keySet().iterator();

               for (int i = 1; i < 4 && i <= sortedMap.size(); i++) {
                  Player nextPlayer = sortedMapIterator.next();
                  switch (i) {
                     case 1:
                        topDamager = nextPlayer;
                        break;
                     case 2:
                        secondDamager = nextPlayer;
                        break;
                     case 3:
                        thirdDamager = nextPlayer;
                  }
               }

               for (String string : customBossEntity.customBossesConfigFields.getDeathMessages()) {
                  if (string.contains("$damager1Name")) {
                     if (topDamager != null) {
                        string = string.replace("$damager1Name", topDamager.getName());
                     } else {
                        string = "";
                     }
                  }

                  if (string.contains("$damager1name")) {
                     if (topDamager != null) {
                        string = string.replace("$damager1name", topDamager.getDisplayName());
                     } else {
                        string = "";
                     }
                  }

                  if (string.contains("$damager1damage")) {
                     if (topDamager != null) {
                        string = string.replace("$damager1damage", Round.twoDecimalPlaces(customBossEntity.getDamagers().get(topDamager)) + "");
                     } else {
                        string = "";
                     }
                  }

                  if (string.contains("$damager2Name")) {
                     if (secondDamager != null) {
                        string = string.replace("$damager2Name", secondDamager.getName());
                     } else {
                        string = "";
                     }
                  }

                  if (string.contains("$damager2name")) {
                     if (secondDamager != null) {
                        string = string.replace("$damager2name", secondDamager.getDisplayName());
                     } else {
                        string = "";
                     }
                  }

                  if (string.contains("$damager2damage")) {
                     if (secondDamager != null) {
                        string = string.replace("$damager2damage", Round.twoDecimalPlaces(customBossEntity.getDamagers().get(secondDamager)) + "");
                     } else {
                        string = "";
                     }
                  }

                  if (string.contains("$damager3Name")) {
                     if (thirdDamager != null) {
                        string = string.replace("$damager3Name", thirdDamager.getName());
                     } else {
                        string = "";
                     }
                  }

                  if (string.contains("$damager3name")) {
                     if (thirdDamager != null) {
                        string = string.replace("$damager3name", thirdDamager.getDisplayName());
                     } else {
                        string = "";
                     }
                  }

                  if (string.contains("$damager3damage")) {
                     if (thirdDamager != null) {
                        string = string.replace("$damager3damage", Round.twoDecimalPlaces(customBossEntity.getDamagers().get(thirdDamager)) + "");
                     } else {
                        string = "";
                     }
                  }

                  if (string.contains("$playersName")) {
                     string = string.replace("$playersName", playersNameList.toString());
                  }

                  if (string.contains("$players")) {
                     string = string.replace("$players", playersList.toString());
                  }

                  if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() > 0) {
                     Bukkit.broadcastMessage(ChatColorConverter.convert(string));
                  }

                  if (string.length() > 0) {
                     if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() > 2) {
                        new DiscordSRVAnnouncement(ChatColorConverter.convert(string));
                     } else if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() < 1) {
                        for (Player playerx : customBossEntity.getDamagers().keySet()) {
                           playerx.sendMessage(ChatColorConverter.convert(string));
                        }
                     }
                  }
               }

               for (Player playerx : Bukkit.getOnlinePlayers()) {
                  if (customBossEntity.getDamagers().containsKey(playerx)) {
                     playerx.sendMessage(
                        MobCombatSettingsConfig.getBossKillParticipationMessage()
                           .replace("$playerDamage", Round.twoDecimalPlaces(customBossEntity.getDamagers().get(playerx)) + "")
                     );
                  }
               }
            } else if (customBossEntity.customBossesConfigFields.getDeathMessage() != null
               && !customBossEntity.customBossesConfigFields.getDeathMessage().isEmpty()) {
               String deathMsg = customBossEntity.customBossesConfigFields
                  .getDeathMessage()
                  .replace("$playersName", playersNameList.toString())
                  .replace("$players", playersList.toString());
               if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() == 0) {
                  for (Player playerxx : customBossEntity.getDamagers().keySet()) {
                     playerxx.sendMessage(ChatColorConverter.convert(deathMsg));
                  }
               }

               if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() > 0) {
                  Bukkit.broadcastMessage(ChatColorConverter.convert(deathMsg));
               }

               if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() > 2) {
                  new DiscordSRVAnnouncement(ChatColorConverter.convert(deathMsg));
               }
            }
         }
      }
   }

   private static HashMap<Player, Double> sortByComparator(Map<Player, Double> unsortMap, final boolean order) {
      List<Entry<Player, Double>> list = new LinkedList<>(unsortMap.entrySet());
      Collections.sort(list, new Comparator<Entry<Player, Double>>() {
         public int compare(Entry<Player, Double> o1, Entry<Player, Double> o2) {
            return order ? o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue());
         }
      });
      HashMap<Player, Double> sortedMap = new LinkedHashMap<>();

      for (Entry<Player, Double> entry : list) {
         sortedMap.put(entry.getKey(), entry.getValue());
      }

      return sortedMap;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onEliteMobDeath(EliteMobDeathEvent event) {
      if (event.getEliteEntity() instanceof CustomBossEntity customBossEntity) {
         doLoot(customBossEntity);
         doDeathMessage(customBossEntity);
         CustomBossEntity.trackableCustomBosses.remove(customBossEntity);
         if (customBossEntity.customBossesConfigFields.getOnDeathCommands() != null
            && !customBossEntity.customBossesConfigFields.getOnDeathCommands().isEmpty()) {
            OnDeathCommands.parseConsoleCommand(customBossEntity.customBossesConfigFields.getOnDeathCommands(), event);
         }

         if (!customBossEntity.customBossesConfigFields.isDropsVanillaLoot()) {
            event.getEntityDeathEvent().setDroppedExp(0);

            for (ItemStack itemStack : event.getEntityDeathEvent().getDrops()) {
               itemStack.setAmount(0);
            }
         }
      }
   }
}
