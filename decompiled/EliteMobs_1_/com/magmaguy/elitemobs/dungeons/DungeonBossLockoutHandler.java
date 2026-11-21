package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DungeonBossLockoutHandler implements Listener {
   public static String getBossIdentifier(InstancedBossEntity boss) {
      String filename;
      if (boss.getPhaseBossEntity() != null && boss.getPhaseBossEntity().getPhase1Config() != null) {
         filename = boss.getPhaseBossEntity().getPhase1Config().getFilename();
      } else {
         filename = boss.getCustomBossesConfigFields().getFilename();
      }

      if (boss.getSpawnLocation() != null) {
         int x = boss.getSpawnLocation().getBlockX();
         int y = boss.getSpawnLocation().getBlockY();
         int z = boss.getSpawnLocation().getBlockZ();
         return filename + ":" + x + "," + y + "," + z;
      } else if (boss.getLocation() != null) {
         int x = boss.getLocation().getBlockX();
         int y = boss.getLocation().getBlockY();
         int z = boss.getLocation().getBlockZ();
         return filename + ":" + x + "," + y + "," + z;
      } else {
         return filename + ":unknown";
      }
   }

   public static Set<Player> processLockouts(InstancedBossEntity boss, Set<Player> damagers, int lockoutMinutes) {
      Set<Player> lockedOutPlayers = new HashSet<>();
      String bossIdentifier = getBossIdentifier(boss);

      for (Player player : damagers) {
         if (!player.hasMetadata("NPC") && PlayerData.isInMemory(player.getUniqueId())) {
            DungeonBossLockout lockout = PlayerData.getDungeonBossLockout(player.getUniqueId());
            if (lockout == null) {
               lockout = new DungeonBossLockout();
            }

            if (lockout.isLockedOut(bossIdentifier)) {
               lockedOutPlayers.add(player);
               notifyLockout(player, boss, lockout, bossIdentifier);
            } else {
               lockout.addLockout(bossIdentifier, lockoutMinutes);
               PlayerData.updateDungeonBossLockout(player.getUniqueId(), lockout);
            }
         }
      }

      return lockedOutPlayers;
   }

   private static void notifyLockout(Player player, InstancedBossEntity boss, DungeonBossLockout lockout, String bossIdentifier) {
      ActionBarCompositor.show(player, ActionBarCompositor.Source.LOCKOUT, DungeonsConfig.getDungeonLockoutSubtitle());
      String title = DungeonsConfig.getDungeonLockoutTitle();
      if (!title.isBlank()) {
         player.sendTitle(title, "", 10, 70, 20);
      }

      String bossName = boss.getName();
      String remainingTime = lockout.getFormattedRemainingTime(bossIdentifier);
      String chatMessage = DungeonsConfig.getDungeonLockoutChatMessage().replace("$bossName", bossName).replace("$remainingTime", remainingTime);
      player.sendMessage(chatMessage);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onInstancedBossDeath(EliteMobDeathEvent event) {
      if (event.getEliteEntity() instanceof InstancedBossEntity instancedBoss) {
         DungeonInstance dungeonInstance = instancedBoss.getDungeonInstance();
         if (dungeonInstance != null) {
            ContentPackagesConfigFields config = dungeonInstance.getContentPackagesConfigFields();
            if (config != null) {
               int lockoutMinutes = config.getDungeonLockoutMinutes();
               if (lockoutMinutes > 0) {
                  Set<Player> eligiblePlayers = new HashSet<>(PartyManager.expandSharedCreditParticipants(instancedBoss.getDamagers().keySet(), instancedBoss));
                  Set<Player> lockedOutPlayers = processLockouts(instancedBoss, eligiblePlayers, lockoutMinutes);
                  instancedBoss.setLockoutPlayers(lockedOutPlayers);
               }
            }
         }
      }
   }
}
