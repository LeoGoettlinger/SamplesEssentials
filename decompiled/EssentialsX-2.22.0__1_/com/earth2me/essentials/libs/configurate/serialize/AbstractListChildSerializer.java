package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationOptions;
import com.earth2me.essentials.libs.configurate.util.CheckedConsumer;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import com.google.errorprone.annotations.ForOverride;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public abstract class AbstractListChildSerializer<T> implements TypeSerializer.Annotated<T> {
   protected AbstractListChildSerializer() {
   }

   @Override
   public final T deserialize(final AnnotatedType type, final ConfigurationNode node) throws SerializationException {
      AnnotatedType entryType = this.elementType(type);
      TypeSerializer<?> entrySerial = node.options().serializers().get(entryType);
      if (entrySerial == null) {
         throw new SerializationException(node, entryType, "No applicable type serializer for type");
      } else if (!node.isList()) {
         Object unwrappedVal = node.raw();
         if (unwrappedVal != null) {
            T ret = this.createNew(1, entryType);
            this.deserializeSingle(0, ret, entrySerial.deserialize(entryType, node));
            return ret;
         } else {
            return this.createNew(0, entryType);
         }
      } else {
         List<? extends ConfigurationNode> values = node.childrenList();
         T ret = this.createNew(values.size(), entryType);

         for (int i = 0; i < values.size(); i++) {
            try {
               this.deserializeSingle(i, ret, entrySerial.deserialize(entryType, values.get(i)));
            } catch (SerializationException var9) {
               var9.initPath(values.get(i)::path);
               throw var9;
            }
         }

         return ret;
      }
   }

   @Override
   public final void serialize(final AnnotatedType type, final T obj, final ConfigurationNode node) throws SerializationException {
      AnnotatedType entryType = this.elementType(type);
      TypeSerializer entrySerial = node.options().serializers().get(entryType);
      if (entrySerial == null) {
         throw new SerializationException(node, entryType, "No applicable type serializer for type");
      } else {
         node.raw(Collections.emptyList());
         if (obj != null) {
            this.forEachElement(obj, el -> {
               ConfigurationNode child = node.appendListNode();

               try {
                  entrySerial.serialize(entryType, el, child);
               } catch (SerializationException var6) {
                  var6.initPath(child::path);
                  throw var6;
               }
            });
         }
      }
   }

   @Nullable
   @Override
   public T emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
      try {
         return this.createNew(0, this.elementType(specificType));
      } catch (SerializationException var4) {
         return null;
      }
   }

   @ForOverride
   protected AnnotatedType elementType(final AnnotatedType containerType) throws SerializationException {
      return GenericTypeReflector.annotate(this.elementType(containerType.getType()));
   }

   @Deprecated
   protected Type elementType(final Type containerType) throws SerializationException {
      throw new IllegalStateException("AbstractListChildSerializer implementations should override elementType(AnnotatedType)");
   }

   @ForOverride
   protected T createNew(final int length, final AnnotatedType elementType) throws SerializationException {
      return this.createNew(length, elementType.getType());
   }

   @Deprecated
   @ForOverride
   protected T createNew(final int length, final Type elementType) throws SerializationException {
      throw new IllegalStateException("AbstractListChildSerializer implementations should override createNew(int, AnnotatedType)");
   }

   @ForOverride
   protected abstract void forEachElement(T collection, CheckedConsumer<Object, SerializationException> action) throws SerializationException;

   @ForOverride
   protected abstract void deserializeSingle(int index, T collection, @Nullable Object deserialized) throws SerializationException;
}
