package com.fastasyncworldedit.core.util.task;

import com.sk89q.worldedit.extent.Extent;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class FaweThreadUtil {
   private static final ThreadLocal<Extent> EXTENTS = new ThreadLocal<>();

   private FaweThreadUtil() {
   }

   public static void clearCurrentExtent() {
      if (Thread.currentThread() instanceof FaweThread ft) {
         ft.clearCurrentExtent();
      } else {
         EXTENTS.remove();
      }
   }

   public static void setCurrentExtent(Extent extent) {
      if (Thread.currentThread() instanceof FaweForkJoinThread ft) {
         ft.setCurrentExtent(extent);
      } else {
         EXTENTS.set(extent);
      }
   }

   public static Extent getCurrentExtent() {
      return Thread.currentThread() instanceof FaweForkJoinThread ft ? ft.getCurrentExtent() : EXTENTS.get();
   }
}
