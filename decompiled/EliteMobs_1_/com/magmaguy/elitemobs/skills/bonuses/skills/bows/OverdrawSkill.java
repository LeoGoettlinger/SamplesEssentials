package com.magmaguy.elitemobs.skills.bonuses.skills.bows;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class OverdrawSkill extends SkillBonus implements ConditionalSkill {
   public static final String SKILL_ID = "bows_overdraw";
   private static final long FULL_DRAW_TIME = 1500L;
   private static final double BASE_BONUS = 0.1;
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
   private static final Map<UUID, Long> drawStartTimes = new ConcurrentHashMap<>();

   public OverdrawSkill() {
      super(SkillType.BOWS, 25, "Overdraw", "Holding your bow longer increases damage.", SkillBonusType.CONDITIONAL, 2, "bows_overdraw");
   }

   public static boolean hasActiveSkill(UUID playerUUID) {
      return activePlayers.contains(playerUUID);
   }

   public static void startDrawing(UUID uuid) {
      drawStartTimes.put(uuid, System.nanoTime());
   }

   public static void snapshotDrawDuration(UUID uuid, Projectile projectile) {
      Long startTime = drawStartTimes.remove(uuid);
      ItemTagger.setArrowDrawMillis(projectile, startTime == null ? 0L : (System.nanoTime() - startTime) / 1000000L);
   }

   public static void stopDrawing(UUID uuid) {
      drawStartTimes.remove(uuid);
   }

   public static long getDrawTime(UUID uuid) {
      Long startTime = drawStartTimes.get(uuid);
      return startTime == null ? 0L : (System.nanoTime() - startTime) / 1000000L;
   }

   @Override
   public boolean conditionMet(Player player, Object context) {
      return context instanceof EliteMobDamagedByPlayerEvent event
            && event.getEntityDamageByEntityEvent() != null
            && event.getEntityDamageByEntityEvent().getDamager() instanceof Projectile projectile
         ? ItemTagger.getArrowDrawMillis(projectile) >= 1500L
         : CombatSimulator.isTestingActive(player) && getDrawTime(player.getUniqueId()) >= 1500L;
   }

   @Override
   public double getConditionalBonus(int skillLevel) {
      return scaled(0.1, 0.003, skillLevel);
   }

   public double calculateBonus(Player player, int skillLevel) {
      long drawTime = getDrawTime(player.getUniqueId());
      if (drawTime < 1500L) {
         return 0.0;
      } else {
         double overdrawMultiplier = Math.min((drawTime - 1500L) / 1000.0, 2.0);
         return this.getConditionalBonus(skillLevel) * (1.0 + overdrawMultiplier);
      }
   }

   public static void simulateFullDraw(UUID uuid) {
      drawStartTimes.put(uuid, System.nanoTime() - 2000000000L);
   }

   @Override
   public SkillBonus.TestStrategy getTestStrategy() {
      return SkillBonus.TestStrategy.CONDITION_SETUP;
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      activePlayers.remove(player.getUniqueId());
      drawStartTimes.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      activePlayers.remove(player.getUniqueId());
      drawStartTimes.remove(player.getUniqueId());
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("baseBonus", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getConditionalBonus(skillLevel) * 3.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("baseBonus", String.format("%.1f", this.getConditionalBonus(skillLevel) * 100.0)));
   }

   @Override
   public void shutdown() {
      activePlayers.clear();
      drawStartTimes.clear();
   }
}
