package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class IndeterminateDelay extends Delay {
   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      this.debug(event, true);
      long start = Skript.debug() ? System.nanoTime() : 0L;
      TriggerItem next = this.getNext();
      if (next != null && Skript.getInstance().isEnabled()) {
         Timespan duration = this.duration.getSingle(event);
         if (duration == null) {
            return null;
         }

         Object localVars = Variables.removeLocals(event);
         Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
            Delay.addDelayedEvent(event);
            Skript.debug(this.getIndentation() + "... continuing after " + (System.nanoTime() - start) / 1.0E9 + "s");
            if (localVars != null) {
               Variables.setLocalVariables(event, localVars);
            }

            TriggerItem.walk(next, event);
         }, duration.getAs(Timespan.TimePeriod.TICK));
      }

      return null;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "wait for operation to finish";
   }
}
