package org.skriptlang.skript.bukkit.potion.util;

import ch.njol.skript.util.Timespan;
import java.util.ArrayDeque;
import java.util.Deque;
import org.bukkit.potion.PotionEffect;

public final class PotionUtils {
   public static final int DEFAULT_DURATION_TICKS = 600;
   public static final String DEFAULT_DURATION_STRING = new Timespan(Timespan.TimePeriod.TICK, 600L).toString();

   public static Deque<PotionEffect> getHiddenEffects(PotionEffect effect) {
      Deque<PotionEffect> hiddenEffects = new ArrayDeque<>();

      for (PotionEffect hiddenEffect = effect.getHiddenPotionEffect(); hiddenEffect != null; hiddenEffect = hiddenEffect.getHiddenPotionEffect()) {
         hiddenEffects.push(hiddenEffect);
      }

      return hiddenEffects;
   }
}
