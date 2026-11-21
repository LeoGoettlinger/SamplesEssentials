package org.postgresql.largeobject;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.jdbc.ResourceLock;
import org.postgresql.util.GT;

public class BlobInputStream extends InputStream {
   static final int DEFAULT_MAX_BUFFER_SIZE = 524288;
   static final int INITIAL_BUFFER_SIZE = 65536;
   @Nullable
   private LargeObject lo;
   private final ResourceLock lock = new ResourceLock();
   private long absolutePosition;
   @Nullable
   private byte[] buffer;
   private int bufferPosition;
   private int lastBufferSize;
   private final int maxBufferSize;
   private long markPosition;
   private final long limit;

   public BlobInputStream(LargeObject lo) {
      this(lo, 524288);
   }

   public BlobInputStream(LargeObject lo, int bsize) {
      this(lo, bsize, Long.MAX_VALUE);
   }

   public BlobInputStream(LargeObject lo, int bsize, long limit) {
      this.lo = lo;
      this.maxBufferSize = bsize;
      this.lastBufferSize = 32768;
      this.limit = limit == -1L ? Long.MAX_VALUE : limit;
   }

   @Override
   public int read() throws IOException {
      try {
         ResourceLock ignore = this.lock.obtain();

         int nextBufferSize;
         label78: {
            int var11;
            label79: {
               try {
                  LargeObject lo = this.getLo();
                  if (this.absolutePosition >= this.limit) {
                     this.buffer = null;
                     this.bufferPosition = 0;
                     nextBufferSize = -1;
                     break label78;
                  }

                  if (this.buffer == null || this.bufferPosition >= this.buffer.length) {
                     nextBufferSize = this.getNextBufferSize(1);
                     this.buffer = lo.read(nextBufferSize);
                     this.bufferPosition = 0;
                     if (this.buffer.length == 0) {
                        var11 = -1;
                        break label79;
                     }
                  }

                  nextBufferSize = this.buffer[this.bufferPosition] & 255;
                  this.bufferPosition++;
                  this.absolutePosition++;
                  if (this.bufferPosition >= this.buffer.length) {
                     this.buffer = null;
                     this.bufferPosition = 0;
                  }

                  var11 = nextBufferSize;
               } catch (Throwable var6) {
                  if (ignore != null) {
                     try {
                        ignore.close();
                     } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                     }
                  }

                  throw var6;
               }

               if (ignore != null) {
                  ignore.close();
               }

               return var11;
            }

            if (ignore != null) {
               ignore.close();
            }

            return var11;
         }

         if (ignore != null) {
            ignore.close();
         }

         return nextBufferSize;
      } catch (SQLException var7) {
         long loId = this.lo == null ? -1L : this.lo.getLongOID();
         throw new IOException(
            GT.tr("Can not read data from large object {0}, position: {1}, buffer size: {2}", loId, this.absolutePosition, this.lastBufferSize), var7
         );
      }
   }

   private int getNextBufferSize(int len) {
      int nextBufferSize = Math.min(this.maxBufferSize, this.lastBufferSize * 2);
      if (len > nextBufferSize) {
         nextBufferSize = Math.min(this.maxBufferSize, Integer.highestOneBit(len * 2));
      }

      this.lastBufferSize = nextBufferSize;
      return nextBufferSize;
   }

   @Override
   public int read(byte[] dest, int off, int len) throws IOException {
      if (len == 0) {
         return 0;
      } else {
         ResourceLock ignore = this.lock.obtain();

         int bytesInBuffer;
         label78: {
            try {
               int bytesCopied = 0;
               LargeObject lo = this.getLo();
               if (this.absolutePosition >= this.limit) {
                  bytesInBuffer = -1;
                  break label78;
               }

               len = Math.min(len, (int)Math.min(this.limit - this.absolutePosition, 2147483647L));
               if (this.buffer != null) {
                  bytesInBuffer = this.buffer.length - this.bufferPosition;
                  int bytesToCopy = Math.min(len, bytesInBuffer);
                  System.arraycopy(this.buffer, this.bufferPosition, dest, off, bytesToCopy);
                  this.bufferPosition += bytesToCopy;
                  if (this.bufferPosition >= this.buffer.length) {
                     this.buffer = null;
                     this.bufferPosition = 0;
                  }

                  this.absolutePosition += bytesToCopy;
                  off += bytesToCopy;
                  len -= bytesToCopy;
                  bytesCopied = bytesToCopy;
               }

               if (len > 0) {
                  bytesInBuffer = this.getNextBufferSize(len);
                  this.buffer = null;
                  this.bufferPosition = 0;

                  int bytesRead;
                  try {
                     if (len >= bytesInBuffer) {
                        bytesRead = lo.read(dest, off, len);
                     } else {
                        this.buffer = lo.read(bytesInBuffer);
                        bytesRead = Math.min(len, this.buffer.length);
                        System.arraycopy(this.buffer, 0, dest, off, bytesRead);
                        if (bytesRead == this.buffer.length) {
                           this.buffer = null;
                           this.bufferPosition = 0;
                        } else {
                           this.bufferPosition = bytesRead;
                        }
                     }
                  } catch (SQLException var11) {
                     throw new IOException(
                        GT.tr("Can not read data from large object {0}, position: {1}, buffer size: {2}", lo.getLongOID(), this.absolutePosition, len), var11
                     );
                  }

                  bytesCopied += bytesRead;
                  this.absolutePosition += bytesRead;
               }

               bytesInBuffer = bytesCopied == 0 ? -1 : bytesCopied;
            } catch (Throwable var12) {
               if (ignore != null) {
                  try {
                     ignore.close();
                  } catch (Throwable var10) {
                     var12.addSuppressed(var10);
                  }
               }

               throw var12;
            }

            if (ignore != null) {
               ignore.close();
            }

            return bytesInBuffer;
         }

         if (ignore != null) {
            ignore.close();
         }

         return bytesInBuffer;
      }
   }

   @Override
   public void close() throws IOException {
      long loId = 0L;

      try {
         ResourceLock ignore = this.lock.obtain();

         try {
            LargeObject lo = this.lo;
            if (lo != null) {
               loId = lo.getLongOID();
               lo.close();
            }

            this.lo = null;
         } catch (Throwable var7) {
            if (ignore != null) {
               try {
                  ignore.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (ignore != null) {
            ignore.close();
         }
      } catch (SQLException var8) {
         throw new IOException(GT.tr("Can not close large object {0}", loId), var8);
      }
   }

   @Override
   public void mark(int readlimit) {
      ResourceLock ignore = this.lock.obtain();

      try {
         this.markPosition = this.absolutePosition;
      } catch (Throwable var6) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   @Override
   public void reset() throws IOException {
      ResourceLock ignore = this.lock.obtain();

      try {
         LargeObject lo = this.getLo();
         long loId = lo.getLongOID();

         try {
            if (this.markPosition <= 2147483647L) {
               lo.seek((int)this.markPosition);
            } else {
               lo.seek64(this.markPosition, 0);
            }

            this.buffer = null;
            this.absolutePosition = this.markPosition;
         } catch (SQLException var7) {
            throw new IOException(GT.tr("Can not reset stream for large object {0} to position {1}", loId, this.markPosition), var7);
         }
      } catch (Throwable var8) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var6) {
               var8.addSuppressed(var6);
            }
         }

         throw var8;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   @Override
   public boolean markSupported() {
      return true;
   }

   private LargeObject getLo() throws IOException {
      if (this.lo == null) {
         throw new IOException("BlobOutputStream is closed");
      } else {
         return this.lo;
      }
   }
}
