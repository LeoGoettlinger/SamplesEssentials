package com.sk89q.worldedit.function.generator;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Random;

public class GardenPatchGenerator implements RegionFunction {
   private final Random random = new Random();
   private final EditSession editSession;
   private Pattern plant = getPumpkinPattern();
   private final Pattern leafPattern = BlockTypes.OAK_LEAVES.getDefaultState().with(BlockTypes.OAK_LEAVES.getProperty("persistent"), true);
   private int affected;

   public GardenPatchGenerator(EditSession editSession) {
      this.editSession = editSession;
   }

   public Pattern getPlant() {
      return this.plant;
   }

   public void setPlant(Pattern plant) {
      this.plant = plant;
   }

   public int getAffected() {
      return this.affected;
   }

   private void placeVine(BlockVector3 basePos, BlockVector3 pos) throws MaxChangedBlocksException {
      if (!(pos.distance(basePos) > 4.0)) {
         if (this.editSession.getBlock(pos).getBlockType().getMaterial().isAir()) {
            for (int i = -1; i > -3; i--) {
               BlockVector3 testPos = pos.add(0, i, 0);
               if (!this.editSession.getBlock(testPos).getBlockType().getMaterial().isAir()) {
                  break;
               }

               pos = testPos;
            }

            setBlockIfAir(this.editSession, pos, this.leafPattern);
            this.affected++;
            int t = this.random.nextInt(4);
            int h = this.random.nextInt(3) - 1;
            BlockState log = BlockTypes.OAK_LOG.getDefaultState();
            switch (t) {
               case 0:
                  if (this.random.nextBoolean()) {
                     this.placeVine(basePos, pos.add(1, 0, 0));
                  }

                  if (this.random.nextBoolean()) {
                     setBlockIfAir(this.editSession, pos.add(1, h, -1), log);
                     this.affected++;
                  }

                  BlockVector3 p;
                  setBlockIfAir(this.editSession, p = pos.add(0, 0, -1), this.plant.applyBlock(p));
                  this.affected++;
                  break;
               case 1:
                  if (this.random.nextBoolean()) {
                     this.placeVine(basePos, pos.add(0, 0, 1));
                  }

                  if (this.random.nextBoolean()) {
                     setBlockIfAir(this.editSession, pos.add(1, h, 0), log);
                     this.affected++;
                  }

                  BlockVector3 p;
                  setBlockIfAir(this.editSession, p = pos.add(1, 0, 1), this.plant.applyBlock(p));
                  this.affected++;
                  break;
               case 2:
                  if (this.random.nextBoolean()) {
                     this.placeVine(basePos, pos.add(0, 0, -1));
                  }

                  if (this.random.nextBoolean()) {
                     setBlockIfAir(this.editSession, pos.add(-1, h, 0), log);
                     this.affected++;
                  }

                  BlockVector3 p;
                  setBlockIfAir(this.editSession, p = pos.add(-1, 0, 1), this.plant.applyBlock(p));
                  this.affected++;
                  break;
               case 3:
                  if (this.random.nextBoolean()) {
                     this.placeVine(basePos, pos.add(-1, 0, 0));
                  }

                  if (this.random.nextBoolean()) {
                     setBlockIfAir(this.editSession, pos.add(-1, h, -1), log);
                     this.affected++;
                  }

                  BlockVector3 p;
                  setBlockIfAir(this.editSession, p = pos.add(-1, 0, -1), this.plant.applyBlock(p));
                  this.affected++;
            }
         }
      }
   }

   @Override
   public boolean apply(BlockVector3 position) throws WorldEditException {
      if (!this.editSession.getBlock(position).getBlockType().getMaterial().isAir()) {
         position = position.add(0, 1, 0);
      }

      if (!this.editSession.getBlock(position.add(0, -1, 0)).getBlockType().equals(BlockTypes.GRASS_BLOCK)) {
         return false;
      } else {
         setBlockIfAir(this.editSession, position, this.leafPattern);
         this.placeVine(position, position.add(0, 0, 1));
         this.placeVine(position, position.add(0, 0, -1));
         this.placeVine(position, position.add(1, 0, 0));
         this.placeVine(position, position.add(-1, 0, 0));
         return true;
      }
   }

   public static Pattern getPumpkinPattern() {
      return BlockTypes.PUMPKIN.getDefaultState();
   }

   private static boolean setBlockIfAir(EditSession session, BlockVector3 position, Pattern pattern) throws MaxChangedBlocksException {
      return session.getBlock(position).getBlockType().getMaterial().isAir() && session.setBlock(position, pattern);
   }

   public static Pattern getMelonPattern() {
      return BlockTypes.MELON.getDefaultState();
   }
}
