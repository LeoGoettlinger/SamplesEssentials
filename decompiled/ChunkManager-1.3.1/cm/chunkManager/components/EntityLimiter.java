package cm.chunkManager.components;

import cm.chunkManager.utils.Scheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityLimiter {
   private final JavaPlugin plugin;
   private final int totalLimit;
   private final int hostileLimit;
   private final int passiveLimit;
   private final int itemLimit;
   private final Map<Long, Long> cooldownMap;
   private volatile boolean enabled;
   private final AtomicLong totalCulled = new AtomicLong(0L);
   private final AtomicLong chunksChecked = new AtomicLong(0L);
   private static final long COOLDOWN_MS = 5000L;

   public EntityLimiter(JavaPlugin plugin, int totalLimit, int hostileLimit, int passiveLimit, int itemLimit) {
      this.plugin = plugin;
      this.totalLimit = totalLimit;
      this.hostileLimit = hostileLimit;
      this.passiveLimit = passiveLimit;
      this.itemLimit = itemLimit;
      this.cooldownMap = new ConcurrentHashMap<>();
      this.enabled = true;
   }

   public int enforceLimit(Chunk chunk) {
      if (!this.enabled) {
         return 0;
      } else {
         long key = this.chunkKey(chunk);
         long now = System.currentTimeMillis();
         Long lastRun = this.cooldownMap.get(key);
         if (lastRun != null && now - lastRun < 5000L) {
            return 0;
         } else {
            this.cooldownMap.put(key, now);
            this.chunksChecked.incrementAndGet();
            if (Scheduler.isFolia()) {
               AtomicInteger result = new AtomicInteger(0);
               Location loc = new Location(chunk.getWorld(), chunk.getX() << 4, 64.0, chunk.getZ() << 4);
               Scheduler.runAt(this.plugin, loc, () -> {
                  try {
                     result.set(this.cullEntities(chunk));
                  } catch (Exception var4x) {
                  }
               });
               return result.get();
            } else {
               return this.cullEntities(chunk);
            }
         }
      }
   }

   private int cullEntities(Chunk chunk) {
      Entity[] entities;
      try {
         entities = chunk.getEntities();
      } catch (Exception var12) {
         return 0;
      }

      List<Item> items = new ArrayList<>();
      List<Entity> passives = new ArrayList<>();
      List<Entity> hostiles = new ArrayList<>();
      int total = 0;

      for (Entity entity : entities) {
         if (!this.isProtected(entity)) {
            total++;
            if (entity instanceof Item item) {
               items.add(item);
            } else if (entity instanceof Monster) {
               hostiles.add(entity);
            } else if (entity instanceof Animals || entity instanceof Ambient) {
               passives.add(entity);
            }
         }
      }

      int culled = 0;
      culled += this.cullList(items, items.size() - this.itemLimit);
      culled += this.cullList(passives, passives.size() - this.passiveLimit);
      culled += this.cullList(hostiles, hostiles.size() - this.hostileLimit);
      int remaining = total - culled;
      if (remaining > this.totalLimit) {
         int excess = remaining - this.totalLimit;
         List<Entity> all = new ArrayList<>();
         all.addAll(items.stream().filter(e -> !e.isDead()).toList());
         all.addAll(passives.stream().filter(e -> !e.isDead()).toList());
         all.addAll(hostiles.stream().filter(e -> !e.isDead()).toList());
         culled += this.cullList(all, excess);
      }

      this.totalCulled.addAndGet(culled);
      return culled;
   }

   private int cullList(List<? extends Entity> entities, int count) {
      if (count <= 0) {
         return 0;
      } else {
         int removed = 0;

         for (int i = entities.size() - 1; i >= 0 && removed < count; i--) {
            Entity entity = entities.get(i);
            if (!entity.isDead() && !this.isProtected(entity)) {
               entity.remove();
               removed++;
            }
         }

         return removed;
      }
   }

   private boolean isProtected(Entity entity) {
      if (entity instanceof Player) {
         return true;
      } else if (entity.getCustomName() != null) {
         return true;
      } else {
         if (entity instanceof ArmorStand stand) {
            EntityEquipment eq = stand.getEquipment();
            if (eq != null) {
               for (ItemStack item : eq.getArmorContents()) {
                  if (item != null && !item.getType().isAir()) {
                     return true;
                  }
               }

               if (eq.getItemInMainHand().getType() != Material.AIR) {
                  return true;
               }

               if (eq.getItemInOffHand().getType() != Material.AIR) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private long chunkKey(Chunk chunk) {
      return (long)chunk.getX() << 32 | chunk.getZ() & 4294967295L;
   }

   public Map<String, Object> getStats() {
      Map<String, Object> stats = new HashMap<>();
      stats.put("totalCulled", this.totalCulled.get());
      stats.put("chunksChecked", this.chunksChecked.get());
      stats.put("enabled", this.enabled);
      stats.put("cooldownEntries", this.cooldownMap.size());
      return stats;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }
}
