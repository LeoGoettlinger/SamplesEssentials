package com.donutorders.libs.slf4j.spi;

import com.donutorders.libs.slf4j.ILoggerFactory;

/** @deprecated */
public interface LoggerFactoryBinder {
   ILoggerFactory getLoggerFactory();

   String getLoggerFactoryClassStr();
}
