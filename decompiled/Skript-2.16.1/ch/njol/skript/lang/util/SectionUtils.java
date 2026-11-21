package ch.njol.skript.lang.util;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.HintManager;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import org.jetbrains.annotations.Nullable;

public final class SectionUtils {
   private SectionUtils() {
   }

   @Nullable
   public static Trigger loadLinkedCode(String name, BiFunction<Runnable, Runnable, Trigger> triggerSupplier) {
      return loadLinkedCode(name, false, triggerSupplier);
   }

   @Nullable
   public static Trigger loadDelayableLinkedCode(String name, BiFunction<Runnable, Runnable, Trigger> triggerSupplier) {
      return loadLinkedCode(name, true, triggerSupplier);
   }

   @Nullable
   private static Trigger loadLinkedCode(String name, boolean allowDelays, BiFunction<Runnable, Runnable, Trigger> triggerSupplier) {
      AtomicBoolean delayed = new AtomicBoolean(false);
      AtomicReference<HintManager.Backup> hintBackup = new AtomicReference<>();
      Runnable beforeLoading = () -> ParserInstance.get().getHintManager().enterScope(false);
      Runnable afterLoading = () -> {
         ParserInstance parser = ParserInstance.get();
         delayed.set(!parser.getHasDelayBefore().isFalse());
         HintManager hintManagerx = parser.getHintManager();
         hintBackup.set(hintManagerx.backup());
         hintManagerx.exitScope();
      };
      Trigger trigger = triggerSupplier.apply(beforeLoading, afterLoading);
      if (!allowDelays && delayed.get()) {
         Skript.error("Delays can't be used within a '" + name + "' section");
         return null;
      } else {
         HintManager hintManager = ParserInstance.get().getHintManager();
         hintManager.enterScope(false);
         hintManager.restore(hintBackup.get());
         hintManager.exitScope();
         return trigger;
      }
   }
}
