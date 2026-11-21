package com.oheers.fish.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CooldownHelper {
   private final Map<UUID, Instant> cooldownMap = new HashMap<>();

   private CooldownHelper() {
   }

   public static CooldownHelper create() {
      return new CooldownHelper();
   }

   public void applyCooldown(@NonNull UUID uuid, @NonNull Duration duration) {
      this.cooldownMap.put(uuid, Instant.now().plus(duration));
   }

   public boolean hasCooldown(@NonNull UUID uuid) {
      Instant cooldown = this.cooldownMap.get(uuid);
      return cooldown != null && Instant.now().isBefore(cooldown);
   }

   @Nullable
   public Instant removeCooldown(@NonNull UUID uuid) {
      return this.cooldownMap.remove(uuid);
   }

   public Duration getRemainingCooldown(@NonNull UUID uuid) {
      Instant cooldown = this.cooldownMap.get(uuid);
      Instant now = Instant.now();
      return cooldown != null && now.isBefore(cooldown) ? Duration.between(now, cooldown) : Duration.ZERO;
   }
}
