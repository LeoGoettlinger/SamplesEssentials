package com.sk89q.worldedit.util.io.file;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ArchiveUnpacker {
   private static final String UNPACK_FINISHED = ".unpack_finished";
   private static final Lock lock = new ReentrantLock();
   private final Path unpackDir;

   public ArchiveUnpacker(Path unpackDir) throws IOException {
      this.unpackDir = unpackDir;
      Files.createDirectories(unpackDir);
   }

   public Path unpackArchive(URL archiveUrl) throws IOException {
      String hash;
      try (InputStream data = archiveUrl.openStream()) {
         hash = (String)ByteStreams.readBytes(data, new ByteProcessor<String>() {
            private final Hasher hasher = Hashing.crc32c().newHasher();

            public boolean processBytes(byte[] buf, int off, int len) {
               this.hasher.putBytes(buf, off, len);
               return true;
            }

            public String getResult() {
               return this.hasher.hash().toString();
            }
         });
      }

      Path dest = this.unpackDir.resolve(hash);
      if (Files.exists(dest.resolve(".unpack_finished"))) {
         return dest;
      } else {
         lock.lock();

         Path in;
         try {
            if (!Files.exists(dest.resolve(".unpack_finished"))) {
               ZipEntry next;
               try (
                  InputStream inx = archiveUrl.openStream();
                  ZipInputStream zipReader = new ZipInputStream(inx);
               ) {
                  while ((next = zipReader.getNextEntry()) != null) {
                     Path resolved = dest.resolve(next.getName());
                     if (resolved.startsWith(dest)) {
                        if (next.isDirectory()) {
                           Files.createDirectories(resolved, SafeFiles.getOwnerOnlyFileAttributes(AttributeTarget.DIRECTORY));
                        } else {
                           try (SeekableByteChannel channel = Files.newByteChannel(
                                 resolved,
                                 ImmutableSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING),
                                 SafeFiles.getOwnerOnlyFileAttributes(AttributeTarget.FILE)
                              )) {
                              ByteStreams.copy(Channels.newChannel(zipReader), channel);
                           }
                        }
                     }
                  }
               }

               Files.createFile(dest.resolve(".unpack_finished"));
               return dest;
            }

            in = dest;
         } finally {
            lock.unlock();
         }

         return in;
      }
   }
}
