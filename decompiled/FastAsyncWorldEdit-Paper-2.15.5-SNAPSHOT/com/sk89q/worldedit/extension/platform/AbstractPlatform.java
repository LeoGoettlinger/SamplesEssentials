package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.util.io.WorldEditResourceLoader;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.World;
import java.util.Collections;
import java.util.List;

public abstract class AbstractPlatform implements Platform {
   private final ResourceLoader resourceLoader = new WorldEditResourceLoader(WorldEdit.getInstance());

   @Override
   public ResourceLoader getResourceLoader() {
      return this.resourceLoader;
   }

   @Override
   public int schedule(long delay, long period, Runnable task) {
      return -1;
   }

   @Override
   public List<? extends World> getWorlds() {
      return Collections.emptyList();
   }

   @Override
   public DataFixer getDataFixer() {
      return null;
   }

   @Override
   public long getTickCount() {
      return System.nanoTime() / 50000000L;
   }
}
