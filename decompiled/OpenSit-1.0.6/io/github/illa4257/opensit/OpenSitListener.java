package io.github.illa4257.opensit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

public final class OpenSitListener implements Listener {
   public final Class<Event> dismountEvent;
   public final double offset;
   public final double offsetHalf;
   public final double offsetFull;

   public static boolean isSit(Entity e) {
      return e instanceof BlockDisplay && (e.getScoreboardTags().contains("sit") || e.getScoreboardTags().contains("sit2"));
   }

   public static void eachSit(Location l, OpenSitListener.RunnableBlockArg r) {
      double sy = l.getBlockY() - 0.1;
      double ey = l.getBlockY() + 1;
      int sx = l.getBlockX();
      int sz = l.getBlockZ();
      int ex = sx + 1;
      int ez = sz + 1;

      for (Entity e : l.getChunk().getEntities()) {
         if (isSit(e)) {
            Location l2 = e.getLocation();
            if (l2.getY() > sy && l2.getY() <= ey && l2.getX() >= sx && l2.getX() <= ex && l2.getZ() >= sz && l2.getZ() <= ez) {
               r.run(e);
            }
         }
      }
   }

   public static void removeSitsInBlock(Location l) {
      double sy = l.getBlockY() - 0.1;
      double ey = l.getBlockY() + 1;
      int sx = l.getBlockX();
      int sz = l.getBlockZ();
      int ex = sx + 1;
      int ez = sz + 1;

      for (Entity e : l.getChunk().getEntities()) {
         if (isSit(e)) {
            Location l2 = e.getLocation();
            if (l2.getY() > sy && l2.getY() <= ey && l2.getX() >= sx && l2.getX() <= ex && l2.getZ() >= sz && l2.getZ() <= ez) {
               e.remove();
            }
         }
      }
   }

   public OpenSitListener(OpenSit plugin) {
      Class<Event> cl = null;
      double o = 0.0;

      try {
         cl = (Class<Event>)Class.forName("org.bukkit.event.entity.EntityDismountEvent");
         o = 0.2;
      } catch (Exception var8) {
         try {
            plugin.getLogger().info("Old server, I will use another event.");
            cl = (Class<Event>)Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
         } catch (Exception var7) {
            plugin.getLogger().warning(var8.toString());
         }
      }

      this.dismountEvent = cl;
      this.offset = o;
      this.offsetHalf = this.offset + 0.3;
      this.offsetFull = this.offsetHalf + 0.5;
   }

