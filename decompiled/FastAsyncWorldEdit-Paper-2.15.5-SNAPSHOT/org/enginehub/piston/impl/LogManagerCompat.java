package org.enginehub.piston.impl;

import java.lang.StackWalker.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogManagerCompat {
   private static final StackWalker WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

   public static Logger getLogger() {
      return LogManager.getLogger(WALKER.getCallerClass());
   }

   private LogManagerCompat() {
   }
}
