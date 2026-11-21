package com.sk89q.worldedit.extent.world;

import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.task.RunnableVal;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Collection;
import org.enginehub.linbus.tree.LinCompoundTag;

public class SurvivalModeExtent extends AbstractDelegateExtent {
   private final World world;
   private boolean toolUse = false;
   private boolean stripNbt = false;

   public SurvivalModeExtent(Extent extent, World world) {
      super(extent);
      Preconditions.checkNotNull(world);
      this.world = world;
   }

   public boolean hasToolUse() {
      return this.toolUse;
   }

   public void setToolUse(boolean toolUse) {
      this.toolUse = toolUse;
   }

   public boolean hasStripNbt() {
      return this.stripNbt;
   }

   public void setStripNbt(boolean stripNbt) {
      this.stripNbt = stripNbt;
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      if (this.toolUse && block.getBlockType().getMaterial().isAir()) {
         final Collection<BaseItemStack> drops = this.world.getBlockDrops(location);
         boolean canSet = super.setBlock(location, block);
         if (canSet) {
            TaskManager.taskManager().sync(new RunnableVal<Object>() {
               @Override
               public void run(Object value) {
                  for (BaseItemStack stack : drops) {
                     SurvivalModeExtent.this.world.dropItem(location.toVector3(), stack);
                  }
               }
            });
            return true;
         } else {
            return false;
         }
      } else {
         return this.stripNbt ? super.setBlock(location, block.toBaseBlock((LinCompoundTag)null)) : super.setBlock(location, block);
      }
   }
}
