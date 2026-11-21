package com.fastasyncworldedit.core.internal.io.parallelgzip;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class ParallelGZIPOutputStream extends FilterOutputStream {
   private static final int GZIP_MAGIC = 35615;
   private static final int SIZE = 65536;
   private static final ThreadLocal<ParallelGZIPOutputStream.State> STATE = new ThreadLocal<ParallelGZIPOutputStream.State>() {
      protected ParallelGZIPOutputStream.State initialValue() {
         return new ParallelGZIPOutputStream.State();
      }
   };
   private final ExecutorService executor;
   private final CRC32 crc = new CRC32();
   private final int emitQueueSize;
   private final BlockingQueue<Future<ParallelGZIPOutputStream.Block>> emitQueue;
   @Nonnull
   private ParallelGZIPOutputStream.Block block = new ParallelGZIPOutputStream.Block();
   @CheckForNull
   private ParallelGZIPOutputStream.Block freeBlock = null;
   private long bytesWritten = 0L;

   @Nonnull
   private static Deflater newDeflater() {
      return new Deflater(-1, true);
   }

   @Nonnull
   private static DeflaterOutputStream newDeflaterOutputStream(@Nonnull OutputStream out, @Nonnull Deflater deflater) {
      return new DeflaterOutputStream(out, deflater, 512, true);
   }

   @Nonnegative
   private static int getThreadCount(@Nonnull ExecutorService executor) {
      return executor instanceof ThreadPoolExecutor ? ((ThreadPoolExecutor)executor).getMaximumPoolSize() : Runtime.getRuntime().availableProcessors();
   }

   @Deprecated
   public ParallelGZIPOutputStream(@Nonnull OutputStream out, @Nonnull ExecutorService executor, @Nonnegative int nthreads) throws IOException {
      super(out);
      this.executor = executor;
      this.emitQueueSize = nthreads * 3;
      this.emitQueue = new ArrayBlockingQueue<>(this.emitQueueSize);
      this.writeHeader();
   }

   @Deprecated
   public ParallelGZIPOutputStream(@Nonnull OutputStream out, @Nonnegative int nthreads) throws IOException {
      this(out, ParallelGZIPEnvironment.getSharedThreadPool(), nthreads);
   }

   public ParallelGZIPOutputStream(@Nonnull OutputStream out, @Nonnull ExecutorService executor) throws IOException {
      this(out, executor, getThreadCount(executor));
   }

   public ParallelGZIPOutputStream(@Nonnull OutputStream out) throws IOException {
      this(out, Runtime.getRuntime().availableProcessors());
   }

   private void writeHeader() throws IOException {
      this.out.write(new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, 3});
   }

   @Override
   public void write(int b) throws IOException {
      byte[] single = new byte[]{(byte)(b & 0xFF)};
      this.write(single);
   }

   @Override
   public void write(byte[] b) throws IOException {
      this.write(b, 0, b.length);
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      this.crc.update(b, off, len);
      this.bytesWritten += len;

      while (len > 0) {
         byte[] blockBuf = this.block.buf;
         int capacity = 65536 - this.block.buf_length;
         if (len < capacity) {
            System.arraycopy(b, off, blockBuf, this.block.buf_length, len);
            ParallelGZIPOutputStream.Block var7 = this.block;
            var7.buf_length = var7.buf_length + len;
            break;
         }

         System.arraycopy(b, off, blockBuf, this.block.buf_length, capacity);
         ParallelGZIPOutputStream.Block var6 = this.block;
         var6.buf_length = var6.buf_length + capacity;
         off += capacity;
         len -= capacity;
         this.submit();
      }
   }

   private void submit() throws IOException {
      this.emitUntil(this.emitQueueSize - 1);
      this.emitQueue.add(this.executor.submit(this.block));
      ParallelGZIPOutputStream.Block b = this.freeBlock;
      if (b != null) {
         this.freeBlock = null;
      } else {
         b = new ParallelGZIPOutputStream.Block();
      }

      this.block = b;
   }

   private void tryEmit() throws IOException, InterruptedException, ExecutionException {
      while (true) {
         Future<ParallelGZIPOutputStream.Block> future = this.emitQueue.peek();
         if (future == null) {
            return;
         }

         if (!future.isDone()) {
            return;
         }

         ParallelGZIPOutputStream.Block b = this.emitQueue.remove().get();
         this.out.write(b.buf, 0, b.buf_length);
         b.buf_length = 0;
         this.freeBlock = b;
      }
   }

   private void emitUntil(@Nonnegative int taskCountAllowed) throws IOException {
      try {
         while (this.emitQueue.size() > taskCountAllowed) {
            ParallelGZIPOutputStream.Block b = this.emitQueue.remove().get();
            this.out.write(b.buf, 0, b.buf_length);
            b.buf_length = 0;
            this.freeBlock = b;
         }

         this.tryEmit();
      } catch (ExecutionException var3) {
         throw new IOException(var3);
      } catch (InterruptedException var4) {
         throw new InterruptedIOException();
      }
   }

   @Override
   public void flush() throws IOException {
      if (this.block.buf_length > 0) {
         this.submit();
      }

      this.emitUntil(0);
      super.flush();
   }

   @Override
   public void close() throws IOException {
      if (this.bytesWritten >= 0L) {
         this.flush();
         newDeflaterOutputStream(this.out, newDeflater()).finish();
         ByteBuffer buf = ByteBuffer.allocate(8);
         buf.order(ByteOrder.LITTLE_ENDIAN);
         buf.putInt((int)this.crc.getValue());
         buf.putInt((int)(this.bytesWritten % 4294967296L));
         this.out.write(buf.array());
         this.out.flush();
         this.out.close();
         this.bytesWritten = -2147483648L;
         this.freeBlock = null;
      }
   }

   private static class Block implements Callable<ParallelGZIPOutputStream.Block> {
      private byte[] buf = new byte[73728];
      private int buf_length = 0;

      private Block() {
      }

      public ParallelGZIPOutputStream.Block call() throws IOException {
         ParallelGZIPOutputStream.State state = ParallelGZIPOutputStream.STATE.get();
         state.def.reset();
         state.buf.reset();
         state.str.write(this.buf, 0, this.buf_length);
         state.str.flush();
         int out_length = state.buf.size();
         if (out_length > this.buf.length) {
            this.buf = new byte[out_length];
         }

         this.buf_length = out_length;
         state.buf.writeTo(this.buf);
         return this;
      }

      @Override
      public String toString() {
         return "Block(" + this.buf_length + "/" + this.buf.length + " bytes)";
      }
   }

   private static class ByteArrayOutputStreamExposed extends ByteArrayOutputStream {
      public ByteArrayOutputStreamExposed(int size) {
         super(size);
      }

      public void writeTo(@Nonnull byte[] buf) throws IOException {
         System.arraycopy(this.buf, 0, buf, 0, this.count);
      }
   }

   private static class State {
      private final Deflater def = ParallelGZIPOutputStream.newDeflater();
      private final ParallelGZIPOutputStream.ByteArrayOutputStreamExposed buf = new ParallelGZIPOutputStream.ByteArrayOutputStreamExposed(73728);
      private final DeflaterOutputStream str = ParallelGZIPOutputStream.newDeflaterOutputStream(this.buf, this.def);

      private State() {
      }
   }
}
