package com.fastasyncworldedit.core.internal.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

public class ByteBufferInputStream extends InputStream {
   ByteBuffer buf;

   public ByteBufferInputStream(ByteBuffer buf) {
      this.buf = buf;
   }

   @Override
   public int read() throws IOException {
      return !this.buf.hasRemaining() ? -1 : this.buf.get() & 0xFF;
   }

   @Override
   public int read(@Nonnull byte[] bytes, int off, int len) throws IOException {
      if (!this.buf.hasRemaining()) {
         return -1;
      } else {
         len = Math.min(len, this.buf.remaining());
         this.buf.get(bytes, off, len);
         return len;
      }
   }
}
