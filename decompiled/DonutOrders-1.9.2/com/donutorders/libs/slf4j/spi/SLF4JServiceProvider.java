package com.donutorders.libs.slf4j.spi;

import com.donutorders.libs.slf4j.ILoggerFactory;
import com.donutorders.libs.slf4j.IMarkerFactory;

public interface SLF4JServiceProvider {
   ILoggerFactory getLoggerFactory();

   IMarkerFactory getMarkerFactory();

   MDCAdapter getMDCAdapter();

   String getRequestedApiVersion();

   void initialize();
}
