package com.magmaguy.elitemobs.skills.bonuses.skills.staves;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusType;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.magic.MagicWeaponSkill;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class StokeTheFlamesSkill extends MagicWeaponSkill implements StackingSkill {
   public static final String SKILL_ID = "staves_stoke_the_flames";
   private static final int MAX_STACKS = 5;
   private static final long DECAY_MILLIS = 6000L;
   private final Map<UUID, StokeTheFlamesSkill.Heat> heat = new ConcurrentHashMap<>();

   public StokeTheFlamesSkill() {
      super(SkillType.STAVES, 2, "Stoke the Flames", "Each fireball that hits builds heat for the next.", SkillBonusType.STACKING, "staves_stoke_the_flames");
   }

   @Override
   public boolean stacksOnHit(EliteMobDamagedByPlayerEvent event) {
      if (event.getMagicStrike() != MagicStrike.STAFF_FIREBALL) {
         return false;
      } else {
         StokeTheFlamesSkill.Heat state = this.heat.computeIfAbsent(event.getPlayer().getUniqueId(), id -> new StokeTheFlamesSkill.Heat());
         UUID attackId = event.getMagicHit().attackId();
         if (!attackId.equals(state.attackId)) {
            if (System.currentTimeMillis() - state.lastFireball > 6000L) {
               state.stacks = 0;
            }

            state.attackId = attackId;
            state.stacksBeforeAttack = state.stacks;
            state.stackPending = true;
         }

         return true;
      }
   }

   @Override
   public int getMaxStacks() {
      return 5;
   }

   @Override
   public int getCurrentStacks(Player player) {
      StokeTheFlamesSkill.Heat state = this.heat.get(player.getUniqueId());
      return state == null ? 0 : state.stacksBeforeAttack;
   }

   @Override
   public void addStack(Player player) {
      StokeTheFlamesSkill.Heat state = this.heat.get(player.getUniqueId());
      if (state != null && state.stackPending) {
         state.stackPending = false;
         state.stacks = Math.min(5, state.stacksBeforeAttack + 1);
         state.lastFireball = System.currentTimeMillis();
      }
   }

   @Override
   public void resetStacks(Player player) {
      this.heat.remove(player.getUniqueId());
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      return scaled(0.05, 6.0E-4, skillLevel);
   }

   @Override
   protected void clearPlayer(UUID playerId) {
      this.heat.remove(playerId);
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("bonusPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0), "maxStacks", String.valueOf(5)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel) * 5.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("bonusPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0), "maxStacks", String.valueOf(5))
      );
   }

   @Override
   public void shutdown() {
      this.heat.clear();
      super.shutdown();
   }

   private static final class Heat {
      private UUID attackId;
      private int stacksBeforeAttack;
      private int stacks;
      private long lastFireball;
      private boolean stackPending;
   }
}
