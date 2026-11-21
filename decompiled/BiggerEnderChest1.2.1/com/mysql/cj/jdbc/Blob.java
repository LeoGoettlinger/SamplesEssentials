package com.mysql.cj.jdbc;

import com.mysql.cj.Constants;
import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import com.mysql.cj.jdbc.result.ResultSetInternalMethods;
import com.mysql.cj.protocol.OutputStreamWatcher;
import com.mysql.cj.protocol.WatchableOutputStream;
import com.mysql.cj.protocol.WatchableStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Blob implements java.sql.Blob, OutputStreamWatcher {
   private byte[] binaryData = null;
   private boolean isClosed = false;
   private ExceptionInterceptor exceptionInterceptor;
   private final Lock lock = new ReentrantLock();

   Blob(ExceptionInterceptor exceptionInterceptor) {
      this.setBinaryData(Constants.EMPTY_BYTE_ARRAY);
      this.exceptionInterceptor = exceptionInterceptor;
   }

   public Blob(byte[] data, ExceptionInterceptor exceptionInterceptor) {
      this.setBinaryData(data);
      this.exceptionInterceptor = exceptionInterceptor;
   }

   Blob(byte[] data, ResultSetInternalMethods creatorResultSetToSet, int columnIndexToSet) {
      this.setBinaryData(data);
   }

   private byte[] getBinaryData() {
      this.lock.lock();

      byte[] var1;
      try {
         var1 = this.binaryData;
      } finally {
         this.lock.unlock();
      }

      return var1;
   }

   @Override
   public InputStream getBinaryStream() throws SQLException {
      try {
         this.lock.lock();

         ByteArrayInputStream var1;
         try {
            this.checkClosed();
            var1 = new ByteArrayInputStream(this.getBinaryData());
         } finally {
            this.lock.unlock();
         }

         return var1;
      } catch (CJException var7) {
         throw SQLExceptionsMapping.translateException(var7, this.exceptionInterceptor);
      }
   }

   @Override
   public byte[] getBytes(long pos, int length) throws SQLException {
      try {
         this.lock.lock();

         byte[] var5;
         try {
            this.checkClosed();
            if (pos < 1L) {
               throw SQLError.createSQLException(Messages.getString("Blob.2"), "S1009", this.exceptionInterceptor);
            }

            if (--pos > this.binaryData.length) {
               throw SQLError.createSQLException(Messages.getString("Blob.3"), "S1009", this.exceptionInterceptor);
            }

            if (pos + length > this.binaryData.length) {
               throw SQLError.createSQLException(Messages.getString("Blob.4"), "S1009", this.exceptionInterceptor);
            }

            byte[] newData = new byte[length];
            System.arraycopy(this.getBinaryData(), (int)pos, newData, 0, length);
            var5 = newData;
         } finally {
            this.lock.unlock();
         }

         return var5;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.exceptionInterceptor);
      }
   }

   @Override
   public long length() throws SQLException {
      try {
         this.lock.lock();

         long var1;
         try {
            this.checkClosed();
            var1 = this.getBinaryData().length;
         } finally {
            this.lock.unlock();
         }

         return var1;
      } catch (CJException var8) {
         throw SQLExceptionsMapping.translateException(var8, this.exceptionInterceptor);
      }
   }

   @Override
   public long position(byte[] pattern, long start) throws SQLException {
      try {
         this.lock.lock();

         try {
            throw SQLError.createSQLFeatureNotSupportedException();
         } finally {
            this.lock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.exceptionInterceptor);
      }
   }

   @Override
   public long position(java.sql.Blob pattern, long start) throws SQLException {
      try {
         this.lock.lock();

         long var4;
         try {
            this.checkClosed();
            var4 = this.position(pattern.getBytes(0L, (int)pattern.length()), start);
         } finally {
            this.lock.unlock();
         }

         return var4;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.exceptionInterceptor);
      }
   }

   private void setBinaryData(byte[] newBinaryData) {
      this.lock.lock();

      try {
         this.binaryData = newBinaryData;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public OutputStream setBinaryStream(long indexToWriteAt) throws SQLException {
      try {
         this.lock.lock();

         WatchableOutputStream var4;
         try {
            this.checkClosed();
            if (indexToWriteAt < 1L) {
               throw SQLError.createSQLException(Messages.getString("Blob.0"), "S1009", this.exceptionInterceptor);
            }

            WatchableOutputStream bytesOut = new WatchableOutputStream();
            bytesOut.setWatcher(this);
            if (indexToWriteAt > 0L) {
               bytesOut.write(this.binaryData, 0, (int)(indexToWriteAt - 1L));
            }

            var4 = bytesOut;
         } finally {
            this.lock.unlock();
         }

         return var4;
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.exceptionInterceptor);
      }
   }

   @Override
   public int setBytes(long writeAt, byte[] bytes) throws SQLException {
      try {
         this.lock.lock();

         int var4;
         try {
            this.checkClosed();
            var4 = this.setBytes(writeAt, bytes, 0, bytes.length);
         } finally {
            this.lock.unlock();
         }

         return var4;
      } catch (CJException var10) {
         throw SQLExceptionsMapping.translateException(var10, this.exceptionInterceptor);
      }
   }

   @Override
   public int setBytes(long writeAt, byte[] bytes, int offset, int length) throws SQLException {
      try {
         this.lock.lock();

         int ioEx;
         try {
            this.checkClosed();
            OutputStream bytesOut = this.setBinaryStream(writeAt);

            try {
               bytesOut.write(bytes, offset, length);
            } catch (IOException var26) {
               SQLException sqlEx = SQLError.createSQLException(Messages.getString("Blob.1"), "S1000", this.exceptionInterceptor);
               sqlEx.initCause(var26);
               throw sqlEx;
            } finally {
               try {
                  bytesOut.close();
               } catch (IOException var25) {
               }
            }

            ioEx = length;
         } finally {
            this.lock.unlock();
         }

         return ioEx;
      } catch (CJException var29) {
         throw SQLExceptionsMapping.translateException(var29, this.exceptionInterceptor);
      }
   }

   public void streamClosed(byte[] byteData) {
      this.lock.lock();

      try {
         this.binaryData = byteData;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public void streamClosed(WatchableStream out) {
      this.lock.lock();

      try {
         int streamSize = out.size();
         if (streamSize < this.binaryData.length) {
            out.write(this.binaryData, streamSize, this.binaryData.length - streamSize);
         }

         this.binaryData = out.toByteArray();
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public void truncate(long len) throws SQLException {
      try {
         this.lock.lock();

         try {
            this.checkClosed();
            if (len < 0L) {
               throw SQLError.createSQLException(Messages.getString("Blob.5"), "S1009", this.exceptionInterceptor);
            }

            if (len > this.binaryData.length) {
               throw SQLError.createSQLException(Messages.getString("Blob.6"), "S1009", this.exceptionInterceptor);
            }

            byte[] newData = new byte[(int)len];
            System.arraycopy(this.getBinaryData(), 0, newData, 0, (int)len);
            this.binaryData = newData;
         } finally {
            this.lock.unlock();
         }
      } catch (CJException var9) {
         throw SQLExceptionsMapping.translateException(var9, this.exceptionInterceptor);
      }
   }

   @Override
   public void free() throws SQLException {
      try {
         this.lock.lock();

         try {
            this.binaryData = null;
            this.isClosed = true;
         } finally {
            this.lock.unlock();
         }
      } catch (CJException var6) {
         throw SQLExceptionsMapping.translateException(var6, this.exceptionInterceptor);
      }
   }

   @Override
   public InputStream getBinaryStream(long pos, long length) throws SQLException {
      try {
         this.lock.lock();

         ByteArrayInputStream var5;
         try {
            this.checkClosed();
            if (pos < 1L) {
               throw SQLError.createSQLException(Messages.getString("Blob.2"), "S1009", this.exceptionInterceptor);
            }

            if (--pos > this.binaryData.length) {
               throw SQLError.createSQLException(Messages.getString("Blob.6"), "S1009", this.exceptionInterceptor);
            }

            if (pos + length > this.binaryData.length) {
               throw SQLError.createSQLException(Messages.getString("Blob.4"), "S1009", this.exceptionInterceptor);
            }

            var5 = new ByteArrayInputStream(this.getBinaryData(), (int)pos, (int)length);
         } finally {
            this.lock.unlock();
         }

         return var5;
      } catch (CJException var11) {
         throw SQLExceptionsMapping.translateException(var11, this.exceptionInterceptor);
      }
   }

   private void checkClosed() throws SQLException {
      this.lock.lock();

      try {
         if (this.isClosed) {
            throw SQLError.createSQLException(Messages.getString("Blob.7"), "S1009", this.exceptionInterceptor);
         }
      } finally {
         this.lock.unlock();
      }
   }
}
