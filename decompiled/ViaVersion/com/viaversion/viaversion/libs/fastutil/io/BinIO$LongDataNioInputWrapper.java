package com.viaversion.viaversion.libs.fastutil.io;

import com.viaversion.viaversion.libs.fastutil.longs.LongIterator;
import com.viaversion.viaversion.libs.fastutil.longs.LongMappedBigList;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.NoSuchElementException;

final class BinIO$LongDataNioInputWrapper implements LongIterator {
   private final ReadableByteChannel channel;
   private final ByteBuffer byteBuffer;
   private final LongBuffer buffer;

   public BinIO$LongDataNioInputWrapper(ReadableByteChannel channel, ByteOrder byteOrder) {
      this.channel = channel;
      this.byteBuffer = ByteBuffer.allocateDirect(BinIO.BUFFER_SIZE).order(byteOrder);
      this.buffer = this.byteBuffer.asLongBuffer();
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
         ((Buffer)this.buffer).limit(this.byteBuffer.limit() >>> LongMappedBigList.LOG2_BYTES);
      }

      return this.buffer.hasRemaining();
   }

   @Override
   public long nextLong() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         return this.buffer.get();
      }
   }
}
