package com.fastasyncworldedit.core.internal.io;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ResettableFileInputStream extends FilterInputStream {
   private final FileChannel myFileChannel;
   private long mark = 0L;

   public ResettableFileInputStream(FileInputStream fis) {
      super(fis);
      this.myFileChannel = fis.getChannel();
   }

   @Override
   public boolean markSupported() {
      return true;
   }

   @Override
   public synchronized void mark(int readlimit) {
      try {
         this.mark = this.myFileChannel.position();
      } catch (IOException var3) {
         var3.printStackTrace();
         this.mark = -1L;
      }
   }

   @Override
   public synchronized void reset() throws IOException {
      if (this.mark == -1L) {
         throw new IOException("not marked");
      } else {
         this.myFileChannel.position(this.mark);
      }
   }
}
