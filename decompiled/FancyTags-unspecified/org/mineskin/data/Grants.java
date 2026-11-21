package org.mineskin.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Optional;

public class Grants {
   private final JsonObject raw;

   public Grants(JsonObject raw) {
      this.raw = raw;
   }

   public Optional<JsonElement> getRaw(String key) {
      return this.raw.has(key) && !this.raw.get(key).isJsonNull() ? Optional.of(this.raw.get(key)) : Optional.empty();
   }

   public Optional<Boolean> getBoolean(String key) {
      return this.getRaw(key).map(JsonElement::getAsBoolean);
   }

   public Optional<Integer> getInt(String key) {
      return this.getRaw(key).map(JsonElement::getAsInt);
   }

   public Optional<Double> getDouble(String key) {
      return this.getRaw(key).map(JsonElement::getAsDouble);
   }

   public Optional<String> getString(String key) {
      return this.getRaw(key).map(JsonElement::getAsString);
   }

   public Optional<Integer> perMinute() {
      return this.getInt("per_minute");
   }

   public Optional<Integer> concurrency() {
      return this.getInt("concurrency");
   }

   @Override
   public String toString() {
      return "Grants" + this.raw;
   }
}
