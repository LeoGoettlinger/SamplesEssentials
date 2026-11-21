package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.command.tool.MovableTool;
import com.fastasyncworldedit.core.command.tool.ResettableTool;
import com.fastasyncworldedit.core.command.tool.TargetMode;
import com.fastasyncworldedit.core.command.tool.brush.BrushSettings;
import com.fastasyncworldedit.core.command.tool.scroll.Scroll;
import com.fastasyncworldedit.core.command.tool.scroll.ScrollTool;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.function.mask.MaskedTargetBlock;
import com.fastasyncworldedit.core.function.pattern.PatternTraverser;
import com.fastasyncworldedit.core.util.BrushCache;
import com.fastasyncworldedit.core.util.MaskTraverser;
import com.fastasyncworldedit.core.util.StringMan;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.annotation.Nullable;

public class BrushTool implements DoubleActionTraceTool, ScrollTool, MovableTool, ResettableTool, Serializable, TraceTool {
   protected static int MAX_RANGE = 500;
   protected int range = -1;
   private Mask traceMask = null;
   protected static int DEFAULT_RANGE = 240;
   private TargetMode targetMode = TargetMode.TARGET_BLOCK_RANGE;
   private int targetOffset;
   private transient BrushSettings primary = new BrushSettings();
   private transient BrushSettings secondary = new BrushSettings();
   private transient BrushSettings context = this.primary;
   private transient BaseItem holder;

   public BrushTool(String permission) {
      Preconditions.checkNotNull(permission);
      this.getContext().addPermission(permission);
   }

   public BrushTool() {
   }

   public void setHolder(BaseItem holder) {
      this.holder = holder;
   }

   public boolean isSet() {
      return this.primary.getBrush() != null || this.secondary.getBrush() != null;
   }

   public void update() {
      if (this.holder != null) {
         BrushCache.setTool(this.holder, this);
      }
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeBoolean(this.primary == this.secondary);
      stream.writeObject(this.primary);
      if (this.primary != this.secondary) {
         stream.writeObject(this.secondary);
      }
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      boolean multi = stream.readBoolean();
      this.primary = (BrushSettings)stream.readObject();
      if (multi) {
         this.secondary = (BrushSettings)stream.readObject();
      } else {
         this.secondary = this.primary;
      }

      this.context = this.primary;
   }

   public BrushSettings getContext() {
      BrushSettings tmp = this.context;
      if (tmp == null) {
         tmp = this.primary;
         this.context = this.primary;
      }

      return tmp;
   }

   public void setContext(BrushSettings context) {
      this.context = context;
   }

   @Override
   public boolean canUse(Actor player) {
      return this.primary == this.secondary ? this.primary.canUse(player) : this.primary.canUse(player) && this.secondary.canUse(player);
   }

   public ResettableExtent getTransform() {
      return this.getContext().getTransform();
   }

   public BrushSettings getPrimary() {
      return this.primary;
   }

   public BrushSettings getSecondary() {
      return this.secondary;
   }

   public BrushSettings getOffHand() {
      return this.context == this.primary ? this.secondary : this.primary;
   }

   public void setPrimary(BrushSettings primary) {
      Preconditions.checkNotNull(primary);
      this.primary = primary;
      this.context = primary;
      this.update();
   }

   public void setSecondary(BrushSettings secondary) {
      Preconditions.checkNotNull(secondary);
      this.secondary = secondary;
      this.context = secondary;
      this.update();
   }

   public void setTransform(ResettableExtent transform) {
      this.getContext().setTransform(transform);
      this.update();
   }

   public Mask getSourceMask() {
      return this.getContext().getSourceMask();
   }

   @Override
   public boolean reset() {
      Brush br = this.getBrush();
      return br instanceof ResettableTool ? ((ResettableTool)br).reset() : false;
   }

   public void setMask(Mask filter) {
      this.getContext().setMask(filter);
      this.update();
   }

   public Mask getMask() {
      return this.getContext().getMask();
   }

   @Nullable
   public Mask getTraceMask() {
      return this.traceMask;
   }

   public void setTraceMask(@Nullable Mask traceMask) {
      this.traceMask = traceMask;
      this.update();
   }

   public void setSourceMask(Mask filter) {
      this.getContext().setSourceMask(filter);
      this.update();
   }

   public void setBrush(Brush brush, String permission) {
      BrushSettings current = this.getContext();
      current.clearPerms();
      current.setBrush(brush);
      current.addPermission(permission);
      this.update();
   }

   public Brush getBrush() {
      return this.getContext().getBrush();
   }

   public void setFill(@Nullable Pattern material) {
      this.getContext().setFill(material);
   }

   @Nullable
   public Pattern getMaterial() {
      return this.getContext().getMaterial();
   }

   public double getSize() {
      return this.getContext().getSize();
   }

   public void setSize(double radius) {
      this.getContext().setSize(radius);
   }

   public void setSize(Expression radius) {
      this.getContext().setSize(radius);
   }

   public int getRange() {
      return this.range < 0 ? DEFAULT_RANGE : Math.min(this.range, MAX_RANGE);
   }

   public void setRange(int range) {
      this.range = range;
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
      return this.act(BrushTool.BrushAction.PRIMARY, player, session);
   }

