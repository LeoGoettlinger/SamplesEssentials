package net.thenextlvl.protect.listeners;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.flag.Flag;
import net.thenextlvl.protect.flag.ProtectionFlag;
import net.thenextlvl.protect.utils.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jspecify.annotations.Nullable;

public final class WorldListener implements Listener {
   private final ProtectPlugin plugin;

   public WorldListener(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onWorldLoad(WorldLoadEvent event) {
      this.plugin.areaProvider().load(event.getWorld());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      this.plugin.areaProvider().save(event.getWorld());
      this.plugin.areaProvider().unload(event.getWorld());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onWorldSave(WorldSaveEvent event) {
      this.plugin.areaProvider().save(event.getWorld());
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      event.setCancelled(!this.plugin.protectionService().canDestroy(event.getPlayer(), event.getBlock().getLocation()));
      this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(event.getBlock()), "area.failed.break");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      event.setCancelled(!this.plugin.protectionService().canPlace(event.getPlayer(), event.getBlock().getLocation()));
      this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(event.getBlock()), "area.failed.place");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPhysicalInteract(PlayerInteractEvent event) {
      if (event.getAction().equals(Action.PHYSICAL)) {
         if (event.getClickedBlock() != null) {
            Location location = event.getClickedBlock().getLocation();
            event.setCancelled(
               event.getClickedBlock().getType().equals(Material.FARMLAND)
                  ? !this.plugin.protectionService().canTrample(event.getPlayer(), location)
                  : !this.plugin.protectionService().canInteractPhysical(event.getPlayer(), location)
            );
            if (event.getClickedBlock().getType().equals(Material.FARMLAND)) {
               this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(location), "area.failed.break");
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
         if (event.getClickedBlock() != null) {
            if (!event.getPlayer().isSneaking() || !event.hasItem() || !event.getMaterial().isBlock()) {
               if (BlockUtil.isInteractable(event.getClickedBlock().getType(), event.getMaterial())) {
                  event.setCancelled(!this.plugin.protectionService().canInteract(event.getPlayer(), event.getClickedBlock().getLocation()));
                  this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(event.getClickedBlock()), "area.failed.interact");
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockMultiPlace(BlockMultiPlaceEvent event) {
      event.setCancelled(
         !event.getReplacedBlockStates().stream().allMatch(blockState -> this.plugin.protectionService().canPlace(event.getPlayer(), blockState.getLocation()))
      );
      this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(event.getBlock()), "area.failed.place");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockReceiveGameEvent(BlockReceiveGameEvent event) {
      event.setCancelled(
         this.isInteractionRestricted(
            event.getBlock().getLocation(), event.getEntity() != null ? event.getEntity().getLocation() : null, this.plugin.flags.gameEvents
         )
      );
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockFade(BlockFadeEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getBlock());
      event.setCancelled(!area.getFlag(this.plugin.flags.blockFading));
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockForm(BlockFormEvent event) {
      event.setCancelled(this.isInteractionRestricted(event.getBlock().getLocation(), event.getNewState().getLocation(), this.plugin.flags.blockForming));
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockBurn(BlockBurnEvent event) {
      event.setCancelled(
         this.isInteractionRestricted(
            Objects.requireNonNullElse(event.getIgnitingBlock(), event.getBlock()).getLocation(),
            event.getBlock().getLocation(),
            this.plugin.flags.blockBurning
         )
      );
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockIgnite(BlockIgniteEvent event) {
      event.setCancelled(
         this.isInteractionRestricted(
            event.getIgnitingBlock() != null
               ? event.getIgnitingBlock().getLocation()
               : (event.getIgnitingEntity() != null ? event.getIgnitingEntity().getLocation() : event.getBlock().getLocation()),
            event.getBlock().getLocation(),
            this.plugin.flags.blockIgniting
         )
      );
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onMoistureChange(MoistureChangeEvent event) {
      if (event.getBlock().getBlockData() instanceof Farmland current) {
         if (event.getNewState().getBlockData() instanceof Farmland future) {
            Area var6 = this.plugin.areaProvider().getArea(event.getBlock());
            event.setCancelled(
               !var6.getFlag(current.getMoisture() > future.getMoisture() ? this.plugin.flags.blockDrying : this.plugin.flags.blockMoisturising)
            );
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onLiquidFlow(BlockFromToEvent event) {
      if (event.getBlock().isLiquid()) {
         event.setCancelled(this.isInteractionRestricted(event.getBlock().getLocation(), event.getToBlock().getLocation(), this.plugin.flags.liquidFlow));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockGrow(BlockGrowEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getBlock());
      event.setCancelled(!area.getFlag(this.plugin.flags.blockGrowth));
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onSpongeAbsorb(SpongeAbsorbEvent event) {
      this.filterStates(event.getBlock().getLocation(), event.getBlocks(), null, this.plugin.flags.blockAbsorb);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onStructureGrow(StructureGrowEvent event) {
      this.filterStates(event.getLocation(), event.getBlocks(), event.getPlayer(), this.plugin.flags.blockGrowth);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockFertilize(BlockFertilizeEvent event) {
      this.filterStates(event.getBlock().getLocation(), event.getBlocks(), event.getPlayer(), this.plugin.flags.blockFertilize);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockExplode(BlockExplodeEvent event) {
      this.filterBlocks(event.getBlock().getLocation(), event.blockList(), this.plugin.flags.explosions);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEntityExplode(EntityExplodeEvent event) {
      this.filterBlocks(event.getLocation(), event.blockList(), this.plugin.flags.explosions);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockRedstone(BlockRedstoneEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getBlock());
      if (!area.getFlag(this.plugin.flags.redstone)) {
         event.setNewCurrent(event.getOldCurrent());
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerBucketFill(PlayerBucketFillEvent event) {
      event.setCancelled(!this.plugin.protectionService().canDestroy(event.getPlayer(), event.getBlock().getLocation()));
      this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(event.getBlock()), "area.failed.break");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
      event.setCancelled(!this.plugin.protectionService().canDestroy(event.getPlayer(), event.getBlock().getLocation()));
      this.plugin.failed(event.getPlayer(), event, this.plugin.areaProvider().getArea(event.getBlock()), "area.failed.place");
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockSpread(BlockSpreadEvent event) {
      event.setCancelled(this.isInteractionRestricted(event.getSource().getLocation(), event.getBlock().getLocation(), this.plugin.flags.blockSpread));
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onLeavesDecay(LeavesDecayEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getBlock());
      event.setCancelled(!area.getFlag(this.plugin.flags.leavesDecay));
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
      Area area = this.plugin.areaProvider().getArea(event.getBlock());

      ProtectionFlag<Boolean> flag = switch (event.getReason()) {
         case EXTINGUISH -> this.plugin.flags.cauldronExtinguishEntity;
         case EVAPORATE -> this.plugin.flags.cauldronEvaporation;
         case NATURAL_FILL -> this.plugin.flags.naturalCauldronFill;
         default -> null;
      };
      if (flag != null) {
         event.setCancelled(!area.getFlag(flag));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockPistonExtend(BlockPistonExtendEvent event) {
      this.handlePistonMovement(event, event.getDirection(), event.getBlocks(), 1);
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onBlockPistonRetract(BlockPistonRetractEvent event) {
      this.handlePistonMovement(event, event.getDirection(), event.getBlocks(), 0);
   }

   private void handlePistonMovement(BlockPistonEvent event, BlockFace direction, List<Block> blocks, int offset) {
      Area area = this.plugin.areaProvider().getArea(event.getBlock());
      Area relative = this.plugin.areaProvider().getArea(event.getBlock().getRelative(direction));
      event.setCancelled(
         !area.canInteract(relative)
            || !blocks.stream().allMatch(block -> area.canInteract(this.plugin.areaProvider().getArea(block.getRelative(direction, offset))))
      );
   }

   private boolean isInteractionRestricted(Location source, @Nullable Location target, Flag<Boolean> flag) {
      Area area = this.plugin.areaProvider().getArea(source);
      return !area.getFlag(flag) ? true : target != null && !source.equals(target) && !area.canInteract(this.plugin.areaProvider().getArea(target));
   }

   private void filterStates(Location source, List<BlockState> blocks, @Nullable Player player, Flag<Boolean> flag) {
      this.filter(source, blocks, BlockState::getLocation, player, flag);
   }

   private void filterBlocks(Location source, List<Block> blocks, Flag<Boolean> flag) {
      this.filter(source, blocks, Block::getLocation, null, flag);
   }

   private <T> void filter(Location source, List<T> list, Function<T, Location> function, @Nullable Player player, Flag<Boolean> flag) {
      this.filter(
         source,
         list,
         function,
         (area, target) -> player != null
            ? this.plugin.protectionService().canPerformAction(player, target, flag, null)
            : area.canInteract(target) && target.getFlag(flag)
      );
   }

   private <T> void filter(Location source, List<T> list, Function<T, Location> function, BiPredicate<Area, Area> filter) {
      Area area = this.plugin.areaProvider().getArea(source);
      list.removeIf(object -> !filter.test(area, this.plugin.areaProvider().getArea(function.apply((T)object))));
   }
}
