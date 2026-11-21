package com.donutorders.libs.slf4j.spi;

import com.donutorders.libs.slf4j.IMarkerFactory;

/** @deprecated */
public interface MarkerFactoryBinder {
   IMarkerFactory getMarkerFactory();

   String getMarkerFactoryClassStr();
}
