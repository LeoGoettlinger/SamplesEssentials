package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v26_2;

import com.fastasyncworldedit.core.extent.processor.lighting.NullRelighter;
import com.fastasyncworldedit.core.extent.processor.lighting.RelightMode;
import com.fastasyncworldedit.core.extent.processor.lighting.Relighter;
import com.fastasyncworldedit.core.extent.processor.lighting.RelighterFactory;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.sk89q.worldedit.world.World;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;

public class PaperweightStarlightRelighterFactory implements RelighterFactory {
   @Nonnull
   @Override
   public Relighter createRelighter(RelightMode relightMode, World world, IQueueExtent<?> queue) {
      org.bukkit.World w = Bukkit.getWorld(world.getName());
      return (Relighter)(w == null ? NullRelighter.INSTANCE : new PaperweightStarlightRelighter(((CraftWorld)w).getHandle(), queue));
   }
}
