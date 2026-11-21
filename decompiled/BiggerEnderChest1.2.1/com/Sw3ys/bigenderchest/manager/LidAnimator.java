package com.Sw3ys.bigenderchest.manager;

import com.Sw3ys.bigenderchest.BiggerEnderChest;
import com.Sw3ys.bigenderchest.util.Sched;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lidded;
import org.bukkit.entity.Player;

public class LidAnimator {
   private final BiggerEnderChest plugin;
   private final Map<UUID, Location> openBlocks = new HashMap<>();
   private final Map<Location, Integer> counts = new HashMap<>();

   public LidAnimator(BiggerEnderChest plugin) {
      this.plugin = plugin;
   }

   public void open(Player viewer, Block block) {
      Location loc = block.getLocation();
      this.openBlocks.put(viewer.getUniqueId(), loc);
      int now = this.counts.merge(loc, 1, Integer::sum);
      if (now == 1) {
         this.setLid(loc, true);
      }
   }

   public void close(Player viewer) {
      Location loc = this.openBlocks.remove(viewer.getUniqueId());
      if (loc != null) {
         Integer current = this.counts.get(loc);
         if (current != null) {
            if (current <= 1) {
               this.counts.remove(loc);
               this.setLid(loc, false);
            } else {
               this.counts.put(loc, current - 1);
            }
         }
      }
   }

   private void setLid(Location loc, boolean open) {
      Sched.region(this.plugin, loc, () -> {
         Block block = loc.getBlock();
         if (block.getType() == Material.ENDER_CHEST) {
            if (block.getState() instanceof Lidded lidded) {
               if (open) {
                  lidded.open();
               } else {
                  lidded.close();
               }
            }
         }
      });
   }
}
