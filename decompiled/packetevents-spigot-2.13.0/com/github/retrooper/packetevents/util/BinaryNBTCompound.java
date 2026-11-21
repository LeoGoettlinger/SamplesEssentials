package com.github.retrooper.packetevents.util;

import java.util.Arrays;

public class BinaryNBTCompound {
   private final byte[] data;
   private final int hash;

   public BinaryNBTCompound(byte[] data) {
      this.data = data;
      if (data[0] == 10 && data[data.length - 1] == 0) {
         this.hash = this.calculateHash(data);
      } else {
         throw new IllegalArgumentException("Data is not a valid NBT compound");
      }
   }

   public byte[] getData() {
      return this.data;
   }

   private int calculateHash(byte[] data) {
      return Arrays.hashCode(data);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof BinaryNBTCompound) {
         BinaryNBTCompound other = (BinaryNBTCompound)obj;
         byte[] otherData = other.data;
         if (otherData.length != this.data.length) {
            return false;
         } else {
            for (int i = 1; i < this.data.length - 1; i++) {
               if (this.data[i] != otherData[i]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public String toString() {
      return Arrays.toString(this.data) + " hash: " + this.hash;
   }
}
