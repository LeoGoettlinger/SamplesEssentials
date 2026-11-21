package com.fastasyncworldedit.core.extent.clipboard;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

public class URIClipboardHolder extends ClipboardHolder {
   private final URI uri;

   public URIClipboardHolder(URI uri, Clipboard clipboard) {
      super(clipboard);
      Preconditions.checkNotNull(uri);
      this.uri = uri;
   }

   public boolean contains(URI uri) {
      Preconditions.checkNotNull(uri);
      return this.uri.equals(uri);
   }

   @Deprecated
   public URI getUri() {
      return this.uri;
   }

   public Set<URI> getURIs() {
      return Collections.singleton(this.uri);
   }

   public URI getURI(Clipboard clipboard) {
      return this.getClipboard() == clipboard ? this.getUri() : null;
   }
}
