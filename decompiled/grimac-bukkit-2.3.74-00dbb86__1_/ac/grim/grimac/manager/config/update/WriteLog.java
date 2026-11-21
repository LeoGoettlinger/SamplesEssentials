package ac.grim.grimac.manager.config.update;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class WriteLog {
   abstract void recordPut(@NotNull String var1, @NotNull Object var2);

   abstract void recordRemove(@NotNull String var1);

   @NotNull
   abstract Map<String, Object> finalState();

   @NotNull
   static WriteLog active() {
      return new WriteLog.ActiveLog();
   }

   @NotNull
   static WriteLog noop() {
      return WriteLog.NoopLog.INSTANCE;
   }

   @NotNull
   static WriteLog sibling(@NotNull String siblingName, @NotNull Map<String, List<WriteLog.Entry>> sharedQueue, @NotNull Logger logger) {
      return new WriteLog.SiblingLog(siblingName, sharedQueue, logger);
   }

   private static final class ActiveLog extends WriteLog {
      private final List<WriteLog.Entry> entries = new ArrayList<>();

      @Override
      void recordPut(@NotNull String path, @NotNull Object value) {
         this.entries.add(new WriteLog.Entry(WriteLog.Op.PUT, path, value));
      }

      @Override
      void recordRemove(@NotNull String path) {
         this.entries.add(new WriteLog.Entry(WriteLog.Op.REMOVE, path, null));
      }

      @NotNull
      @Override
      Map<String, Object> finalState() {
         Map<String, Object> out = new LinkedHashMap<>();

         for (WriteLog.Entry e : this.entries) {
            out.put(e.path, e.op == WriteLog.Op.PUT ? e.value : null);
         }

         return out;
      }
   }

   record Entry(WriteLog.Op op, String path, Object value) {
   }

   private static final class NoopLog extends WriteLog {
      static final WriteLog.NoopLog INSTANCE = new WriteLog.NoopLog();

      @Override
      void recordPut(@NotNull String path, @NotNull Object value) {
      }

      @Override
      void recordRemove(@NotNull String path) {
      }

      @NotNull
      @Override
      Map<String, Object> finalState() {
         return Map.of();
      }
   }

   static enum Op {
      PUT,
      REMOVE;
   }

   private static final class SiblingLog extends WriteLog {
      private final String siblingName;
      private final Map<String, List<WriteLog.Entry>> sharedQueue;
      private final Logger logger;

      SiblingLog(String siblingName, Map<String, List<WriteLog.Entry>> sharedQueue, Logger logger) {
         this.siblingName = siblingName;
         this.sharedQueue = sharedQueue;
         this.logger = logger;
      }

      @Override
      void recordPut(@NotNull String path, @NotNull Object value) {
         this.sharedQueue.computeIfAbsent(this.siblingName, k -> new ArrayList<>()).add(new WriteLog.Entry(WriteLog.Op.PUT, path, value));
      }

      @Override
      void recordRemove(@NotNull String path) {
         this.logger
            .log(
               Level.FINE,
               "[grim-config-updater] cross-file REMOVE op for '"
                  + path
                  + "' on "
                  + this.siblingName
                  + " is not yet supported; expected the bundled default to drop the key"
            );
      }

      @NotNull
      @Override
      Map<String, Object> finalState() {
         return Map.of();
      }
   }
}
