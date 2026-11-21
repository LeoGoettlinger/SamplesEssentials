package com.github.retrooper.packetevents.protocol.nbt;

import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.NullMarked;

@NullMarked
@NonExtendable
public interface NBTLimiter {
   int DEFAULT_MAX_SIZE = Integer.getInteger("packetevents.nbt.default-max-size", 2097152);
   int DEFAULT_MAX_DEPTH = Integer.getInteger("packetevents.nbt.default-max-depth", 512);

   static NBTLimiter noop() {
      return new NBTLimiter() {
         @Override
         public void increment(int amount) {
         }

         @Override
         public void checkReadability(int length) {
         }

         @Override
         public void enterDepth() {
         }

         @Override
         public void exitDepth() {
         }
      };
   }

   static NBTLimiter forBuffer(Object byteBuf) {
      return forBuffer(byteBuf, DEFAULT_MAX_SIZE);
   }

   static NBTLimiter forBuffer(Object byteBuf, int maxBytes) {
      return forBuffer(byteBuf, maxBytes, DEFAULT_MAX_DEPTH);
   }

   static NBTLimiter forBuffer(Object byteBuf, int maxBytes, int maxDepth) {
      return new NBTLimiter() {
         private int bytes;
         private int depth;

         @Override
         public void increment(int amount) {
            if (amount < 0) {
               throw new IllegalArgumentException("Can't increment NBT limiter by negative amount: " + amount);
            } else if (this.bytes + amount > maxBytes) {
               throw new IllegalArgumentException("NBT size limit reached (" + this.bytes + " + " + amount + " > " + maxBytes + ")");
            } else {
               this.bytes += amount;
            }
         }

         @Override
         public void checkReadability(int length) {
            int readableBytes = ByteBufHelper.readableBytes(byteBuf);
            if (length > readableBytes) {
               throw new IllegalArgumentException("Can't read more than possible: " + length + " > " + readableBytes);
            }
         }

         @Override
         public void enterDepth() {
            if (this.depth >= maxDepth) {
               throw new IllegalArgumentException("NBT depth limit reached (" + this.depth + "/" + maxDepth + ")");
            } else {
               this.depth++;
            }
         }

         @Override
         public void exitDepth() {
            if (this.depth <= 0) {
               throw new IllegalArgumentException("Can't exit top-level depth");
            } else {
               this.depth--;
            }
         }
      };
   }

   void increment(int amount);

   void checkReadability(int length);

   void enterDepth();

   void exitDepth();
}
