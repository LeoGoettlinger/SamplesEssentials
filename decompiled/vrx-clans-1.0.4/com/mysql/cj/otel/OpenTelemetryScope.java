package com.mysql.cj.otel;

import com.mysql.cj.telemetry.TelemetryScope;
import io.opentelemetry.context.Scope;

public class OpenTelemetryScope implements TelemetryScope {
   private Scope scope = null;

   public OpenTelemetryScope(Scope scope) {
      this.scope = scope;
   }

   @Override
   public void close() {
      this.scope.close();
   }
}
