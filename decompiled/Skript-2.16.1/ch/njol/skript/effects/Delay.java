package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Delay")
@Description({"Delays the script's execution by a given timespan.", "When used as an effect, all code after the <code>wait</code> runs once the delay elapses. The whole trigger pauses until the wait finishes.", "When used as a section, only the code within the section is deferred. Code after the section continues immediately, and the body runs on the same event once the delay elapses. This is useful for scheduling follow-up work without blocking the rest of the trigger.", "Note that delays are not persistent. For example, <code>ban player → wait 7 days → unban player</code> will not resume if the server restarts during the delay.", "Inside a section body, outer <code>loop-value</code>s are not available because the body is parsed as a separate trigger. Event values (like <code>player</code>) still work. If you need a value from before the delay, copy it to a local variable. Local variables are snapshotted when the section is scheduled, so subsequent changes aren't reflected."})
@Example.Examples({@Example("wait 2 minutes"), @Example("halt for 5 minecraft hours"), @Example("wait a tick"), @Example("# Section form: outer continues immediately, body runs 3 seconds later.\nsend \"hello\" to player\nwait 3 seconds:\n\tsend \"...and goodbye\" to player\n"), @Example("send \"This runs first!\" to player\nwait 1 seconds:\n\tsend \"This runs third! (one second later)\" to player\nsend \"This runs second!\" to player\n")})
@Since({"1.4, 2.16 (Delayed Sections)"})
public class Delay extends EffectSection {
   protected Expression<Timespan> duration;
   @Nullable
   private Trigger trigger;
   private static final Set<Event> DELAYED = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

   @Override
   public boolean init(
      Expression<?>[] exprs,
      int matchedPattern,
      Kleenean isDelayed,
      SkriptParser.ParseResult parseResult,
      @Nullable SectionNode sectionNode,
      @Nullable List<TriggerItem> triggerItems
   ) {
      this.duration = (Expression<Timespan>)exprs[0];
      if (this.duration instanceof Literal) {
         Timespan timespan = ((Literal)this.duration).getSingle();
         if (timespan.isInfinite()) {
            Skript.error("Delaying for an eternity is not allowed. Use the 'stop' effect instead.");
            return false;
         }

         long millis = timespan.getAs(Timespan.TimePeriod.MILLISECOND);
         if (millis < 50L) {
            Skript.warning("Delays less than one tick are not possible, defaulting to one tick.");
         }
      }

      if (this.hasSection()) {
         assert sectionNode != null;

         Class<? extends Event>[] outerEvents = this.getParser().getCurrentEvents();
         this.trigger = SectionUtils.loadDelayableLinkedCode("wait", (beforeLoading, afterLoading) -> {
            Runnable bodyBefore = () -> {
               beforeLoading.run();
               this.getParser().setHasDelayBefore(Kleenean.TRUE);
            };
            return this.loadCode(sectionNode, "wait", bodyBefore, afterLoading, outerEvents);
         });
         return this.trigger != null;
      } else {
         this.getParser().setHasDelayBefore(Kleenean.TRUE);
         return true;
      }
   }

   @Nullable
   @Override
   protected TriggerItem walk(Event event) {
      this.debug(event, true);
      long start = Skript.debug() ? System.nanoTime() : 0L;
      if (!Skript.getInstance().isEnabled()) {
         this.error("Cannot delay code execution while the server is shutting down. The delay will be ignored.");
         return this.trigger != null ? super.walk(event, false) : null;
      } else {
         Timespan duration = this.duration.getSingle(event);
         if (duration == null) {
            return this.trigger != null ? super.walk(event, false) : null;
         } else {
            long ticks = Math.max(duration.getAs(Timespan.TimePeriod.TICK), 1L);
            TriggerItem afterDelay = (TriggerItem)(this.trigger != null ? this.trigger : this.getNext());
            if (afterDelay != null) {
               boolean isSection = this.trigger != null;
               Object localVars = isSection ? Variables.copyLocalVariables(event) : Variables.removeLocals(event);
               Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
                  addDelayedEvent(event);
                  Skript.debug(this.getIndentation() + "... continuing after " + (System.nanoTime() - start) / 1.0E9 + "s");
                  if (localVars != null) {
                     Variables.setLocalVariables(event, localVars);
                  }

                  TriggerItem.walk(afterDelay, event);
                  Variables.removeLocals(event);
               }, ticks);
            }

            return this.trigger != null ? super.walk(event, false) : null;
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "wait for " + this.duration.toString(event, debug) + (event == null ? "" : "...");
   }

   public static boolean isDelayed(Event event) {
      return DELAYED.contains(event);
   }

   public static void addDelayedEvent(Event event) {
      DELAYED.add(event);
   }

   static {
      Skript.registerSection(Delay.class, "(wait|halt) [for] %timespan%");
   }
}
