package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Objects;

final class FileSerializer implements TypeSerializer<File> {
   static final FileSerializer INSTANCE = new FileSerializer();
   static final Class<File> TYPE = File.class;

   private FileSerializer() {
   }

   public File deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
      return Objects.requireNonNull(node.get(Path.class), "node did not contain a valid path").toFile();
   }

   public void serialize(final Type type, @Nullable final File obj, final ConfigurationNode node) throws SerializationException {
      if (obj == null) {
         node.set(null);
      } else {
         node.set(Path.class, obj.toPath());
      }
   }
}
