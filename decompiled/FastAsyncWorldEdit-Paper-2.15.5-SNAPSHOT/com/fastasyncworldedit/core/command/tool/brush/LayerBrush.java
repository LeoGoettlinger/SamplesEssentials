package com.fastasyncworldedit.core.command.tool.brush;

import com.fastasyncworldedit.core.function.mask.AdjacentAnyMask;
import com.fastasyncworldedit.core.function.mask.LayerBrushMask;
import com.fastasyncworldedit.core.function.mask.RadiusMask;
import com.fastasyncworldedit.core.math.BlockVectorSet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.BreadthFirstSearch;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Arrays;

public class LayerBrush implements Brush {
   private final BlockState[] layers;
   private RecursiveVisitor visitor;

   public LayerBrush(Pattern[] layers) {
      this.layers = Arrays.stream(layers).map(p -> p.applyBlock(BlockVector3.ZERO).toBlockState()).toArray(BlockState[]::new);
   }

   @Override
   public void build(EditSession editSession, BlockVector3 position, Pattern ignore, double size) throws MaxChangedBlocksException {
      AdjacentAnyMask adjacent = new AdjacentAnyMask(
         new BlockMask(editSession).add(BlockTypes.AIR, BlockTypes.CAVE_AIR, BlockTypes.VOID_AIR), editSession.getMinY(), editSession.getMaxY()
      );
      SolidBlockMask solid = new SolidBlockMask(editSession);
      RadiusMask radius = new RadiusMask(0, (int)size);
      this.visitor = new RecursiveVisitor(
         new MaskIntersection(adjacent, solid, radius), funcion -> true, Integer.MAX_VALUE, editSession.getMinY(), editSession.getMaxY()
      );
      this.visitor.visit(position);
      this.visitor.setDirections(Arrays.asList(BreadthFirstSearch.DIAGONAL_DIRECTIONS));
      Operations.completeBlindly(this.visitor);
      BlockVectorSet visited = this.visitor.getVisited();
      this.visitor = new RecursiveVisitor(new LayerBrushMask(editSession, this.visitor, this.layers, adjacent), posx -> {
         int depth = this.visitor.getDepth();
         Pattern currentPattern = this.layers[depth];
         return currentPattern.apply(editSession, posx, posx);
      }, this.layers.length - 1, editSession.getMinY(), editSession.getMaxY());

      for (BlockVector3 pos : visited) {
         this.visitor.visit(pos);
      }

      Operations.completeBlindly(this.visitor);
      this.visitor = null;
   }
}
