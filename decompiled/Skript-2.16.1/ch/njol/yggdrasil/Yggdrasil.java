package ch.njol.yggdrasil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.concurrent.NotThreadSafe;
import org.jetbrains.annotations.Nullable;

@NotThreadSafe
public final class Yggdrasil {
   public static final int MAGIC_NUMBER = 1499948800;
   public static final short LATEST_VERSION = 1;
   public final short version;
   private final List<ClassResolver> classResolvers = new ArrayList<>();
   private final List<FieldHandler> fieldHandlers = new ArrayList<>();
   private final SimpleClassResolver simpleClassResolver = new SimpleClassResolver();

   public Yggdrasil() {
      this((short)1);
   }

   public Yggdrasil(short version) {
      if (version > 0 && version <= 1) {
         this.version = version;
         this.classResolvers.add(new JRESerializer());
         this.classResolvers.add(this.simpleClassResolver);
      } else {
         throw new YggdrasilException("Unsupported version number");
      }
   }

   public YggdrasilOutputStream newOutputStream(OutputStream out) throws IOException {
      return new DefaultYggdrasilOutputStream(this, out);
   }

   public YggdrasilInputStream newInputStream(InputStream in) throws IOException {
      return new DefaultYggdrasilInputStream(this, in);
   }

   public void registerClassResolver(ClassResolver resolver) {
      if (!this.classResolvers.contains(resolver)) {
         this.classResolvers.add(resolver);
      }
   }

   public void registerSingleClass(Class<?> type, String id) {
      this.simpleClassResolver.registerClass(type, id);
   }

   public void registerSingleClass(Class<?> type) {
      YggdrasilID id = type.getAnnotation(YggdrasilID.class);
      if (id == null) {
         throw new IllegalArgumentException(type.toString());
      } else {
         this.simpleClassResolver.registerClass(type, id.value());
      }
   }

   public void registerFieldHandler(FieldHandler handler) {
      if (!this.fieldHandlers.contains(handler)) {
         this.fieldHandlers.add(handler);
      }
   }

   public boolean isSerializable(Class<?> type) {
      try {
         return type.isPrimitive()
            || type == Object.class
            || (Enum.class.isAssignableFrom(type) || PseudoEnum.class.isAssignableFrom(type)) && this.getIDNoError(type) != null
            || (YggdrasilSerializable.class.isAssignableFrom(type) || this.getSerializer(type) != null) && this.newInstance(type) != type;
      } catch (StreamCorruptedException var3) {
         return false;
      } catch (NotSerializableException var4) {
         return false;
      }
   }

   @Nullable
   YggdrasilSerializer<?> getSerializer(Class<?> type) {
      for (ClassResolver resolver : this.classResolvers) {
         if (resolver instanceof YggdrasilSerializer && resolver.getID(type) != null) {
            return (YggdrasilSerializer<?>)resolver;
         }
      }

      return null;
   }

   public Class<?> getClass(String id) throws StreamCorruptedException {
      if ("Object".equals(id)) {
         return Object.class;
      } else {
         for (ClassResolver resolver : this.classResolvers) {
            Class<?> type = resolver.getClass(id);
            if (type != null) {
               if ($assertionsDisabled || Tag.byName(id) == null && (Tag.getType(type) == Tag.T_OBJECT || Tag.getType(type) == Tag.T_ENUM)) {
                  assert id.equals(resolver.getID(type)) : resolver
                     + " returned "
                     + type
                     + " for id "
                     + id
                     + ", but returns id "
                     + resolver.getID(type)
                     + " for that class";

                  return type;
               }

               throw new AssertionError("Tag IDs should not be matched: " + id + " (class resolver: " + resolver + ")");
            }
         }

         throw new StreamCorruptedException("No class found for ID " + id);
      }
   }

   @Nullable
   private String getIDNoError(Class<?> type) {
      if (type == Object.class) {
         return "Object";
      } else {
         assert Tag.getType(type) == Tag.T_OBJECT || Tag.getType(type) == Tag.T_ENUM;

         if (Enum.class.isAssignableFrom(type) && type.getSuperclass() != Enum.class) {
            Class<?> s = type.getSuperclass();

            assert s != null;

            type = s;
         }

         if (PseudoEnum.class.isAssignableFrom(type)) {
            type = PseudoEnum.getDeclaringClass(type);
         }

         for (ClassResolver resolver : this.classResolvers) {
            String id = resolver.getID(type);
            if (id != null) {
               assert Tag.byName(id) == null : "Class IDs should not match Tag IDs: " + id + " (class resolver: " + resolver + ")";

               Class<?> c2 = resolver.getClass(id);
               if ($assertionsDisabled
                  || c2 != null && (resolver instanceof YggdrasilSerializer ? id.equals(resolver.getID(c2)) : resolver.getClass(id) == type)) {
                  return id;
               }

               throw new AssertionError(resolver + " returned id " + id + " for " + type + ", but returns " + c2 + " for that id");
            }
         }

         return null;
      }
   }

