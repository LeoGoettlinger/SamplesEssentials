package org.sethomegui.SetHomeGUI.libs.mysql.cj.otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryAttribute;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryScope;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpan;

public class OpenTelemetrySpan implements TelemetrySpan {
   private Span span = null;
   private OpenTelemetryScope scope = null;

   public OpenTelemetrySpan(Span span) {
      this.span = span;
   }

   @Override
   public TelemetryScope makeCurrent() {
      this.scope = new OpenTelemetryScope(this.span.makeCurrent());
      return this.scope;
   }

   @Override
   public void setAttribute(TelemetryAttribute key, String value) {
      this.span.setAttribute(key.getKey(), value);
   }

   @Override
   public void setAttribute(TelemetryAttribute key, long value) {
      this.span.setAttribute(key.getKey(), value);
   }

   @Override
   public void setError(Throwable cause) {
      this.span.setStatus(StatusCode.ERROR, cause.getMessage()).recordException(cause);
   }

   @Override
   public void end() {
      this.span.end();
   }

   @Override
   public void close() {
      if (this.scope != null) {
         this.scope.close();
         this.scope = null;
      }

      this.end();
   }
}