   public void register(OpenSit plugin) {
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
      Method d = null;
      Method e = null;
      if (this.dismountEvent != null) {
         try {
            d = this.dismountEvent.getMethod("getDismounted");
            e = this.dismountEvent.getMethod("getEntity");
         } catch (Exception var7) {
            plugin.getLogger().warning(var7.toString());
         }
      }

      Method md = d;
      Method me = e;
      plugin.getServer().getPluginManager().registerEvent(this.dismountEvent, this, EventPriority.LOWEST, (listener, event) -> {
         if (listener == this) {
            if (this.dismountEvent.isInstance(event)) {
               try {
                  Entity dx = (Entity)md.invoke(event);
                  Entity ex = (Entity)me.invoke(event);
                  if (dx instanceof BlockDisplay) {
                     if (dx.getScoreboardTags().contains("sit")) {
                        ex.teleport(ex.getLocation().add(0.0, 1.0, 0.0));
                     } else {
                        if (!dx.getScoreboardTags().contains("sit2")) {
                           return;
                        }

                        ex.teleport(ex.getLocation().add(0.0, 1.5, 0.0));
                     }

                     int s = dx.getPassengers().size();
                     if (s == 1 && dx.getPassengers().contains(ex) || s == 0) {
                        dx.remove();
                     }
                  }
               } catch (Exception var9) {
                  plugin.getLogger().warning(var9.toString());
               }
            }
         }
      }, plugin, true);
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      Block b = event.getClickedBlock();
      if (event.getPlayer().getInventory().getItemInMainHand().getType().isAir()
         && event.getPlayer().getInventory().getItemInOffHand().getType().isAir()
         && b != null
         && !event.getAction().isLeftClick()
         && event.getPlayer().hasPermission("OpenSit.SitClick")) {
         Location l = b.getLocation();
         Block b2 = b.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() + 1, l.getBlockZ());
         if (!b2.isSolid() || b2.getState().getBlockData() instanceof TrapDoor) {
            BlockData d = b.getState().getBlockData();
            if (d instanceof Slab || d instanceof Stairs) {
               boolean t = d instanceof Slab ? ((Slab)d).getType() != Type.BOTTOM : ((Stairs)d).getHalf() == Half.TOP;
               BlockDisplay r = (BlockDisplay)l.getWorld()
                  .spawnEntity(
                     new Location(l.getWorld(), l.getX() + 0.5, l.getY() + (t ? this.offsetFull : this.offsetHalf), l.getZ() + 0.5), EntityType.BLOCK_DISPLAY
                  );
               r.addScoreboardTag(!(d instanceof Slab) && !t ? "sit2" : "sit");
               r.addPassenger(event.getPlayer());
            }
         }
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Entity e = event.getPlayer().getVehicle();
      if (isSit(e)) {
         Location l = new Location(e.getWorld(), e.getLocation().getX(), e.getLocation().getY() + 0.2, e.getLocation().getZ());
         Block b = l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY() + 1, l.getBlockZ());
         if (!l.getBlock().isSolid() && !l.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()).isSolid()
            || b.isSolid() && !(b.getState().getBlockData() instanceof TrapDoor)) {
            e.remove();
         }
      }
   }

   @EventHandler
   public void onExplodeBlock(BlockExplodeEvent event) {
      for (Block b : event.blockList()) {
         removeSitsInBlock(b.getLocation());
      }
   }

   @EventHandler
   public void onExplodeBlock(EntityExplodeEvent event) {
      for (Block b : event.blockList()) {
         removeSitsInBlock(b.getLocation());
      }
   }

   public void moved(Vector d, List<Block> bl) {
      ArrayList<Entity> el = new ArrayList<>();

      for (Block b : bl) {
         Location tl = b.getLocation().clone().add(d);
         Location ul = tl.clone().subtract(0.0, 1.0, 0.0);
         Location ul2 = tl.clone().add(0.0, 1.0, 0.0);
         if (!bl.contains(ul.getBlock()) && b.isSolid()) {
            removeSitsInBlock(ul);
         }

         if (!bl.contains(ul2.getBlock()) && ul2.getBlock().isSolid()) {
            removeSitsInBlock(b.getLocation());
         } else {
            eachSit(b.getLocation(), e -> {
               if (!el.contains(e)) {
                  el.add(e);
                  List<Entity> lp = e.getPassengers();
                  if (!lp.isEmpty()) {
                     BlockDisplay r = (BlockDisplay)b.getWorld().spawnEntity(e.getLocation().add(d), EntityType.BLOCK_DISPLAY);

                     for (String s : e.getScoreboardTags()) {
                        r.addScoreboardTag(s);
                     }

                     for (Entity p : lp) {
                        r.addPassenger(p);
                     }

                     el.add(r);
                  }

                  e.remove();
               }
            });
         }
      }
   }

   @EventHandler
   public void onPistonRetract(BlockPistonRetractEvent event) {
      this.moved(event.getDirection().getDirection(), event.getBlocks());
   }

   @EventHandler
   public void onPistonExtend(BlockPistonExtendEvent event) {
      if (event.getDirection().getDirection().getY() == 0.0 && event.getBlocks().isEmpty()) {
         removeSitsInBlock(event.getBlock().getLocation().clone().add(event.getDirection().getDirection()));
      } else if (event.getDirection().getDirection().getY() < 0.0 && event.getBlocks().isEmpty()) {
         removeSitsInBlock(event.getBlock().getLocation().clone().subtract(0.0, 2.0, 0.0));
      }

      this.moved(event.getDirection().getDirection(), event.getBlocks());
   }

   @EventHandler
   public void onBreakBlock(BlockBreakEvent event) {
      removeSitsInBlock(event.getBlock().getLocation());
   }

   public interface RunnableBlockArg {
      void run(Entity var1);
   }
}
