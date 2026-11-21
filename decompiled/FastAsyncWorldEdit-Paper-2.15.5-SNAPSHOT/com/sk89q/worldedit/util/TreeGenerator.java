package com.sk89q.worldedit.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

@Deprecated
public final class TreeGenerator {
   private static final Random RANDOM = new Random();

   private TreeGenerator() {
   }

   private static void makePineTree(EditSession editSession, BlockVector3 basePosition) throws MaxChangedBlocksException {
      int trunkHeight = (int)Math.floor(Math.random() * 2.0) + 3;
      int height = (int)Math.floor(Math.random() * 5.0) + 8;
      BlockState logBlock = BlockTypes.OAK_LOG.getDefaultState();
      BlockState leavesBlock = BlockTypes.OAK_LEAVES.getDefaultState();

      for (int i = 0; i < trunkHeight; i++) {
         if (!setBlockIfAir(editSession, basePosition.add(0, i, 0), logBlock)) {
            return;
         }
      }

      basePosition = basePosition.add(0, trunkHeight, 0);

      for (int ix = 0; ix < height; ix++) {
         setBlockIfAir(editSession, basePosition.add(0, ix, 0), logBlock);
         double chance = ix != 0 && ix != height - 1 ? 1.0 : 0.6;
         setChanceBlockIfAir(editSession, basePosition.add(-1, ix, 0), leavesBlock, chance);
         setChanceBlockIfAir(editSession, basePosition.add(1, ix, 0), leavesBlock, chance);
         setChanceBlockIfAir(editSession, basePosition.add(0, ix, -1), leavesBlock, chance);
         setChanceBlockIfAir(editSession, basePosition.add(0, ix, 1), leavesBlock, chance);
         setChanceBlockIfAir(editSession, basePosition.add(1, ix, 1), leavesBlock, chance);
         setChanceBlockIfAir(editSession, basePosition.add(-1, ix, 1), leavesBlock, chance);
         setChanceBlockIfAir(editSession, basePosition.add(1, ix, -1), leavesBlock, chance);
         setChanceBlockIfAir(editSession, basePosition.add(-1, ix, -1), leavesBlock, chance);
         if (ix != 0 && ix != height - 1) {
            for (int j = -2; j <= 2; j++) {
               setChanceBlockIfAir(editSession, basePosition.add(-2, ix, j), leavesBlock, 0.6);
            }

            for (int j = -2; j <= 2; j++) {
               setChanceBlockIfAir(editSession, basePosition.add(2, ix, j), leavesBlock, 0.6);
            }

            for (int j = -2; j <= 2; j++) {
               setChanceBlockIfAir(editSession, basePosition.add(j, ix, -2), leavesBlock, 0.6);
            }

            for (int j = -2; j <= 2; j++) {
               setChanceBlockIfAir(editSession, basePosition.add(j, ix, 2), leavesBlock, 0.6);
            }
         }
      }

      setBlockIfAir(editSession, basePosition.add(0, height, 0), leavesBlock);
   }

   @Nullable
   public static TreeGenerator.TreeType lookup(String type) {
      return TreeGenerator.TreeType.lookup(type);
   }

   private static <B extends BlockStateHolder<B>> boolean setChanceBlockIfAir(EditSession session, BlockVector3 position, B block, double probability) throws MaxChangedBlocksException {
      return Math.random() <= probability && setBlockIfAir(session, position, block);
   }

   private static <B extends BlockStateHolder<B>> boolean setBlockIfAir(EditSession session, BlockVector3 position, B block) throws MaxChangedBlocksException {
      return session.getBlock(position).getBlockType().getMaterial().isAir() && session.setBlock(position, block);
   }

