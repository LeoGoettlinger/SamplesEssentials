package org.postgresql.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

class ByteBuffersByteStreamWriter implements ByteStreamWriter {
   private final ByteBuffer[] buffers;
   private final int length;

   ByteBuffersByteStreamWriter(ByteBuffer... buffers) {
      this.buffers = buffers;
      int length = 0;

      for (ByteBuffer buffer : buffers) {
         length += buffer.remaining();
      }

      this.length = length;
   }

   @Override
   public int getLength() {
      return this.length;
   }

   @Override
   public void writeTo(ByteStreamWriter.ByteStreamTarget target) throws IOException {
      boolean allArraysAreAccessible = true;

      for (ByteBuffer buffer : this.buffers) {
         if (!buffer.hasArray()) {
            allArraysAreAccessible = false;
            break;
         }
      }

      OutputStream os = target.getOutputStream();
      if (allArraysAreAccessible) {
         for (ByteBuffer bufferx : this.buffers) {
            os.write(bufferx.array(), bufferx.arrayOffset() + bufferx.position(), bufferx.remaining());
         }
      } else {
         WritableByteChannel c = Channels.newChannel(os);

         try {
            for (ByteBuffer bufferx : this.buffers) {
               if (bufferx.hasArray()) {
                  os.write(bufferx.array(), bufferx.arrayOffset() + bufferx.position(), bufferx.remaining());
               } else {
                  c.write(bufferx);
               }
            }
         } catch (Throwable var10) {
            if (c != null) {
               try {
                  c.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (c != null) {
            c.close();
         }
      }
   }
}
