package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CustomEntityPropertyState {
   private final Map<String, Object> values = new LinkedHashMap<>();

   public CustomEntityPropertyState set(String identifier, Object value) {
      if (identifier != null && !identifier.isBlank()) {
         if (value == null) {
            this.values.remove(identifier);
         } else {
            this.values.put(identifier, value);
         }

         return this;
      } else {
         throw new IllegalArgumentException("Property identifier cannot be blank");
      }
   }

   public CustomEntityPropertyState setBoolean(String identifier, boolean value) {
      return this.set(identifier, value);
   }

   public CustomEntityPropertyState setInt(String identifier, int value) {
      return this.set(identifier, value);
   }

   public CustomEntityPropertyState setFloat(String identifier, float value) {
      return this.set(identifier, value);
   }

   public CustomEntityPropertyState setString(String identifier, String value) {
      return this.set(identifier, value);
   }

   public CustomEntityPropertyState setAll(Map<String, ?> updates) {
      if (updates == null) {
         return this;
      } else {
         updates.forEach(this::set);
         return this;
      }
   }

   public Map<String, Object> snapshot() {
      return Collections.unmodifiableMap(new LinkedHashMap<>(this.values));
   }

   public boolean isEmpty() {
      return this.values.isEmpty();
   }
}