   @Deprecated
   public static enum TreeType {
      TREE("Oak tree", "oak", "tree", "regular"),
      BIG_TREE("Large oak tree", "largeoak", "bigoak", "big", "bigtree"),
      REDWOOD("Spruce tree", "spruce", "redwood", "sequoia", "sequoioideae"),
      TALL_REDWOOD("Tall spruce tree", "tallspruce", "bigspruce", "tallredwood", "tallsequoia", "tallsequoioideae"),
      MEGA_REDWOOD("Large spruce tree", "largespruce", "megaredwood"),
      RANDOM_REDWOOD("Random spruce tree", "randspruce", "randredwood", "randomredwood", "anyredwood") {
         @Override
         public boolean generate(EditSession editSession, BlockVector3 pos) throws MaxChangedBlocksException {
            TreeGenerator.TreeType[] choices = new TreeGenerator.TreeType[]{REDWOOD, TALL_REDWOOD, MEGA_REDWOOD};
            return choices[TreeGenerator.RANDOM.nextInt(choices.length)].generate(editSession, pos);
         }
      },
      BIRCH("Birch tree", "birch", "white", "whitebark"),
      TALL_BIRCH("Tall birch tree", "tallbirch"),
      RANDOM_BIRCH("Random birch tree", "randbirch", "randombirch") {
         @Override
         public boolean generate(EditSession editSession, BlockVector3 pos) throws MaxChangedBlocksException {
            TreeGenerator.TreeType[] choices = new TreeGenerator.TreeType[]{BIRCH, TALL_BIRCH};
            return choices[TreeGenerator.RANDOM.nextInt(choices.length)].generate(editSession, pos);
         }
      },
      JUNGLE("Jungle tree", "jungle"),
      SMALL_JUNGLE("Small jungle tree", "smalljungle"),
      SHORT_JUNGLE("Short jungle tree", "shortjungle") {
         @Override
         public boolean generate(EditSession editSession, BlockVector3 pos) throws MaxChangedBlocksException {
            return SMALL_JUNGLE.generate(editSession, pos);
         }
      },
      RANDOM_JUNGLE("Random jungle tree", "randjungle", "randomjungle") {
         @Override
         public boolean generate(EditSession editSession, BlockVector3 pos) throws MaxChangedBlocksException {
            TreeGenerator.TreeType[] choices = new TreeGenerator.TreeType[]{JUNGLE, SMALL_JUNGLE};
            return choices[TreeGenerator.RANDOM.nextInt(choices.length)].generate(editSession, pos);
         }
      },
      JUNGLE_BUSH("Jungle bush", "junglebush", "jungleshrub"),
      RED_MUSHROOM("Red mushroom", "redmushroom", "redgiantmushroom") {
         @Override
         public boolean canGenerateOn(final BlockType type) {
            return type == BlockTypes.MYCELIUM || type == BlockTypes.WARPED_NYLIUM || type == BlockTypes.CRIMSON_NYLIUM || super.canGenerateOn(type);
         }
      },
      BROWN_MUSHROOM("Brown mushroom", "brownmushroom", "browngiantmushroom") {
         @Override
         public boolean canGenerateOn(final BlockType type) {
            return type == BlockTypes.MYCELIUM || type == BlockTypes.WARPED_NYLIUM || type == BlockTypes.CRIMSON_NYLIUM || super.canGenerateOn(type);
         }
      },
      CRIMSON_FUNGUS("Crimson fungus", "crimsonfungus", "rednethermushroom") {
         @Override
         public boolean canGenerateOn(final BlockType type) {
            return type == BlockTypes.MYCELIUM || type == BlockTypes.WARPED_NYLIUM || type == BlockTypes.CRIMSON_NYLIUM || super.canGenerateOn(type);
         }
      },
      WARPED_FUNGUS("Warped fungus", "warpedfungus", "greennethermushroom") {
         @Override
         public boolean canGenerateOn(final BlockType type) {
            return type == BlockTypes.MYCELIUM || type == BlockTypes.WARPED_NYLIUM || type == BlockTypes.CRIMSON_NYLIUM || super.canGenerateOn(type);
         }
      },
      RANDOM_MUSHROOM("Random mushroom", "randmushroom", "randommushroom") {
         @Override
         public boolean generate(EditSession editSession, BlockVector3 pos) throws MaxChangedBlocksException {
            TreeGenerator.TreeType[] choices = new TreeGenerator.TreeType[]{RED_MUSHROOM, BROWN_MUSHROOM, CRIMSON_FUNGUS, WARPED_FUNGUS};
            return choices[TreeGenerator.RANDOM.nextInt(choices.length)].generate(editSession, pos);
         }
      },
      SWAMP("Swamp tree", "swamp", "swamptree"),
      ACACIA("Acacia tree", "acacia"),
      AZALEA("Azalea tree", "azalea"),
      DARK_OAK("Dark oak tree", "darkoak"),
      PINE("Pine tree", "pine") {
         @Override
         public boolean generate(EditSession editSession, BlockVector3 pos) throws MaxChangedBlocksException {
            if (editSession.getBlockType(pos.x(), pos.y(), pos.z()).getMaterial().isAir()
               && this.canGenerateOn(editSession.getBlockType(pos.x(), pos.y() - 1, pos.z()))) {
               TreeGenerator.makePineTree(editSession, pos);
               return true;
            } else {
               return false;
            }
         }
      },
      CHORUS_PLANT("Chorus plant", "chorusplant") {
         @Override
         public boolean generate(EditSession editSession, BlockVector3 pos) throws MaxChangedBlocksException {
            BlockVector3 down = pos.subtract(0, 1, 0);
            return !this.canGenerateOn(editSession.getBlockType(down.x(), down.y(), down.z()))
               ? false
               : editSession.getWorld().generateTree(this, editSession, down);
         }

         @Override
         public boolean canGenerateOn(BlockType type) {
            return type == BlockTypes.END_STONE;
         }
      },
      MANGROVE("Mangrove tree", "mangrove"),
      TALL_MANGROVE("Tall mangrove tree", "tall_mangrove"),
      CHERRY("Cherry blossom", "cherry"),
      PALE_OAK("Pale oak tree", "pale_oak"),
      PALE_OAK_CREAKING("Pale oak creaking tree", "pale_oak_creaking"),
      RANDOM("Random tree", "rand", "random") {
         @Override
         public boolean generate(EditSession editSession, BlockVector3 pos) throws MaxChangedBlocksException {
            TreeGenerator.TreeType[] choices = TreeGenerator.TreeType.values();
            return choices[TreeGenerator.RANDOM.nextInt(choices.length)].generate(editSession, pos);
         }
      };

