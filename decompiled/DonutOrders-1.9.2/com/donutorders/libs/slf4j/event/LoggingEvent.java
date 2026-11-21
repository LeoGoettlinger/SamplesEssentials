package com.donutorders.libs.slf4j.event;

import com.donutorders.libs.slf4j.Marker;
import java.util.List;

public interface LoggingEvent {
   Level getLevel();

   String getLoggerName();

   String getMessage();

   List<Object> getArguments();

   Object[] getArgumentArray();

   List<Marker> getMarkers();

   List<KeyValuePair> getKeyValuePairs();

   Throwable getThrowable();

   long getTimeStamp();

   String getThreadName();

   default String getCallerBoundary() {
      return null;
   }
}
