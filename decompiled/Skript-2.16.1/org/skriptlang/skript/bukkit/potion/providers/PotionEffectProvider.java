package org.skriptlang.skript.bukkit.potion.providers;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import java.util.Collection;
import java.util.function.Consumer;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionDuration;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

@Internal
public abstract class PotionEffectProvider<T> {
   protected final T source;

   public static PotionEffectProvider<?> of(Object object, Consumer<String> errorProducer) {
      return (PotionEffectProvider<?>)(switch (object) {
         case AreaEffectCloud areaEffectCloud -> new EntityProvider(areaEffectCloud);
         case Arrow arrow -> new EntityProvider(arrow);
         case ItemType itemType -> new ItemTypeProvider(itemType);
         case LivingEntity livingEntity -> new LivingEntityProvider(livingEntity);
         default -> {
            errorProducer.accept(Utils.A(Classes.toString(object)) + " does not have potion effects");
            yield new NullProvider();
         }
      });
   }

   public PotionEffectProvider(T source) {
      this.source = source;
   }

   public abstract Collection<SkriptPotionEffect> get(PotionEffectType[] var1, PotionEffectProvider.RetrievalState var2);

   public abstract Collection<SkriptPotionEffect> getAll(PotionEffectProvider.RetrievalState var1);

   public abstract void add(PotionEffect var1);

   public abstract void remove(SkriptPotionEffect var1, PotionEffectProvider.RetrievalState var2);

   public abstract void removeAll(PotionEffectType var1, PotionEffectProvider.RetrievalState var2);

   public abstract void clear(PotionEffectType[] var1, PotionEffectProvider.RetrievalState var2);

   public abstract void clearAll(PotionEffectProvider.RetrievalState var1);

   public void modify(PotionEffectType[] types, PotionEffectProvider.RetrievalState state, Object[] delta, Changer.ChangeMode mode) {
      if (delta[0] instanceof Timespan timespan) {
         this.get(types, state).forEach(potionEffect -> ExprPotionDuration.changeSafe(potionEffect, timespan, mode));
      }
   }

   public abstract void mirrorEffectChanges(SkriptPotionEffect var1, Runnable var2);

   public static enum RetrievalState {
      UNSET,
      ACTIVE,
      HIDDEN,
      BOTH;

      public static PotionEffectProvider.RetrievalState fromParseTag(String tag) {
         return switch (tag) {
            case "active" -> ACTIVE;
            case "hidden" -> HIDDEN;
            case "both" -> BOTH;
            default -> UNSET;
         };
      }

      public boolean includesActive() {
         return this != HIDDEN;
      }

      public boolean includesHidden() {
         return this == HIDDEN || this == BOTH;
      }

      public String displayName() {
         return switch (this) {
            case UNSET -> "";
            case ACTIVE -> "active";
            case HIDDEN -> "hidden";
            case BOTH -> "active and hidden";
         };
      }
   }
}