      private static final Map<String, TreeGenerator.TreeType> lookup = new HashMap<>();
      private static final Set<String> primaryAliases = Sets.newHashSet();
      private final String name;
      public final ImmutableList<String> lookupKeys;

      private TreeType(String name, String... lookupKeys) {
         this.name = name;
         this.lookupKeys = ImmutableList.copyOf(lookupKeys);
      }

      public static Set<String> getAliases() {
         return Collections.unmodifiableSet(lookup.keySet());
      }

      public static Set<String> getPrimaryAliases() {
         return Collections.unmodifiableSet(primaryAliases);
      }

      public boolean generate(EditSession editSession, BlockVector3 pos) throws MaxChangedBlocksException {
         return editSession.getBlockType(pos.x(), pos.y(), pos.z()).getMaterial().isAir()
               && this.canGenerateOn(editSession.getBlockType(pos.x(), pos.y() - 1, pos.z()))
            ? editSession.getWorld().generateTree(this, editSession, pos)
            : false;
      }

      public boolean canGenerateOn(BlockType type) {
         return type == BlockTypes.DIRT
            || type == BlockTypes.GRASS_BLOCK
            || type == BlockTypes.PODZOL
            || type == BlockTypes.COARSE_DIRT
            || type == BlockTypes.MOSS_BLOCK
            || type == BlockTypes.ROOTED_DIRT
            || type == BlockTypes.MUD;
      }

      public String getName() {
         return this.name;
      }

      @Nullable
      public static TreeGenerator.TreeType lookup(String name) {
         return lookup.get(name.toLowerCase(Locale.ROOT));
      }

      static {
         for (TreeGenerator.TreeType type : EnumSet.allOf(TreeGenerator.TreeType.class)) {
            UnmodifiableIterator var2 = type.lookupKeys.iterator();

            while (var2.hasNext()) {
               String key = (String)var2.next();
               lookup.put(key, type);
            }

            if (type.lookupKeys.size() > 0) {
               primaryAliases.add((String)type.lookupKeys.get(0));
            }
         }
      }
   }
}
