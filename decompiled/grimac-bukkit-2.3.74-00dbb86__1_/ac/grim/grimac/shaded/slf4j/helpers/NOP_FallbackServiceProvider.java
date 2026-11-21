package ac.grim.grimac.shaded.slf4j.helpers;

import ac.grim.grimac.shaded.slf4j.ILoggerFactory;
import ac.grim.grimac.shaded.slf4j.IMarkerFactory;
import ac.grim.grimac.shaded.slf4j.spi.MDCAdapter;
import ac.grim.grimac.shaded.slf4j.spi.SLF4JServiceProvider;

public class NOP_FallbackServiceProvider implements SLF4JServiceProvider {
   public static String REQUESTED_API_VERSION = "2.0.99";
   private final ILoggerFactory loggerFactory = new NOPLoggerFactory();
   private final IMarkerFactory markerFactory = new BasicMarkerFactory();
   private final MDCAdapter mdcAdapter = new NOPMDCAdapter();

   @Override
   public ILoggerFactory getLoggerFactory() {
      return this.loggerFactory;
   }

   @Override
   public IMarkerFactory getMarkerFactory() {
      return this.markerFactory;
   }

   @Override
   public MDCAdapter getMDCAdapter() {
      return this.mdcAdapter;
   }

   @Override
   public String getRequestedApiVersion() {
      return REQUESTED_API_VERSION;
   }

   @Override
   public void initialize() {
   }
}
