package me.chancesd.pvpmanager.library.rollbar;

import java.lang.Thread.UncaughtExceptionHandler;
import me.chancesd.pvpmanager.sdutils.utils.Log;

public class PMRUncaughExceptionHandler implements UncaughtExceptionHandler {
   @Override
   public void uncaughtException(Thread t, Throwable e) {
      Log.severe(e.getMessage(), e);
   }
}
