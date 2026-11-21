package net.thenextlvl.nbt.tag;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullUnmarked;

public sealed interface CompoundTag extends ValueTag<Map<String, Tag>> permits CompoundTagImpl {
   byte ID = 10;

   void forEach(BiConsumer<? super String, ? super Tag> var1);

   @Contract(
      pure = true
   )
   Set<Entry<String, Tag>> entrySet();

   @Contract(
      pure = true
   )
   Set<String> keySet();

   @Contract(
      pure = true
   )
   boolean isEmpty();

   @Contract(
      pure = true
   )
   int size();

   @Contract(
      pure = true
   )
   boolean containsKey(String var1);

   @NullUnmarked
   @Contract(
      pure = true
   )
   <T extends Tag> T get(String var1);

   @Contract(
      pure = true
   )
   <E extends Tag> ListTag<E> getAsList(String var1);

   @Contract(
      pure = true
   )
   CompoundTag getAsCompound(String var1);

   @NullUnmarked
   @Contract(
      pure = true
   )
   <T extends Tag> T getOrDefault(String var1, T var2);

   @Contract(
      pure = true
   )
   <T extends Tag> Optional<T> optional(String var1);

   @Contract(
      value = " -> new",
      pure = true
   )
   CompoundTag.Builder toBuilder();

   @Contract(
      value = " -> new",
      pure = true
   )
   static CompoundTag.Builder builder() {
      return new CompoundTagImpl.Builder();
   }

   @Contract(
      value = " -> new",
      pure = true
   )
   static CompoundTag empty() {
      return new CompoundTagImpl();
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static CompoundTag of(Map<String, Tag> value) {
      return new CompoundTagImpl(value);
   }

   public sealed interface Builder permits CompoundTagImpl.Builder {
      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      CompoundTag.Builder put(String var1, boolean var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      CompoundTag.Builder put(String var1, byte... var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      CompoundTag.Builder put(String var1, int... var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      CompoundTag.Builder put(String var1, long... var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      CompoundTag.Builder put(String var1, Number var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      CompoundTag.Builder put(String var1, String var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      CompoundTag.Builder put(String var1, Tag var2);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      CompoundTag.Builder putAll(CompoundTag var1);

      @Contract(
         pure = true
      )
      boolean isEmpty();

      @Contract(
         pure = true
      )
      int size();

      @Contract(
         value = " -> new",
         pure = true
      )
      CompoundTag build();
   }
}
