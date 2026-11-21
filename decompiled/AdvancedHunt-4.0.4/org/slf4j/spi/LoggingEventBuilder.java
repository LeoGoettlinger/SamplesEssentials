package org.slf4j.spi;

import java.util.function.Supplier;
import org.slf4j.Marker;

public interface LoggingEventBuilder {
   LoggingEventBuilder setCause(Throwable var1);

   LoggingEventBuilder addMarker(Marker var1);

   LoggingEventBuilder addArgument(Object var1);

   LoggingEventBuilder addArgument(Supplier<Object> var1);

   LoggingEventBuilder addKeyValue(String var1, Object var2);

   LoggingEventBuilder addKeyValue(String var1, Supplier<Object> var2);

   void log(String var1);

   void log(String var1, Object var2);

   void log(String var1, Object var2, Object var3);

   void log(String var1, Object... var2);

   void log(Supplier<String> var1);
}
