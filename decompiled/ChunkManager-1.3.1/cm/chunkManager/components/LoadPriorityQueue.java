package cm.chunkManager.components;

import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class LoadPriorityQueue {
   private final JavaPlugin plugin;
   private final PriorityBlockingQueue<LoadPriorityQueue.PrioritizedLoad> queue;

   public LoadPriorityQueue(JavaPlugin plugin) {
      this.plugin = plugin;
      this.queue = new PriorityBlockingQueue<>(64);
   }

   public void enqueue(String world, int cx, int cz, LoadPriorityQueue.LoadReason reason, double priority) {
      this.queue.offer(new LoadPriorityQueue.PrioritizedLoad(world, cx, cz, reason, priority));
   }

   public LoadPriorityQueue.PrioritizedLoad poll() {
      return this.queue.poll();
   }

   public boolean isEmpty() {
      return this.queue.isEmpty();
   }

   public int size() {
      return this.queue.size();
   }

   public void clear() {
      this.queue.clear();
   }

   public static double calculatePriority(Location playerLoc, int cx, int cz, LoadPriorityQueue.LoadReason reason) {
      double playerCX = playerLoc.getBlockX() >> 4;
      double playerCZ = playerLoc.getBlockZ() >> 4;
      double dist = Math.sqrt(Math.pow(cx - playerCX, 2.0) + Math.pow(cz - playerCZ, 2.0));
      double distancePriority = Math.max(0.0, 100.0 - dist * 2.0);
      return distancePriority + reason.boost * 10.0;
   }

   public static enum LoadReason {
      PLAYER_MOVEMENT(1.0),
      TELEPORT(3.0),
      PORTAL(2.5),
      PREFETCH(0.5),
      BACKGROUND(0.1);

      public final double boost;

      private LoadReason(double boost) {
         this.boost = boost;
      }
   }

   public static class PrioritizedLoad implements Comparable<LoadPriorityQueue.PrioritizedLoad> {
      public final String world;
      public final int chunkX;
      public final int chunkZ;
      public final double priority;
      public final long timestamp;
      public final LoadPriorityQueue.LoadReason reason;

      public PrioritizedLoad(String world, int chunkX, int chunkZ, LoadPriorityQueue.LoadReason reason, double priority) {
         this.world = world;
         this.chunkX = chunkX;
         this.chunkZ = chunkZ;
         this.reason = reason;
         this.priority = priority;
         this.timestamp = System.currentTimeMillis();
      }

      public int compareTo(LoadPriorityQueue.PrioritizedLoad other) {
         int cmp = Double.compare(other.priority, this.priority);
         return cmp != 0 ? cmp : Long.compare(this.timestamp, other.timestamp);
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof LoadPriorityQueue.PrioritizedLoad that)
               ? false
               : this.chunkX == that.chunkX && this.chunkZ == that.chunkZ && this.world.equals(that.world);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.world, this.chunkX, this.chunkZ);
      }
   }
}
