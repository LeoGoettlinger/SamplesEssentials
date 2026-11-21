package com.fastasyncworldedit.core.util.task;

import com.sk89q.worldedit.extent.Extent;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public sealed interface FaweThread permits FaweBasicThread, FaweForkJoinThread {
   void clearCurrentExtent();

   void setCurrentExtent(Extent extent);

   Extent getCurrentExtent();
}
