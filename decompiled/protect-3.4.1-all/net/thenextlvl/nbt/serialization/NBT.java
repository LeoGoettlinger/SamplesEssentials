package net.thenextlvl.nbt.serialization;

import java.lang.reflect.Type;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;

public sealed interface NBT extends TagSerializationContext, TagDeserializationContext permits SimpleNBT {
   @Contract(
      pure = true
   )
   boolean isPrettyPrinting();

   @Contract(
      pure = true
   )
   int getIndents();

   @Contract(
      value = "_ -> new",
      pure = true
   )
   String toString(Tag var1);

   @Contract(
      value = " -> new",
      pure = true
   )
   static NBT.Builder builder() {
      return new SimpleNBT.Builder();
   }

   public sealed interface Builder permits SimpleNBT.Builder {
      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      <T> NBT.Builder registerTypeHierarchyAdapter(Class<?> var1, TagAdapter<T> var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      <T> NBT.Builder registerTypeHierarchyAdapter(Class<?> var1, TagDeserializer<T> var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      <T> NBT.Builder registerTypeHierarchyAdapter(Class<?> var1, TagSerializer<T> var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      <T> NBT.Builder registerTypeAdapter(Type var1, TagAdapter<T> var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      <T> NBT.Builder registerTypeAdapter(Type var1, TagDeserializer<T> var2);

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      <T> NBT.Builder registerTypeAdapter(Type var1, TagSerializer<T> var2);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      NBT.Builder setPrettyPrinting(boolean var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      NBT.Builder setIndents(int var1);

      @Contract(
         value = " -> new",
         pure = true
      )
      NBT build();
   }
}
