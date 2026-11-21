package com.mysql.cj.otel;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.telemetry.TelemetryHandler;
import com.mysql.cj.telemetry.TelemetrySpan;
import com.mysql.cj.telemetry.TelemetrySpanName;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

public class OpenTelemetryHandler implements TelemetryHandler {
   private static boolean otelApiAvaliable = true;
   private OpenTelemetry openTelemetry = null;
   private Tracer tracer = null;
   private WeakHashMap<TelemetrySpan, Span> spans = new WeakHashMap<>();
   private List<Span> linkTargets = new ArrayList<>();

   public static boolean isOpenTelemetryApiAvailable() {
      return otelApiAvaliable;
   }

   public OpenTelemetryHandler() {
      if (!isOpenTelemetryApiAvailable()) {
         throw ExceptionFactory.createException(Messages.getString("Connection.OtelApiNotFound"));
      } else {
         this.openTelemetry = GlobalOpenTelemetry.get();
         this.tracer = this.openTelemetry.getTracer("MySQL Connector/J", "9.1.0");
      }
   }

   @Override
   public TelemetrySpan startSpan(TelemetrySpanName spanName, Object... args) {
      SpanBuilder spanBuilder = this.tracer.spanBuilder(spanName.getName(args)).setSpanKind(SpanKind.CLIENT);
      this.linkTargets.stream().map(Span::getSpanContext).forEach(spanBuilder::addLink);
      Span otelSpan = spanBuilder.startSpan();
      TelemetrySpan span = new OpenTelemetrySpan(otelSpan);
      this.spans.put(span, otelSpan);
      return span;
   }

   @Override
   public void addLinkTarget(TelemetrySpan span) {
      Span otelSpan = this.spans.get(span);
      if (otelSpan != null) {
         this.linkTargets.add(otelSpan);
      }
   }

   @Override
   public void removeLinkTarget(TelemetrySpan span) {
      Span otelSpan = this.spans.get(span);
      if (otelSpan != null) {
         this.linkTargets.remove(otelSpan);
      }
   }

   @Override
   public void propagateContext(BiConsumer<String, String> traceparentConsumer) {
      this.openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), traceparentConsumer, BiConsumer::accept);
   }

   static {
      try {
         Class.forName("io.opentelemetry.api.GlobalOpenTelemetry");
      } catch (ClassNotFoundException var1) {
         otelApiAvaliable = false;
      }
   }
}
