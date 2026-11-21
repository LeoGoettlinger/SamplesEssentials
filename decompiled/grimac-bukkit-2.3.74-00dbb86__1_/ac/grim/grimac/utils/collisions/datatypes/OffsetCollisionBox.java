package ac.grim.grimac.utils.collisions.datatypes;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.utils.math.GrimMath;
import java.util.HashSet;

public class OffsetCollisionBox extends SimpleCollisionBox {
   private static final HashSet<StateType> XZ_OFFSET_BLOCKSTATES = new HashSet<>();
   private static final HashSet<StateType> XYZ_OFFSET_BLOCKSTATES = new HashSet<>();
   private float maxHorizontalModelOffset = 0.25F;
   private float maxVerticalModelOffset = 0.2F;
   private double offsetX = 0.0;
   private double offsetY = 0.0;
   private double offsetZ = 0.0;
   private final OffsetCollisionBox.OffsetType offsetType;

   public OffsetCollisionBox(StateType block, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      super(minX, minY, minZ, maxX, maxY, maxZ);
      if (block.equals(StateTypes.POINTED_DRIPSTONE)) {
         this.maxHorizontalModelOffset = 0.125F;
      }

      if (XZ_OFFSET_BLOCKSTATES.contains(block)) {
         this.offsetType = OffsetCollisionBox.OffsetType.XZ;
      } else if (XYZ_OFFSET_BLOCKSTATES.contains(block)) {
         this.offsetType = OffsetCollisionBox.OffsetType.XYZ;
      } else {
         throw new RuntimeException("Invalid State Type for OffSetCollisionBox: " + block);
      }
   }

   @Override
   public SimpleCollisionBox offset(double x, double y, double z) {
      this.resetBlockStateOffSet();
      switch (this.offsetType) {
         case NONE:
            return super.offset(x, y, z);
         case XZ: {
            long l = GrimMath.hashCode(x, 0, z);
            this.offsetX = GrimMath.clamp(
               ((float)(l & 15L) / 15.0F - 0.5) * 0.5, (double)(-this.maxHorizontalModelOffset), (double)this.maxHorizontalModelOffset
            );
            this.offsetZ = GrimMath.clamp(
               ((float)(l >> 8 & 15L) / 15.0F - 0.5) * 0.5, (double)(-this.maxHorizontalModelOffset), (double)this.maxHorizontalModelOffset
            );
            return super.offset(x + this.offsetX, y, z + this.offsetZ);
         }
         case XYZ: {
            long l = GrimMath.hashCode(x, 0, z);
            this.offsetY = ((float)(l >> 4 & 15L) / 15.0F - 1.0) * this.maxVerticalModelOffset;
            this.offsetX = GrimMath.clamp(
               ((float)(l & 15L) / 15.0F - 0.5) * 0.5, (double)(-this.maxHorizontalModelOffset), (double)this.maxHorizontalModelOffset
            );
            this.offsetZ = GrimMath.clamp(
               ((float)(l >> 8 & 15L) / 15.0F - 0.5) * 0.5, (double)(-this.maxHorizontalModelOffset), (double)this.maxHorizontalModelOffset
            );
            return super.offset(x + this.offsetX, this.offsetY, z + this.offsetZ);
         }
         default:
            return null;
      }
   }

   public void resetBlockStateOffSet() {
      this.minX = this.minX + this.offsetX;
      this.minY = this.minY + this.offsetY;
      this.minZ = this.minZ + this.offsetZ;
      this.maxX = this.maxX + this.offsetX;
      this.maxY = this.maxY + this.offsetY;
      this.maxZ = this.maxZ + this.offsetZ;
   }

   static {
      XZ_OFFSET_BLOCKSTATES.add(StateTypes.MANGROVE_PROPAGULE);
      XZ_OFFSET_BLOCKSTATES.addAll(BlockTags.SMALL_FLOWERS.getStates());
      XZ_OFFSET_BLOCKSTATES.add(StateTypes.BAMBOO_SAPLING);
      XZ_OFFSET_BLOCKSTATES.add(StateTypes.BAMBOO);
      XZ_OFFSET_BLOCKSTATES.add(StateTypes.POINTED_DRIPSTONE);
      XZ_OFFSET_BLOCKSTATES.add(StateTypes.SULFUR_SPIKE);
   }

   public static enum OffsetType {
      NONE,
      XZ,
      XYZ;
   }
}
