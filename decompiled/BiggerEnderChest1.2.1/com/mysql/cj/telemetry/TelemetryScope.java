package com.mysql.cj.telemetry;

public interface TelemetryScope extends AutoCloseable {
   @Override
   default void close() {
   }
}
