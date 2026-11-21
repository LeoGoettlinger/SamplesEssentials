package me.byteful.plugin.leveltools.api.trigger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import me.byteful.plugin.leveltools.api.trigger.impl.ArmorDurabilityTrigger;
import me.byteful.plugin.leveltools.api.trigger.impl.BlockBreakTrigger;
import me.byteful.plugin.leveltools.api.trigger.impl.ConsumeTrigger;
import me.byteful.plugin.leveltools.api.trigger.impl.EntityKillTrigger;
import me.byteful.plugin.leveltools.api.trigger.impl.FarmingTrigger;
import me.byteful.plugin.leveltools.api.trigger.impl.FishingTrigger;
import me.byteful.plugin.leveltools.api.trigger.impl.LeftClickTrigger;
import me.byteful.plugin.leveltools.api.trigger.impl.RightClickTrigger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TriggerRegistry {
   private final Map<String, Trigger> triggers = new ConcurrentHashMap<>();

   public void register(@NotNull Trigger trigger) {
      String id = TriggerIds.normalize(trigger.getTriggerId());
      if (id.isEmpty()) {
         throw new IllegalArgumentException("Trigger ID cannot be null or empty");
      } else {
         this.triggers.put(id, trigger);
      }
   }

   public boolean unregister(@NotNull String triggerId) {
      return this.triggers.remove(TriggerIds.normalize(triggerId)) != null;
   }

   @Nullable
   public Trigger get(@NotNull String triggerId) {
      return this.triggers.get(TriggerIds.normalize(triggerId));
   }

   @NotNull
   public Optional<Trigger> getOptional(@NotNull String triggerId) {
      return Optional.ofNullable(this.get(triggerId));
   }

   public boolean has(@NotNull String triggerId) {
      return this.triggers.containsKey(TriggerIds.normalize(triggerId));
   }

   @NotNull
   public Set<String> getRegisteredIds() {
      return Collections.unmodifiableSet(this.triggers.keySet());
   }

   @NotNull
   public Collection<Trigger> getAll() {
      return Collections.unmodifiableCollection(this.triggers.values());
   }

   public void clear() {
      this.triggers.clear();
   }

   public void registerDefaults() {
      this.register(new BlockBreakTrigger());
      this.register(new EntityKillTrigger());
      this.register(new FishingTrigger());
      this.register(new RightClickTrigger());
      this.register(new LeftClickTrigger());
      this.register(new ConsumeTrigger());
      this.register(new FarmingTrigger());
      this.register(new ArmorDurabilityTrigger());
   }
}
