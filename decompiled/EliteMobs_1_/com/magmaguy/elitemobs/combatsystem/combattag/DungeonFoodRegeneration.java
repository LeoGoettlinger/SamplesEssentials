package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class DungeonFoodRegeneration {
   static final int SATURATION_DURATION_TICKS = 1;
   static final int SATURATION_AMPLIFIER = 0;

   private DungeonFoodRegeneration() {
   }

   static boolean shouldRegenerateFood(Player player, boolean inCombat) {
      return !inCombat
         && usesVanillaHunger(player)
         && isEligibleDungeonPlayer(player)
         && player.getFoodLevel() < 20
         && !player.hasPotionEffect(PotionEffectType.SATURATION);
   }

   static boolean usesVanillaHunger(Player player) {
      GameMode gameMode = player.getGameMode();
      return gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;
   }

   static boolean hasPositiveDamageBeforeAbsorption(double finalDamage, double absorptionModifier) {
      double damageBeforeAbsorption = finalDamage - absorptionModifier;
      return Double.isFinite(damageBeforeAbsorption) && damageBeforeAbsorption > 0.0;
   }

   static boolean isEligibleDungeonPlayer(Player player) {
      return player.isOnline() && !player.isDead() ? isInEligibleCombatContent(player) : false;
   }

   static boolean isInEligibleCombatContent(Player player) {
      return matchesCombatContent(
         PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeon && player.getWorld().equals(dungeon.getWorld()),
         EliteMobsWorld.isEliteMobsWorld(player.getWorld().getUID())
      );
   }

   static boolean matchesCombatContent(boolean activeDungeonInstance, boolean eliteProtectedWorld) {
      return activeDungeonInstance || eliteProtectedWorld;
   }

   static PotionEffect createFoodRegenerationEffect() {
      return new PotionEffect(PotionEffectType.SATURATION, 1, 0, false, false, false);
   }
}
