package de.oliver.fancyanalytics.sdk.records;

import java.util.Map;

public record Record(String senderId, String projectId, long timestamp, Map<String, Object> data) {
   public Record withEntry(String key, Object value) {
      this.data.put(key, value);
      return this;
   }
}
