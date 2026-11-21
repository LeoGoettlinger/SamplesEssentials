package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CombatLevelCalculator {
   private static final AtomicLong onlineLevelRevision = new AtomicLong();
   private static CombatLevelCalculator.OnlineLevelSnapshot onlineLevelSnapshot;

   private CombatLevelCalculator() {
   }

   public static void invalidateOnlineCombatLevel() {
      onlineLevelRevision.incrementAndGet();
   }

   public static synchronized int highestOnlineCombatLevel() {
      long revision = onlineLevelRevision.get();
      Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
      int onlineCount = onlinePlayers.size();
      if (onlineLevelSnapshot != null && onlineLevelSnapshot.revision == revision && onlineLevelSnapshot.onlineCount == onlineCount) {
         return onlineLevelSnapshot.level;
      } else {
         int highestLevel = 0;

         for (Player player : onlinePlayers) {
            highestLevel = Math.max(highestLevel, calculateCombatLevel(player.getUniqueId()));
         }

         onlineLevelSnapshot = new CombatLevelCalculator.OnlineLevelSnapshot(revision, onlineCount, highestLevel);
         return highestLevel;
      }
   }

   public static int calculateCombatLevel(UUID playerUUID) {
      int highestWeapon = 1;
      int secondHighestWeapon = 1;

      for (SkillType skillType : SkillType.values()) {
         if (skillType != SkillType.ARMOR) {
            long xp = PlayerData.getSkillXP(playerUUID, skillType);
            int level = SkillXPCalculator.levelFromTotalXP(xp);
            if (level >= highestWeapon) {
               secondHighestWeapon = highestWeapon;
               highestWeapon = level;
            } else if (level > secondHighestWeapon) {
               secondHighestWeapon = level;
            }
         }
      }

      long armorXP = PlayerData.getSkillXP(playerUUID, SkillType.ARMOR);
      int armorLevel = SkillXPCalculator.levelFromTotalXP(armorXP);
      return (highestWeapon + secondHighestWeapon + armorLevel) / 3;
   }

   public static String getFormattedCombatLevel(UUID playerUUID) {
      int combatLevel = calculateCombatLevel(playerUUID);
      return ChatColorConverter.convert(SkillsConfig.getCombatLevelFormat().replace("$level", String.valueOf(combatLevel)));
   }

   private record OnlineLevelSnapshot(long revision, int onlineCount, int level) {
   }
}
