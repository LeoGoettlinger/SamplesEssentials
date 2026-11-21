package com.magmaguy.elitemobs.magmacore;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DrawLine {
   private static final ConcurrentLinkedQueue<DrawLine.PendingRemoval> pendingRemovals = new ConcurrentLinkedQueue<>();
   private static BukkitTask tickerTask = null;
   private static volatile boolean paused = false;

   private DrawLine() {
   }

   public static DrawLine.LineData drawLine(Location start, Location end, float width, Material material, int tickDuration) {
      float height = (float)start.distance(end);
      if (height <= 0.0F) {
         return null;
      } else {
         Vector dir = end.toVector().subtract(start.toVector()).normalize();
         Vector3f target = new Vector3f((float)dir.getX(), (float)dir.getY(), (float)dir.getZ());
         Quaternionf rotation = new Quaternionf().rotateTo(new Vector3f(0.0F, 1.0F, 0.0F), target);
         Vector mid = start.toVector().add(end.toVector()).multiply(0.5);
         Location spawnLoc = new Location(end.getWorld(), mid.getX(), mid.getY(), mid.getZ());
         Vector3f localCenterOffset = new Vector3f(-width / 2.0F, -height / 2.0F, -width / 2.0F);
         Vector3f worldCenterOffset = rotation.transform(new Vector3f(localCenterOffset));
         spawnLoc.add(worldCenterOffset.x, worldCenterOffset.y, worldCenterOffset.z);
         BlockDisplay display = (BlockDisplay)spawnLoc.getWorld().spawn(spawnLoc, BlockDisplay.class, entity -> {
            entity.setBlock(Bukkit.createBlockData(material));
            entity.setInterpolationDuration(0);
            entity.setViewRange(128.0F);
            entity.setBrightness(new Brightness(15, 15));
            entity.setPersistent(false);
            entity.setTransformation(new Transformation(new Vector3f(0.0F, 0.0F, 0.0F), rotation, new Vector3f(width, height, width), new Quaternionf()));
         });
         if (tickDuration > 0) {
            pendingRemovals.add(new DrawLine.PendingRemoval(display, tickDuration));
            ensureTickerRunning();
         }

         return new DrawLine.LineData(display, start, end, width);
      }
   }

   public static void pauseAll() {
      paused = true;
   }

   public static void resumeAll() {
      paused = false;
   }

   public static boolean isPaused() {
      return paused;
   }

   private static void ensureTickerRunning() {
      if (tickerTask == null) {
         tickerTask = (new BukkitRunnable() {
            public void run() {
               if (!DrawLine.paused) {
                  Iterator<DrawLine.PendingRemoval> it = DrawLine.pendingRemovals.iterator();

                  while (it.hasNext()) {
                     DrawLine.PendingRemoval pr = it.next();
                     if (pr.display != null && pr.display.isValid()) {
                        pr.remainingTicks--;
                        if (pr.remainingTicks <= 0) {
                           pr.display.remove();
                           it.remove();
                        }
                     } else {
                        it.remove();
                     }
                  }
               }
            }
         }).runTaskTimer(MagmaCore.getInstance().getRequestingPlugin(), 1L, 1L);
      }
   }

   public static void updateLine(DrawLine.LineData lineData, Location start, Location end) {
      if (lineData != null && lineData.getDisplay() != null && lineData.getDisplay().isValid()) {
         float height = (float)start.distance(end);
         if (!(height <= 0.0F)) {
            BlockDisplay display = lineData.getDisplay();
            float width = lineData.getWidth();
            Vector dir = end.toVector().subtract(start.toVector()).normalize();
            Vector3f target = new Vector3f((float)dir.getX(), (float)dir.getY(), (float)dir.getZ());
            Quaternionf rotation = new Quaternionf().rotateTo(new Vector3f(0.0F, 1.0F, 0.0F), target);
            Vector mid = start.toVector().add(end.toVector()).multiply(0.5);
            Location newLoc = new Location(end.getWorld(), mid.getX(), mid.getY(), mid.getZ());
            Vector3f localCenterOffset = new Vector3f(-width / 2.0F, -height / 2.0F, -width / 2.0F);
            Vector3f worldCenterOffset = rotation.transform(new Vector3f(localCenterOffset));
            newLoc.add(worldCenterOffset.x, worldCenterOffset.y, worldCenterOffset.z);
            display.teleport(newLoc);
            display.setTransformation(new Transformation(new Vector3f(0.0F, 0.0F, 0.0F), rotation, new Vector3f(width, height, width), new Quaternionf()));
            lineData.updatePositions(start, end);
         }
      }
   }

   public static class LineData {
      private final BlockDisplay display;
      private final float width;
      private Location start;
      private Location end;

      public LineData(BlockDisplay display, Location start, Location end, float width) {
         this.display = display;
         this.start = start.clone();
         this.end = end.clone();
         this.width = width;
      }

      public BlockDisplay getDisplay() {
         return this.display;
      }

      public Location getStart() {
         return this.start.clone();
      }

      public Location getEnd() {
         return this.end.clone();
      }

      public float getWidth() {
         return this.width;
      }

      public void updatePositions(Location newStart, Location newEnd) {
         this.start = newStart.clone();
         this.end = newEnd.clone();
      }

      public void remove() {
         if (this.display != null && this.display.isValid()) {
            this.display.remove();
         }
      }
   }

   private static final class PendingRemoval {
      final BlockDisplay display;
      int remainingTicks;

      PendingRemoval(BlockDisplay display, int remainingTicks) {
         this.display = display;
         this.remainingTicks = remainingTicks;
      }
   }
}
