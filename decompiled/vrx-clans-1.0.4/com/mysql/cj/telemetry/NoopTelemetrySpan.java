package com.mysql.cj.telemetry;

public class NoopTelemetrySpan implements TelemetrySpan {
   private static NoopTelemetrySpan INSTANCE = new NoopTelemetrySpan();

   public static NoopTelemetrySpan getInstance() {
      return INSTANCE;
   }

   @Override
   public TelemetryScope makeCurrent() {
      return NoopTelemetryScope.getInstance();
   }

   private NoopTelemetrySpan() {
   }
}
