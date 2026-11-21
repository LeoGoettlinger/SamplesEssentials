package cm.chunkManager.components;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.utils.Scheduler;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

public class ChunkVisualizer {
   private final ChunkManager plugin;
   private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
   private Scheduler.Handle timerHandle;
   private static final DustOptions RED = new DustOptions(Color.fromRGB(255, 60, 60), 1.2F);
   private static final DustOptions YELLOW = new DustOptions(Color.fromRGB(255, 200, 40), 1.2F);
   private static final DustOptions GREEN = new DustOptions(Color.fromRGB(80, 220, 80), 1.2F);
   private static final DustOptions BLUE = new DustOptions(Color.fromRGB(80, 130, 255), 1.0F);
   private static final DustOptions CYAN = new DustOptions(Color.fromRGB(0, 255, 255), 1.4F);
   private static final DustOptions CORNER = new DustOptions(Color.WHITE, 1.5F);

   public ChunkVisualizer(ChunkManager plugin) {
      this.plugin = plugin;
      this.start();
   }

   private void start() {
      this.timerHandle = Scheduler.runGlobalTimer(this.plugin, this::render, 8L, 8L);
   }

   public boolean toggle(Player player) {
      UUID id = player.getUniqueId();
      if (this.viewers.contains(id)) {
         this.viewers.remove(id);
         return false;
      } else {
         this.viewers.add(id);
         return true;
      }
   }

   public void remove(UUID id) {
      this.viewers.remove(id);
   }

   public boolean isViewing(Player player) {
      return this.viewers.contains(player.getUniqueId());
   }

   private void render() {
      for (UUID id : this.viewers) {
         Player player = this.plugin.getServer().getPlayer(id);
         if (player == null || !player.isOnline()) {
            this.viewers.remove(id);
         } else if (Scheduler.isFolia()) {
            Scheduler.runEntity(this.plugin, player, () -> this.drawBorders(player));
         } else {
            this.drawBorders(player);
         }
      }
   }

   private void drawBorders(Player player) {
      World world = player.getWorld();
      Location loc = player.getLocation();
      int pcx = loc.getBlockX() >> 4;
      int pcz = loc.getBlockZ() >> 4;
      double baseY = loc.getY();

      for (int dx = -2; dx <= 2; dx++) {
         for (int dz = -2; dz <= 2; dz++) {
            int cx = pcx + dx;
            int cz = pcz + dz;
            DustOptions color = this.getColor(world.getName(), cx, cz);
            this.drawChunkBorder(player, world, cx, cz, baseY, color);
         }
      }
   }

   private DustOptions getColor(String worldName, int cx, int cz) {
      ChunkFreezer freezer = this.plugin.getChunkFreezer();
      if (freezer != null && freezer.isFrozen(worldName, cx, cz)) {
         return CYAN;
      } else {
         double heat = this.plugin.getSmartUnloader().getChunkHeat(worldName, cx, cz);
         if (heat > 50.0) {
            return RED;
         } else if (heat > 10.0) {
            return YELLOW;
         } else {
            return heat > 1.0 ? GREEN : BLUE;
         }
      }
   }

   private void drawChunkBorder(Player player, World world, int cx, int cz, double baseY, DustOptions color) {
      int bx = cx << 4;
      int bz = cz << 4;
      Location pLoc = player.getLocation();

      for (int layer = -1; layer <= 1; layer++) {
         double y = baseY + layer * 1.5;

         for (int i = 0; i <= 16; i++) {
            this.spawn(player, pLoc, world, bx + i, y, bz, color);
            this.spawn(player, pLoc, world, bx + i, y, bz + 16, color);
            this.spawn(player, pLoc, world, bx, y, bz + i, color);
            this.spawn(player, pLoc, world, bx + 16, y, bz + i, color);
         }
      }

      for (double vy = baseY - 2.0; vy <= baseY + 3.0; vy += 0.5) {
         this.spawn(player, pLoc, world, bx, vy, bz, CORNER);
         this.spawn(player, pLoc, world, bx + 16, vy, bz, CORNER);
         this.spawn(player, pLoc, world, bx, vy, bz + 16, CORNER);
         this.spawn(player, pLoc, world, bx + 16, vy, bz + 16, CORNER);
      }
   }

   private void spawn(Player player, Location pLoc, World world, double x, double y, double z, DustOptions color) {
      double dx = x - pLoc.getX();
      double dy = y - pLoc.getY();
      double dz = z - pLoc.getZ();
      if (!(dx * dx + dy * dy + dz * dz > 3600.0)) {
         player.spawnParticle(Particle.DUST, new Location(world, x, y, z), 1, 0.0, 0.0, 0.0, 0.0, color);
      }
   }

   public void shutdown() {
      if (this.timerHandle != null) {
         this.timerHandle.cancel();
      }

      this.viewers.clear();
   }
}
