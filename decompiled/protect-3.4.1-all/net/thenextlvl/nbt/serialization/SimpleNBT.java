package net.thenextlvl.nbt.serialization;

import java.io.File;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.thenextlvl.nbt.serialization.adapters.AddressAdapter;
import net.thenextlvl.nbt.serialization.adapters.BooleanAdapter;
import net.thenextlvl.nbt.serialization.adapters.ByteAdapter;
import net.thenextlvl.nbt.serialization.adapters.DoubleAdapter;
import net.thenextlvl.nbt.serialization.adapters.DurationAdapter;
import net.thenextlvl.nbt.serialization.adapters.FileAdapter;
import net.thenextlvl.nbt.serialization.adapters.FloatAdapter;
import net.thenextlvl.nbt.serialization.adapters.IntegerAdapter;
import net.thenextlvl.nbt.serialization.adapters.LongAdapter;
import net.thenextlvl.nbt.serialization.adapters.PathAdapter;
import net.thenextlvl.nbt.serialization.adapters.ShortAdapter;
import net.thenextlvl.nbt.serialization.adapters.StringAdapter;
import net.thenextlvl.nbt.serialization.adapters.UUIDAdapter;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.IterableTag;
import net.thenextlvl.nbt.tag.ListTag;
import net.thenextlvl.nbt.tag.Tag;

final class SimpleNBT implements NBT {
   private final SimpleNBT.SerializationRegistry registry;
   private final boolean prettyPrinting;
   private final int indents;

   private SimpleNBT(SimpleNBT.SerializationRegistry registry, boolean prettyPrinting, int indents) {
      this.registry = registry;
      this.prettyPrinting = prettyPrinting;
      this.indents = indents;
   }

   @Override
   public <T> T deserialize(Tag tag, Class<T> type) throws ParserException {
      TagDeserializer<?> deserializer = this.registry.hierarchyDeserializers.get(type);
      return (T)(deserializer != null
         ? deserializer.deserialize(tag, this)
         : this.registry
            .hierarchyDeserializers
            .entrySet()
            .stream()
            .filter(entry -> type.isAssignableFrom(entry.getKey()))
            .findAny()
            .map(entry -> (T)entry.getValue().deserialize(tag, this))
            .orElseGet(() -> {
               TagDeserializer<Object> typed = (TagDeserializer<Object>)this.registry.deserializers.get(type);
               if (typed != null) {
                  return (T)typed.deserialize(tag, this);
               } else {
                  throw new ParserException("No tag deserializer registered for type: " + type);
               }
            }));
   }

   @Override
   public <T> T deserialize(Tag tag, Type type) throws ParserException {
      TagDeserializer<?> deserializer = this.registry.deserializers.get(type);
      if (deserializer != null) {
         return (T)deserializer.deserialize(tag, this);
      } else if (type instanceof Class<?> clazz) {
         return this.deserialize(tag, (Class<T>)clazz);
      } else {
         throw new ParserException("No tag deserializer registered for type: " + type);
      }
   }

   @Override
   public Tag serialize(Object object) throws ParserException {
      return this.serialize(object, object.getClass());
   }

