package com.sk89q.worldedit.util.time;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ModificationDateTimeParser implements SnapshotDateTimeParser {
   private static final ModificationDateTimeParser INSTANCE = new ModificationDateTimeParser();

   public static ModificationDateTimeParser getInstance() {
      return INSTANCE;
   }

   private ModificationDateTimeParser() {
   }

   @Override
   public ZonedDateTime detectDateTime(Path path) {
      if (!Files.exists(path)) {
         return null;
      } else {
         try {
            return Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault());
         } catch (IOException var3) {
            throw new UncheckedIOException(var3);
         }
      }
   }
}
