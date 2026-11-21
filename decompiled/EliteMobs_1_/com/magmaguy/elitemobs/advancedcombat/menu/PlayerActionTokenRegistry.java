package com.magmaguy.elitemobs.advancedcombat.menu;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;

final class PlayerActionTokenRegistry {
   private static final Duration DEFAULT_TTL = Duration.ofMinutes(3L);
   private final long ttlMillis;
   private final LongSupplier clock;
   private final Map<String, PlayerActionTokenRegistry.Entry> entries = new HashMap<>();
   private final Map<UUID, Set<String>> playerTokens = new HashMap<>();

   PlayerActionTokenRegistry() {
      this(DEFAULT_TTL, System::currentTimeMillis);
   }

   PlayerActionTokenRegistry(Duration ttl, LongSupplier clock) {
      Objects.requireNonNull(ttl, "ttl");
      if (!ttl.isZero() && !ttl.isNegative()) {
         this.ttlMillis = ttl.toMillis();
         this.clock = Objects.requireNonNull(clock, "clock");
      } else {
         throw new IllegalArgumentException("ttl must be positive");
      }
   }

   synchronized void beginPage(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      this.invalidate(playerId);
      this.removeExpired(this.clock.getAsLong());
   }

   synchronized String issue(UUID playerId, ClassMenuAction action) {
      Objects.requireNonNull(playerId, "playerId");
      Objects.requireNonNull(action, "action");
      long now = this.clock.getAsLong();

      String token;
      do {
         token = UUID.randomUUID().toString().replace("-", "");
      } while (this.entries.containsKey(token));

      this.entries.put(token, new PlayerActionTokenRegistry.Entry(playerId, action, now + this.ttlMillis));
      this.playerTokens.computeIfAbsent(playerId, ignored -> new HashSet<>()).add(token);
      return token;
   }

   synchronized Optional<ClassMenuAction> consume(UUID playerId, String token) {
      Objects.requireNonNull(playerId, "playerId");
      if (token != null && !token.isBlank()) {
         PlayerActionTokenRegistry.Entry entry = this.entries.get(token);
         if (entry == null) {
            return Optional.empty();
         } else if (entry.expiresAtMillis() < this.clock.getAsLong()) {
            this.remove(token);
            return Optional.empty();
         } else if (!entry.playerId().equals(playerId)) {
            return Optional.empty();
         } else {
            this.remove(token);
            return Optional.of(entry.action());
         }
      } else {
         return Optional.empty();
      }
   }

   synchronized void invalidate(UUID playerId) {
      Set<String> tokens = this.playerTokens.remove(playerId);
      if (tokens != null) {
         for (String token : tokens) {
            this.entries.remove(token);
         }
      }
   }

   synchronized void clear() {
      this.entries.clear();
      this.playerTokens.clear();
   }

   synchronized int size() {
      this.removeExpired(this.clock.getAsLong());
      return this.entries.size();
   }

   private PlayerActionTokenRegistry.Entry remove(String token) {
      PlayerActionTokenRegistry.Entry entry = this.entries.remove(token);
      if (entry == null) {
         return null;
      } else {
         Set<String> tokens = this.playerTokens.get(entry.playerId());
         if (tokens != null) {
            tokens.remove(token);
            if (tokens.isEmpty()) {
               this.playerTokens.remove(entry.playerId());
            }
         }

         return entry;
      }
   }

   private void removeExpired(long now) {
      for (String token : Set.copyOf(this.entries.keySet())) {
         PlayerActionTokenRegistry.Entry entry = this.entries.get(token);
         if (entry != null && entry.expiresAtMillis() < now) {
            this.remove(token);
         }
      }
   }

   private record Entry(UUID playerId, ClassMenuAction action, long expiresAtMillis) {
   }
}
