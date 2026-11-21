package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

final class PathSerializer implements TypeSerializer<Path> {
   private static final String[] EMPTY_STRING_ARRAY = new String[0];
   static final PathSerializer INSTANCE = new PathSerializer();
   static final Class<Path> TYPE = Path.class;

   private PathSerializer() {
   }

   public Path deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
      if (node.isList()) {
         List<String> elements = node.getList(String.class);
         if (elements != null && !elements.isEmpty()) {
            return elements.size() == 1
               ? Paths.get(elements.get(0))
               : Paths.get(elements.get(0), elements.subList(1, elements.size()).toArray(EMPTY_STRING_ARRAY));
         } else {
            return Paths.get(".");
         }
      } else if (node.isMap()) {
         throw new SerializationException("Paths must be a list of strings, or a single string");
      } else {
         Object value = node.rawScalar();
         if (value == null) {
            throw new SerializationException("must have scalar value");
         } else {
            return value instanceof URI ? Paths.get((URI)value) : Paths.get(value.toString());
         }
      }
   }

   public void serialize(final Type type, @Nullable final Path obj, final ConfigurationNode node) throws SerializationException {
      if (obj == null) {
         node.set(null);
      } else {
         if (node.isList()) {
            node.set(null);

            for (Path element : obj) {
               node.appendListNode().set(element.toString());
            }
         } else if (!obj.getFileSystem().equals(FileSystems.getDefault())) {
            node.set(URI.class, obj.toUri());
         } else {
            node.raw(obj.toString());
         }
      }
   }
}
