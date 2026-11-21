package org.slf4j.helpers;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class NOPServiceProvider implements SLF4JServiceProvider {
   public static String REQUESTED_API_VERSION = "1.8.99";
   private ILoggerFactory loggerFactory = new NOPLoggerFactory();
   private IMarkerFactory markerFactory = new BasicMarkerFactory();
   private MDCAdapter mdcAdapter = new NOPMDCAdapter();

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
   public String getRequesteApiVersion() {
      return REQUESTED_API_VERSION;
   }

   @Override
   public void initialize() {
   }
}
