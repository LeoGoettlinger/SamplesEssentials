package com.fastasyncworldedit.core.extent.clipboard;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class MultiClipboardHolder extends URIClipboardHolder implements Iterable<URIClipboardHolder> {
   private final List<URIClipboardHolder> holders = new ArrayList<>();
   private Clipboard[] cached;

   public MultiClipboardHolder() {
      this(URI.create(""));
   }

   public MultiClipboardHolder(URI uri) {
      super(uri, EmptyClipboard.getInstance());
   }

   public MultiClipboardHolder(URI uri, URIClipboardHolder... addHolders) {
      this(uri);

      for (URIClipboardHolder h : addHolders) {
         this.add(h);
      }
   }

   public MultiClipboardHolder(Clipboard clipboard) {
      super(URI.create(""), EmptyClipboard.getInstance());
      URI uri = URI.create("");
      if (clipboard.getURI() != null) {
         uri = clipboard.getURI();
      }

      this.add(uri, clipboard);
   }

   public void remove(URI uri) {
      this.cached = null;
      if (!this.getUri().equals(uri)) {
         for (int i = this.holders.size() - 1; i >= 0; i--) {
            URIClipboardHolder holder = this.holders.get(i);
            if (holder.contains(uri)) {
               if (holder instanceof MultiClipboardHolder) {
                  ((MultiClipboardHolder)holder).remove(uri);
               } else {
                  this.holders.remove(i).close();
               }
            }
         }
      } else {
         for (ClipboardHolder holder : this.holders) {
            holder.close();
         }

         this.holders.clear();
      }
   }

   @Override
   public URI getURI(Clipboard clipboard) {
      for (ClipboardHolder holder : this.getHolders()) {
         if (holder instanceof URIClipboardHolder uriHolder) {
            URI uri = uriHolder.getURI(clipboard);
            if (uri != null) {
               return uri;
            }
         }
      }

      return null;
   }

   public void add(URIClipboardHolder holder) {
      this.add((ClipboardHolder)holder);
   }

   @Override
   public boolean contains(Clipboard clipboard) {
      for (ClipboardHolder holder : this.holders) {
         if (holder.contains(clipboard)) {
            return true;
         }
      }

      return false;
   }

   @Deprecated
   public void add(ClipboardHolder holder) {
      Preconditions.checkNotNull(holder);
      if (holder instanceof URIClipboardHolder) {
         this.holders.add((URIClipboardHolder)holder);
      } else {
         URI uri = URI.create(UUID.randomUUID().toString());
         if (!this.contains(uri)) {
            this.holders.add(new URIClipboardHolder(uri, holder.getClipboard()));
         }
      }

      this.cached = null;
   }

   public void add(URI uri, Clipboard clip) {
      Preconditions.checkNotNull(clip);
      Preconditions.checkNotNull(uri);
      this.add(new URIClipboardHolder(uri, clip));
   }

   @Override
   public List<Clipboard> getClipboards() {
      ArrayList<Clipboard> all = new ArrayList<>();

      for (ClipboardHolder holder : this.holders) {
         all.addAll(holder.getClipboards());
      }

      return all;
   }

   @Override
   public List<ClipboardHolder> getHolders() {
      ArrayList<ClipboardHolder> holders = new ArrayList<>();

      for (ClipboardHolder holder : this.holders) {
         holders.addAll(holder.getHolders());
      }

      return holders;
   }

   @Override
   public boolean contains(URI uri) {
      if (this.getUri().equals(uri)) {
         return true;
      } else {
         for (URIClipboardHolder uch : this.holders) {
            if (uch.contains(uri)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public Clipboard getClipboard() {
      Clipboard[] available = this.cached;
      if (available == null) {
         this.cached = available = this.getClipboards().toArray(new Clipboard[0]);
      }

      switch (available.length) {
         case 0:
            return EmptyClipboard.getInstance();
         case 1:
            return available[0];
         default:
            int index = ThreadLocalRandom.current().nextInt(available.length);
            return available[index];
      }
   }

   @Override
   public Set<URI> getURIs() {
      Set<URI> set = new HashSet<>();

      for (ClipboardHolder holder : this.getHolders()) {
         if (holder instanceof URIClipboardHolder) {
            URI uri = ((URIClipboardHolder)holder).getUri();
            if (!uri.toString().isEmpty()) {
               set.add(uri);
            }
         }
      }

      return set;
   }

   @Override
   public void close() {
      this.cached = null;

      for (ClipboardHolder holder : this.holders) {
         holder.close();
      }
   }

   @Override
   public void flush() {
      for (ClipboardHolder holder : this.holders) {
         try {
            holder.flush();
         } catch (IOException var4) {
            var4.printStackTrace();
         }
      }
   }

   @Nonnull
   @Override
   public Iterator<URIClipboardHolder> iterator() {
      return this.holders.iterator();
   }
}
