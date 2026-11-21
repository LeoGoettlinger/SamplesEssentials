package com.fastasyncworldedit.core;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.processor.lighting.RelightMode;
import com.fastasyncworldedit.core.extent.processor.lighting.Relighter;
import com.fastasyncworldedit.core.history.DiskStorageHistory;
import com.fastasyncworldedit.core.history.changeset.SimpleChangeSetSummary;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.queue.implementation.ParallelQueueExtent;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.fastasyncworldedit.core.regions.RegionWrapper;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.MemUtil;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.WEManager;
import com.fastasyncworldedit.core.wrappers.WorldWrapper;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class FaweAPI {
   private static final Logger LOGGER = LogManagerCompat.getLogger();

   public static TaskManager getTaskManager() {
      return TaskManager.taskManager();
   }

   public static IQueueExtent<IQueueChunk> createQueue(World world, boolean autoQueue) {
      IQueueExtent<IQueueChunk> queue = Fawe.instance().getQueueHandler().getQueue(world);
      if (!autoQueue) {
         queue.disableQueue();
      }

      return queue;
   }

   public static World getWorld(String worldName) {
      Platform platform = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);

      for (World current : platform.getWorlds()) {
         if (current.getName().equals(worldName)) {
            return WorldWrapper.wrap(current);
         }
      }

      return null;
   }

   public static URL upload(final Clipboard clipboard, final ClipboardFormat format) {
      return format.upload(clipboard);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.11.1"
   )
   public static Clipboard load(File file) throws IOException {
      return ClipboardFormats.findByFile(file).load(file);
   }

   public static Set<FaweMaskManager> getMaskManagers() {
      return new HashSet<>(WEManager.weManager().getManagers());
   }

   public static boolean isMemoryLimited() {
      return MemUtil.isMemoryLimited();
   }

   public static Region[] getRegions(Player player) {
      return WEManager.weManager().getMask(player);
   }

   public static Region[] getRegions(Player player, FaweMaskManager.MaskType type, boolean isWhiteList) {
      return WEManager.weManager().getMask(player, type, isWhiteList);
   }

   public static void cancelEdit(AbstractDelegateExtent extent, Component reason) {
      try {
         WEManager.weManager().cancelEdit(extent, new FaweException(reason));
      } catch (WorldEditException var3) {
      }
   }

   public static void addMaskManager(FaweMaskManager maskMan) {
      WEManager.weManager().addManager(maskMan);
   }

   public static DiskStorageHistory getChangeSetFromFile(File file) {
      if (!file.exists() || file.isDirectory()) {
         throw new IllegalArgumentException("Not a file!");
      } else if (Settings.settings().HISTORY.USE_DISK) {
         throw new IllegalArgumentException("History on disk not enabled!");
      } else if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".bd")) {
         throw new IllegalArgumentException("Not a BD file!");
      } else {
         String[] path = file.getPath().split(File.separatorChar == '\\' ? "\\\\" : File.separator);
         if (path.length < 3) {
            throw new IllegalArgumentException("Not in history directory!");
         } else {
            String worldName = path[path.length - 3];
            String uuidString = path[path.length - 2];
            World world = getWorld(worldName);
            if (world == null) {
               throw new IllegalArgumentException("Corresponding world does not exist: " + worldName);
            } else {
               UUID uuid;
               try {
                  uuid = UUID.fromString(uuidString);
               } catch (IllegalArgumentException var7) {
                  throw new IllegalArgumentException("Invalid UUID from file path: " + uuidString);
               }

               return new DiskStorageHistory(world, uuid, Integer.parseInt(file.getName().split("\\.")[0]));
            }
         }
      }
   }

   public static List<DiskStorageHistory> getBDFiles(Location origin, UUID user, int radius, long timediff, boolean shallow) {
      Extent extent = origin.getExtent();
      if (!(extent instanceof World world)) {
         throw new IllegalArgumentException("Origin is not a valid world");
      } else {
         File history = MainUtil.getFile(Fawe.<IFawe>platform().getDirectory(), Settings.settings().PATHS.HISTORY + File.separator + world.getName());
         if (!history.exists()) {
            return new ArrayList<>();
         } else {
            long now = System.currentTimeMillis();
            ArrayList<File> files = new ArrayList<>();

            for (File userFile : history.listFiles()) {
               if (userFile.isDirectory()) {
                  UUID userUUID;
                  try {
                     userUUID = UUID.fromString(userFile.getName());
                  } catch (IllegalArgumentException var26) {
                     continue;
                  }

                  if (user == null || userUUID.equals(user)) {
                     new ArrayList();

                     for (File file : userFile.listFiles()) {
                        if (file.getName().endsWith(".bd") && (timediff >= 2147483647L || now - file.lastModified() <= timediff)) {
                           files.add(file);
                           if (files.size() > 2048) {
                              return null;
                           }
                        }
                     }
                  }
               }
            }

            files.sort((a, b) -> {
               String aName = a.getName();
               String bName = b.getName();
               int aI = Integer.parseInt(aName.substring(0, aName.length() - 3));
               int bI = Integer.parseInt(bName.substring(0, bName.length() - 3));
               long value = aI - bI;
               return value == 0L ? 0 : (value < 0L ? -1 : 1);
            });
            RegionWrapper bounds = new RegionWrapper(
               origin.getBlockX() - radius,
               origin.getBlockX() + radius,
               extent.getMinY(),
               extent.getMaxY(),
               origin.getBlockZ() - radius,
               origin.getBlockZ() + radius
            );
            RegionWrapper boundsPlus = new RegionWrapper(bounds.minX - 64, bounds.maxX + 512, bounds.minY, bounds.maxY, bounds.minZ - 64, bounds.maxZ + 512);
            HashSet<RegionWrapper> regionSet = Sets.newHashSet(new RegionWrapper[]{bounds});
            ArrayList<DiskStorageHistory> result = new ArrayList<>();

            for (File filex : files) {
               UUID uuid = UUID.fromString(filex.getParentFile().getName());
               DiskStorageHistory dsh = new DiskStorageHistory(world, uuid, Integer.parseInt(filex.getName().split("\\.")[0]));
               SimpleChangeSetSummary summary = dsh.summarize(boundsPlus, shallow);
               RegionWrapper region = new RegionWrapper(summary.minX, summary.maxX, extent.getMinY(), extent.getMaxY(), summary.minZ, summary.maxZ);
               boolean encompassed = false;
               boolean isIn = false;

               for (RegionWrapper allowed : regionSet) {
                  isIn = isIn || allowed.intersects(region);
                  if (encompassed = allowed.isIn(region.minX, region.maxX) && allowed.isIn(region.minZ, region.maxZ)) {
                     break;
                  }
               }

               if (isIn) {
                  result.add(0, dsh);
                  if (!encompassed) {
                     regionSet.add(region);
                  }

                  if (shallow && result.size() > 64) {
                     return result;
                  }
               }
            }

            return result;
         }
      }
   }

   public static DiskStorageHistory getChangeSetFromDisk(World world, UUID uuid, int index) {
      return new DiskStorageHistory(world, uuid, index);
   }

   public static int fixLighting(World world, Region selection, @Nullable IQueueExtent<IQueueChunk> queuex, final RelightMode mode) {
      BlockVector3 bot = selection.getMinimumPoint();
      BlockVector3 top = selection.getMaximumPoint();
      int minX = bot.x() >> 4;
      int minZ = bot.z() >> 4;
      int maxX = top.x() >> 4;
      int maxZ = top.z() >> 4;
      int count = 0;
      if (queuex == null && !(WorldWrapper.unwrap(world) instanceof IQueueExtent<IQueueChunk> queuex)) {
         if (Settings.settings().QUEUE.PARALLEL_THREADS > 1) {
            ParallelQueueExtent parallel = new ParallelQueueExtent(
               Fawe.instance().getQueueHandler(), world, true, SideEffectSet.none().with(SideEffect.LIGHTING)
            );
            queuex = parallel.getExtent();
         } else {
            queuex = Fawe.instance().getQueueHandler().getQueue(world);
         }
      }

      try (Relighter relighter = WorldEdit.getInstance()
            .getPlatformManager()
            .queryCapability(Capability.WORLD_EDITING)
            .getRelighterFactory()
            .createRelighter(mode, world, queuex)) {
         for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
               relighter.addChunk(x, z, null, 65535);
               count++;
            }
         }

         if (mode != RelightMode.NONE) {
            if (Settings.settings().LIGHTING.REMOVE_FIRST) {
               relighter.removeAndRelight(true);
            } else {
               relighter.fixSkyLighting();
               relighter.fixBlockLighting();
            }
         } else {
            relighter.removeLighting();
         }
      } catch (Exception var16) {
         LOGGER.error("Error occurred on fix lighting", var16);
      }

      return count;
   }

   public static void addMemoryLimitedTask(Runnable run) {
      MemUtil.addMemoryLimitedTask(run);
   }

   public static void addMemoryPlentifulTask(Runnable run) {
      MemUtil.addMemoryPlentifulTask(run);
   }

   public static Map<String, String> getTranslations(Locale locale) {
      return WorldEdit.getInstance().getTranslationManager().getTranslationMap(locale);
   }
}
