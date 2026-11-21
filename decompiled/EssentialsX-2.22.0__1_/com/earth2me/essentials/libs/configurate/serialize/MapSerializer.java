package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.configurate.BasicConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationOptions;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

final class MapSerializer implements TypeSerializer.Annotated<Map<?, ?>> {
   static final TypeToken<Map<?, ?>> TYPE = new TypeToken<Map<?, ?>>() {};

   public Map<?, ?> deserialize(final AnnotatedType type, final ConfigurationNode node) throws SerializationException {
      Map<Object, Object> ret = new LinkedHashMap<>();
      if (node.isMap()) {
         if (!(type instanceof AnnotatedParameterizedType)) {
            throw new SerializationException(type, "Raw types are not supported for collections");
         }

         AnnotatedParameterizedType param = (AnnotatedParameterizedType)type;
         AnnotatedType[] typeArgs = param.getAnnotatedActualTypeArguments();
         if (typeArgs.length != 2) {
            throw new SerializationException(type, "Map expected two type arguments!");
         }

         AnnotatedType key = typeArgs[0];
         AnnotatedType value = typeArgs[1];
         TypeSerializer<?> keySerial = node.options().serializers().get(key);
         TypeSerializer<?> valueSerial = node.options().serializers().get(value);
         if (keySerial == null) {
            throw new SerializationException(type, "No type serializer available for key type " + key);
         }

         if (valueSerial == null) {
            throw new SerializationException(type, "No type serializer available for value type " + value);
         }

         BasicConfigurationNode keyNode = BasicConfigurationNode.root(node.options());

         for (Entry<Object, ? extends ConfigurationNode> ent : node.childrenMap().entrySet()) {
            ret.put(
               Objects.requireNonNull(keySerial.deserialize(key, keyNode.set(ent.getKey())), "key"),
               Objects.requireNonNull(valueSerial.deserialize(value, ent.getValue()), "value")
            );
         }
      }

      return ret;
   }

   public void serialize(final AnnotatedType type, final Map<?, ?> obj, final ConfigurationNode node) throws SerializationException {
      if (!(type instanceof AnnotatedParameterizedType)) {
         throw new SerializationException(type, "Raw types are not supported for collections");
      } else {
         AnnotatedParameterizedType param = (AnnotatedParameterizedType)type;
         AnnotatedType[] typeArgs = param.getAnnotatedActualTypeArguments();
         if (typeArgs.length != 2) {
            throw new SerializationException(type, "Map expected two type arguments!");
         } else {
            AnnotatedType key = typeArgs[0];
            AnnotatedType value = typeArgs[1];
            TypeSerializer keySerial = node.options().serializers().get(key);
            TypeSerializer valueSerial = node.options().serializers().get(value);
            if (keySerial == null) {
               throw new SerializationException(type, "No type serializer available for key type " + key);
            } else if (valueSerial == null) {
               throw new SerializationException(type, "No type serializer available for value type " + value);
            } else {
               if (obj != null && !obj.isEmpty()) {
                  Set<Object> unvisitedKeys;
                  if (node.empty()) {
                     node.raw(Collections.emptyMap());
                     unvisitedKeys = Collections.emptySet();
                  } else {
                     unvisitedKeys = new HashSet<>(node.childrenMap().keySet());
                  }

                  BasicConfigurationNode keyNode = BasicConfigurationNode.root(node.options());

                  for (Entry<?, ?> ent : obj.entrySet()) {
                     keySerial.serialize(key, ent.getKey(), keyNode);
                     Object keyObj = Objects.requireNonNull(keyNode.raw(), "Key must not be null!");
                     ConfigurationNode child = node.node(keyObj);

                     try {
                        valueSerial.serialize(value, ent.getValue(), child);
                     } catch (SerializationException var20) {
                        var20.initPath(child::path);
                     } finally {
                        unvisitedKeys.remove(keyObj);
                     }
                  }

                  for (Object unusedChild : unvisitedKeys) {
                     node.removeChild(unusedChild);
                  }
               } else {
                  node.set(Collections.emptyMap());
               }
            }
         }
      }
   }

   public Map<?, ?> emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
      return new LinkedHashMap();
   }
}
