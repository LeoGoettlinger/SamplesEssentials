package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class TimedScriptStateManager {
   private static final Map<TimedScriptStateManager.StateKey, TimedScriptStateManager.State> states = new HashMap<>();
   private static final Map<Object, Set<TimedScriptStateManager.StateKey>> ownerKeys = new IdentityHashMap<>();

   private TimedScriptStateManager() {
   }

   static <T> void apply(UUID targetId, String property, T value, int durationTicks, Supplier<T> currentValue, Consumer<T> writer) {
      TimedScriptStateManager.StateKey key = new TimedScriptStateManager.StateKey(targetId, property);
      if (durationTicks <= 0) {
         writer.accept(value);
         discard(key, states.get(key));
      } else {
         applyOwned(new Object(), targetId, property, value, durationTicks, currentValue, writer);
      }
   }

   public static <T> void applyOwned(Object owner, UUID targetId, String property, T value, int durationTicks, Supplier<T> currentValue, Consumer<T> writer) {
      Objects.requireNonNull(owner, "owner");
      TimedScriptStateManager.StateKey key = new TimedScriptStateManager.StateKey(targetId, property);
      TimedScriptStateManager.State state = states.get(key);
      T current = currentValue.get();
      if (state != null && !Objects.equals(current, state.appliedValue)) {
         discard(key, state);
         state = null;
      }

      TimedScriptStateManager.State selected = state == null ? new TimedScriptStateManager.State(current) : state;
      TimedScriptStateManager.Mutation mutation = new TimedScriptStateManager.Mutation(owner, value);
      if (durationTicks > 0) {
         mutation.task = MetadataHandler.PLUGIN
            .getServer()
            .getScheduler()
            .runTaskLater(MetadataHandler.PLUGIN, () -> release(key, selected, mutation), durationTicks);
      }

      try {
         writer.accept(value);
      } catch (Error | RuntimeException var13) {
         if (mutation.task != null) {
            mutation.task.cancel();
         }

         throw var13;
      }

      TimedScriptStateManager.Mutation previous = selected.active.remove(owner);
      if (previous != null && previous.task != null) {
         previous.task.cancel();
      }

      selected.active.put(owner, mutation);
      selected.reader = currentValue::get;
      selected.writer = raw -> writer.accept(cast(raw));
      selected.appliedValue = value;
      states.put(key, selected);
      ownerKeys.computeIfAbsent(owner, ignored -> new HashSet<>()).add(key);
   }

   public static void applyInvulnerability(Object owner, LivingEntity target, boolean value, int durationTicks) {
      UUID id = target.getUniqueId();
      applyOwned(
         owner,
         id,
         "invulnerable",
         new TimedScriptStateManager.Invulnerability(value, target instanceof Player && value),
         durationTicks,
         () -> new TimedScriptStateManager.Invulnerability(
            target.isInvulnerable(), target instanceof Player && ScriptAction.getInvulnerablePlayers().contains(id)
         ),
         state -> {
            target.setInvulnerable(state.nativeValue());
            if (target instanceof Player) {
               if (state.scriptOwned()) {
                  ScriptAction.getInvulnerablePlayers().add(id);
               } else {
                  ScriptAction.getInvulnerablePlayers().remove(id);
               }
            }
         }
      );
   }

   public static boolean releaseProperty(Object owner, UUID targetId, String property) {
      TimedScriptStateManager.StateKey key = new TimedScriptStateManager.StateKey(targetId, property);
      TimedScriptStateManager.State state = states.get(key);
      if (state != null && state.active.containsKey(owner)) {
         release(key, state, state.active.get(owner));
         return true;
      } else {
         return false;
      }
   }

   public static void releaseOwner(Object owner) {
      Set<TimedScriptStateManager.StateKey> keys = ownerKeys.get(owner);
      if (keys != null) {
         for (TimedScriptStateManager.StateKey key : Set.copyOf(keys)) {
            TimedScriptStateManager.State state = states.get(key);
            if (state != null) {
               release(key, state, state.active.get(owner));
            }
         }
      }
   }

   static void shutdown() {
      for (Entry<TimedScriptStateManager.StateKey, TimedScriptStateManager.State> entry : new ArrayList<>(states.entrySet())) {
         TimedScriptStateManager.State state = entry.getValue();
         if (restore(entry.getKey(), state, state.baseline)) {
            discard(entry.getKey(), state);
         }
      }
   }

   private static void release(TimedScriptStateManager.StateKey key, TimedScriptStateManager.State state, TimedScriptStateManager.Mutation mutation) {
      if (mutation != null && states.get(key) == state && state.active.get(mutation.owner) == mutation) {
         Object next = state.baseline;

         for (TimedScriptStateManager.Mutation candidate : state.active.values()) {
            if (candidate != mutation) {
               next = candidate.value;
            }
         }

         if (restore(key, state, next)) {
            if (states.get(key) == state) {
               state.active.remove(mutation.owner);
               if (mutation.task != null) {
                  mutation.task.cancel();
               }

               unindex(mutation.owner, key);
               if (state.active.isEmpty()) {
                  states.remove(key, state);
               }
            }
         }
      }
   }

   private static boolean restore(TimedScriptStateManager.StateKey key, TimedScriptStateManager.State state, Object value) {
      try {
         if (!Objects.equals(state.reader.get(), state.appliedValue)) {
            discard(key, state);
            return true;
         } else {
            state.writer.accept(value);
            state.appliedValue = value;
            return true;
         }
      } catch (RuntimeException var4) {
         Logger.warn("Could not restore timed state " + key.property + ": " + var4.getMessage());
         return false;
      }
   }

   private static void discard(TimedScriptStateManager.StateKey key, TimedScriptStateManager.State state) {
      if (state != null && states.remove(key, state)) {
         for (TimedScriptStateManager.Mutation mutation : state.active.values()) {
            if (mutation.task != null) {
               mutation.task.cancel();
            }

            unindex(mutation.owner, key);
         }
      }
   }

   private static void unindex(Object owner, TimedScriptStateManager.StateKey key) {
      Set<TimedScriptStateManager.StateKey> keys = ownerKeys.get(owner);
      if (keys != null) {
         keys.remove(key);
         if (keys.isEmpty()) {
            ownerKeys.remove(owner);
         }
      }
   }

   private static <T> T cast(Object value) {
      return (T)value;
   }

   private record Invulnerability(boolean nativeValue, boolean scriptOwned) {
   }

   private static final class Mutation {
      final Object owner;
      final Object value;
      BukkitTask task;

      Mutation(Object owner, Object value) {
         this.owner = owner;
         this.value = value;
      }
   }

   private static final class State {
      final Object baseline;
      final Map<Object, TimedScriptStateManager.Mutation> active = new LinkedHashMap<>();
      Supplier<Object> reader;
      Consumer<Object> writer;
      Object appliedValue;

      State(Object baseline) {
         this.baseline = baseline;
      }
   }

   private record StateKey(UUID targetId, String property) {
   }
}
