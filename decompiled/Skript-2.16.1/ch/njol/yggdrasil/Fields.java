package ch.njol.yggdrasil;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.concurrent.NotThreadSafe;
import org.jetbrains.annotations.Nullable;

@NotThreadSafe
public final class Fields implements Iterable<Fields.FieldContext> {
   @Nullable
   private final Yggdrasil yggdrasil;
   private final Map<String, Fields.FieldContext> fields = new HashMap<>();
   private static final Map<Class<?>, Collection<Field>> cache = new HashMap<>();

   public Fields() {
      this.yggdrasil = null;
   }

   public Fields(Yggdrasil yggdrasil) {
      this.yggdrasil = yggdrasil;
   }

   public Fields(Class<?> type, Yggdrasil yggdrasil) throws NotSerializableException {
      this.yggdrasil = yggdrasil;

      for (Field field : getFields(type)) {
         assert field != null;

         String id = Yggdrasil.getID(field);
         this.fields.put(id, new Fields.FieldContext(id));
      }
   }

   public Fields(Object object) throws NotSerializableException {
      this(object, null);
   }

   public Fields(Object object, @Nullable Yggdrasil yggdrasil) throws NotSerializableException {
      this.yggdrasil = yggdrasil;
      Class<?> type = object.getClass();

      assert type != null;

      for (Field field : getFields(type)) {
         assert field != null;

         try {
            this.fields.put(Yggdrasil.getID(field), new Fields.FieldContext(field, object));
         } catch (IllegalAccessException | IllegalArgumentException var7) {
            assert false;
         }
      }
   }

   public static Fields singletonObject(String fieldID, @Nullable Object value) {
      Fields fields = new Fields();
      fields.putObject(fieldID.toLowerCase(Locale.ENGLISH), value);
      return fields;
   }

   public static Fields singletonPrimitive(String fieldID, Object value) {
      Fields fields = new Fields();
      fields.putPrimitive(fieldID.toLowerCase(Locale.ENGLISH), value);
      return fields;
   }

   public <T, R> R mapObject(String fieldID, Class<T> type, Function<T, R> function) throws StreamCorruptedException {
      return function.apply(this.getObject(fieldID, type));
   }

   public <T, R> R mapPrimitive(String fieldID, Class<T> type, Function<T, R> function) throws StreamCorruptedException {
      return function.apply(this.getPrimitive(fieldID, type));
   }

   public static Collection<Field> getFields(Class<?> type) throws NotSerializableException {
      Collection<Field> fields = cache.get(type);
      if (fields != null) {
         return fields;
      } else {
         Collection<Field> var11 = new ArrayList();
         Set<String> ids = new HashSet<>();

         for (Class<?> superClass = type; superClass != null; superClass = superClass.getSuperclass()) {
            Field[] declaredFields = superClass.getDeclaredFields();

            for (Field field : declaredFields) {
               int modifiers = field.getModifiers();
               if (!field.isSynthetic() && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                  String id = Yggdrasil.getID(field);
                  if (ids.contains(id)) {
                     throw new NotSerializableException(type + "/" + superClass + ": duplicate field id '" + id + "'");
                  }

                  field.setAccessible(true);
                  var11.add(field);
                  ids.add(id);
               }
            }
         }

         fields = Collections.unmodifiableCollection(var11);
         cache.put(type, fields);
         return fields;
      }
   }

   public void setFields(Object object) throws StreamCorruptedException, NotSerializableException {
      Yggdrasil yggdrasil = this.yggdrasil;
      if (yggdrasil == null) {
         throw new YggdrasilException("");
      } else {
         Set<Fields.FieldContext> excessive = new HashSet<>(this.fields.values());
         Class<?> type = object.getClass();

         assert type != null;

         for (Field field : getFields(type)) {
            assert field != null;

            String id = Yggdrasil.getID(field);
            Fields.FieldContext context = this.fields.get(id);
            if (context == null) {
               if (!(object instanceof YggdrasilSerializable.YggdrasilRobustSerializable)
                  || !((YggdrasilSerializable.YggdrasilRobustSerializable)object).missingField(field)) {
                  yggdrasil.missingField(object, field);
               }
            } else {
               context.setField(object, field, yggdrasil);
            }

            excessive.remove(context);
         }

         for (Fields.FieldContext context : excessive) {
            assert context != null;

            if (!(object instanceof YggdrasilSerializable.YggdrasilRobustSerializable)
               || !((YggdrasilSerializable.YggdrasilRobustSerializable)object).excessiveField(context)) {
               yggdrasil.excessiveField(object, context);
            }
         }
      }
   }

