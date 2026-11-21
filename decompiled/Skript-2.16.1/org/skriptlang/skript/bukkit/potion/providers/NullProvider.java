package org.skriptlang.skript.bukkit.potion.providers;

import java.util.Collection;
import java.util.List;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

public class NullProvider extends PotionEffectProvider<Object> {
   public NullProvider() {
      super(null);
   }

   @Override
   public Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, PotionEffectProvider.RetrievalState state) {
      return List.of();
   }

   @Override
   public Collection<SkriptPotionEffect> getAll(PotionEffectProvider.RetrievalState state) {
      return List.of();
   }

   @Override
   public void add(PotionEffect potionEffect) {
   }

   @Override
   public void remove(SkriptPotionEffect potionEffect, PotionEffectProvider.RetrievalState state) {
   }

   @Override
   public void removeAll(PotionEffectType potionEffectType, PotionEffectProvider.RetrievalState state) {
   }

   @Override
   public void clear(PotionEffectType[] potionEffectTypes, PotionEffectProvider.RetrievalState state) {
   }

   @Override
   public void clearAll(PotionEffectProvider.RetrievalState state) {
   }

   @Override
   public void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable) {
   }
}
