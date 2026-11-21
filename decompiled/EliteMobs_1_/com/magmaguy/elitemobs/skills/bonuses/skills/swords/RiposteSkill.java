package com.magmaguy.elitemobs.skills.bonuses.skills.swords;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class RiposteSkill extends SkillBonus implements CooldownSkill {
   public static final String SKILL_ID = "swords_riposte";
   private static final double BASE_COOLDOWN = 10.0;
   private static final double BASE_DAMAGE_MULTIPLIER = 1.83;
   private static final Map<UUID, Long> cooldownEnds = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> readyEnds = new ConcurrentHashMap<>();
   private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();

   public RiposteSkill() {
      super(SkillType.SWORDS, 25, "Riposte", "After blocking an attack, your next attack deals bonus damage.", SkillBonusType.COOLDOWN, 2, "swords_riposte");
   }

   @Override
   public long getCooldownSeconds(int skillLevel) {
      if (this.configFields != null && this.configFields.getCooldownSeconds() > 0.0) {
         return Math.max(1L, Math.round(this.configFields.calculateCooldown(skillLevel)));
      } else {
         double reduction = 1.0 - skillLevel * 0.005;
         return (long)Math.max(5.0, 10.0 * reduction);
      }
   }

   @Override
   public boolean isOnCooldown(Player player) {
      return this.getRemainingCooldown(player) > 0L;
   }

   @Override
   public void startCooldown(Player player, int skillLevel) {
      cooldownEnds.put(player.getUniqueId(), GameClock.getCurrentTick() + this.getCooldownSeconds(skillLevel) * 20L);
   }

   @Override
   public long getRemainingCooldown(Player player) {
      Long end = cooldownEnds.get(player.getUniqueId());
      if (end == null) {
         return 0L;
      } else {
         long ticks = end - GameClock.getCurrentTick();
         if (ticks <= 0L) {
            cooldownEnds.remove(player.getUniqueId(), end);
         }

         return Math.max(0L, (ticks + 19L) / 20L);
      }
   }

   @Override
   public void endCooldown(Player player) {
      cooldownEnds.remove(player.getUniqueId());
   }

   @Override
   public void onActivate(Player player, Object event) {
   }

   public void onProc(Player player, Object context) {
      if (context instanceof EliteMobDamagedByPlayerEvent event) {
         if (hasRiposteReady(player.getUniqueId())) {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(player, SkillType.SWORDS);
            double multiplier = this.getDamageMultiplier(skillLevel);
            event.setDamage(event.getDamage() * multiplier);
            readyEnds.remove(player.getUniqueId());
            this.startCooldown(player, skillLevel);
            this.incrementProcCount(player);
            SkillBonus.sendSkillActionBar(player, this);
         }
      }
   }

   public static void onPlayerBlock(Player player) {
      UUID uuid = player.getUniqueId();
      if (activePlayers.contains(uuid)) {
         if (cooldownEnds.getOrDefault(uuid, 0L) <= GameClock.getCurrentTick()) {
            readyEnds.put(uuid, GameClock.getCurrentTick() + 60L);
         }
      }
   }

   public static boolean hasRiposteReady(UUID playerUUID) {
      Long end = readyEnds.get(playerUUID);
      if (end == null) {
         return false;
      } else if (end > GameClock.getCurrentTick()) {
         return true;
      } else {
         readyEnds.remove(playerUUID, end);
         return false;
      }
   }

   private double getDamageMultiplier(int skillLevel) {
      return scaled(1.83, 0.01, 3.0, skillLevel);
   }

   @Override
   public void applyBonus(Player player, int skillLevel) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void removeBonus(Player player) {
      UUID uuid = player.getUniqueId();
      activePlayers.remove(uuid);
      cooldownEnds.remove(uuid);
      readyEnds.remove(uuid);
   }

   @Override
   public void onActivate(Player player) {
      activePlayers.add(player.getUniqueId());
   }

   @Override
   public void onDeactivate(Player player) {
      this.removeBonus(player);
   }

   @Override
   public boolean isActive(Player player) {
      return activePlayers.contains(player.getUniqueId());
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      double multiplier = (this.getDamageMultiplier(skillLevel) - 1.0) * 100.0;
      double cooldown = this.getCooldownSeconds(skillLevel);
      return this.applyLoreTemplates(Map.of("damage", String.format("%.0f", multiplier), "cooldown", String.format("%.1f", cooldown)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getDamageMultiplier(skillLevel);
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(Map.of("damage", String.format("%.0f", (this.getDamageMultiplier(skillLevel) - 1.0) * 100.0)));
   }

   @Override
   public boolean affectsDamage() {
      return false;
   }

   @Override
   public void shutdown() {
      cooldownEnds.clear();
      readyEnds.clear();
      activePlayers.clear();
   }
}
