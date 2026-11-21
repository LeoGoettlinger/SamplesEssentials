package com.viaversion.viabackwards.protocol.v1_11to1_10.storage;

import com.viaversion.viaversion.api.connection.StorableObject;

public final class ChestedHorseStorage implements StorableObject {
   private boolean chested;
   private int liamaStrength;
   private int liamaCarpetColor = -1;
   private int liamaVariant;

   public boolean isChested() {
      return this.chested;
   }

   public void setChested(boolean chested) {
      this.chested = chested;
   }

   public int getLiamaStrength() {
      return this.liamaStrength;
   }

   public void setLiamaStrength(int liamaStrength) {
      this.liamaStrength = liamaStrength;
   }

   public int getLiamaCarpetColor() {
      return this.liamaCarpetColor;
   }

   public void setLiamaCarpetColor(int liamaCarpetColor) {
      this.liamaCarpetColor = liamaCarpetColor;
   }

   public int getLiamaVariant() {
      return this.liamaVariant;
   }

   public void setLiamaVariant(int liamaVariant) {
      this.liamaVariant = liamaVariant;
   }

   @Override
   public String toString() {
      return "ChestedHorseStorage{chested="
         + this.chested
         + ", liamaStrength="
         + this.liamaStrength
         + ", liamaCarpetColor="
         + this.liamaCarpetColor
         + ", liamaVariant="
         + this.liamaVariant
         + "}";
   }
}
