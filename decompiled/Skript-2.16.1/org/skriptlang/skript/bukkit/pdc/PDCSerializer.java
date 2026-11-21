package org.skriptlang.skript.bukkit.pdc;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.Yggdrasil;
import ch.njol.yggdrasil.YggdrasilInputStream;
import ch.njol.yggdrasil.YggdrasilOutputStream;
import com.google.common.primitives.Primitives;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converters;

public class PDCSerializer {
   private static final NamespacedKey BLOB_KEY = new NamespacedKey("skript", "blob");
   private static final NamespacedKey TYPE_KEY = new NamespacedKey("skript", "pdc_type");
   private static final NamespacedKey VALUE_KEY = new NamespacedKey("skript", "pdc_value");
   private static final Map<Class<?>, PersistentDataType<?, ?>> REPRESENTABLE_TYPES = new LinkedHashMap<>();
   private static final Yggdrasil YGGDRASIL = Variables.yggdrasil;

   @Unmodifiable
   public static Collection<PersistentDataType<?, ?>> getRepresentablePDCTypes() {
      return Collections.unmodifiableCollection(REPRESENTABLE_TYPES.values());
   }

   public static PersistentDataType<?, ?> getPDCType(ClassInfo<?> classInfo) {
      return (PersistentDataType<?, ?>)(REPRESENTABLE_TYPES.containsKey(classInfo.getC()) ? REPRESENTABLE_TYPES.get(classInfo.getC()) : SkriptDataType.get());
   }

   @NotNull
   public static PersistentDataContainer serialize(@NotNull Object unserializedData, @NotNull PersistentDataAdapterContext context) {
      return serialize(unserializedData, context, false);
   }

   @NotNull
   private static PersistentDataContainer serialize(@NotNull Object unserializedData, @NotNull PersistentDataAdapterContext context, boolean nested) {
      assert Bukkit.isPrimaryThread();

      ClassInfo<?> classInfo = Classes.getSuperClassInfo(unserializedData.getClass());
      if (classInfo.getSerializeAs() != null) {
         classInfo = Classes.getExactClassInfo(classInfo.getSerializeAs());
         if (classInfo == null) {
            assert false : unserializedData.getClass();

            return null;
         }

         unserializedData = Converters.convert(unserializedData, classInfo.getC());
         if (unserializedData == null) {
            assert false : classInfo.getCodeName();

            return null;
         }
      }

      Serializer<Object> serializer = (Serializer<Object>)classInfo.getSerializer();
      if (serializer == null) {
         if (nested) {
            return serializeToBase64(unserializedData, context);
         } else {
            throw new RuntimeException("The value " + unserializedData + " is not serializable!");
         }
      } else {
         assert !serializer.mustSyncDeserialization() || Bukkit.isPrimaryThread();

         PersistentDataContainer container = context.newPersistentDataContainer();
         if (REPRESENTABLE_TYPES.containsKey(classInfo.getC())) {
            container.set(TYPE_KEY, PersistentDataType.STRING, classInfo.getCodeName());
            PersistentDataType<Object, Object> pdcType = (PersistentDataType<Object, Object>)REPRESENTABLE_TYPES.get(classInfo.getC());
            container.set(VALUE_KEY, pdcType, unserializedData);
            return container;
         } else {
            try {
               Fields fields = serializer.serialize(unserializedData);
               container.set(TYPE_KEY, PersistentDataType.STRING, classInfo.getCodeName());

               for (Fields.FieldContext field : fields) {
                  NamespacedKey tag = new NamespacedKey("skript", field.getID());
                  Object data = field.isPrimitive() ? field.getPrimitive() : field.getObject();
                  if (data == null) {
                     container.set(tag, PersistentDataType.TAG_CONTAINER, context.newPersistentDataContainer());
                  } else if (!field.isPrimitive() && !(data instanceof String)) {
                     data = serialize(data, context, true);
                     container.set(tag, PersistentDataType.TAG_CONTAINER, (PersistentDataContainer)data);
                  } else {
                     PersistentDataType<?, ?> type = REPRESENTABLE_TYPES.get(data.getClass());
                     if (type == null) {
                        throw new NotSerializableException("Unsupported primitive type: " + data.getClass());
                     }

                     container.set(tag, type, data);
                  }
               }

               return container;
            } catch (StreamCorruptedException | NotSerializableException var12) {
               throw new RuntimeException(var12);
            }
         }
      }
   }

