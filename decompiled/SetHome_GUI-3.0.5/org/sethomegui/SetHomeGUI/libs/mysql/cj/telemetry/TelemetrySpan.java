package org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry;

public interface TelemetrySpan extends AutoCloseable {
   TelemetryScope makeCurrent();

   default void setAttribute(TelemetryAttribute key, String value) {
   }

   default void setAttribute(TelemetryAttribute key, long value) {
   }

   default void setError(Throwable cause) {
   }

   default void end() {
   }

   @Override
   default void close() {
   }
}
