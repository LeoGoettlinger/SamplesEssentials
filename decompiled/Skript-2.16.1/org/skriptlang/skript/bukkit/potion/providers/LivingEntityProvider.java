package org.skriptlang.skript.bukkit.potion.providers;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.util.Timespan;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionDuration;
import org.skriptlang.skript.bukkit.potion.util.PotionUtils;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

class LivingEntityProvider extends PotionEffectProvider<LivingEntity> {
   public LivingEntityProvider(LivingEntity source) {
      super(source);
   }

   @Override
   public Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, PotionEffectProvider.RetrievalState state) {
      List<SkriptPotionEffect> potionEffects = new ArrayList<>();

      for (PotionEffectType type : potionEffectTypes) {
         PotionEffect potionEffect = this.source.getPotionEffect(type);
         if (potionEffect != null) {
            this.getEffects(potionEffect, state, potionEffects);
         }
      }

      return potionEffects;
   }

   @Override
   public Collection<SkriptPotionEffect> getAll(PotionEffectProvider.RetrievalState state) {
      List<SkriptPotionEffect> potionEffects = new ArrayList<>();

      for (PotionEffect potionEffect : this.source.getActivePotionEffects()) {
         this.getEffects(potionEffect, state, potionEffects);
      }

      return potionEffects;
   }

   private void getEffects(PotionEffect potionEffect, PotionEffectProvider.RetrievalState state, Collection<SkriptPotionEffect> destination) {
      if (state.includesActive()) {
         destination.add(SkriptPotionEffect.fromBukkitEffect(potionEffect, this));
      }

      if (state.includesHidden()) {
         for (PotionEffect hiddenEffect = potionEffect.getHiddenPotionEffect(); hiddenEffect != null; hiddenEffect = hiddenEffect.getHiddenPotionEffect()) {
            destination.add(SkriptPotionEffect.fromBukkitEffect(hiddenEffect));
         }
      }
   }

   @Override
   public void add(PotionEffect potionEffect) {
      this.source.addPotionEffect(potionEffect);
   }

   @Override
   public void remove(SkriptPotionEffect potionEffect, PotionEffectProvider.RetrievalState state) {
      PotionEffect entityEffect = this.source.getPotionEffect(potionEffect.potionEffectType());
      if (entityEffect != null) {
         Deque<PotionEffect> effects = PotionUtils.getHiddenEffects(entityEffect);
         boolean madeChanges = false;
         if (state.includesHidden()) {
            Iterator<PotionEffect> effectsIterator = effects.iterator();

            while (effectsIterator.hasNext()) {
               if (potionEffect.matchesQualities(effectsIterator.next())) {
                  effectsIterator.remove();
                  madeChanges = true;
               }
            }
         }

         if (state != PotionEffectProvider.RetrievalState.HIDDEN && potionEffect.matchesQualities(entityEffect)) {
            madeChanges = true;
         } else {
            effects.addLast(entityEffect);
         }

         if (madeChanges) {
            this.source.removePotionEffect(entityEffect.getType());
            this.source.addPotionEffects(effects);
         }
      }
   }

   @Override
   public void removeAll(PotionEffectType potionEffectType, PotionEffectProvider.RetrievalState state) {
      if (state != PotionEffectProvider.RetrievalState.ACTIVE && state != PotionEffectProvider.RetrievalState.HIDDEN) {
         this.source.removePotionEffect(potionEffectType);
      } else {
         this.clear(new PotionEffectType[]{potionEffectType}, state);
      }
   }

   @Override
   public void clear(PotionEffectType[] potionEffectTypes, PotionEffectProvider.RetrievalState state) {
      if (state == PotionEffectProvider.RetrievalState.ACTIVE) {
         for (PotionEffectType type : potionEffectTypes) {
            PotionEffect potionEffect = this.source.getPotionEffect(type);
            if (potionEffect != null) {
               Deque<PotionEffect> hiddenEffects = PotionUtils.getHiddenEffects(potionEffect);
               this.source.removePotionEffect(type);
               this.source.addPotionEffects(hiddenEffects);
            }
         }
      } else if (state == PotionEffectProvider.RetrievalState.HIDDEN) {
         for (PotionEffectType typex : potionEffectTypes) {
            PotionEffect original = this.source.getPotionEffect(typex);
            this.source.removePotionEffect(typex);
            if (original != null) {
               this.source.addPotionEffect(original);
            }
         }
      } else {
         for (PotionEffectType typexx : potionEffectTypes) {
            this.source.removePotionEffect(typexx);
         }
      }
   }

   @Override
   public void clearAll(PotionEffectProvider.RetrievalState state) {
      if (state == PotionEffectProvider.RetrievalState.ACTIVE) {
         for (PotionEffect potionEffect : this.source.getActivePotionEffects()) {
            Deque<PotionEffect> hiddenEffects = PotionUtils.getHiddenEffects(potionEffect);
            this.source.removePotionEffect(potionEffect.getType());
            this.source.addPotionEffects(hiddenEffects);
         }
      } else if (state == PotionEffectProvider.RetrievalState.HIDDEN) {
         for (PotionEffect potionEffect : this.source.getActivePotionEffects()) {
            this.source.removePotionEffect(potionEffect.getType());
            this.source.addPotionEffect(potionEffect);
         }
      } else {
         this.source.clearActivePotionEffects();
      }
   }

   @Override
   public void modify(PotionEffectType[] types, PotionEffectProvider.RetrievalState state, Object[] delta, Changer.ChangeMode mode) {
      for (PotionEffectType type : types) {
         PotionEffect potionEffect = this.source.getPotionEffect(type);
         if (potionEffect != null) {
            boolean madeChanges = false;
            Deque<PotionEffect> finalEffects;
            Deque<PotionEffect> effects;
            if (state.includesHidden()) {
               finalEffects = new ArrayDeque<>();
               effects = PotionUtils.getHiddenEffects(potionEffect);
            } else {
               finalEffects = PotionUtils.getHiddenEffects(potionEffect);
               effects = new ArrayDeque<>();
            }

            if (state.includesActive()) {
               effects.addLast(potionEffect);
            }

            label57:
            for (PotionEffect effect : effects) {
               SkriptPotionEffect skriptEffect = SkriptPotionEffect.fromBukkitEffect(effect);

               for (Object object : delta) {
                  if (object instanceof Timespan timespan) {
                     ExprPotionDuration.changeSafe(skriptEffect, timespan, mode);
                     madeChanges = true;
                  } else if (object instanceof SkriptPotionEffect base && base.matchesQualities(effect)) {
                     madeChanges = true;
                     continue label57;
                  }
               }

               finalEffects.addLast(skriptEffect.asBukkitPotionEffect());
            }

            if (!madeChanges) {
               return;
            }

            if (!state.includesActive()) {
               effects.addLast(potionEffect);
            }

            this.source.removePotionEffect(type);
            this.source.addPotionEffects(finalEffects);
         }
      }
   }

   @Override
   public void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable) {
      Deque<PotionEffect> hiddenEffects = null;
      PotionEffectType potionEffectType = potionEffect.potionEffectType();
      if (this.source.hasPotionEffect(potionEffectType)) {
         hiddenEffects = PotionUtils.getHiddenEffects(this.source.getPotionEffect(potionEffectType));
         this.source.removePotionEffect(potionEffectType);
      }

      runnable.run();
      PotionEffect updatedPotionEffect = potionEffect.asBukkitPotionEffect();
      if (hiddenEffects != null) {
         for (PotionEffect hiddenEffect : hiddenEffects) {
            if (updatedPotionEffect != null
               && (hiddenEffect.isShorterThan(updatedPotionEffect) || hiddenEffect.getAmplifier() > updatedPotionEffect.getAmplifier())) {
               this.source.addPotionEffect(updatedPotionEffect);
               updatedPotionEffect = null;
            }

            this.source.addPotionEffect(hiddenEffect);
         }
      }

      if (updatedPotionEffect != null) {
         this.source.addPotionEffect(updatedPotionEffect);
      }
   }
}
