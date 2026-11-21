package net.thenextlvl.protect.area;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import net.kyori.adventure.key.Key;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

public interface DataContainer {
   @Contract(
      pure = true
   )
   <T extends Tag> Optional<T> get(Key var1);

   @Contract(
      mutates = "this"
   )
   <T extends Tag> Optional<T> remove(Key var1);

   @Contract(
      pure = true
   )
   <T extends Tag> T getOrDefault(Key var1, T var2);

   @Contract(
      mutates = "this"
   )
   <T extends Tag> void set(Key var1, T var2);

   @Contract(
      pure = true
   )
   @Unmodifiable
   Map<Key, Tag> getTags();

   @Contract(
      pure = true
   )
   @Unmodifiable
   Set<Key> getKeys();

   @Contract(
      pure = true
   )
   @Unmodifiable
   Set<Entry<Key, Tag>> entrySet();

   @Contract(
      pure = true
   )
   boolean has(Key var1);

   @Contract(
      pure = true
   )
   boolean isEmpty();

   @Contract(
      mutates = "this"
   )
   void clear();

   @Contract(
      mutates = "param"
   )
   void copyTo(DataContainer var1);

   @Contract(
      mutates = "param"
   )
   void copyTo(DataContainer var1, boolean var2);

   void forEach(BiConsumer<Key, Tag> var1);
}
