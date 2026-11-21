package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.regex.Pattern;

public class TypeSwapPattern extends AbstractExtentPattern {
   private static final Pattern SPLITTER = Pattern.compile("[|,]");
   private final String inputString;
   private final String outputString;
   private final String[] inputs;
   private Pattern inputPattern = null;

   public TypeSwapPattern(Extent extent, String inputString, String outputString, boolean allowRegex) {
      super(extent);
      this.inputString = inputString;
      this.outputString = outputString;
      if (!StringMan.isAlphanumericUnd(inputString)) {
         if (allowRegex) {
            this.inputPattern = Pattern.compile(inputString.replace(",", "|"));
            this.inputs = null;
         } else {
            this.inputs = SPLITTER.split(inputString);
         }
      } else {
         this.inputs = null;
      }
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      BlockState existing = get.getBlock(extent);
      BlockState newBlock = this.getNewBlock(existing);
      return newBlock == null ? false : set.setBlock(extent, newBlock);
   }

   @Override
   public void applyBlock(final FilterBlock block) {
      BlockState existing = block.getBlock();
      BlockState newState = this.getNewBlock(existing);
      if (newState != null) {
         block.setBlock(newState);
      }
   }

   @Override
   public BaseBlock applyBlock(final BlockVector3 position) {
      BaseBlock existing = position.getFullBlock(this.getExtent());
      BlockState newState = this.getNewBlock(existing.toBlockState());
      return newState == null ? existing : newState.toBaseBlock();
   }

   private BlockState getNewBlock(BlockState existing) {
      String oldId = existing.getBlockType().id();
      String newId = oldId;
      if (this.inputPattern != null) {
         newId = this.inputPattern.matcher(oldId).replaceAll(this.outputString);
      } else if (this.inputs != null && this.inputs.length > 0) {
         for (String input : this.inputs) {
            newId = newId.replace(input, this.outputString);
         }
      } else {
         newId = oldId.replace(this.inputString, this.outputString);
      }

      if (newId.equals(oldId)) {
         return null;
      } else {
         BlockType newType = BlockTypes.get(newId);
         return newType == null ? null : newType.getDefaultState().withProperties(existing);
      }
   }
}
