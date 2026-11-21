package com.magmaguy.elitemobs.easyminecraftgoals.visuals.terrain;

import java.util.List;
import java.util.Objects;
import org.bukkit.block.data.BlockData;

record TerrainImpactPlan(List<TerrainImpactPlan.Tile> tiles) {
   TerrainImpactPlan(List<TerrainImpactPlan.Tile> tiles) {
      tiles = List.copyOf(tiles);
      this.tiles = tiles;
   }

   record Point(double x, double y, double z) {
   }

   private record Quaternion(double x, double y, double z, double w) {
      private static TerrainImpactPlan.Quaternion fromTilts(double tiltXDegrees, double tiltZDegrees) {
         double halfX = Math.toRadians(tiltXDegrees) * 0.5;
         double halfZ = Math.toRadians(tiltZDegrees) * 0.5;
         double sinX = Math.sin(halfX);
         double cosX = Math.cos(halfX);
         double sinZ = Math.sin(halfZ);
         double cosZ = Math.cos(halfZ);
         return new TerrainImpactPlan.Quaternion(cosZ * sinX, sinZ * sinX, sinZ * cosX, cosZ * cosX);
      }

      private TerrainImpactPlan.Point rotate(double pointX, double pointY, double pointZ) {
         double crossX = this.y * pointZ - this.z * pointY;
         double crossY = this.z * pointX - this.x * pointZ;
         double crossZ = this.x * pointY - this.y * pointX;
         double secondCrossX = this.y * crossZ - this.z * crossY;
         double secondCrossY = this.z * crossX - this.x * crossZ;
         double secondCrossZ = this.x * crossY - this.y * crossX;
         return new TerrainImpactPlan.Point(
            pointX + 2.0 * (this.w * crossX + secondCrossX), pointY + 2.0 * (this.w * crossY + secondCrossY), pointZ + 2.0 * (this.w * crossZ + secondCrossZ)
         );
      }
   }

   record Tile(int x, int y, int z, BlockData blockData, double radialDistance, double radialFalloff, TerrainImpactPlan.Transform transform) {
      Tile(int x, int y, int z, BlockData blockData, double radialDistance, double radialFalloff, TerrainImpactPlan.Transform transform) {
         blockData = Objects.requireNonNull(blockData, "blockData").clone();
         transform = Objects.requireNonNull(transform, "transform");
         this.x = x;
         this.y = y;
         this.z = z;
         this.blockData = blockData;
         this.radialDistance = radialDistance;
         this.radialFalloff = radialFalloff;
         this.transform = transform;
      }

      public BlockData blockData() {
         return this.blockData.clone();
      }
   }

   record Transform(double uniformScale, double tiltXDegrees, double tiltZDegrees, double lift, double translationX, double translationY, double translationZ) {
      double rotationX() {
         return this.quaternion().x();
      }

      double rotationY() {
         return this.quaternion().y();
      }

      double rotationZ() {
         return this.quaternion().z();
      }

      double rotationW() {
         return this.quaternion().w();
      }

      TerrainImpactPlan.Point applyTo(double x, double y, double z) {
         TerrainImpactPlan.Point rotated = this.quaternion().rotate(x * this.uniformScale, y * this.uniformScale, z * this.uniformScale);
         return new TerrainImpactPlan.Point(rotated.x() + this.translationX, rotated.y() + this.translationY, rotated.z() + this.translationZ);
      }

      private TerrainImpactPlan.Quaternion quaternion() {
         return TerrainImpactPlan.Quaternion.fromTilts(this.tiltXDegrees, this.tiltZDegrees);
      }
   }
}
