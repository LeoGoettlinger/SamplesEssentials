package com.fastasyncworldedit.core.internal.io;

import java.io.IOException;
import java.io.OutputStream;

public class AbstractDelegateOutputStream extends OutputStream {
   private final OutputStream parent;

   @Override
   public void write(int b) throws IOException {
      this.parent.write(b);
   }

   @Override
   public void write(byte[] b) throws IOException {
      this.parent.write(b);
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      this.parent.write(b, off, len);
   }

   @Override
   public void flush() throws IOException {
      this.parent.flush();
   }

   @Override
   public void close() throws IOException {
      this.parent.close();
   }

   public AbstractDelegateOutputStream(OutputStream os) {
      this.parent = os;
   }
}
