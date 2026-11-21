package com.magmaguy.elitemobs.skills.bonuses.skills.wands;

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

public class SpellweaveSkill extends MagicWeaponSkill implements StackingSkill {
   public static final String SKILL_ID = "wands_spellweave";
   private static final int MAX_STACKS = 8;
   private static final long DECAY_MILLIS = 3000L;
   private final Map<UUID, SpellweaveSkill.Weave> weaves = new ConcurrentHashMap<>();

   public SpellweaveSkill() {
      super(SkillType.WANDS, 2, "Spellweave", "Consecutive missiles on one target build damage.", SkillBonusType.STACKING, "wands_spellweave");
   }

   @Override
   public boolean stacksOnHit(EliteMobDamagedByPlayerEvent event) {
      if (event.getMagicStrike() == MagicStrike.WAND_MISSILE && event.getEliteMobEntity().getLivingEntity() != null) {
         SpellweaveSkill.Weave weave = this.weaves.computeIfAbsent(event.getPlayer().getUniqueId(), id -> new SpellweaveSkill.Weave());
         UUID target = event.getEliteMobEntity().getLivingEntity().getUniqueId();
         UUID cast = event.getMagicHit().attackId();
         if (weave.strayCast != null && !weave.strayCast.equals(cast)) {
            weave.weaveOnto(weave.strayTarget);
         }

         if (weave.target == null) {
            weave.weaveOnto(target);
         }

         if (target.equals(weave.target)) {
            weave.wovenCast = cast;
            if (cast.equals(weave.strayCast)) {
               weave.strayCast = null;
            }

            return true;
         } else {
            if (!cast.equals(weave.wovenCast) && weave.strayCast == null) {
               weave.strayCast = cast;
               weave.strayTarget = target;
            }

            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int getMaxStacks() {
      return 8;
   }

   @Override
   public int getCurrentStacks(Player player) {
      SpellweaveSkill.Weave weave = this.weaves.get(player.getUniqueId());
      if (weave == null) {
         return 0;
      } else {
         if (System.currentTimeMillis() - weave.lastHit > 3000L) {
            weave.stacks = 0;
         }

         return weave.stacks;
      }
   }

   @Override
   public void addStack(Player player) {
      SpellweaveSkill.Weave weave = this.weaves.computeIfAbsent(player.getUniqueId(), id -> new SpellweaveSkill.Weave());
      weave.stacks = Math.min(8, this.getCurrentStacks(player) + 1);
      weave.lastHit = System.currentTimeMillis();
   }

   @Override
   public void resetStacks(Player player) {
      this.weaves.remove(player.getUniqueId());
   }

   @Override
   public double getBonusPerStack(int skillLevel) {
      return scaled(0.03, 4.0E-4, skillLevel);
   }

   @Override
   protected void clearPlayer(UUID playerId) {
      this.weaves.remove(playerId);
   }

   @Override
   public List<String> getLoreDescription(int skillLevel) {
      return this.applyLoreTemplates(Map.of("bonusPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0), "maxStacks", String.valueOf(8)));
   }

   @Override
   public double getBonusValue(int skillLevel) {
      return this.getBonusPerStack(skillLevel) * 8.0;
   }

   @Override
   public String getFormattedBonus(int skillLevel) {
      return this.applyFormattedBonusTemplate(
         Map.of("bonusPerStack", String.format("%.1f", this.getBonusPerStack(skillLevel) * 100.0), "maxStacks", String.valueOf(8))
      );
   }

   @Override
   public void shutdown() {
      this.weaves.clear();
      super.shutdown();
   }

   private static final class Weave {
      private UUID target;
      private int stacks;
      private long lastHit;
      private UUID wovenCast;
      private UUID strayCast;
      private UUID strayTarget;

      private void weaveOnto(UUID newTarget) {
         this.target = newTarget;
         this.stacks = 0;
         this.wovenCast = null;
         this.strayCast = null;
         this.strayTarget = null;
      }
   }
}
