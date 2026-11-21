package ch.njol.skript.test.utils;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class TestResults {
   private final Set<String> succeeded;
   private final Map<String, String> failed;
   private final boolean docsFailed;

   public TestResults(Set<String> succeeded, Map<String, String> failed, boolean docs_failed) {
      this.docsFailed = docs_failed;
      this.succeeded = succeeded;
      this.failed = failed;
   }

   public Set<String> getSucceeded() {
      return this.succeeded;
   }

   public Map<String, String> getFailed() {
      return this.failed;
   }

   public boolean docsFailed() {
      return this.docsFailed;
   }

   public String createReport() {
      StringBuilder sb = new StringBuilder("Succeeded:\n");
      if (this.succeeded.isEmpty()) {
         sb.append("<reset> - none\n");
      }

      for (String test : this.succeeded) {
         sb.append("<reset> - <light green>").append(test).append('\n');
      }

      sb.append("<reset>Failed:\n");
      if (this.failed.isEmpty()) {
         sb.append("<reset> - none");
      }

      for (Entry<String, String> entry : this.failed.entrySet()) {
         sb.append("<reset> - <light red>").append(entry.getKey()).append("<reset>: <gray>").append(entry.getValue()).append('\n');
      }

      return sb.toString();
   }
}
