package com.sk89q.worldedit.extent.clipboard.io;

import com.fastasyncworldedit.core.extent.clipboard.URIClipboardHolder;
import com.fastasyncworldedit.core.internal.io.parallelgzip.ParallelGZIPOutputStream;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.task.RunnableVal;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Set;

public interface ClipboardFormat {
   String getName();

   Set<String> getAliases();

   ClipboardReader getReader(InputStream inputStream) throws IOException;

   ClipboardWriter getWriter(OutputStream outputStream) throws IOException;

   default boolean isFormat(File file) {
      try {
         boolean var3;
         try (InputStream stream = Files.newInputStream(file.toPath())) {
            var3 = this.isFormat(stream);
         }

         return var3;
      } catch (IOException var7) {
         return false;
      }
   }

   default boolean isFormat(InputStream inputStream) {
      return false;
   }

   String getPrimaryFileExtension();

   Set<String> getFileExtensions();

   Set<String> getExplicitFileExtensions();

   default URIClipboardHolder hold(Actor actor, URI uri, InputStream inputStream) throws IOException {
      Preconditions.checkNotNull(actor);
      Preconditions.checkNotNull(uri);
      Preconditions.checkNotNull(inputStream);
      ClipboardReader reader = this.getReader(inputStream);
      LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);
      session.setClipboard(null);
      Clipboard clipboard = reader.read(actor.getUniqueId());
      URIClipboardHolder holder = new URIClipboardHolder(uri, clipboard);
      session.setClipboard(holder);
      return holder;
   }

   default Clipboard load(File file) throws IOException {
      return this.load(new FileInputStream(file));
   }

   default Clipboard load(InputStream stream) throws IOException {
      return this.getReader(stream).read();
   }

   default URL upload(final Clipboard clipboard) {
      return MainUtil.upload(null, null, this.getPrimaryFileExtension(), new RunnableVal<OutputStream>() {
         public void run(OutputStream value) {
            ClipboardFormat.this.write(value, clipboard);
         }
      });
   }

   default void write(OutputStream value, Clipboard clipboard) {
      try {
         try (
            ParallelGZIPOutputStream gzip = new ParallelGZIPOutputStream(value);
            ClipboardWriter writer = this.getWriter(gzip);
         ) {
            writer.write(clipboard);
         }
      } catch (IOException var11) {
         throw new RuntimeException(var11);
      }
   }
}
