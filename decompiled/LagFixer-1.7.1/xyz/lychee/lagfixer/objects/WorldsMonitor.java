package xyz.lychee.lagfixer.objects;

import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import xyz.lychee.lagfixer.managers.SupportManager;

public class WorldsMonitor extends AbstractMonitor {
   private int entities = 0;
   private int creatures = 0;
   private int items = 0;
   private int projectiles = 0;
   private int vehicles = 0;
   private int tiles = 0;
   private int chunks = 0;

   public WorldsMonitor() {
      super(false, "worlds");
   }

   @Override
   public void run() {
      int entities = 0;
      int creatures = 0;
      int items = 0;
      int projectiles = 0;
      int vehicles = 0;
      int tiles = 0;
      int chunks = 0;
      ISupportNms nms = SupportManager.getInstance().getNms();

      for (World world : Bukkit.getWorlds()) {
         Chunk[] loaded = world.getLoadedChunks();
         List<Entity> list = world.getEntities();
         entities += list.size();
         chunks += loaded.length;

         for (Entity entity : list) {
            if (entity instanceof Mob) {
               creatures++;
            } else if (entity instanceof Vehicle) {
               vehicles++;
            } else if (entity instanceof Item) {
               items++;
            } else if (entity instanceof Projectile) {
               projectiles++;
            }
         }

         for (Chunk chunk : loaded) {
            tiles += nms.getTileEntitiesCount(chunk);
         }
      }

      this.entities = entities;
      this.creatures = creatures;
      this.items = items;
      this.projectiles = projectiles;
      this.vehicles = vehicles;
      this.tiles = tiles;
      this.chunks = chunks;
   }

   @Generated
   public int getEntities() {
      return this.entities;
   }

   @Generated
   public int getCreatures() {
      return this.creatures;
   }

   @Generated
   public int getItems() {
      return this.items;
   }

   @Generated
   public int getProjectiles() {
      return this.projectiles;
   }

   @Generated
   public int getVehicles() {
      return this.vehicles;
   }

   @Generated
   public int getTiles() {
      return this.tiles;
   }

   @Generated
   public int getChunks() {
      return this.chunks;
   }
}