   @Override
   public Tag serialize(Object object, Class<?> type) throws ParserException {
      TagSerializer<Object> serializer = (TagSerializer<Object>)this.registry.hierarchySerializers.get(type);
      return serializer != null
         ? serializer.serialize(object, this)
         : this.registry
            .hierarchySerializers
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().isInstance(object))
            .findAny()
            .map(entry -> entry.getValue())
            .map(value -> ((TagSerializer<Object>)value).serialize(object, this))
            .orElseGet(() -> {
               TagSerializer<Object> typed = (TagSerializer<Object>)this.registry.serializers.get(type);
               if (typed != null) {
                  return typed.serialize(object, this);
               } else {
                  throw new ParserException("No tag serializer registered for type: " + object.getClass());
               }
            });
   }

   @Override
   public Tag serialize(Object object, Type type) throws ParserException {
      TagSerializer<Object> serializer = (TagSerializer<Object>)this.registry.serializers.get(type);
      if (serializer != null) {
         return serializer.serialize(object, this);
      } else if (type instanceof Class<?> clazz) {
         return this.serialize(object, clazz);
      } else {
         throw new ParserException("No tag serializer registered for type: " + type);
      }
   }

   @Override
   public boolean isPrettyPrinting() {
      return this.prettyPrinting;
   }

   @Override
   public int getIndents() {
      return this.indents;
   }

   @Override
   public String toString(Tag tag) {
      return this.prettyPrinting ? "\"\": " + this.prettify(tag, 0) : tag.toString();
   }

   private String prettify(Tag tag, int depth) {
      String indent = " ".repeat(this.indents);
      String currentIndent = indent.repeat(depth);

      return switch (tag) {
         case CompoundTag compoundTag -> {
            StringBuilder builder = new StringBuilder("{");

            for (Entry<String, Tag> entry : compoundTag.entrySet()) {
               builder.append("\n");
               builder.append(indent.repeat(depth + 1)).append(entry.getKey()).append(": ").append(this.prettify(entry.getValue(), depth + 1));
            }

            yield builder.append("\n").append(currentIndent).append("}").toString();
         }
         case ListTag<?> listTag -> {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;

            for (Tag value : listTag) {
               if (!first) {
                  builder.append(",");
               }

               first = false;
               builder.append("\n");
               builder.append(indent.repeat(depth + 1)).append(this.prettify(value, depth + 1));
            }

            yield builder.append("\n").append(currentIndent).append("]").toString();
         }
         case IterableTag<?> iterableTag -> {
            StringBuilder builder = new StringBuilder("[ ");

            for (int i = 0; i < iterableTag.size(); i++) {
               if (i > 0) {
                  builder.append(", ");
               }

               Object obj = iterableTag.get(i);
               builder.append(obj);
               if (obj instanceof Long) {
                  builder.append("l");
               } else if (obj instanceof Byte) {
                  builder.append("b");
               }
            }

            yield builder.append(" ]").toString();
         }
         default -> tag.toString();
      };
   }

   static final class Builder implements NBT.Builder {
      private final SimpleNBT.SerializationRegistry registry = new SimpleNBT.SerializationRegistry();
      private boolean prettyPrinting = false;
      private int indents = 4;

      @Override
      public <T> NBT.Builder registerTypeHierarchyAdapter(Class<?> type, TagAdapter<T> adapter) {
         this.registry.registerTypeHierarchyAdapter(type, adapter);
         return this;
      }

      @Override
      public <T> NBT.Builder registerTypeHierarchyAdapter(Class<?> type, TagDeserializer<T> deserializer) {
         this.registry.registerTypeHierarchyAdapter(type, deserializer);
         return this;
      }

      @Override
      public <T> NBT.Builder registerTypeHierarchyAdapter(Class<?> type, TagSerializer<T> serializer) {
         this.registry.registerTypeHierarchyAdapter(type, serializer);
         return this;
      }

      @Override
      public <T> NBT.Builder registerTypeAdapter(Type type, TagAdapter<T> adapter) {
         this.registry.registerTypeAdapter(type, adapter);
         return this;
      }

      @Override
      public <T> NBT.Builder registerTypeAdapter(Type type, TagDeserializer<T> deserializer) {
         this.registry.registerTypeAdapter(type, deserializer);
         return this;
      }

      @Override
      public <T> NBT.Builder registerTypeAdapter(Type type, TagSerializer<T> serializer) {
         this.registry.registerTypeAdapter(type, serializer);
         return this;
      }

      @Override
      public NBT.Builder setPrettyPrinting(boolean prettyPrinting) {
         this.prettyPrinting = prettyPrinting;
         return this;
      }

      @Override
      public NBT.Builder setIndents(int indents) {
         this.indents = indents;
         return this;
      }

      @Override
      public NBT build() {
         return new SimpleNBT(this.registry.immutableCopy(), this.prettyPrinting, this.indents);
      }
   }

   private record SerializationRegistry(
      Map<Class<?>, TagDeserializer<?>> hierarchyDeserializers,
      Map<Class<?>, TagSerializer<?>> hierarchySerializers,
      Map<Type, TagDeserializer<?>> deserializers,
      Map<Type, TagSerializer<?>> serializers
   ) {
      public SerializationRegistry() {
         this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
         this.registerTypeAdapter(Boolean.class, BooleanAdapter.INSTANCE);
         this.registerTypeAdapter(Byte.class, ByteAdapter.INSTANCE);
         this.registerTypeAdapter(Double.class, DoubleAdapter.INSTANCE);
         this.registerTypeAdapter(Duration.class, DurationAdapter.INSTANCE);
         this.registerTypeAdapter(File.class, FileAdapter.INSTANCE);
         this.registerTypeAdapter(Float.class, FloatAdapter.INSTANCE);
         this.registerTypeAdapter(InetSocketAddress.class, AddressAdapter.INSTANCE);
         this.registerTypeAdapter(Integer.class, IntegerAdapter.INSTANCE);
         this.registerTypeAdapter(Long.class, LongAdapter.INSTANCE);
         this.registerTypeAdapter(Path.class, PathAdapter.INSTANCE);
         this.registerTypeAdapter(Short.class, ShortAdapter.INSTANCE);
         this.registerTypeAdapter(String.class, StringAdapter.INSTANCE);
         this.registerTypeAdapter(UUID.class, UUIDAdapter.INSTANCE);
         this.registerTypeAdapter(boolean.class, BooleanAdapter.INSTANCE);
         this.registerTypeAdapter(byte.class, ByteAdapter.INSTANCE);
         this.registerTypeAdapter(double.class, DoubleAdapter.INSTANCE);
         this.registerTypeAdapter(float.class, FloatAdapter.INSTANCE);
         this.registerTypeAdapter(int.class, IntegerAdapter.INSTANCE);
         this.registerTypeAdapter(long.class, LongAdapter.INSTANCE);
         this.registerTypeAdapter(short.class, ShortAdapter.INSTANCE);
      }

      public void registerTypeHierarchyAdapter(Class<?> clazz, TagAdapter<?> adapter) {
         this.hierarchyDeserializers.put(clazz, adapter);
         this.hierarchySerializers.put(clazz, adapter);
      }

      public void registerTypeHierarchyAdapter(Class<?> clazz, TagDeserializer<?> deserializer) {
         this.hierarchyDeserializers.put(clazz, deserializer);
      }

      public void registerTypeHierarchyAdapter(Class<?> clazz, TagSerializer<?> serializer) {
         this.hierarchySerializers.put(clazz, serializer);
      }

      public void registerTypeAdapter(Type type, TagAdapter<?> adapter) {
         this.deserializers.put(type, adapter);
         this.serializers.put(type, adapter);
      }

      public void registerTypeAdapter(Type type, TagDeserializer<?> deserializer) {
         this.deserializers.put(type, deserializer);
      }

      public void registerTypeAdapter(Type type, TagSerializer<?> serializer) {
         this.serializers.put(type, serializer);
      }

      public SimpleNBT.SerializationRegistry immutableCopy() {
         return new SimpleNBT.SerializationRegistry(
            Map.copyOf(this.hierarchyDeserializers), Map.copyOf(this.hierarchySerializers), Map.copyOf(this.deserializers), Map.copyOf(this.serializers)
         );
      }
   }
}
