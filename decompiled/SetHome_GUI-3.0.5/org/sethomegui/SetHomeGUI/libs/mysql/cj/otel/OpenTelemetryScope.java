package org.sethomegui.SetHomeGUI.libs.mysql.cj.otel;

import io.opentelemetry.context.Scope;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryScope;

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