   @NotNull
   public static Object deserialize(@NotNull PersistentDataContainer serializedData, @NotNull PersistentDataAdapterContext context) {
      if (serializedData.has(BLOB_KEY, PersistentDataType.STRING)) {
         return deserializeFromBase64(serializedData);
      } else {
         String typeName = (String)serializedData.get(TYPE_KEY, PersistentDataType.STRING);
         if (typeName == null) {
            throw new IllegalArgumentException("Cannot deserialize PDC because it has no type");
         } else {
            ClassInfo<?> classInfo = Classes.getClassInfo(typeName);
            Serializer<Object> serializer = (Serializer<Object>)classInfo.getSerializer();
            if (serializer == null) {
               throw new IllegalArgumentException("Cannot deserialize " + classInfo.getCodeName() + " because it has no serializer");
            } else if (REPRESENTABLE_TYPES.containsKey(classInfo.getC())) {
               PersistentDataType<Object, Object> pdcType = (PersistentDataType<Object, Object>)REPRESENTABLE_TYPES.get(classInfo.getC());
               return serializedData.get(VALUE_KEY, pdcType);
            } else {
               try {
                  Fields fields = new Fields(YGGDRASIL);

                  for (NamespacedKey key : serializedData.getKeys()) {
                     if (!key.equals(TYPE_KEY)) {
                        Object data = null;
                        boolean primitive = true;

                        for (Entry<Class<?>, PersistentDataType<?, ?>> entry : REPRESENTABLE_TYPES.entrySet()) {
                           PersistentDataType<?, ?> type = entry.getValue();
                           if (serializedData.has(key, type)) {
                              data = serializedData.get(key, type);
                              primitive = entry.getKey().isPrimitive() || isPrimitiveWrapper(entry.getKey());
                              break;
                           }
                        }

                        if (data == null) {
                           if (!serializedData.has(key, PersistentDataType.TAG_CONTAINER)) {
                              throw new NotSerializableException("Unsupported data type for key: " + key);
                           }

                           PersistentDataContainer nestedContainer = (PersistentDataContainer)serializedData.get(key, PersistentDataType.TAG_CONTAINER);

                           assert nestedContainer != null;

                           if (nestedContainer.isEmpty()) {
                              fields.putObject(key.getKey(), null);
                              continue;
                           }

                           data = deserialize(nestedContainer, context);
                           primitive = false;
                        }

                        if (primitive) {
                           fields.putPrimitive(key.getKey(), data);
                        } else {
                           fields.putObject(key.getKey(), data);
                        }
                     }
                  }

                  assert !serializer.mustSyncDeserialization() || Bukkit.isPrimaryThread();

                  if (serializer.canBeInstantiated((Class<? extends Object>)classInfo.getC())) {
                     Object obj = serializer.newInstance(classInfo.getC());
                     serializer.deserialize(obj, fields);
                     return obj;
                  } else {
                     return serializer.deserialize(classInfo.getC(), fields);
                  }
               } catch (Exception var13) {
                  throw new RuntimeException(var13);
               }
            }
         }
      }
   }

   private static PersistentDataContainer serializeToBase64(@NotNull Object data, @NotNull PersistentDataAdapterContext context) {
      try {
         ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
         YggdrasilOutputStream yggOut = YGGDRASIL.newOutputStream(byteOut);
         yggOut.writeObject(data);
         yggOut.flush();
         yggOut.close();
         PersistentDataContainer container = context.newPersistentDataContainer();
         container.set(BLOB_KEY, PersistentDataType.STRING, Base64.getEncoder().encodeToString(byteOut.toByteArray()));
         return container;
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      }
   }

   private static Object deserializeFromBase64(@NotNull PersistentDataContainer container) {
      String blob = (String)container.get(BLOB_KEY, PersistentDataType.STRING);
      if (blob == null) {
         throw new IllegalArgumentException("Cannot deserialize PDC because blob value is missing");
      } else {
         try {
            YggdrasilInputStream yggIn = YGGDRASIL.newInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(blob)));
            return yggIn.readObject();
         } catch (IOException var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   private static boolean isPrimitiveWrapper(Class<?> key) {
      return Primitives.isWrapperType(key);
   }

   static {
      REPRESENTABLE_TYPES.put(Boolean.class, PersistentDataType.BOOLEAN);
      REPRESENTABLE_TYPES.put(Byte.class, PersistentDataType.BYTE);
      REPRESENTABLE_TYPES.put(Short.class, PersistentDataType.SHORT);
      REPRESENTABLE_TYPES.put(Integer.class, PersistentDataType.INTEGER);
      REPRESENTABLE_TYPES.put(Long.class, PersistentDataType.LONG);
      REPRESENTABLE_TYPES.put(Double.class, PersistentDataType.DOUBLE);
      REPRESENTABLE_TYPES.put(Float.class, PersistentDataType.FLOAT);
      REPRESENTABLE_TYPES.put(String.class, PersistentDataType.STRING);
   }
}
