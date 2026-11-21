package org.skriptlang.skript.bukkit.potion.providers;

import ch.njol.skript.aliases.ItemType;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

class ItemTypeProvider extends PotionEffectProvider<ItemType> {
   public static List<PotionEffect> getPotionEffects(ItemType itemType) {
      List<PotionEffect> effects = new ArrayList<>();
      ItemMeta meta = itemType.getItemMeta();
      if (meta instanceof PotionMeta potionMeta) {
         if (potionMeta.hasCustomEffects()) {
            effects.addAll(potionMeta.getCustomEffects());
         }

         if (potionMeta.hasBasePotionType()) {
            effects.addAll(potionMeta.getBasePotionType().getPotionEffects());
         }
      } else if (meta instanceof SuspiciousStewMeta stewMeta) {
         effects.addAll(stewMeta.getCustomEffects());
      }

      return effects;
   }

   public static void addPotionEffects(ItemType itemType, PotionEffect... potionEffects) {
      ItemMeta meta = itemType.getItemMeta();
      if (meta instanceof PotionMeta potionMeta) {
         for (PotionEffect potionEffect : potionEffects) {
            potionMeta.addCustomEffect(potionEffect, true);
         }
      } else if (meta instanceof SuspiciousStewMeta stewMeta) {
         for (PotionEffect potionEffect : potionEffects) {
            stewMeta.addCustomEffect(SuspiciousEffectEntry.create(potionEffect.getType(), potionEffect.getDuration()), true);
         }
      }

      itemType.setItemMeta(meta);
   }

   public static void removePotionEffects(ItemType itemType, PotionEffectType... potionEffectTypes) {
      ItemMeta meta = itemType.getItemMeta();
      if (meta instanceof PotionMeta potionMeta) {
         for (PotionEffectType potionEffectType : potionEffectTypes) {
            potionMeta.removeCustomEffect(potionEffectType);
         }
      } else if (meta instanceof SuspiciousStewMeta stewMeta) {
         for (PotionEffectType potionEffectType : potionEffectTypes) {
            stewMeta.removeCustomEffect(potionEffectType);
         }
      }

      itemType.setItemMeta(meta);
   }

   public static void clearPotionEffects(ItemType itemType) {
      ItemMeta meta = itemType.getItemMeta();
      if (meta instanceof PotionMeta potionMeta) {
         potionMeta.clearCustomEffects();
      } else if (meta instanceof SuspiciousStewMeta stewMeta) {
         stewMeta.clearCustomEffects();
      }

      itemType.setItemMeta(meta);
   }

   public ItemTypeProvider(ItemType source) {
      super(source);
   }

   @Override
   public Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, PotionEffectProvider.RetrievalState state) {
      if (!state.includesActive()) {
         return List.of();
      } else {
         List<SkriptPotionEffect> potionEffects = new ArrayList<>();

         for (PotionEffect effect : getPotionEffects(this.source)) {
            for (PotionEffectType type : potionEffectTypes) {
               if (type.equals(effect.getType())) {
                  potionEffects.add(SkriptPotionEffect.fromBukkitEffect(effect, this));
                  break;
               }
            }
         }

         return potionEffects;
      }
   }

   @Override
   public Collection<SkriptPotionEffect> getAll(PotionEffectProvider.RetrievalState state) {
      return !state.includesActive()
         ? List.of()
         : getPotionEffects(this.source).stream().map(potionEffect -> SkriptPotionEffect.fromBukkitEffect(potionEffect, this)).toList();
   }

   @Override
   public void add(PotionEffect potionEffect) {
      addPotionEffects(this.source, potionEffect);
   }

   @Override
   public void remove(SkriptPotionEffect potionEffect, PotionEffectProvider.RetrievalState state) {
      if (state.includesActive()) {
         for (PotionEffect itemEffect : getPotionEffects(this.source)) {
            if (potionEffect.matchesQualities(itemEffect)) {
               removePotionEffects(this.source, potionEffect.potionEffectType());
               break;
            }
         }
      }
   }

   @Override
   public void removeAll(PotionEffectType potionEffectType, PotionEffectProvider.RetrievalState state) {
      if (state.includesActive()) {
         removePotionEffects(this.source, potionEffectType);
      }
   }

   @Override
   public void clear(PotionEffectType[] potionEffectTypes, PotionEffectProvider.RetrievalState state) {
      if (state.includesActive()) {
         removePotionEffects(this.source, potionEffectTypes);
      }
   }

   @Override
   public void clearAll(PotionEffectProvider.RetrievalState state) {
      if (state.includesActive()) {
         clearPotionEffects(this.source);
      }
   }

   @Override
   public void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable) {
      removePotionEffects(this.source, potionEffect.potionEffectType());
      runnable.run();
      addPotionEffects(this.source, potionEffect.asBukkitPotionEffect());
   }
}
