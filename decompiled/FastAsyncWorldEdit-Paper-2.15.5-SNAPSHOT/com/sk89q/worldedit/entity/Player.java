package com.sk89q.worldedit.entity;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.regions.FaweMaskManager;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.gamemode.GameMode;
import javax.annotation.Nullable;

public interface Player extends Entity, Actor {
   World getWorld();

   boolean isHoldingPickAxe();

   Direction getCardinalDirection(int yawOffset);

   BaseItemStack getItemInHand(HandSide handSide);

   BaseBlock getBlockInHand(HandSide handSide) throws WorldEditException;

   void giveItem(BaseItemStack itemStack);

   BlockBag getInventoryBlockBag();

   GameMode getGameMode();

   void setGameMode(GameMode gameMode);

   void findFreePosition(Location searchPos);

   void setOnGround(Location searchPos);

   void findFreePosition();

   boolean ascendLevel();

   boolean descendLevel();

   boolean ascendToCeiling(int clearance);

   boolean ascendToCeiling(int clearance, boolean alwaysGlass);

   boolean ascendUpwards(int distance);

   boolean ascendUpwards(int distance, boolean alwaysGlass);

   void floatAt(int x, int y, int z, boolean alwaysGlass);

   @Deprecated
   default Location getBlockIn() {
      return this.getBlockLocation();
   }

   Location getBlockOn();

   Location getBlockTrace(int range, boolean useLastBlock);

   Location getBlockTrace(int range, boolean useLastBlock, @Nullable Mask stopMask);

   Location getBlockTraceFace(int range, boolean useLastBlock);

   Location getBlockTraceFace(int range, boolean useLastBlock, @Nullable Mask stopMask);

   Location getBlockTrace(int range);

   Location getSolidBlockTrace(int range);

   Direction getCardinalDirection();

   boolean passThroughForwardWall(int range);

   @Deprecated
   default void setPosition(Vector3 pos, float pitch, float yaw) {
      this.trySetPosition(pos, pitch, yaw);
   }

   @NonAbstractForCompatibility(
      delegateName = "setPosition",
      delegateParams = {Vector3.class, float.class, float.class}
   )
   default boolean trySetPosition(Vector3 pos, float pitch, float yaw) {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      this.setPosition(pos, pitch, yaw);
      return true;
   }

   <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, @Nullable B block);

   void sendFakeOP();

   Region[] getAllowedRegions();

   Region[] getAllowedRegions(FaweMaskManager.MaskType type);

   Region[] getDisallowedRegions();

   Region[] getDisallowedRegions(FaweMaskManager.MaskType type);

   Region getLargestRegion();

   void setSelection(Region region);

   default Region getSelection() throws IncompleteRegionException {
      return this.getSession().getSelection(this.getWorld());
   }

   default void setSelection(RegionSelector selector) {
      this.getSession().setRegionSelector(this.getWorld(), selector);
   }

   default World getWorldForEditing() {
      return WorldEdit.getInstance().getPlatformManager().getWorldForEditing(this.getWorld());
   }

   default void unregister() {
      this.cancel(true);
      LocalSession session = this.getSession();
      if (Settings.settings().CLIPBOARD.USE_DISK && Settings.settings().CLIPBOARD.DELETE_ON_LOGOUT) {
         session.deleteClipboardOnDisk();
      } else if (Settings.settings().CLIPBOARD.USE_DISK) {
         Fawe.instance().submitUUIDKeyQueuedTask(this.getUniqueId(), () -> session.setClipboard(null));
      } else if (Settings.settings().CLIPBOARD.DELETE_ON_LOGOUT) {
         session.setClipboard(null);
      }

      if (!Settings.settings().HISTORY.USE_DISK && Settings.settings().HISTORY.DELETE_ON_LOGOUT
         || Settings.settings().HISTORY.USE_DISK && Settings.settings().HISTORY.DELETE_DISK_ON_LOGOUT) {
         session.clearHistory();
      }
   }

   void sendTitle(Component title, Component sub);

   void loadClipboardFromDisk();
}
