package com.sk89q.worldedit.util.formatting.text.util;

import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

@Deprecated
public interface ToStringer {
   @Deprecated
   @NonNull
   static String toString(@NonNull final Object object, @NonNull final Map<String, Object> builder) {
      return ShadyPines.toString(object, builder);
   }
}
