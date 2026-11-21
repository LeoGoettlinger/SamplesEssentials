package com.magmaguy.elitemobs.api.power;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.NamespacedKey;

public record ElitePowerActionRequest(EliteEntity actor, NamespacedKey actionKey, Map<String, Object> payload, long gameTick, long generation) {
   public ElitePowerActionRequest(EliteEntity actor, NamespacedKey actionKey, Map<String, Object> payload, long gameTick, long generation) {
      Objects.requireNonNull(actor, "actor");
      Objects.requireNonNull(actionKey, "actionKey");
      Objects.requireNonNull(payload, "payload");
      if (generation < 1L) {
         throw new IllegalArgumentException("generation must be positive");
      } else {
         LinkedHashMap<String, Object> copy = new LinkedHashMap<>();

         for (Entry<String, Object> entry : payload.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "payload key");
            Object value = Objects.requireNonNull(entry.getValue(), "payload value for " + key);
            if (!(value instanceof String)
               && !(value instanceof Boolean)
               && !(value instanceof Long)
               && !(value instanceof Double)
               && !(value instanceof UUID)
               && !(value instanceof ElitePowerActionPosition)) {
               throw new IllegalArgumentException("Unsupported power action payload type for " + key + ": " + value.getClass().getName());
            }

            copy.put(key, value);
         }

         payload = Collections.unmodifiableMap(copy);
         this.actor = actor;
         this.actionKey = actionKey;
         this.payload = payload;
         this.gameTick = gameTick;
         this.generation = generation;
      }
   }
}