   public BlockVector3 getPosition(EditSession editSession, Player player) {
      Location loc = player.getLocation();
      switch (this.targetMode) {
         case TARGET_BLOCK_RANGE:
            return this.offset(this.trace(editSession, player, this.getRange(), true), loc).toBlockPoint();
         case FORWARD_POINT_PITCH:
            int d = 0;
            float pitch = loc.getPitch();
            pitch = 23.0F - pitch / 4.0F;
            d += (int)(Math.sin(Math.toRadians(pitch)) * 50.0);
            Vector3 vector = loc.getDirection().withY(0.0).normalize().multiply(d).add(loc.x(), loc.y(), loc.z());
            return this.offset(vector, loc).toBlockPoint();
         case TARGET_POINT_HEIGHT:
            int height = loc.getBlockY();
            int x = loc.getBlockX();
            int z = loc.getBlockZ();

            int y;
            for (y = height; y > editSession.getMinY(); y--) {
               BlockType block = editSession.getBlockType(x, y, z);
               if (block.getMaterial().isMovementBlocker()) {
                  break;
               }
            }

            int distance = height - y + 8;
            return this.offset(this.trace(editSession, player, distance, true), loc).toBlockPoint();
         case TARGET_FACE_RANGE:
            return this.offset(this.trace(editSession, player, this.getRange(), true), loc).toBlockPoint();
         default:
            return null;
      }
   }

   private Vector3 offset(Vector3 target, Vector3 playerPos) {
      return this.targetOffset == 0 ? target : target.subtract(target.subtract(playerPos).normalize().multiply(this.targetOffset));
   }

   private Vector3 trace(EditSession editSession, Player player, int range, boolean useLastBlock) {
      Mask mask = (Mask)(this.traceMask == null ? new SolidBlockMask(editSession) : this.traceMask);
      new MaskTraverser(mask).reset(editSession);
      MaskedTargetBlock tb = new MaskedTargetBlock(mask, player, range, 0.2);
      return tb.getMaskedTargetBlock(useLastBlock);
   }

   public boolean act(BrushTool.BrushAction action, Player player, LocalSession session) {
      switch (action) {
         case PRIMARY:
            this.setContext(this.primary);
            break;
         case SECONDARY:
            this.setContext(this.secondary);
            break;
         default:
            throw new IllegalStateException("Unexpected value: " + action);
      }

      BrushSettings current = this.getContext();
      Brush brush = current.getBrush();
      if (brush == null) {
         return false;
      } else if (!current.canUse(player)) {
         player.print(Caption.of("fawe.error.no-perm", StringMan.join(current.getPermissions(), ",")));
         return false;
      } else {
         try (EditSession editSession = session.createEditSession(player, current.toString())) {
            Location target = player.getBlockTrace(this.getRange(), true, this.traceMask);
            if (target == null) {
               editSession.cancel();
               player.print(Caption.of("worldedit.tool.no-block"));
               return true;
            }

            BlockBag bag = session.getBlockBag(player);
            Request.request().setEditSession(editSession);
            Mask mask = current.getMask();
            if (mask != null) {
               Mask existingMask = editSession.getMask();
               if (existingMask == null) {
                  editSession.setMask(mask);
               } else if (existingMask instanceof MaskIntersection) {
                  ((MaskIntersection)existingMask).add(mask);
               } else {
                  MaskIntersection newMask = new MaskIntersection(existingMask);
                  newMask.add(mask);
                  editSession.setMask(newMask);
               }
            }

            Mask sourceMask = current.getSourceMask();
            if (sourceMask != null) {
               editSession.addSourceMask(sourceMask);
            }

            ResettableExtent transform = current.getTransform();
            if (transform != null) {
               editSession.addTransform(transform);
            }

            try {
               new PatternTraverser(current).reset(editSession);
               double size = current.getSize();
               WorldEdit.getInstance().checkMaxBrushRadius(size, player);
               brush.build(editSession, target.toBlockPoint(), current.getMaterial(), size);
            } catch (MaxChangedBlocksException var27) {
               player.print(Caption.of("worldedit.tool.max-block-changes"));
            } finally {
               session.remember(editSession);
               if (bag != null) {
                  bag.flushChanges();
               }
            }
         } finally {
            Request.reset();
         }

         return true;
      }
   }

   @Override
   public boolean actSecondary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
      return this.act(BrushTool.BrushAction.SECONDARY, player, session);
   }

   public void setScrollAction(Scroll scrollAction) {
      this.getContext().setScrollAction(scrollAction);
      this.update();
   }

   public void setTargetOffset(int targetOffset) {
      this.targetOffset = targetOffset;
      this.update();
   }

   public void setTargetMode(TargetMode targetMode) {
      this.targetMode = targetMode != null ? targetMode : TargetMode.TARGET_BLOCK_RANGE;
      this.update();
   }

   public TargetMode getTargetMode() {
      return this.targetMode;
   }

   public int getTargetOffset() {
      return this.targetOffset;
   }

   @Override
   public boolean increment(Player player, int amount) {
      BrushSettings current = this.getContext();
      Scroll tmp = current.getScrollAction();
      if (tmp != null) {
         tmp.setTool(this);
         return tmp.increment(player, amount);
      } else {
         return false;
      }
   }

   @Override
   public boolean move(Player player) {
      return false;
   }

   static enum BrushAction {
      PRIMARY,
      SECONDARY;
   }
}
