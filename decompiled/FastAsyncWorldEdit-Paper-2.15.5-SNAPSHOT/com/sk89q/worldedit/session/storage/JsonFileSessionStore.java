package com.sk89q.worldedit.session.storage;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.gson.GsonUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.apache.logging.log4j.Logger;

public class JsonFileSessionStore implements SessionStore {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final Gson gson;
   private final Path dir;

   @Deprecated
   public JsonFileSessionStore(File dir) {
      this(dir.toPath());
   }

   public JsonFileSessionStore(Path dir) {
      Preconditions.checkNotNull(dir);

      try {
         Files.createDirectories(dir);
      } catch (IOException var3) {
         LOGGER.warn("Failed to create directory '" + dir + "' for sessions", var3);
      }

      this.dir = dir;
      GsonBuilder builder = GsonUtil.createBuilder();
      this.gson = builder.create();
   }

   private Path getPath(UUID id) {
      Preconditions.checkNotNull(id);
      return this.dir.resolve(id + ".json");
   }

   @Override
   public LocalSession load(UUID id) throws IOException {
      Path file = this.getPath(id);

      try {
         LocalSession var5;
         try (BufferedReader reader = Files.newBufferedReader(file)) {
            LocalSession session = (LocalSession)this.gson.fromJson(reader, LocalSession.class);
            if (session == null) {
               LOGGER.warn("Loaded a null session from {}, creating new session", file);
               if (!Files.deleteIfExists(file)) {
                  LOGGER.warn("Failed to delete corrupted session {}", file);
               }

               session = new LocalSession();
            }

            var5 = session;
         }

         return var5;
      } catch (JsonParseException var8) {
         throw new IOException(var8);
      } catch (NoSuchFileException var9) {
         return new LocalSession();
      }
   }

   @Override
   public void save(UUID id, LocalSession session) throws IOException {
      Preconditions.checkNotNull(session);
      Path finalFile = this.getPath(id);
      Path tempFile = finalFile.getParent().resolve(finalFile.getFileName() + ".tmp");

      try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
         this.gson.toJson(session, writer);
      } catch (JsonIOException var11) {
         throw new IOException(var11);
      }

      if (Files.size(tempFile) == 0L) {
         throw new IllegalStateException("Gson wrote zero bytes");
      } else {
         try {
            Files.move(tempFile, finalFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (IOException var9) {
            LOGGER.warn("Failed to rename temporary session file to " + finalFile.toAbsolutePath(), var9);
         }
      }
   }
}
