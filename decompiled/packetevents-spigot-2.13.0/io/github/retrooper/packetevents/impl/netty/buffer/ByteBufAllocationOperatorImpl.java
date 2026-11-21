package io.github.retrooper.packetevents.impl.netty.buffer;

import com.github.retrooper.packetevents.netty.buffer.ByteBufAllocationOperator;
import io.netty.buffer.Unpooled;

public class ByteBufAllocationOperatorImpl implements ByteBufAllocationOperator {
   @Override
   public Object wrappedBuffer(byte[] bytes) {
      return Unpooled.wrappedBuffer(bytes);
   }

   @Override
   public Object copiedBuffer(byte[] bytes) {
      return Unpooled.copiedBuffer(bytes);
   }

   @Override
   public Object buffer() {
      return Unpooled.buffer();
   }

   @Override
   public Object buffer(int initialCapacity) {
      return Unpooled.buffer(initialCapacity);
   }

   @Override
   public Object directBuffer() {
      return Unpooled.directBuffer();
   }

   @Override
   public Object directBuffer(int initialCapacity) {
      return Unpooled.directBuffer(initialCapacity);
   }

   @Override
   public Object compositeBuffer() {
      return Unpooled.compositeBuffer();
   }

   @Override
   public Object compositeBuffer(int maxNumComponents) {
      return Unpooled.compositeBuffer(maxNumComponents);
   }

   @Override
   public Object emptyBuffer() {
      return Unpooled.EMPTY_BUFFER;
   }
}
