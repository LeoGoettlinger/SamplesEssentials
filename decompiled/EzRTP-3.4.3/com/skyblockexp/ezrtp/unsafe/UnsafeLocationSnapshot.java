package com.skyblockexp.ezrtp.unsafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class UnsafeLocationSnapshot {
   private static final double RECOMMENDATION_THRESHOLD = 0.3;
   private final String timestamp;
   private final int periodMinutes;
   private final long totalAttempts;
   private final long unsafeAttempts;
   private final double unsafeRate;
   private final Map<UnsafeLocationCause, Long> causeCounts;
   private final List<String> recommendations;

   public UnsafeLocationSnapshot(
      String timestamp, int periodMinutes, long totalAttempts, long unsafeAttempts, double unsafeRate, Map<UnsafeLocationCause, Long> causeCounts
   ) {
      this.timestamp = timestamp;
      this.periodMinutes = periodMinutes;
      this.totalAttempts = totalAttempts;
      this.unsafeAttempts = unsafeAttempts;
      this.unsafeRate = unsafeRate;
      this.causeCounts = Collections.unmodifiableMap(new EnumMap<>(causeCounts));
      this.recommendations = Collections.unmodifiableList(computeRecommendations(unsafeAttempts, causeCounts));
   }

   public String getTimestamp() {
      return this.timestamp;
   }

   public int getPeriodMinutes() {
      return this.periodMinutes;
   }

   public long getTotalAttempts() {
      return this.totalAttempts;
   }

   public long getUnsafeAttempts() {
      return this.unsafeAttempts;
   }

   public double getUnsafeRate() {
      return this.unsafeRate;
   }

   public Map<UnsafeLocationCause, Long> getCauseCounts() {
      return this.causeCounts;
   }

   public List<String> getRecommendations() {
      return this.recommendations;
   }

   public String toJson() {
      StringBuilder sb = new StringBuilder();
      sb.append("{\n");
      sb.append("  \"timestamp\": \"").append(this.timestamp).append("\",\n");
      sb.append("  \"period_minutes\": ").append(this.periodMinutes).append(",\n");
      sb.append("  \"rtp_operations\": {\n");
      sb.append("    \"total_attempts\": ").append(this.totalAttempts).append(",\n");
      sb.append("    \"unsafe_attempts\": ").append(this.unsafeAttempts).append(",\n");
      sb.append(String.format("    \"unsafe_rate\": %.3f%n", this.unsafeRate));
      sb.append("  },\n");
      sb.append("  \"unsafe_causes\": {\n");
      UnsafeLocationCause[] causes = UnsafeLocationCause.values();

      for (int i = 0; i < causes.length; i++) {
         long count = this.causeCounts.getOrDefault(causes[i], 0L);
         sb.append("    \"").append(causes[i].name().toLowerCase()).append("\": ").append(count);
         if (i < causes.length - 1) {
            sb.append(',');
         }

         sb.append('\n');
      }

      sb.append("  },\n");
      sb.append("  \"recommendations\": [\n");

      for (int i = 0; i < this.recommendations.size(); i++) {
         sb.append("    \"").append(escapeJson(this.recommendations.get(i))).append("\"");
         if (i < this.recommendations.size() - 1) {
            sb.append(',');
         }

         sb.append('\n');
      }

      sb.append("  ]\n");
      sb.append("}");
      return sb.toString();
   }

   private static List<String> computeRecommendations(long totalUnsafe, Map<UnsafeLocationCause, Long> counts) {
      if (totalUnsafe == 0L) {
         return List.of();
      } else {
         List<String> recs = new ArrayList<>();

         for (Entry<UnsafeLocationCause, Long> entry : counts.entrySet()) {
            if (entry.getValue() != 0L) {
               double share = (double)entry.getValue().longValue() / totalUnsafe;
               if (!(share < 0.3)) {
                  switch ((UnsafeLocationCause)entry.getKey()) {
                     case VOID:
                        recs.add("High void rejections: consider raising min Y limit.");
                        break;
                     case LAVA:
                        recs.add("High lava rejections: consider adjusting Y range or expanding radius.");
                        break;
                     case LIQUID:
                     case LIQUID_SURFACE:
                        recs.add("High liquid rejections: consider restricting radius away from ocean biomes.");
                        break;
                     case UNSAFE_BLOCK:
                        recs.add("Unsafe block rejections high: review unsafe-blocks list in rtp.yml.");
                        break;
                     case OUT_OF_BOUNDS:
                        recs.add("Many out-of-bounds rejections: consider expanding teleport radius or adjusting Y limits.");
                        break;
                     case NULL_CANDIDATE:
                        recs.add("Frequent null candidate rejections: check world height and radius configuration.");
                  }
               }
            }
         }

         return recs;
      }
   }

   private static String escapeJson(String value) {
      return value.replace("\\", "\\\\").replace("\"", "\\\"");
   }
}
