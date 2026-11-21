package github.nighter.smartspawner.libs.mariadb.client.socket.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReadAheadBufferedStream extends FilterInputStream {
   private static final int BUF_SIZE = 16384;
   private final byte[] buf = new byte[16384];
   private int end;
   private int pos;

   public ReadAheadBufferedStream(InputStream in) {
      super(in);
   }

   @Override
   public int read(byte[] externalBuf, int off, int len) throws IOException {
      if (len == 0) {
         return 0;
      } else {
         int totalReads = 0;

         do {
            if (this.pos >= this.end) {
               if (len - totalReads >= this.buf.length) {
                  int reads = super.read(externalBuf, off + totalReads, len - totalReads);
                  if (reads <= 0) {
                     return totalReads == 0 ? -1 : totalReads;
                  }

                  return totalReads + reads;
               }

               this.fillingBuffer(len - totalReads);
               if (this.end <= 0) {
                  return totalReads == 0 ? -1 : totalReads;
               }
            }

            int copyLength = Math.min(len - totalReads, this.end - this.pos);
            System.arraycopy(this.buf, this.pos, externalBuf, off + totalReads, copyLength);
            this.pos += copyLength;
            totalReads += copyLength;
         } while (totalReads < len && super.available() > 0);

         return totalReads;
      }
   }

   private void fillingBuffer(int minNeededBytes) throws IOException {
      int lengthToReallyRead = Math.min(16384, Math.max(super.available(), minNeededBytes));
      this.end = super.read(this.buf, 0, lengthToReallyRead);
      this.pos = 0;
   }

   @Override
   public boolean markSupported() {
      return false;
   }

   @Override
   public void close() throws IOException {
      super.close();
   }

   @Override
   public int available() throws IOException {
      return this.end - this.pos + super.available();
   }

   @Override
   public int read() throws IOException {
      throw new IOException("read() from socket not implemented");
   }

   @Override
   public long skip(long n) throws IOException {
      throw new IOException("Skip from socket not implemented");
   }

   @Override
   public void reset() throws IOException {
      throw new IOException("reset from socket not implemented");
   }
}
