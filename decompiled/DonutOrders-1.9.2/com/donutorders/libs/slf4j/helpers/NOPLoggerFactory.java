package com.donutorders.libs.slf4j.helpers;

import com.donutorders.libs.slf4j.ILoggerFactory;
import com.donutorders.libs.slf4j.Logger;

public class NOPLoggerFactory implements ILoggerFactory {
   @Override
   public Logger getLogger(String name) {
      return NOPLogger.NOP_LOGGER;
   }
}
