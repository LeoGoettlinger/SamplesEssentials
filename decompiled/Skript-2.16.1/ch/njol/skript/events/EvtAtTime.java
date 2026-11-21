package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptEventHandler;
import ch.njol.skript.events.bukkit.ScheduledEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Time;
import ch.njol.util.Math2;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EvtAtTime extends SkriptEvent implements Comparable<EvtAtTime> {
   private static final int CHECK_PERIOD = 10;
   private static final Map<World, EvtAtTime.EvtAtInfo> TRIGGERS = new ConcurrentHashMap<>();
   private int time;
   private World[] worlds;
   private static int taskID = -1;

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.time = ((Time)args[0].getSingle()).getTicks();
      this.worlds = args[1] == null ? Bukkit.getWorlds().toArray(new World[0]) : (World[])args[1].getAll();
      return true;
   }

   @Override
   public boolean postLoad() {
      for (World world : this.worlds) {
         EvtAtTime.EvtAtInfo info = TRIGGERS.get(world);
         if (info == null) {
            TRIGGERS.put(world, info = new EvtAtTime.EvtAtInfo());
            info.lastCheckedTime = (int)world.getTime() - 1;
         }

         info.instances.add(this);
      }

      registerListener();
      return true;
   }

   @Override
   public void unload() {
      Iterator<EvtAtTime.EvtAtInfo> iterator = TRIGGERS.values().iterator();

      while (iterator.hasNext()) {
         EvtAtTime.EvtAtInfo info = iterator.next();
         info.instances.remove(this);
         if (info.instances.isEmpty()) {
            iterator.remove();
         }
      }

      if (taskID != -1 && TRIGGERS.isEmpty()) {
         Bukkit.getScheduler().cancelTask(taskID);
         taskID = -1;
      }
   }

   @Override
   public boolean check(Event event) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isEventPrioritySupported() {
      return false;
   }

   private static void registerListener() {
      if (taskID == -1) {
         taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Skript.getInstance(), () -> {
            for (Entry<World, EvtAtTime.EvtAtInfo> entry : TRIGGERS.entrySet()) {
               EvtAtTime.EvtAtInfo info = entry.getValue();
               int worldTime = (int)entry.getKey().getTime();
               if (info.lastCheckedTime != worldTime) {
                  if (info.lastCheckedTime + 20 < worldTime || info.lastCheckedTime > worldTime && info.lastCheckedTime - 24000 + 20 < worldTime) {
                     info.lastCheckedTime = Math2.mod(worldTime - 10, 24000);
                  }

                  boolean midnight = info.lastCheckedTime > worldTime;
                  if (midnight) {
                     info.lastCheckedTime -= 24000;
                  }

                  for (EvtAtTime event : info.instances) {
                     int eventTime = midnight && event.time > 12000 ? event.time - 24000 : event.time;
                     if (eventTime > worldTime) {
                        break;
                     }

                     if (eventTime > info.lastCheckedTime) {
                        ScheduledEvent scheduledEvent = new ScheduledEvent(entry.getKey());
                        SkriptEventHandler.logEventStart(scheduledEvent);
                        SkriptEventHandler.logTriggerEnd(event.trigger);
                        event.trigger.execute(scheduledEvent);
                        SkriptEventHandler.logTriggerEnd(event.trigger);
                        SkriptEventHandler.logEventEnd();
                     }
                  }

                  info.lastCheckedTime = worldTime;
               }
            }
         }, 0L, 10L);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "at " + Time.toString(this.time) + " in worlds " + Classes.toString(this.worlds, true);
   }

   public int compareTo(@Nullable EvtAtTime event) {
      return event == null ? this.time : this.time - event.time;
   }

   static {
      Skript.registerEvent("*At Time", EvtAtTime.class, ScheduledEvent.class, "at %time% [in %worlds%]")
         .description("An event that occurs at a given <a href='#time'>minecraft time</a> in every world or only in specific worlds.")
         .examples("at 18:00", "at 7am in \"world\"")
         .since("1.3.4");
   }

   private static final class EvtAtInfo {
      private int lastCheckedTime;
      private final PriorityQueue<EvtAtTime> instances = new PriorityQueue<>(EvtAtTime::compareTo);
   }
}
