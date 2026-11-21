package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ElitePotionEffectContainer {
   public ElitePotionEffectContainer(ItemMeta itemMeta, List<String> potionEffects) {
      itemMeta.getPersistentDataContainer().remove(ItemTagger.onHitPotionEffectKey);
      itemMeta.getPersistentDataContainer().remove(ItemTagger.continuousPotionEffectKey);
      if (!potionEffects.isEmpty()) {
         StringBuilder onHit = new StringBuilder();
         StringBuilder continuous = new StringBuilder();

         for (String substring : potionEffects) {
            ElitePotionEffect elitePotionEffect = parse(substring);
            if (elitePotionEffect != null) {
               switch (elitePotionEffect.getApplicationMethod()) {
                  case ONHIT:
                     onHit.append(substring).append(":");
                     break;
                  case CONTINUOUS:
                  default:
                     continuous.append(substring).append(":");
               }
            }
         }

         if (onHit.length() > 0) {
            itemMeta.getPersistentDataContainer().set(ItemTagger.onHitPotionEffectKey, PersistentDataType.STRING, onHit.toString());
         }

         if (continuous.length() > 0) {
            itemMeta.getPersistentDataContainer().set(ItemTagger.continuousPotionEffectKey, PersistentDataType.STRING, continuous.toString());
         }
      }
   }

   public static ArrayList<ElitePotionEffect> getElitePotionEffectContainer(ItemMeta itemMeta, NamespacedKey namespacedKey) {
      if (itemMeta == null) {
         return new ArrayList<>();
      } else if (!itemMeta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING)) {
         return new ArrayList<>();
      } else {
         ArrayList<ElitePotionEffect> elitePotionEffects = new ArrayList<>();

         for (String string : ((String)itemMeta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)).split(":")) {
            if (!string.isEmpty()) {
               ElitePotionEffect effect = parse(string);
               if (effect != null && effect.isEnabled()) {
                  elitePotionEffects.add(effect);
               }
            }
         }

         return elitePotionEffects;
      }
   }

   private static ElitePotionEffect parse(String definition) {
      try {
         return new ElitePotionEffect(definition);
      } catch (ArithmeticException | IllegalArgumentException var2) {
         Logger.warn("Invalid equipment potion entry '" + definition + "': " + var2.getMessage());
         return null;
      }
   }
}
