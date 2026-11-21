package org.skriptlang.skript.bukkit.potion.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

class EntityProvider extends PotionEffectProvider<EntityProvider.PotionAccessor> {
   public EntityProvider(AreaEffectCloud areaEffectCloud) {
      super(new EntityProvider.AreaEffectCloudAccessor(areaEffectCloud));
   }

   public EntityProvider(Arrow arrow) {
      super(new EntityProvider.ArrowAccessor(arrow));
   }

   @Override
   public Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, PotionEffectProvider.RetrievalState state) {
      if (!state.includesActive()) {
         return List.of();
      } else {
         List<PotionEffect> potionEffects = new ArrayList<>();
         PotionType potionType = this.source.getBasePotionType();
         if (potionType != null) {
            potionEffects.addAll(potionType.getPotionEffects());
         }

         if (this.source.hasCustomEffects()) {
            potionEffects.addAll(this.source.getCustomEffects());
         }

         return potionEffects.stream().filter(effect -> {
            for (PotionEffectType type : potionEffectTypes) {
               if (type.equals(effect.getType())) {
                  return true;
               }
            }

            return false;
         }).map(effect -> SkriptPotionEffect.fromBukkitEffect(effect, this)).toList();
      }
   }

   @Override
   public Collection<SkriptPotionEffect> getAll(PotionEffectProvider.RetrievalState state) {
      if (!state.includesActive()) {
         return List.of();
      } else {
         List<SkriptPotionEffect> potionEffects = new ArrayList<>();
         PotionType potionType = this.source.getBasePotionType();
         if (potionType != null) {
            for (PotionEffect effect : potionType.getPotionEffects()) {
               potionEffects.add(SkriptPotionEffect.fromBukkitEffect(effect, this));
            }
         }

         if (this.source.hasCustomEffects()) {
            for (PotionEffect effect : this.source.getCustomEffects()) {
               potionEffects.add(SkriptPotionEffect.fromBukkitEffect(effect, this));
            }
         }

         return potionEffects;
      }
   }

   @Override
   public void add(PotionEffect potionEffect) {
      this.source.addCustomEffect(potionEffect, true);
   }

   @Override
   public void remove(SkriptPotionEffect potionEffect, PotionEffectProvider.RetrievalState state) {
      if (state.includesActive() && this.source.hasCustomEffects()) {
         for (PotionEffect itemEffect : this.source.getCustomEffects()) {
            if (potionEffect.matchesQualities(itemEffect)) {
               this.source.removeCustomEffect(potionEffect.potionEffectType());
               break;
            }
         }
      }
   }

   @Override
   public void removeAll(PotionEffectType potionEffectType, PotionEffectProvider.RetrievalState state) {
      if (state.includesActive()) {
         this.source.removeCustomEffect(potionEffectType);
      }
   }

   @Override
   public void clear(PotionEffectType[] potionEffectTypes, PotionEffectProvider.RetrievalState state) {
      if (state.includesActive()) {
         for (PotionEffectType potionEffectType : potionEffectTypes) {
            this.source.removeCustomEffect(potionEffectType);
         }
      }
   }

   @Override
   public void clearAll(PotionEffectProvider.RetrievalState state) {
      if (state.includesActive()) {
         this.source.clearCustomEffects();
      }
   }

   @Override
   public void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable) {
      this.source.removeCustomEffect(potionEffect.potionEffectType());
      runnable.run();
      this.source.addCustomEffect(potionEffect.asBukkitPotionEffect(), true);
   }

   private record AreaEffectCloudAccessor(AreaEffectCloud areaEffectCloud) implements EntityProvider.PotionAccessor {
      @Nullable
      @Override
      public PotionType getBasePotionType() {
         return this.areaEffectCloud.getBasePotionType();
      }

      @Override
      public boolean hasCustomEffects() {
         return this.areaEffectCloud.hasCustomEffects();
      }

      @Override
      public List<PotionEffect> getCustomEffects() {
         return this.areaEffectCloud.getCustomEffects();
      }

      @Override
      public void addCustomEffect(PotionEffect effect, boolean overwrite) {
         this.areaEffectCloud.addCustomEffect(effect, overwrite);
      }

      @Override
      public void removeCustomEffect(PotionEffectType potionEffectType) {
         this.areaEffectCloud.removeCustomEffect(potionEffectType);
      }

      @Override
      public void clearCustomEffects() {
         this.areaEffectCloud.clearCustomEffects();
      }
   }

   private record ArrowAccessor(Arrow arrow) implements EntityProvider.PotionAccessor {
      @Nullable
      @Override
      public PotionType getBasePotionType() {
         return this.arrow.getBasePotionType();
      }

      @Override
      public boolean hasCustomEffects() {
         return this.arrow.hasCustomEffects();
      }

      @Override
      public List<PotionEffect> getCustomEffects() {
         return this.arrow.getCustomEffects();
      }

      @Override
      public void addCustomEffect(PotionEffect effect, boolean overwrite) {
         this.arrow.addCustomEffect(effect, overwrite);
      }

      @Override
      public void removeCustomEffect(PotionEffectType potionEffectType) {
         this.arrow.removeCustomEffect(potionEffectType);
      }

      @Override
      public void clearCustomEffects() {
         this.arrow.clearCustomEffects();
      }
   }

   public interface PotionAccessor {
      @Nullable
      PotionType getBasePotionType();

      boolean hasCustomEffects();

      List<PotionEffect> getCustomEffects();

      void addCustomEffect(PotionEffect var1, boolean var2);

      void removeCustomEffect(PotionEffectType var1);

      void clearCustomEffects();
   }
}
