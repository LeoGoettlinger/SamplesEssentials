package com.artillexstudios.axafkzone.libs.axapi.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class Maps {
   public static <T, Z> Map<T, Z> orderByValue(Map<T, Z> map) {
      return map.entrySet()
         .stream()
         .sorted(UncheckedUtils.unsafeCast(Entry.comparingByValue()))
         .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
   }

   public static <T, Z> Map<T, Z> order(Map<T, Z> map) {
      return map.entrySet()
         .stream()
         .sorted(UncheckedUtils.unsafeCast(Entry.comparingByKey()))
         .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
   }
}
