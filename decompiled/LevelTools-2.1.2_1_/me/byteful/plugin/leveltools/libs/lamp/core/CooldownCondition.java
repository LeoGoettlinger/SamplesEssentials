package me.byteful.plugin.leveltools.libs.lamp.core;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import me.byteful.plugin.leveltools.libs.lamp.annotation.Cooldown;
import me.byteful.plugin.leveltools.libs.lamp.command.CommandActor;
import me.byteful.plugin.leveltools.libs.lamp.command.ExecutableCommand;
import me.byteful.plugin.leveltools.libs.lamp.exception.CooldownException;
import me.byteful.plugin.leveltools.libs.lamp.process.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

enum CooldownCondition implements CommandCondition {
   INSTANCE;

   private static final ScheduledExecutorService COOLDOWN_POOL = Executors.newSingleThreadScheduledExecutor();
   private final Map<UUID, Map<Integer, Long>> cooldowns = new ConcurrentHashMap<>();

   @Override
   public void test(@NotNull CommandActor actor, @NotNull ExecutableCommand command, @NotNull @Unmodifiable List<String> arguments) {
      Cooldown cooldown = command.getAnnotation(Cooldown.class);
      if (cooldown != null && cooldown.value() != 0L) {
         UUID uuid = actor.getUniqueId();
         Map<Integer, Long> spans = this.get(uuid);
         Long created = spans.get(command.getId());
         if (created == null) {
            spans.put(command.getId(), System.currentTimeMillis());
            COOLDOWN_POOL.schedule(() -> spans.remove(command.getId()), cooldown.value(), cooldown.unit());
         } else {
            long passed = System.currentTimeMillis() - created;
            long left = cooldown.unit().toMillis(cooldown.value()) - passed;
            if (left > 0L && left < 1000L) {
               left = 1000L;
            }

            throw new CooldownException(left);
         }
      }
   }

   private Map<Integer, Long> get(@NotNull UUID uuid) {
      return this.cooldowns.computeIfAbsent(uuid, u -> new ConcurrentHashMap<>());
   }
}
