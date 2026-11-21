package org.postgresql.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.internal.Nullness;

class TempFileHolder implements LazyCleaner.CleaningAction<IOException> {
   private static final Logger LOGGER = Logger.getLogger(StreamWrapper.class.getName());
   @Nullable
   private InputStream stream;
   @Nullable
   private Path tempFile;

   TempFileHolder(Path tempFile) {
      this.tempFile = tempFile;
   }

   public InputStream getStream() throws IOException {
      InputStream stream = this.stream;
      if (stream == null) {
         stream = Files.newInputStream(Nullness.castNonNull(this.tempFile));
         this.stream = stream;
      }

      return stream;
   }

   @Override
   public void onClean(boolean leak) throws IOException {
      if (leak) {
         LOGGER.log(Level.WARNING, GT.tr("StreamWrapper leak detected StreamWrapper.close() was not called. "));
      }

      Path tempFile = this.tempFile;
      if (tempFile != null) {
         tempFile.toFile().delete();
         this.tempFile = null;
      }

      InputStream stream = this.stream;
      if (stream != null) {
         stream.close();
         this.stream = null;
      }
   }
}