   @Deprecated(
      since = "2.3.0",
      forRemoval = true
   )
   public void setFields(Object object, Yggdrasil yggdrasil) throws StreamCorruptedException, NotSerializableException {
      assert this.yggdrasil == yggdrasil;

      this.setFields(object);
   }

   public int size() {
      return this.fields.size();
   }

   public void putObject(String fieldID, @Nullable Object value) {
      fieldID = fieldID.toLowerCase(Locale.ENGLISH);
      Fields.FieldContext context = this.fields.computeIfAbsent(fieldID, Fields.FieldContext::new);
      context.setObject(value);
   }

   public void putPrimitive(String fieldID, Object value) {
      fieldID = fieldID.toLowerCase(Locale.ENGLISH);
      Fields.FieldContext context = this.fields.computeIfAbsent(fieldID, Fields.FieldContext::new);
      context.setPrimitive(value);
   }

   public boolean contains(String fieldID) {
      fieldID = fieldID.toLowerCase(Locale.ENGLISH);
      return this.fields.containsKey(fieldID);
   }

   public boolean hasField(String fieldID) {
      fieldID = fieldID.toLowerCase(Locale.ENGLISH);
      return this.fields.containsKey(fieldID);
   }

   @Nullable
   public Object getObject(String field) throws StreamCorruptedException {
      field = field.toLowerCase(Locale.ENGLISH);
      Fields.FieldContext context = this.fields.get(field);
      if (context == null) {
         throw new StreamCorruptedException("Nonexistent field " + field);
      } else {
         return context.getObject();
      }
   }

   @Nullable
   public <T> T getObject(String fieldID, Class<T> expectedType) throws StreamCorruptedException {
      assert !expectedType.isPrimitive();

      fieldID = fieldID.toLowerCase(Locale.ENGLISH);
      Fields.FieldContext context = this.fields.get(fieldID);
      if (context == null) {
         throw new StreamCorruptedException("Nonexistent field " + fieldID);
      } else {
         return context.getObject(expectedType);
      }
   }

   public Object getPrimitive(String fieldID) throws StreamCorruptedException {
      fieldID = fieldID.toLowerCase(Locale.ENGLISH);
      Fields.FieldContext context = this.fields.get(fieldID);
      if (context == null) {
         throw new StreamCorruptedException("Nonexistent field " + fieldID);
      } else {
         return context.getPrimitive();
      }
   }

   public <T> T getPrimitive(String fieldID, Class<T> expectedType) throws StreamCorruptedException {
      assert expectedType.isPrimitive() || Tag.getPrimitiveFromWrapper(expectedType).isPrimitive();

      fieldID = fieldID.toLowerCase(Locale.ENGLISH);
      Fields.FieldContext context = this.fields.get(fieldID);
      if (context == null) {
         throw new StreamCorruptedException("Nonexistent field " + fieldID);
      } else {
         return context.getPrimitive(expectedType);
      }
   }

   @Nullable
   public <T> T getAndRemoveObject(String field, Class<T> expectedType) throws StreamCorruptedException {
      T object = this.getObject(field, expectedType);
      this.removeField(field);
      return object;
   }

   public <T> T getAndRemovePrimitive(String field, Class<T> expectedType) throws StreamCorruptedException {
      T object = this.getPrimitive(field, expectedType);
      this.removeField(field);
      return object;
   }

   public boolean removeField(String fieldID) {
      fieldID = fieldID.toLowerCase(Locale.ENGLISH);
      return this.fields.remove(fieldID) != null;
   }

   @Override
   public Iterator<Fields.FieldContext> iterator() {
      return this.fields.values().iterator();
   }

   @NotThreadSafe
   public static final class FieldContext {
      final String id;
      @Nullable
      private Object value;
      private boolean isPrimitiveValue;

      FieldContext(String id) {
         this.id = id.toLowerCase(Locale.ENGLISH);
      }

