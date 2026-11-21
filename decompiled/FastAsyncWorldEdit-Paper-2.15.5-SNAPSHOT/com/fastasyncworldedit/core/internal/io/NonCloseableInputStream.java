package com.fastasyncworldedit.core.internal.io;

import java.io.IOException;
import java.io.InputStream;

public class NonCloseableInputStream extends InputStream {
   private final InputStream parent;

   public NonCloseableInputStream(InputStream parent) {
      this.parent = parent;
   }

   @Override
   public int read() throws IOException {
      return this.parent.read();
   }

   @Override
   public int read(byte[] b) throws IOException {
      return this.parent.read(b);
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException {
      return this.parent.read(b, off, len);
   }

   @Override
   public long skip(long n) throws IOException {
      return this.parent.skip(n);
   }

   @Override
   public int available() throws IOException {
      return this.parent.available();
   }

   @Override
   public void close() throws IOException {
   }

   @Override
   public void mark(int readlimit) {
      this.parent.mark(readlimit);
   }

   @Override
   public void reset() throws IOException {
      this.parent.reset();
   }

   @Override
   public boolean markSupported() {
      return this.parent.markSupported();
   }
}
