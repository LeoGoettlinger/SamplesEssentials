package com.sk89q.worldedit.util.report;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class DataReport implements Report {
   private final String title;
   private final List<DataReport.Line> lines = Lists.newArrayList();

   public DataReport(String title) {
      Preconditions.checkNotNull(title, "title");
      this.title = title;
   }

   public void append(String key, String message) {
      Preconditions.checkNotNull(key, "key");
      this.lines.add(new DataReport.Line(key, message));
   }

   public void append(String key, String message, Object... values) {
      Preconditions.checkNotNull(message, "values");
      Preconditions.checkNotNull(values, "values");
      this.append(key, String.format(message, values));
   }

   public void append(String key, byte value) {
      this.append(key, String.valueOf((int)value));
   }

   public void append(String key, short value) {
      this.append(key, String.valueOf((int)value));
   }

   public void append(String key, int value) {
      this.append(key, String.valueOf(value));
   }

   public void append(String key, long value) {
      this.append(key, String.valueOf(value));
   }

   public void append(String key, float value) {
      this.append(key, String.valueOf(value));
   }

   public void append(String key, double value) {
      this.append(key, String.valueOf(value));
   }

   public void append(String key, boolean value) {
      this.append(key, String.valueOf(value));
   }

   public void append(String key, char value) {
      this.append(key, String.valueOf(value));
   }

   public void append(String key, Object value) {
      this.append(key, getStringValue(value, Sets.newHashSet()));
   }

   private static String getStringValue(Object value, Set<Object> seen) {
      if (seen.contains(value)) {
         return "<Recursive>";
      } else {
         seen.add(value);
         if (value instanceof Object[]) {
            value = Arrays.asList(value);
         }

         if (value instanceof Collection) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;

            for (Object entry : (Collection)value) {
               if (first) {
                  first = false;
               } else {
                  builder.append("\n");
               }

               builder.append(getStringValue(entry, Sets.newHashSet(seen)));
            }

            return builder.toString();
         } else if (value instanceof Map) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;

            for (Entry<?, ?> entry : ((Map)value).entrySet()) {
               if (first) {
                  first = false;
               } else {
                  builder.append("\n");
               }

               String key = getStringValue(entry.getKey(), Sets.newHashSet(seen)).replaceAll("[\r\n]", "");
               if (key.length() > 60) {
                  key = key.substring(0, 60) + "...";
               }

               builder.append(key).append(": ").append(getStringValue(entry.getValue(), Sets.newHashSet(seen)));
            }

            return builder.toString();
         } else {
            return String.valueOf(value);
         }
      }
   }

   @Override
   public String getTitle() {
      return this.title;
   }

   @Override
   public String toString() {
      if (!this.lines.isEmpty()) {
         StringBuilder builder = new StringBuilder();
         boolean first = true;

         for (DataReport.Line line : this.lines) {
            if (first) {
               first = false;
            } else {
               builder.append("\n");
            }

            builder.append(line.key).append(": ");
            if (line.value == null) {
               builder.append("null");
            } else if (line.value.contains("\n")) {
               builder.append("\n");
               builder.append(line.value.replaceAll("(?m)^", "\t"));
            } else {
               builder.append(line.value);
            }
         }

         return builder.toString();
      } else {
         return "No data.";
      }
   }

   private record Line(String key, String value) {
   }
}
