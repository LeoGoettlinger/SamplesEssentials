package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public record MindPersistentState(int formatVersion, Map<String, Object> memories, Map<String, Long> remainingTtlTicks) {
   public static final int CURRENT_FORMAT = 1;

   public MindPersistentState(int formatVersion, Map<String, Object> memories, Map<String, Long> remainingTtlTicks) {
      if (formatVersion < 1) {
         throw new IllegalArgumentException("formatVersion must be positive");
      } else {
         memories = Collections.unmodifiableMap(new LinkedHashMap<>(memories));
         remainingTtlTicks = Collections.unmodifiableMap(new LinkedHashMap<>(remainingTtlTicks));

         for (Entry<String, Long> entry : remainingTtlTicks.entrySet()) {
            if (!memories.containsKey(entry.getKey()) || entry.getValue() <= 0L) {
               throw new IllegalArgumentException("TTL entries require a matching memory and positive duration");
            }
         }

         this.formatVersion = formatVersion;
         this.memories = memories;
         this.remainingTtlTicks = remainingTtlTicks;
      }
   }

   public MindPersistentState(int formatVersion, Map<String, Object> memories) {
      this(formatVersion, memories, Map.of());
   }

   public static MindPersistentState empty() {
      return new MindPersistentState(1, Map.of(), Map.of());
   }
}
