package com.viaversion.viaversion.libs.fastutil.io;

import com.viaversion.viaversion.libs.fastutil.ints.IntIterator;
import com.viaversion.viaversion.libs.fastutil.ints.IntMappedBigList;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.NoSuchElementException;

final class BinIO$IntDataNioInputWrapper implements IntIterator {
   private final ReadableByteChannel channel;
   private final ByteBuffer byteBuffer;
   private final IntBuffer buffer;

   public BinIO$IntDataNioInputWrapper(ReadableByteChannel channel, ByteOrder byteOrder) {
      this.channel = channel;
      this.byteBuffer = ByteBuffer.allocateDirect(BinIO.BUFFER_SIZE).order(byteOrder);
      this.buffer = this.byteBuffer.asIntBuffer();
      ((Buffer)this.buffer).clear().flip();
   }

   @Override
   public boolean hasNext() {
      if (!this.buffer.hasRemaining()) {
         ((Buffer)this.byteBuffer).clear();

         try {
            this.channel.read(this.byteBuffer);
         } catch (IOException var2) {
            throw new RuntimeException(var2);
         }

         ((Buffer)this.byteBuffer).flip();
         ((Buffer)this.buffer).clear();
         ((Buffer)this.buffer).limit(this.byteBuffer.limit() >>> IntMappedBigList.LOG2_BYTES);
      }

      return this.buffer.hasRemaining();
   }

   @Override
   public int nextInt() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         return this.buffer.get();
      }
   }
}
