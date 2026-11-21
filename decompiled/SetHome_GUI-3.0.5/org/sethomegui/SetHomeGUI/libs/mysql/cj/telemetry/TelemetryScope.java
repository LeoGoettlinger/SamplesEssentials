package org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry;

public interface TelemetryScope extends AutoCloseable {
   @Override
   default void close() {
   }
}
