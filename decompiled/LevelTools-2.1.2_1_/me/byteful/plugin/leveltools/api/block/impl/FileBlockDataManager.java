package me.byteful.plugin.leveltools.api.block.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.scheduler.ScheduledTask;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import me.byteful.plugin.leveltools.util.Text;

public class FileBlockDataManager implements BlockDataManager {
   private static final int MAX_CACHE_SIZE = 1000000;
   private final Set<BlockPosition> cache = ConcurrentHashMap.newKeySet();
   private final Path file;
   private final ScheduledTask saveTask;

   public FileBlockDataManager(Path file, Scheduler scheduler) {
      this.file = file;
      this.saveTask = scheduler.asyncTimer(this::save, 100L, 100L);
   }

   private void save() {
      Set<String> lines;
      synchronized (this.cache) {
         lines = this.cache.stream().map(x -> String.format("{%s}{%s}{%s}%s", x.getX(), x.getY(), x.getZ(), x.getWorld())).collect(Collectors.toSet());
      }

      try {
         Files.write(this.file, lines, StandardCharsets.UTF_8);
      } catch (IOException var4) {
         var4.printStackTrace();
      }
   }

   @Override
   public boolean isPlacedBlock(BlockPosition pos) {
      return this.cache.contains(pos);
   }

   @Override
   public void addPlacedBlock(BlockPosition pos) {
      if (this.cache.size() < 1000000) {
         this.cache.add(pos);
      }
   }

   @Override
   public void removePlacedBlock(BlockPosition pos) {
      this.cache.remove(pos);
   }

   @Override
   public void load() {
      try {
         for (String line : Files.readAllLines(this.file, StandardCharsets.UTF_8)) {
            String[] data = Text.substringsBetween(line, "{", "}");
            if (data != null && data.length == 3) {
               try {
                  int x = Integer.parseInt(data[0]);
                  int y = Integer.parseInt(data[1]);
                  int z = Integer.parseInt(data[2]);
                  String world = line.substring(String.format("{%s}{%s}{%s}", x, y, z).length());
                  this.cache.add(new BlockPosition(world, x, y, z));
               } catch (Exception var8) {
               }
            }
         }
      } catch (IOException var9) {
         throw new RuntimeException(var9);
      }
   }

   @Override
   public void close() {
      this.saveTask.stop();
      this.save();
      this.cache.clear();
   }
}