      FieldContext(Field field, Object object) throws IllegalArgumentException, IllegalAccessException {
         this.id = Yggdrasil.getID(field);
         this.value = field.get(object);
         this.isPrimitiveValue = field.getType().isPrimitive();
      }

      public String getID() {
         return this.id;
      }

      public boolean isPrimitive() {
         return this.isPrimitiveValue;
      }

      @Nullable
      public Class<?> getType() {
         Object value = this.value;
         if (value == null) {
            return null;
         } else {
            Class<?> type = value.getClass();

            assert type != null;

            return this.isPrimitiveValue ? Tag.getPrimitiveFromWrapper(type).type : type;
         }
      }

      @Nullable
      public Object getObject() throws StreamCorruptedException {
         if (this.isPrimitiveValue) {
            throw new StreamCorruptedException("field " + this.id + " is a primitive, but expected an object");
         } else {
            return this.value;
         }
      }

      @Nullable
      public <T> T getObject(Class<T> expectedType) throws StreamCorruptedException {
         if (this.isPrimitiveValue) {
            throw new StreamCorruptedException("field " + this.id + " is a primitive, but expected " + expectedType);
         } else {
            Object value = this.value;
            if (value != null && !expectedType.isInstance(value)) {
               throw new StreamCorruptedException("Field " + this.id + " of " + value.getClass() + ", but expected " + expectedType);
            } else {
               return (T)value;
            }
         }
      }

      public Object getPrimitive() throws StreamCorruptedException {
         if (!this.isPrimitiveValue) {
            throw new StreamCorruptedException("field " + this.id + " is not a primitive, but expected one");
         } else {
            assert this.value != null;

            return this.value;
         }
      }

      public <T> T getPrimitive(Class<T> expectedType) throws StreamCorruptedException {
         if (!this.isPrimitiveValue) {
            throw new StreamCorruptedException("field " + this.id + " is not a primitive, but expected " + expectedType);
         } else {
            assert expectedType.isPrimitive() || Tag.isWrapper(expectedType);

            Object value = this.value;

            assert value != null;

            if (expectedType.isPrimitive() ? Tag.getWrapperClass(expectedType).isInstance(value) : expectedType.isInstance(value)) {
               return (T)value;
            } else {
               throw new StreamCorruptedException("Field " + this.id + " of " + value.getClass() + ", but expected " + expectedType);
            }
         }
      }

      public void setObject(@Nullable Object value) {
         this.value = value;
         this.isPrimitiveValue = false;
      }

      public void setPrimitive(Object value) {
         assert Tag.isWrapper(value.getClass());

         this.value = value;
         this.isPrimitiveValue = true;
      }

      public void setField(Object object, Field field, Yggdrasil yggdrasil) throws StreamCorruptedException {
         if (Modifier.isStatic(field.getModifiers())) {
            throw new StreamCorruptedException("The field " + this.id + " of " + field.getDeclaringClass() + " is static");
         } else if (Modifier.isTransient(field.getModifiers())) {
            throw new StreamCorruptedException("The field " + this.id + " of " + field.getDeclaringClass() + " is transient");
         } else if (field.getType().isPrimitive() != this.isPrimitiveValue) {
            throw new StreamCorruptedException(
               "The field " + this.id + " of " + field.getDeclaringClass() + " is " + (field.getType().isPrimitive() ? "" : "not ") + "primitive"
            );
         } else {
            try {
               field.setAccessible(true);
               field.set(object, this.value);
            } catch (IllegalArgumentException var5) {
               if (!(object instanceof YggdrasilSerializable.YggdrasilRobustSerializable)
                  || !((YggdrasilSerializable.YggdrasilRobustSerializable)object).incompatibleField(field, this)) {
                  yggdrasil.incompatibleField(object, field, this);
               }
            } catch (IllegalAccessException var6) {
               assert false;
            }
         }
      }

      @Override
      public int hashCode() {
         return this.id.hashCode();
      }

      @Override
      public boolean equals(@Nullable Object object) {
         if (this == object) {
            return true;
         } else if (object == null) {
            return false;
         } else {
            return !(object instanceof Fields.FieldContext other) ? false : this.id.equals(other.id);
         }
      }
   }
}
