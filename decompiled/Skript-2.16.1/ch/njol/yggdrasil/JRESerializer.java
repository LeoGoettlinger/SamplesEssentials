package ch.njol.yggdrasil;

import com.google.common.collect.ImmutableList;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class JRESerializer extends YggdrasilSerializer<Object> {
   private static final List<Class<?>> SUPPORTED_CLASSES = ImmutableList.of(ArrayList.class, LinkedList.class, HashSet.class, HashMap.class, UUID.class);

   @Nullable
   @Override
   public Class<?> getClass(String id) {
      for (Class<?> type : SUPPORTED_CLASSES) {
         if (type.getSimpleName().equals(id)) {
            return type;
         }
      }

      return null;
   }

   @Nullable
   @Override
   public String getID(Class<?> type) {
      return SUPPORTED_CLASSES.contains(type) ? type.getSimpleName() : null;
   }

   @Override
   public Fields serialize(Object object) {
      if (!SUPPORTED_CLASSES.contains(object.getClass())) {
         throw new IllegalArgumentException();
      } else {
         Fields fields = new Fields();
         if (object instanceof Collection<?> collection) {
            fields.putObject("values", collection.toArray());
         } else if (object instanceof Map<?, ?> map) {
            fields.putObject("keys", map.keySet().toArray());
            fields.putObject("values", map.values().toArray());
         } else if (object instanceof UUID) {
            fields.putPrimitive("mostSigBits", ((UUID)object).getMostSignificantBits());
            fields.putPrimitive("leastSigBits", ((UUID)object).getLeastSignificantBits());
         }

         assert fields.size() > 0 : object;

         return fields;
      }
   }

   @Override
   public boolean canBeInstantiated(Class<?> type) {
      return type != UUID.class;
   }

   @Nullable
   @Override
   public <T> T newInstance(Class<T> type) {
      try {
         return type.newInstance();
      } catch (InstantiationException var3) {
         var3.printStackTrace();

         assert false;

         return null;
      } catch (IllegalAccessException var4) {
         var4.printStackTrace();

         assert false;

         return null;
      }
   }

   @Override
   public void deserialize(Object object, Fields fields) throws StreamCorruptedException {
      try {
         if (object instanceof Collection<?> collection) {
            Object[] values = fields.getObject("values", Object[].class);
            if (values == null) {
               throw new StreamCorruptedException();
            }

            collection.addAll(Arrays.asList(values));
            return;
         }

         if (object instanceof Map<?, ?> map) {
            Object[] keys = fields.getObject("keys", Object[].class);
            Object[] values = fields.getObject("values", Object[].class);
            if (keys != null && values != null && keys.length == values.length) {
               for (int i = 0; i < keys.length; i++) {
                  ((Map<?, Object>)map).put(keys[i], values[i]);
               }

               return;
            }

            throw new StreamCorruptedException();
         }
      } catch (Exception var7) {
         throw new StreamCorruptedException(var7.getClass().getSimpleName() + ": " + var7.getMessage());
      }

      throw new StreamCorruptedException();
   }

   @Override
   public <E> E deserialize(Class<E> type, Fields fields) throws StreamCorruptedException, NotSerializableException {
      if (type == UUID.class) {
         return (E)(new UUID(fields.getPrimitive("mostSigBits", long.class), fields.getPrimitive("leastSigBits", long.class)));
      } else {
         throw new StreamCorruptedException();
      }
   }
}
