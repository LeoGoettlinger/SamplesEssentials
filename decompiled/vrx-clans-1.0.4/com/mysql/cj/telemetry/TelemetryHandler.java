package com.mysql.cj.telemetry;

import java.util.function.BiConsumer;

public interface TelemetryHandler {
   TelemetrySpan startSpan(TelemetrySpanName var1, Object... var2);

   default void addLinkTarget(TelemetrySpan span) {
   }

   default void removeLinkTarget(TelemetrySpan span) {
   }

   default void propagateContext(BiConsumer<String, String> traceparentConsumer) {
   }

   default String getContextPropagationKey() {
      return "traceparent";
   }
}
