package ch.njol.skript.bukkitutil.block;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.MatchQuality;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import java.io.StreamCorruptedException;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewBlockCompat implements BlockCompat {
   private NewBlockCompat.NewBlockSetter setter = new NewBlockCompat.NewBlockSetter();

   @Deprecated(
      since = "2.8.4",
      forRemoval = true
   )
   @Nullable
   @Override
   public BlockValues getBlockValues(BlockState blockState) {
      return this.getBlockValues(blockState.getBlockData());
   }

   @Nullable
   @Override
   public BlockValues getBlockValues(Material material) {
      return material.isBlock() ? new NewBlockCompat.NewBlockValues(material, Bukkit.createBlockData(material), true) : null;
   }

   @Nullable
   @Override
   public BlockValues getBlockValues(BlockData blockData) {
      return new NewBlockCompat.NewBlockValues(blockData.getMaterial(), blockData, false);
   }

   @Nullable
   @Override
   public BlockValues getBlockValues(ItemStack stack) {
      Material type = stack.getType();
      return type.isBlock() ? new NewBlockCompat.NewBlockValues(type, Bukkit.createBlockData(type), true) : null;
   }

   @Override
   public BlockSetter getSetter() {
      return this.setter;
   }

   @Deprecated(
      since = "2.8.4",
      forRemoval = true
   )
   @Override
   public BlockState fallingBlockToState(FallingBlock entity) {
      BlockState state = entity.getLocation().getBlock().getState();
      state.setBlockData(entity.getBlockData());
      return state;
   }

   @Nullable
   @Override
   public BlockValues createBlockValues(Material type, Map<String, String> states, @Nullable ItemStack item, int itemFlags) {
      if (states.isEmpty()) {
         if (type.isBlock()) {
            BlockData data = Bukkit.createBlockData(type, "[]");

            assert data != null;

            return new NewBlockCompat.NewBlockValues(type, data, true);
         } else {
            return null;
         }
      } else {
         StringBuilder combined = new StringBuilder("[");
         boolean first = true;

         for (Entry<String, String> entry : states.entrySet()) {
            if (first) {
               first = false;
            } else {
               combined.append(',');
            }

            combined.append(entry.getKey()).append('=').append(entry.getValue());
         }

         combined.append(']');

         try {
            BlockData data = Bukkit.createBlockData(type, combined.toString());

            assert data != null;

            return new NewBlockCompat.NewBlockValues(type, data, false);
         } catch (IllegalArgumentException var9) {
            Skript.error("Parsing block state " + combined + " failed!");
            if (Skript.debug()) {
               var9.printStackTrace();
            }

            return null;
         }
      }
   }

   @Override
   public boolean isEmpty(Material type) {
      return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR;
   }

   @Override
   public boolean isLiquid(Material type) {
      return type == Material.WATER || type == Material.LAVA;
   }

   private static class NewBlockSetter implements BlockSetter {
      private static final BlockFace[] CARDINAL_FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
      private static final boolean HAS_BLOCK_SUPPORT = Skript.classExists("org.bukkit.block.BlockSupport");

      public NewBlockSetter() {
      }

      @Override
      public void setBlock(Block block, Material type, @Nullable BlockValues values, int flags) {
         boolean rotate = (flags & 1) != 0;
         boolean rotateForce = (flags & 2) != 0;
         boolean rotateFixType = (flags & 4) != 0;
         boolean multipart = (flags & 8) != 0;
         boolean applyPhysics = (flags & 16) != 0;
         NewBlockCompat.NewBlockValues ourValues = null;
         if (values != null) {
            ourValues = (NewBlockCompat.NewBlockValues)values;
         }

         Class<?> dataType = type.data;
         boolean placed = false;
         if (rotate) {
            if (type != Material.TORCH && (!rotateFixType || type != Material.WALL_TORCH)) {
               if (type == Material.WALL_TORCH) {
                  Directional data;
                  if (ourValues != null) {
                     data = (Directional)ourValues.data;
                  } else {
                     data = (Directional)Bukkit.createBlockData(type);
                  }

                  Block relative = block.getRelative(data.getFacing());
                  if (!canSupportWallTorch(relative, data.getFacing().getOppositeFace()) || rotateForce) {
                     BlockFace face = this.findWallTorchSide(block);
                     if (face != null) {
                        block.setType(type);
                        data.setFacing(face);
                        block.setBlockData(data, applyPhysics);
                        placed = true;
                     }
                  }
               }
            } else {
               Block under = block.getRelative(0, -1, 0);
               boolean canPlace = canSupportFloorTorch(under);
               if (!canPlace) {
                  BlockFace face = this.findWallTorchSide(block);
                  if (face != null) {
                     block.setType(Material.WALL_TORCH);
                     Directional datax = (Directional)block.getBlockData();
                     datax.setFacing(face);
                     block.setBlockData(datax, applyPhysics);
                     placed = true;
                  }
               }
            }
         }

         if (multipart) {
            if (Bed.class.isAssignableFrom(dataType)) {
               Bed datax;
               if (ourValues != null) {
                  datax = (Bed)ourValues.data.clone();
               } else {
                  datax = (Bed)Bukkit.createBlockData(type);
               }

               block.setType(type, false);
               block.setBlockData(datax, applyPhysics);
               BlockFace facing = datax.getFacing();
               BlockFace otherFacing = facing;
               Part otherPart = Part.HEAD;
               if (datax.getPart().equals(Part.HEAD)) {
                  facing = facing.getOppositeFace();
                  otherPart = Part.FOOT;
               }

               Block other = block.getRelative(facing);
               other.setType(type, false);
               datax.setPart(otherPart);
               datax.setFacing(otherFacing);
               other.setBlockData(datax, applyPhysics);
               placed = true;
            }

            if (Bisected.class.isAssignableFrom(dataType) && !Tag.STAIRS.isTagged(type) && !Tag.TRAPDOORS.isTagged(type)) {
               Bisected dataxx;
               if (ourValues != null) {
                  dataxx = (Bisected)ourValues.data.clone();
               } else {
                  dataxx = (Bisected)Bukkit.createBlockData(type);
               }

               block.setType(type, false);
               block.setBlockData(dataxx, applyPhysics);
               BlockFace facing = BlockFace.DOWN;
               Half otherHalf = Half.BOTTOM;
               if (dataxx.getHalf().equals(Half.BOTTOM)) {
                  facing = BlockFace.UP;
                  otherHalf = Half.TOP;
               }

               Block other = block.getRelative(facing);
               other.setType(type, false);
               dataxx.setHalf(otherHalf);
               other.setBlockData(dataxx, applyPhysics);
               placed = true;
            }
         }

         if (!placed) {
            block.setType(type);
            if (ourValues != null && !ourValues.isDefault()) {
               block.setBlockData(ourValues.data, applyPhysics);
            }
         }
      }

      private static boolean canSupportFloorTorch(Block block) {
         if (HAS_BLOCK_SUPPORT) {
            return block.getBlockData().isFaceSturdy(BlockFace.UP, BlockSupport.CENTER);
         } else {
            Material material = block.getType();
            return material.isOccluding()
               || canSupportWallTorch(block, null)
               || ItemUtils.isFence(block)
               || ItemUtils.isGlass(material)
               || material == Material.HOPPER
               || material == Material.SNOW && ((Snow)block.getBlockData()).getLayers() == 8;
         }
      }

      private static boolean canSupportWallTorch(Block block, @Nullable BlockFace face) {
         if (HAS_BLOCK_SUPPORT && face != null) {
            return block.getBlockData().isFaceSturdy(face, BlockSupport.FULL);
         } else {
            Material material = block.getType();
            return material.isOccluding() || material == Material.SOUL_SAND || material == Material.SPAWNER;
         }
      }

      @Nullable
      private BlockFace findWallTorchSide(Block block) {
         for (BlockFace face : CARDINAL_FACES) {
            Block relative = block.getRelative(face);
            if (relative.getType().isOccluding() || canSupportWallTorch(relative, face.getOppositeFace())) {
               return face.getOppositeFace();
            }
         }

         return null;
      }

      @Override
      public void sendBlockChange(Player player, Location location, Material type, @Nullable BlockValues values) {
         BlockData blockData = values != null ? ((NewBlockCompat.NewBlockValues)values).data : type.createBlockData();
         player.sendBlockChange(location, blockData);
      }
   }

   private static class NewBlockValues extends BlockValues {
      Material type;
      BlockData data;
      boolean isDefault;

      public NewBlockValues(Material type, BlockData data, boolean isDefault) {
         if (type != data.getMaterial()) {
            throw new IllegalArgumentException("'type' does not match material of 'data'");
         } else {
            this.type = type;
            this.data = data;
            this.isDefault = isDefault;
         }
      }

      private NewBlockValues() {
      }

      @Override
      public boolean isDefault() {
         return this.isDefault;
      }

      @Override
      public boolean equals(@Nullable Object other) {
         return !(other instanceof NewBlockCompat.NewBlockValues n)
            ? false
            : (this.data.matches(n.data) || n.data.matches(this.data)) && this.type.equals(n.type);
      }

      @Override
      public int hashCode() {
         int prime = 31;
         int result = 1;
         result = prime * result + (this.data == null ? 0 : this.data.hashCode());
         return prime * result + this.type.hashCode();
      }

      @Override
      public String toString() {
         return this.data.toString() + (this.isDefault ? " (default)" : "");
      }

      @Override
      public MatchQuality match(BlockValues other) {
         if (!(other instanceof NewBlockCompat.NewBlockValues n)) {
            throw new IllegalArgumentException("wrong block compat");
         } else if (this.type == n.type) {
            if (this.data.equals(n.data)) {
               return MatchQuality.EXACT;
            } else {
               return this.data.matches(n.data) ? MatchQuality.SAME_ITEM : MatchQuality.SAME_MATERIAL;
            }
         } else {
            return MatchQuality.DIFFERENT;
         }
      }

      @Override
      public Fields serialize() {
         Fields fields = new Fields();
         fields.putObject("data", this.data.getAsString());
         fields.putPrimitive("isDefault", this.isDefault);
         return fields;
      }

      @Override
      public void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
         String data = fields.getObject("data", String.class);
         boolean isDefault = fields.getPrimitive("isDefault", Boolean.class);
         if (data == null) {
            throw new StreamCorruptedException("'data' is missing.");
         } else {
            try {
               this.data = Bukkit.createBlockData(data);
            } catch (IllegalArgumentException var5) {
               throw new StreamCorruptedException("Invalid block data: " + data);
            }

            this.type = this.data.getMaterial();
            this.isDefault = isDefault;
         }
      }

      static {
         Variables.yggdrasil.registerSingleClass(NewBlockCompat.NewBlockValues.class, "NewBlockValues");
      }
   }
}
