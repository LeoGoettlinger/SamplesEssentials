package com.fastasyncworldedit.core.extent.clipboard;

import com.google.common.io.ByteSource;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

public class LazyClipboardHolder extends URIClipboardHolder {
   private final ByteSource source;
   private final ClipboardFormat format;
   private final UUID uuid;
   private Clipboard clipboard;

   public LazyClipboardHolder(URI uri, ByteSource source, ClipboardFormat format, UUID uuid) {
      super(uri, EmptyClipboard.getInstance());
      this.source = source;
      this.format = format;
      this.uuid = uuid != null ? uuid : UUID.randomUUID();
   }

   @Override
   public boolean contains(Clipboard clipboard) {
      return this.clipboard == clipboard;
   }

   @Override
   public synchronized Clipboard getClipboard() {
      if (this.clipboard == null) {
         try (InputStream in = this.source.openBufferedStream()) {
            ClipboardReader reader = this.format.getReader(in);
            this.clipboard = reader.read(this.uuid);
         } catch (Throwable var6) {
            var6.printStackTrace();
         }
      }

      return this.clipboard;
   }

   @Override
   public URI getURI(Clipboard clipboard) {
      return this.clipboard == clipboard ? this.getUri() : null;
   }

   @Override
   public synchronized void close() {
      if (this.clipboard instanceof BlockArrayClipboard) {
         this.clipboard.close();
      }

      this.clipboard = null;
   }

   @Override
   public synchronized void flush() {
      if (this.clipboard instanceof BlockArrayClipboard) {
         this.clipboard.flush();
      }
   }
}
