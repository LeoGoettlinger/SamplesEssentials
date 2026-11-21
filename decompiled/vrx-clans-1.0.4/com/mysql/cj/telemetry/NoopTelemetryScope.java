package com.mysql.cj.telemetry;

public class NoopTelemetryScope implements TelemetryScope {
   private static NoopTelemetryScope INSTANCE = new NoopTelemetryScope();

   public static NoopTelemetryScope getInstance() {
      return INSTANCE;
   }

   private NoopTelemetryScope() {
   }
}
