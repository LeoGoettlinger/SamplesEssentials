package com.donutorders.libs.slf4j.spi;

import com.donutorders.libs.slf4j.event.LoggingEvent;

public interface LoggingEventAware {
   void log(LoggingEvent var1);
}
