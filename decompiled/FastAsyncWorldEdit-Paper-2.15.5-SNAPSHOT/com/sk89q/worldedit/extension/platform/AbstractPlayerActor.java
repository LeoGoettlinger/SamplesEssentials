package com.sk89q.worldedit.extension.platform;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.internal.exception.FaweClipboardVersionMismatchException;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.WEManager;
import com.fastasyncworldedit.core.util.task.AsyncNotifyKeyedQueue;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.ConvexPolyhedralRegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.CylinderRegionSelector;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TargetBlock;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public abstract class AbstractPlayerActor implements Actor, Player, Cloneable {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final Map<String, Object> meta;
   private final Semaphore clipboardLoading = new Semaphore(1);
   private final AtomicInteger runningCount = new AtomicInteger();
   private final AsyncNotifyKeyedQueue asyncNotifyQueue = new AsyncNotifyKeyedQueue((thread, throwable) -> {
      while (throwable.getCause() != null) {
         throwable = throwable.getCause();
      }

      if (throwable instanceof WorldEditException) {
         this.printError(TextComponent.of(throwable.getLocalizedMessage()));
      } else {
         FaweException fe = FaweException.get(throwable);
         if (fe != null) {
            this.printError(fe.getComponent());
         } else {
            LOGGER.error("Error occurred executing player action", throwable);
         }
      }
   }, this::getUniqueId);

   public AbstractPlayerActor(Map<String, Object> meta) {
      this.meta = meta;
   }

   public AbstractPlayerActor() {
      this(new ConcurrentHashMap<>());
   }

   @Override
   public final Extent getExtent() {
      return this.getWorld();
   }

   private static Direction getDirection(double rot) {
      if (0.0 <= rot && rot < 22.5) {
         return Direction.SOUTH;
      } else if (22.5 <= rot && rot < 67.5) {
         return Direction.SOUTHWEST;
      } else if (67.5 <= rot && rot < 112.5) {
         return Direction.WEST;
      } else if (112.5 <= rot && rot < 157.5) {
         return Direction.NORTHWEST;
      } else if (157.5 <= rot && rot < 202.5) {
         return Direction.NORTH;
      } else if (202.5 <= rot && rot < 247.5) {
         return Direction.NORTHEAST;
      } else if (247.5 <= rot && rot < 292.5) {
         return Direction.EAST;
      } else if (292.5 <= rot && rot < 337.5) {
         return Direction.SOUTHEAST;
      } else {
         return 337.5 <= rot && rot < 360.0 ? Direction.SOUTH : null;
      }
   }

   @Override
   public Map<String, Object> getRawMeta() {
      return this.meta;
   }

   @Override
   public boolean isHoldingPickAxe() {
      ItemType item = this.getItemInHand(HandSide.MAIN_HAND).getType();
      return item == ItemTypes.IRON_PICKAXE
         || item == ItemTypes.WOODEN_PICKAXE
         || item == ItemTypes.STONE_PICKAXE
         || item == ItemTypes.DIAMOND_PICKAXE
         || item == ItemTypes.GOLDEN_PICKAXE
         || item == ItemTypes.NETHERITE_PICKAXE;
   }

   @Override
   public void findFreePosition(Location searchPos) {
      Extent world = searchPos.getExtent();
      int worldMinY = world.getMinimumPoint().y();
      int worldMaxY = world.getMaximumPoint().y();
      int x = searchPos.getBlockX();
      int y = Math.max(worldMinY, searchPos.getBlockY());
      int origY = y;
      int z = searchPos.getBlockZ();
      int yPlusSearchHeight = y + WorldEdit.getInstance().getConfiguration().defaultVerticalHeight;
      int maxY = Math.min(worldMaxY, yPlusSearchHeight) + 2;
      byte free = 0;

      for (BlockVector3 mutablePos = new MutableBlockVector3(); y <= maxY; y++) {
         if (!world.getBlock(mutablePos.setComponents(x, y, z)).getBlockType().getMaterial().isMovementBlocker()) {
            free++;
         } else {
            free = 0;
         }

         if (free == 2) {
            boolean worked = true;
            if (y - 1 != origY) {
               worked = this.trySetPosition(Vector3.at(x + 0.5, y - 2 + 1, z + 0.5));
            }

            if (worked) {
               return;
            }
         }
      }
   }

   @Override
   public void setOnGround(Location searchPos) {
      Extent world = searchPos.getExtent();
      int worldMinY = world.getMinimumPoint().y();
      int x = searchPos.getBlockX();
      int y = Math.max(worldMinY, searchPos.getBlockY());
      int z = searchPos.getBlockZ();
      int yLessSearchHeight = y - WorldEdit.getInstance().getConfiguration().defaultVerticalHeight;
      int minY = Math.min(worldMinY, yLessSearchHeight) + 2;

      for (MutableBlockVector3 mutable = new MutableBlockVector3(x, y, z); y >= minY; y--) {
         BlockState id = world.getBlock(mutable.mutY(y));
         if (id.getBlockType().getMaterial().isMovementBlocker() && this.trySetPosition(Vector3.at(x + 0.5, y + 1, z + 0.5))) {
            return;
         }
      }
   }

   @Override
   public void findFreePosition() {
      this.findFreePosition(this.getBlockLocation());
   }

   private boolean isPlayerHarmingBlock(BlockVector3 location) {
      BlockType type = this.getWorld().getBlock(location).getBlockType();
      return type.getMaterial().isMovementBlocker() || type == BlockTypes.LAVA || BlockCategories.FIRE.contains(type);
   }

   private boolean isLocationGoodForStanding(BlockVector3 location) {
      if (this.isPlayerHarmingBlock(location.add(0, 1, 0))) {
         return false;
      } else {
         return this.isPlayerHarmingBlock(location) ? false : this.getWorld().getBlock(location.add(0, -1, 0)).getBlockType().getMaterial().isMovementBlocker();
      }
   }

   @Override
   public boolean ascendLevel() {
      World world = this.getWorld();
      Location pos = this.getBlockLocation();
      int x = pos.getBlockX();
      int y = Math.max(world.getMinY(), pos.getBlockY() + 1);
      int z = pos.getBlockZ();
      int yPlusSearchHeight = y + WorldEdit.getInstance().getConfiguration().defaultVerticalHeight;

      for (int maxY = Math.min(world.getMaxY(), yPlusSearchHeight) + 2; y <= maxY; y++) {
         if (this.isLocationGoodForStanding(BlockVector3.at(x, y, z)) && this.trySetPosition(Vector3.at(x + 0.5, y, z + 0.5))) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean descendLevel() {
      World world = this.getWorld();
      Location pos = this.getBlockLocation();
      int x = pos.getBlockX();
      int y = Math.max(world.getMinY(), pos.getBlockY() - 1);
      int z = pos.getBlockZ();
      int yLessSearchHeight = y - WorldEdit.getInstance().getConfiguration().defaultVerticalHeight;

      for (int minY = Math.min(world.getMinY() + 1, yLessSearchHeight); y >= minY; y--) {
         if (this.isLocationGoodForStanding(BlockVector3.at(x, y, z)) && this.trySetPosition(Vector3.at(x + 0.5, y, z + 0.5))) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean ascendToCeiling(int clearance) {
      return this.ascendToCeiling(clearance, true);
   }

   @Override
   public boolean ascendToCeiling(int clearance, boolean alwaysGlass) {
      World world = this.getWorld();
      Location pos = this.getBlockLocation();
      int x = pos.getBlockX();
      int initialY = Math.max(world.getMinY(), pos.getBlockY());
      int y = Math.max(world.getMinY(), pos.getBlockY() + 2);
      int z = pos.getBlockZ();
      MutableBlockVector3 mutable = new MutableBlockVector3(x, y, z);
      if (!world.getBlock(mutable).getBlockType().getMaterial().isAir()) {
         return false;
      } else {
         int yPlusSearchHeight = y + WorldEdit.getInstance().getConfiguration().defaultVerticalHeight;

         for (int maxY = Math.min(world.getMaxY(), yPlusSearchHeight); y <= maxY; y++) {
            if (world.getBlock(mutable.mutY(y)).getBlockType().getMaterial().isMovementBlocker()) {
               int platformY = Math.max(initialY, y - 3 - clearance);
               if (platformY < initialY) {
                  return false;
               }

               if (platformY == initialY) {
                  return false;
               }

               this.floatAt(x, platformY + 1, z, alwaysGlass);
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean ascendUpwards(int distance) {
      return this.ascendUpwards(distance, true);
   }

   @Override
   public boolean ascendUpwards(int distance, boolean alwaysGlass) {
      World world = this.getWorld();
      Location pos = this.getBlockLocation();
      int x = pos.getBlockX();
      int initialY = Math.max(world.getMinY(), pos.getBlockY());
      int y = Math.max(world.getMinY(), pos.getBlockY() + 1);
      int z = pos.getBlockZ();
      int maxY = Math.min(world.getMaxY() + 1, initialY + distance);

      for (MutableBlockVector3 mutable = new MutableBlockVector3(x, y, z);
         y <= world.getMaxY() + 2 && !world.getBlock(mutable.mutY(y)).getBlockType().getMaterial().isMovementBlocker() && y <= maxY + 1;
         y++
      ) {
         if (y == maxY + 1) {
            this.floatAt(x, y - 1, z, alwaysGlass);
            return true;
         }
      }

      return false;
   }

   @Override
   public void floatAt(int x, int y, int z, boolean alwaysGlass) {
      if (!alwaysGlass && this.isAllowedToFly()) {
         this.setFlying(true);
      } else {
         BlockVector3 spot = BlockVector3.at(x, y - 1, z);
         World world = this.getWorld();
         if (!world.getBlock(spot).getBlockType().getMaterial().isMovementBlocker()) {
            try (EditSession session = WorldEdit.getInstance().newEditSession(this)) {
               session.setBlock(spot, BlockTypes.GLASS.getDefaultState());
            } catch (MaxChangedBlocksException var12) {
            }
         }
      }

      this.trySetPosition(Vector3.at(x + 0.5, y, z + 0.5));
   }

   protected boolean isAllowedToFly() {
      return false;
   }

   protected void setFlying(boolean flying) {
   }

   @Override
   public Location getBlockOn() {
      Location location = this.getLocation();
      return location.setPosition(location.setY(location.y() - 1.0).toVector().floor());
   }

   @Override
   public Location getBlockTrace(int range, boolean useLastBlock) {
      return this.getBlockTrace(range, useLastBlock, null);
   }

   @Override
   public Location getBlockTraceFace(int range, boolean useLastBlock) {
      return this.getBlockTraceFace(range, useLastBlock, null);
   }

   @Override
   public Location getBlockTrace(int range, boolean useLastBlock, @Nullable Mask stopMask) {
      TargetBlock tb = new TargetBlock(this, range, 0.2);
      if (stopMask != null) {
         tb.setStopMask(stopMask);
      }

      return useLastBlock ? tb.getAnyTargetBlock() : tb.getTargetBlock();
   }

   @Override
   public Location getBlockTraceFace(int range, boolean useLastBlock, @Nullable Mask stopMask) {
      TargetBlock tb = new TargetBlock(this, range, 0.2);
      if (stopMask != null) {
         tb.setStopMask(stopMask);
      }

      return useLastBlock ? tb.getAnyTargetBlockFace() : tb.getTargetBlockFace();
   }

   @Override
   public Location getBlockTrace(int range) {
      return this.getBlockTrace(range, false);
   }

   @Override
   public Location getSolidBlockTrace(int range) {
      TargetBlock tb = new TargetBlock(this, range, 0.2);
      return tb.getSolidTargetBlock();
   }

   @Override
   public Direction getCardinalDirection() {
      return this.getCardinalDirection(0);
   }

   @Override
   public Region[] getAllowedRegions() {
      return this.getAllowedRegions(FaweMaskManager.MaskType.getDefaultMaskType());
   }

   @Override
   public Region[] getAllowedRegions(FaweMaskManager.MaskType type) {
      return WEManager.weManager().getMask(this, type, true);
   }

   @Override
   public Region[] getDisallowedRegions() {
      return this.getDisallowedRegions(FaweMaskManager.MaskType.getDefaultMaskType());
   }

   @Override
   public Region[] getDisallowedRegions(FaweMaskManager.MaskType type) {
      return WEManager.weManager().getMask(this, type, false);
   }

   @Override
   public Region getLargestRegion() {
      long area = 0L;
      Region max = null;

      for (Region region : this.getAllowedRegions()) {
         long tmp = region.getVolume();
         if (tmp > area) {
            area = tmp;
            max = region;
         }
      }

      return max;
   }

   @Override
   public void setSelection(Region region) {
      RegionSelector selector = (RegionSelector)(switch (region) {
         case ConvexPolyhedralRegion blockVector3s -> new ConvexPolyhedralRegionSelector(blockVector3s);
         case CylinderRegion blockVector3sx -> new CylinderRegionSelector(blockVector3sx);
         case Polygonal2DRegion blockVector3sxx -> new Polygonal2DRegionSelector(blockVector3sxx);
         default -> new CuboidRegionSelector(null, region.getMinimumPoint(), region.getMaximumPoint());
      });
      selector.setWorld(region.getWorld());
      this.getSession().setRegionSelector(this.getWorld(), selector);
   }

   @Override
   public void loadClipboardFromDisk() {
      if (!this.clipboardLoading.tryAcquire()) {
         if (!Fawe.isMainThread()) {
            try {
               this.clipboardLoading.acquire();
               this.clipboardLoading.release();
            } catch (InterruptedException var3) {
               LOGGER.error("Error waiting for clipboard-on-disk loading for player {}", this.getName(), var3);
            }
         }
      } else {
         File file = MainUtil.getFile(Fawe.<IFawe>platform().getDirectory(), Settings.settings().PATHS.CLIPBOARD + File.separator + this.getUniqueId() + ".bd");

         try {
            Future<?> fut = Fawe.instance().submitUUIDKeyQueuedTask(this.getUniqueId(), () -> {
               try {
                  this.getSession().loadClipboardFromDisk(file);
               } catch (FaweClipboardVersionMismatchException var8) {
                  this.print(var8.getComponent());
               } catch (RuntimeException var9) {
                  this.print(Caption.of("fawe.error.clipboard.invalid"));
                  LOGGER.error("Error loading clipboard from disk", var9);
                  this.print(Caption.of("fawe.error.stacktrace"));
                  this.print(Caption.of("fawe.error.clipboard.load.failure"));
                  this.print(Caption.of("fawe.error.clipboard.invalid.info", file.getName(), file.length()));
                  this.print(Caption.of("fawe.error.stacktrace"));
               } catch (Exception var10) {
                  this.print(Caption.of("fawe.error.clipboard.invalid"));
                  LOGGER.error("Error loading clipboard from disk", var10);
                  this.print(Caption.of("fawe.error.stacktrace"));
                  this.print(Caption.of("fawe.error.no-failure"));
                  this.print(Caption.of("fawe.error.clipboard.invalid.info", file.getName(), file.length()));
                  this.print(Caption.of("fawe.error.stacktrace"));
               } finally {
                  this.clipboardLoading.release();
               }
            });
            if (Fawe.isMainThread()) {
               return;
            }

            fut.get();
         } catch (Exception var4) {
            LOGGER.error("Error loading clipboard from disk", var4);
            this.print(Caption.of("fawe.error.clipboard.load.failure"));
         }
      }
   }

   @Override
   public Direction getCardinalDirection(int yawOffset) {
      Location location = this.getLocation();
      if (location.getPitch() > 67.5) {
         return Direction.DOWN;
      } else if (location.getPitch() < -67.5) {
         return Direction.UP;
      } else {
         double rot = (location.getYaw() + yawOffset) % 360.0F;
         if (rot < 0.0) {
            rot += 360.0;
         }

         return getDirection(rot);
      }
   }

   @Override
   public BaseBlock getBlockInHand(HandSide handSide) throws WorldEditException {
      ItemType typeId = this.getItemInHand(handSide).getType();
      return typeId.hasBlockType() ? typeId.getBlockType().getDefaultState().toBaseBlock() : BlockTypes.AIR.getDefaultState().toBaseBlock();
   }

   private boolean canPassThroughBlock(Location curBlock) {
      BlockVector3 blockPos = curBlock.toVector().toBlockPoint();
      BlockState block = curBlock.getExtent().getBlock(blockPos);
      return !block.getBlockType().getMaterial().isMovementBlocker();
   }

   private boolean advanceToWall(TargetBlock hitBlox) {
      Location curBlock;
      while ((curBlock = hitBlox.getCurrentBlock()) != null) {
         if (!this.canPassThroughBlock(curBlock)) {
            return true;
         }

         hitBlox.getNextBlock();
      }

      return false;
   }

   private boolean advanceToFree(TargetBlock hitBlox) {
      Location curBlock;
      while ((curBlock = hitBlox.getCurrentBlock()) != null) {
         if (this.canPassThroughBlock(curBlock)) {
            return true;
         }

         hitBlox.getNextBlock();
      }

      return false;
   }

   @Override
   public boolean passThroughForwardWall(int range) {
      TargetBlock hitBlox = new TargetBlock(this, range, 0.2);
      if (!this.advanceToWall(hitBlox)) {
         return false;
      } else if (!this.advanceToFree(hitBlox)) {
         return false;
      } else {
         Location foundBlock = hitBlox.getCurrentBlock();
         if (foundBlock != null) {
            this.setOnGround(foundBlock);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean trySetPosition(Vector3 pos) {
      Location location = this.getLocation();
      return this.trySetPosition(pos, location.getPitch(), location.getYaw());
   }

   @Override
   public File openFileOpenDialog(String[] extensions) {
      this.print(Caption.of("worldedit.platform.no-file-dialog"));
      return null;
   }

   @Override
   public File openFileSaveDialog(String[] extensions) {
      this.print(Caption.of("worldedit.platform.no-file-dialog"));
      return null;
   }

   @Override
   public boolean runAction(Runnable ifFree, boolean checkFree, boolean async) {
      if (checkFree && this.runningCount.get() != 0) {
         return false;
      } else {
         Runnable wrapped = () -> {
            try {
               this.runningCount.addAndGet(1);
               ifFree.run();
            } finally {
               this.runningCount.decrementAndGet();
            }
         };
         if (async) {
            this.asyncNotifyQueue.run(wrapped);
         } else {
            TaskManager.taskManager().taskNow(wrapped, false);
         }

         return true;
      }
   }

   @Override
   public boolean canDestroyBedrock() {
      return this.hasPermission("worldedit.override.bedrock");
   }

   @Override
   public void dispatchCUIEvent(CUIEvent event) {
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof Player other2 ? other2.getName().equals(this.getName()) : false;
   }

   @Override
   public int hashCode() {
      return this.getName().hashCode();
   }

   @Override
   public void checkPermission(String permission) throws AuthorizationException {
      if (!this.hasPermission(permission)) {
         throw new AuthorizationException(Caption.of("fawe.error.no-perm", permission));
      }
   }

   @Override
   public boolean isPlayer() {
      return true;
   }

   @Override
   public GameMode getGameMode() {
      return GameModes.SURVIVAL;
   }

   @Override
   public void setGameMode(GameMode gameMode) {
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException("Not supported");
   }

   @Override
   public boolean remove() {
      return false;
   }

   @Override
   public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, @Nullable B block) {
   }

   @Override
   public void sendFakeOP() {
   }
}
