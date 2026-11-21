package com.fastasyncworldedit.core.extent.processor.lighting;

import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.sk89q.worldedit.world.World;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface RelighterFactory {
   @Nonnull
   Relighter createRelighter(RelightMode relightMode, World world, IQueueExtent<?> queue);
}
