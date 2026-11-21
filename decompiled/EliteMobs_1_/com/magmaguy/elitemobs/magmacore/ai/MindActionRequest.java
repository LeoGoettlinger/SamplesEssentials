package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public record MindActionRequest(String identifier, Map<String, ?> payload) {
   public static final int MAX_IDENTIFIER_LENGTH = 128;
   public static final int MAX_PAYLOAD_ENTRIES = 16;
   public static final int MAX_PAYLOAD_KEY_LENGTH = 64;
   public static final int MAX_STRING_LENGTH = 256;
   public static final int MAX_PAYLOAD_WEIGHT = 2048;
   private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9._-]+:[a-z0-9/._-]+");
   private static final Pattern PAYLOAD_KEY = Pattern.compile("[a-z0-9._-]+");

   public MindActionRequest(String identifier, Map<String, ?> payload) {
      identifier = validateIdentifier(identifier);
      payload = normalizePayload(payload);
      this.identifier = identifier;
      this.payload = payload;
   }

   private static String validateIdentifier(String identifier) {
      Objects.requireNonNull(identifier, "identifier");
      if (identifier.length() <= 128 && IDENTIFIER.matcher(identifier).matches()) {
         return identifier;
      } else {
         throw new IllegalArgumentException("Mind action identifier must be a namespaced lower-case key of at most 128 characters");
      }
   }

   private static Map<String, Object> normalizePayload(Map<String, ?> payload) {
      Objects.requireNonNull(payload, "payload");
      if (payload.size() > 16) {
         throw new IllegalArgumentException("Mind action payload cannot exceed 16 entries");
      } else {
         Map<String, Object> normalized = new LinkedHashMap<>();
         int weight = 0;

         for (Entry<String, ?> entry : payload.entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "payload key");
            if (key.length() > 64 || !PAYLOAD_KEY.matcher(key).matches()) {
               throw new IllegalArgumentException("Mind action payload keys must be lower-case identifiers of at most 64 characters: " + key);
            }

            Object value = normalizeValue(key, entry.getValue());
            weight = Math.addExact(weight, key.length() + valueWeight(value));
            if (weight > 2048) {
               throw new IllegalArgumentException("Mind action payload exceeds the bounded weight of 2048");
            }

            normalized.put(key, value);
         }

         return Collections.unmodifiableMap(normalized);
      }
   }

   private static Object normalizeValue(String key, Object value) {
      Objects.requireNonNull(value, "payload value for " + key);
      if (value instanceof String string) {
         if (string.length() > 256) {
            throw new IllegalArgumentException("Mind action string payload cannot exceed 256 characters: " + key);
         } else {
            return string;
         }
      } else if (value instanceof Boolean || value instanceof UUID) {
         return value;
      } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
         return ((Number)value).longValue();
      } else if (value instanceof Float || value instanceof Double) {
         double number = ((Number)value).doubleValue();
         if (!Double.isFinite(number)) {
            throw new IllegalArgumentException("Mind action numeric payload must be finite: " + key);
         } else {
            return number;
         }
      } else if (value instanceof MindPosition position) {
         if (position.world().length() > 256) {
            throw new IllegalArgumentException("Mind action position world cannot exceed 256 characters: " + key);
         } else {
            return position;
         }
      } else {
         throw new IllegalArgumentException("Unsupported Mind action payload type for " + key + ": " + value.getClass().getName());
      }
   }

   private static int valueWeight(Object value) {
      if (value instanceof String string) {
         return string.length();
      } else if (value instanceof Boolean) {
         return 1;
      } else if (value instanceof Long || value instanceof Double) {
         return 8;
      } else if (value instanceof UUID) {
         return 16;
      } else {
         MindPosition position = (MindPosition)value;
         return 24 + position.world().length();
      }
   }
}
