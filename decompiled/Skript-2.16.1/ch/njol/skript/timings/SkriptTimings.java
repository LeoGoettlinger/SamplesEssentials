package ch.njol.skript.timings;

import ch.njol.skript.Skript;
import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import org.jetbrains.annotations.Nullable;

public class SkriptTimings {
   private static volatile boolean enabled;
   private static Skript skript;

   @Nullable
   public static Object start(String name) {
      if (!enabled()) {
         return null;
      } else {
         Timing timing = Timings.of(skript, name);
         timing.startTimingIfSync();

         assert timing != null;

         return timing;
      }
   }

   public static void stop(@Nullable Object timing) {
      if (timing != null) {
         ((Timing)timing).stopTimingIfSync();
      }
   }

   public static boolean enabled() {
      return enabled && Timings.isTimingsEnabled();
   }

   public static void setEnabled(boolean flag) {
      enabled = flag;
   }

   public static void setSkript(Skript plugin) {
      skript = plugin;
   }
}
