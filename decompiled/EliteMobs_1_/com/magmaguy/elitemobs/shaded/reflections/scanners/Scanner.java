package com.magmaguy.elitemobs.shaded.reflections.scanners;

import com.magmaguy.elitemobs.shaded.reflections.vfs.Vfs;
import com.magmaguy.shaded.javassist.bytecode.ClassFile;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public interface Scanner {
   List<Entry<String, String>> scan(ClassFile var1);

   @Nullable
   default List<Entry<String, String>> scan(Vfs.File file) {
      return null;
   }

   default String index() {
      return this.getClass().getSimpleName();
   }

   default boolean acceptsInput(String file) {
      return file.endsWith(".class");
   }

   default Entry<String, String> entry(String key, String value) {
      return new SimpleEntry<>(key, value);
   }

   default List<Entry<String, String>> entries(Collection<String> keys, String value) {
      return keys.stream().map(key -> this.entry(key, value)).collect(Collectors.toList());
   }

   default List<Entry<String, String>> entries(String key, String value) {
      return Collections.singletonList(this.entry(key, value));
   }

   default List<Entry<String, String>> entries(String key, Collection<String> values) {
      return values.stream().map(value -> this.entry(key, value)).collect(Collectors.toList());
   }
}