   public String getID(Class<?> type) throws NotSerializableException {
      String id = this.getIDNoError(type);
      if (id == null) {
         throw new NotSerializableException("No ID found for " + type);
      } else if (!this.isSerializable(type)) {
         throw new NotSerializableException(type.getCanonicalName());
      } else {
         return id;
      }
   }

   public static String getID(Field field) {
      YggdrasilID yid = field.getAnnotation(YggdrasilID.class);
      return yid != null ? yid.value().toLowerCase(Locale.ENGLISH) : field.getName().toLowerCase(Locale.ENGLISH);
   }

   public static String getID(Enum<?> e) {
      try {
         return getID(e.getDeclaringClass().getDeclaredField(e.name()));
      } catch (NoSuchFieldException var2) {
         assert false : e;

         return e.name().toLowerCase(Locale.ENGLISH);
      }
   }

   public static <T extends Enum<T>> Enum<T> getEnumConstant(Class<T> type, String id) throws StreamCorruptedException {
      Field[] fields = type.getDeclaredFields();

      for (Field field : fields) {
         assert field != null;

         if (getID(field).equalsIgnoreCase(id)) {
            return Enum.valueOf(type, field.getName());
         }
      }

      if (YggdrasilSerializable.YggdrasilRobustEnum.class.isAssignableFrom(type)) {
         Object[] constants = type.getEnumConstants();
         if (constants.length == 0) {
            throw new StreamCorruptedException(type + " does not have any enum constants");
         } else {
            Enum<?> e = ((YggdrasilSerializable.YggdrasilRobustEnum)constants[0]).excessiveConstant(id);
            if (!type.isInstance(e)) {
               throw new YggdrasilException(type + " returned a foreign enum constant: " + e.getClass() + "." + e);
            } else {
               return (Enum<T>)e;
            }
         }
      } else {
         throw new StreamCorruptedException("Enum constant " + id + " does not exist in " + type);
      }
   }

   public void excessiveField(Object object, Fields.FieldContext field) throws StreamCorruptedException {
      for (FieldHandler handler : this.fieldHandlers) {
         if (handler.excessiveField(object, field)) {
            return;
         }
      }

      throw new StreamCorruptedException("Excessive field " + field.id + " in class " + object.getClass().getCanonicalName() + " was not handled");
   }

   public void missingField(Object object, Field field) throws StreamCorruptedException {
      for (FieldHandler handler : this.fieldHandlers) {
         if (handler.missingField(object, field)) {
            return;
         }
      }

      throw new StreamCorruptedException("Missing field " + getID(field) + " in class " + object.getClass().getCanonicalName() + " was not handled");
   }

   public void incompatibleField(Object object, Field field, Fields.FieldContext context) throws StreamCorruptedException {
      for (FieldHandler handler : this.fieldHandlers) {
         if (handler.incompatibleField(object, field, context)) {
            return;
         }
      }

      throw new StreamCorruptedException(
         "Incompatible field "
            + getID(field)
            + " in class "
            + object.getClass().getCanonicalName()
            + " of incompatible "
            + context.getType()
            + " was not handled"
      );
   }

   public void saveToFile(Object object, File file) throws IOException {
      try (
         FileOutputStream fout = new FileOutputStream(file);
         YggdrasilOutputStream yout = this.newOutputStream(fout);
      ) {
         yout.writeObject(object);
         yout.flush();
      }
   }

   @Nullable
   public <T> T loadFromFile(File file, Class<T> expectedType) throws IOException {
      Object var5;
      try (
         FileInputStream fin = new FileInputStream(file);
         YggdrasilInputStream yin = this.newInputStream(fin);
      ) {
         var5 = yin.readObject(expectedType);
      }

      return (T)var5;
   }

   @Nullable
   Object newInstance(Class<?> type) throws StreamCorruptedException, NotSerializableException {
      YggdrasilSerializer serializer = this.getSerializer(type);
      if (serializer != null) {
         if (!serializer.canBeInstantiated(type)) {
            try {
               serializer.deserialize(type, new Fields(this));
            } catch (StreamCorruptedException var4) {
            }

            return null;
         } else {
            Object o = serializer.newInstance(type);
            if (o == null) {
               throw new YggdrasilException("YggdrasilSerializer " + serializer + " returned null from newInstance(" + type + ")");
            } else {
               return o;
            }
         }
      } else {
         try {
            Constructor<?> constr = type.getDeclaredConstructor();
            constr.setAccessible(true);
            return constr.newInstance();
         } catch (NoSuchMethodException var5) {
            throw new StreamCorruptedException("Cannot create an instance of " + type + " because it has no nullary constructor");
         } catch (SecurityException var6) {
            throw new StreamCorruptedException("Cannot create an instance of " + type + " because the security manager didn't allow it");
         } catch (InstantiationException var7) {
            throw new StreamCorruptedException("Cannot create an instance of " + type + " because it is abstract");
         } catch (IllegalArgumentException | IllegalAccessException var8) {
            var8.printStackTrace();

            assert false;

            return null;
         } catch (InvocationTargetException var9) {
            throw new RuntimeException(var9);
         }
      }
   }
}
