package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public abstract class AsyncEffect extends Effect {
   @Nullable
   @Override
   protected TriggerItem walk(Event e) {
      this.debug(e, true);
      Object localVars = Variables.removeLocals(e);
      if (!Skript.getInstance().isEnabled()) {
         return null;
      } else {
         Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
            Delay.addDelayedEvent(e);
            if (localVars != null) {
               Variables.setLocalVariables(e, localVars);
            }

            this.execute(e);
            if (this.getNext() != null) {
               Bukkit.getScheduler().runTask(Skript.getInstance(), () -> {
                  Object timing = null;
                  if (SkriptTimings.enabled()) {
                     Trigger trigger = this.getTrigger();
                     if (trigger != null) {
                        timing = SkriptTimings.start(trigger.getDebugLabel());
                     }
                  }

                  TriggerItem.walk(this.getNext(), e);
                  Variables.removeLocals(e);
                  SkriptTimings.stop(timing);
               });
            } else {
               Variables.removeLocals(e);
            }
         });
         return null;
      }
   }
}
