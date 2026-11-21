package com.magmaguy.elitemobs.wormhole;

import com.magmaguy.elitemobs.config.WormholesConfig;
import com.magmaguy.elitemobs.config.wormholes.WormholeConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.util.Vector;

public class VisualEffects {
   private final WormholeConfigFields wormholeConfigFields;
   private final List<Vector> cachedLocations = new ArrayList<>();
   private final List<List<Vector>> cachedRotations = new ArrayList<>();

   public VisualEffects(WormholeConfigFields wormholeConfigFields) {
      this.wormholeConfigFields = wormholeConfigFields;
      this.initializeVisualEffect();
   }

   private void initializeVisualEffect() {
      switch (this.wormholeConfigFields.getStyle()) {
         case CUBE:
            this.generateCube();
            break;
         case CRYSTAL:
            this.generateCrystal();
            break;
         case ICOSAHEDRON:
            this.generateIcosahedron();
            break;
         default:
            Logger.warn("Missing com.magmaguy.elitemobs.wormhole style for " + this.wormholeConfigFields.getStyle());
      }
   }

   private void generateCrystal() {
      Vector top = new Vector(0, 1, 0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      this.cachedLocations.add(top);
      Vector bottom = new Vector(0, -1, 0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      this.cachedLocations.add(bottom);
      Vector front = new Vector(0.5, 0.0, 0.0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      this.cachedLocations.add(front);
      Vector side = new Vector(0.0, 0.0, 0.5).multiply(this.wormholeConfigFields.getSizeMultiplier());
      this.cachedLocations.add(side);
      this.finishCuboidInitialization(top, bottom, front, side);
   }

   private void generateCube() {
      Vector top = new Vector(0, 1, 0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      this.cachedLocations.add(top);
      Vector bottom = new Vector(0, -1, 0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      this.cachedLocations.add(bottom);
      Vector front = new Vector(1, 0, 0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      this.cachedLocations.add(front);
      Vector side = new Vector(0, 0, 1).multiply(this.wormholeConfigFields.getSizeMultiplier());
      this.cachedLocations.add(side);
      this.finishCuboidInitialization(top, bottom, front, side);
   }

   private void generateIcosahedron() {
      Vector top = new Vector(0, 1, 0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      this.cachedLocations.add(top);
      Vector offsetTopPoint = new Vector(1.0, 0.5, 0.0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      List<Vector> topPentagon = this.getPentagonVectors(top, offsetTopPoint);
      Vector bottom = new Vector(0, -1, 0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      Vector offsetBottomPoint = new Vector(1.0, -0.5, 0.0).multiply(this.wormholeConfigFields.getSizeMultiplier());
      offsetBottomPoint.rotateAroundY(Math.PI / 5);
      List<Vector> bottomPentagon = this.getPentagonVectors(bottom, offsetBottomPoint);

      for (int i = 0; i < topPentagon.size(); i++) {
         this.trace(topPentagon.get(i), bottomPentagon.get(i));
         if (i + 1 < topPentagon.size() - 1) {
            this.trace(topPentagon.get(i + 1), bottomPentagon.get(i));
         }
      }

      this.trace(topPentagon.get(0), bottomPentagon.get(4));
      this.cacheRotations();
   }

   private List<Vector> getPentagonVectors(Vector centerPosition, Vector offsetPosition) {
      List<Vector> pentagonVectors = new ArrayList<>();

      for (int i = 0; i < 6; i++) {
         Vector rotatedVector = offsetPosition.clone().rotateAroundY((Math.PI * 2.0 / 5.0) * i).clone();
         if (i > 0) {
            this.trace(pentagonVectors.get(i - 1), rotatedVector);
         }

         this.trace(centerPosition, rotatedVector);
         pentagonVectors.add(rotatedVector);
      }

      this.trace(pentagonVectors.get(0), pentagonVectors.get(pentagonVectors.size() - 1));
      return pentagonVectors;
   }

   private void finishCuboidInitialization(Vector top, Vector bottom, Vector front, Vector side) {
      this.trace(top, front);
      this.trace(bottom, front);
      this.trace(front, side);
      List<Vector> otherSidesList = new ArrayList<>();

      for (int i = 1; i < 4; i++) {
         double rotation = (Math.PI / 2) * i;

         for (Vector vector : this.cachedLocations) {
            Vector rotatedVector = vector.clone().rotateAroundY(rotation);
            if (rotatedVector.getX() != vector.getX() || rotatedVector.getZ() != vector.getZ()) {
               otherSidesList.add(rotatedVector);
            }
         }
      }

      this.cachedLocations.addAll(otherSidesList);
      this.cacheRotations();
   }

   private void cacheRotations() {
      double rotationPointCount = 100.0;

      for (int i = 0; i < rotationPointCount; i++) {
         double rotation = (Math.PI * 2) / rotationPointCount * i;
         List<Vector> vectors = new ArrayList<>();

         for (Vector vector : this.cachedLocations) {
            vectors.add(vector.clone().rotateAroundY(rotation));
         }

         this.cachedRotations.add(vectors);
      }
   }

   private void trace(Vector source, Vector target) {
      Vector ray = target.clone().subtract(source).normalize().multiply(0.2);
      Vector clonedSource = source.clone();

      for (int counter = 0; clonedSource.distance(target) > 0.1 && counter < 20; counter++) {
         if (WormholesConfig.isReducedParticlesMode()) {
            if (counter % 2 == 0) {
               clonedSource = clonedSource.add(ray);
               this.cachedLocations.add(clonedSource.clone());
            }
         } else {
            clonedSource = clonedSource.add(ray);
            this.cachedLocations.add(clonedSource.clone());
         }
      }
   }

   @Generated
   public List<List<Vector>> getCachedRotations() {
      return this.cachedRotations;
   }
}
