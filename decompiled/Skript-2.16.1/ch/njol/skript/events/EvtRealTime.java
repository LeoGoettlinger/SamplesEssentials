package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtRealTime extends SkriptEvent {
   private static final long HOUR_24_MILLISECONDS = 86400000L;
   private static final Timer TIMER = new Timer("EvtSystemTime-Tasks");
   private Literal<Time> times;
   private boolean unloaded = false;
   private final List<TimerTask> timerTasks = new ArrayList<>();

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.times = (Literal<Time>)args[0];
      return true;
   }

   @Override
   public boolean postLoad() {
      Calendar currentCalendar = Calendar.getInstance();
      currentCalendar.setTimeZone(TimeZone.getDefault());

      for (Time time : this.times.getArray()) {
         Calendar expectedCalendar = Calendar.getInstance();
         expectedCalendar.setTimeZone(TimeZone.getDefault());
         expectedCalendar.set(14, 0);
         expectedCalendar.set(13, 0);
         expectedCalendar.set(12, time.getMinute());
         expectedCalendar.set(11, time.getHour());

         while (expectedCalendar.before(currentCalendar)) {
            expectedCalendar.add(11, 24);
         }

         TimerTask task = new TimerTask() {
            @Override
            public void run() {
               EvtRealTime.this.execute();
            }
         };
         this.timerTasks.add(task);
         TIMER.scheduleAtFixedRate(task, new Date(expectedCalendar.getTimeInMillis()), 86400000L);
      }

      return true;
   }

   @Override
   public void unload() {
      this.unloaded = true;

      for (TimerTask task : this.timerTasks) {
         task.cancel();
      }

      TIMER.purge();
   }

   @Override
   public boolean check(Event event) {
      throw new UnsupportedOperationException();
   }

   private void execute() {
      if (!this.unloaded) {
         Bukkit.getScheduler().scheduleSyncDelayedTask(Skript.getInstance(), () -> {
            EvtRealTime.RealTimeEvent event = new EvtRealTime.RealTimeEvent();
            SkriptEventHandler.logEventStart(event);
            SkriptEventHandler.logTriggerStart(this.trigger);
            this.trigger.execute(event);
            SkriptEventHandler.logTriggerEnd(this.trigger);
            SkriptEventHandler.logEventEnd();
         });
      }
   }

   @Override
   public boolean isEventPrioritySupported() {
      return false;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "at " + this.times.toString(event, debug) + " in real time";
   }

   static {
      Skript.registerEvent("System Time", EvtRealTime.class, EvtRealTime.RealTimeEvent.class, "at %times% [in] real time")
         .description("Called when the local time of the system the server is running on reaches the provided real-life time.")
         .examples(
            "at 14:20 in real time:",
            "at 2:30am real time:",
            "at 6:10 pm in real time:",
            "at 5:00 am and 5:00 pm in real time:",
            "at 5:00 and 17:00 in real time:"
         )
         .since("2.11");
   }

   public static class RealTimeEvent extends Event {
      @NotNull
      public HandlerList getHandlers() {
         throw new IllegalStateException();
      }
   }
}
