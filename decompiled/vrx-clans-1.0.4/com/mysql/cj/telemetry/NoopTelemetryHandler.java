package com.mysql.cj.telemetry;

public class NoopTelemetryHandler implements TelemetryHandler {
   private static NoopTelemetryHandler INSTANCE = new NoopTelemetryHandler();

   public static NoopTelemetryHandler getInstance() {
      return INSTANCE;
   }

   private NoopTelemetryHandler() {
   }

   @Override
   public TelemetrySpan startSpan(TelemetrySpanName spanName, Object... args) {
      return NoopTelemetrySpan.getInstance();
   }
}
