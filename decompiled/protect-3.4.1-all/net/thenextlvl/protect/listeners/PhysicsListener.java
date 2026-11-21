package net.thenextlvl.protect.listeners;

import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.flag.Flag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.GlassPane;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Wall;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.jspecify.annotations.Nullable;

public final class PhysicsListener implements Listener {
   private final ProtectPlugin plugin;

   public PhysicsListener(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPhysics(BlockPhysicsEvent event) {
      if ((!event.getSourceBlock().isEmpty() || !event.getChangedType().isAir())
            && !event.getSourceBlock().getType().equals(Material.SNOW)
            && !event.getSourceBlock().getType().equals(Material.POWDER_SNOW)
            && !event.getSourceBlock().getType().equals(Material.SNOW_BLOCK)
         || !event.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.GRASS_BLOCK)) {
         BlockData blockData = event.getChangedBlockData();
         if (!event.getChangedType().equals(Material.IRON_BARS)
            && !(blockData instanceof Stairs)
            && !(blockData instanceof Chest)
            && !(blockData instanceof Fence)
            && !(blockData instanceof GlassPane)
            && !(blockData instanceof Wall)
            && !(blockData instanceof Door)) {
            if (!(blockData instanceof Powerable) || event.getBlock().isEmpty()) {
               event.setCancelled(
                  this.isInteractionRestricted(
                     event.getSourceBlock(), event.getBlock(), event.getChangedType().hasGravity() ? this.plugin.flags.gravity : this.plugin.flags.physics
                  )
               );
            }
         }
      }
   }

   private boolean isInteractionRestricted(Block source, @Nullable Block target, Flag<Boolean> flag) {
      Area area = this.plugin.areaProvider().getArea(source);
      return !area.getFlag(flag) ? true : target != null && !source.equals(target) && !area.canInteract(this.plugin.areaProvider().getArea(target));
   }
}
