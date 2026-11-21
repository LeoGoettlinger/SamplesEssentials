package com.earth2me.essentials.libs.configurate.util;

import java.util.Map;

@FunctionalInterface
public interface MapFactory {
   <K, V> Map<K, V> create();
}
