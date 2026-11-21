package com.sk89q.worldedit.internal.util;

import com.google.common.base.Throwables;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogManagerCompat {
   public static Logger getLogger() {
      return LogManager.getLogger(getCallerCallerClassName());
   }

   private static String getCallerCallerClassName() {
      List<StackTraceElement> lazyStack = Throwables.lazyStackTrace(new Throwable());
      return lazyStack.get(2).getClassName();
   }

   private LogManagerCompat() {
   }
}
