package com.mysql.cj.otel;

import com.mysql.cj.telemetry.TelemetryAttribute;
import com.mysql.cj.telemetry.TelemetryScope;
import com.mysql.cj.telemetry.TelemetrySpan;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

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
