package net.ess3.nms.refl;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;

public final class ReflUtil {
   public static final ReflUtil.NMSVersion V1_12_R1 = ReflUtil.NMSVersion.fromString("v1_12_R1");
   public static final ReflUtil.NMSVersion V1_11_R1 = ReflUtil.NMSVersion.fromString("v1_11_R1");
   public static final ReflUtil.NMSVersion V1_17_R1 = ReflUtil.NMSVersion.fromString("v1_17_R1");
   public static final ReflUtil.NMSVersion V1_18_R1 = ReflUtil.NMSVersion.fromString("v1_18_R1");
   public static final ReflUtil.NMSVersion V1_19_R1 = ReflUtil.NMSVersion.fromString("v1_19_R1");
   public static final ReflUtil.NMSVersion V1_19_R2 = ReflUtil.NMSVersion.fromString("v1_19_R2");
   public static final ReflUtil.NMSVersion V1_20_R4 = ReflUtil.NMSVersion.fromString("v1_20_R4");
   public static final ReflUtil.NMSVersion V1_21_R6 = ReflUtil.NMSVersion.fromString("v1_21_R6");
   public static final ReflUtil.NMSVersion V1_21_R7 = ReflUtil.NMSVersion.fromString("v1_21_R7");
   private static final Map<String, Class<?>> classCache = new HashMap<>();
   private static final Table<Class<?>, String, Method> methodCache = HashBasedTable.create();
   private static final Table<Class<?>, ReflUtil.MethodParams, Method> methodParamCache = HashBasedTable.create();
   private static final Table<Class<?>, String, Field> fieldCache = HashBasedTable.create();
   private static final Map<Class<?>, Constructor<?>> constructorCache = new HashMap<>();
   private static final Table<Class<?>, ReflUtil.ConstructorParams, Constructor<?>> constructorParamCache = HashBasedTable.create();
   private static ReflUtil.NMSVersion nmsVersionObject;
   private static String nmsVersion;

   private ReflUtil() {
   }

   public static String getNMSVersion() {
      if (nmsVersion == null) {
         String name = Bukkit.getServer().getClass().getName();
         String[] parts = name.split("\\.");
         if (parts.length > 3) {
            return nmsVersion = parts[3];
         } else {
            nmsVersion = "";
            return "";
         }
      } else {
         return nmsVersion;
      }
   }

   public static ReflUtil.NMSVersion getNmsVersionObject() {
      if (nmsVersionObject == null) {
         try {
            if (getNMSVersion().equals("ServerMock")) {
               return nmsVersionObject = new ReflUtil.NMSVersion(99, 99, 99);
            }

            nmsVersionObject = ReflUtil.NMSVersion.fromString(getNMSVersion());
         } catch (IllegalArgumentException var3) {
            try {
               Class.forName("org.bukkit.craftbukkit.CraftServer");
               nmsVersionObject = new ReflUtil.NMSVersion(99, 99, 99);
            } catch (ClassNotFoundException var2) {
               throw var3;
            }
         }
      }

      return nmsVersionObject;
   }

   public static boolean isMojMap() {
      return getNmsVersionObject().getMajor() == 99;
   }

   public static Class<?> getNMSClass(final String className) {
      return getClassCached("net.minecraft.server" + (getNmsVersionObject().isLowerThan(V1_17_R1) ? "." + getNMSVersion() : "") + "." + className);
   }

   public static Class<?> getOBCClass(final String className) {
      return getClassCached("org.bukkit.craftbukkit" + (getNmsVersionObject().getMajor() == 99 ? "" : "." + getNMSVersion()) + "." + className);
   }

   public static Class<?> getClassCached(final String className) {
      if (classCache.containsKey(className)) {
         return classCache.get(className);
      } else {
         try {
            Class<?> classForName = Class.forName(className);
            classCache.put(className, classForName);
            return classForName;
         } catch (ClassNotFoundException var2) {
            return null;
         }
      }
   }

   public static Method getMethodCached(final Class<?> clazz, final String methodName) {
      if (methodCache.contains(clazz, methodName)) {
         return (Method)methodCache.get(clazz, methodName);
      } else {
         try {
            Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            methodCache.put(clazz, methodName, method);
            return method;
         } catch (NoSuchMethodException var3) {
            return null;
         }
      }
   }

   public static Method getMethodCached(final Class<?> clazz, final String methodName, final Class<?>... params) {
      ReflUtil.MethodParams methodParams = new ReflUtil.MethodParams(methodName, params);
      if (methodParamCache.contains(clazz, methodParams)) {
         return (Method)methodParamCache.get(clazz, methodParams);
      } else {
         try {
            Method method = clazz.getDeclaredMethod(methodName, params);
            method.setAccessible(true);
            methodParamCache.put(clazz, methodParams, method);
            return method;
         } catch (NoSuchMethodException var5) {
            return null;
         }
      }
   }

   public static Field getFieldCached(final Class<?> clazz, final String fieldName) {
      if (fieldCache.contains(clazz, fieldName)) {
         return (Field)fieldCache.get(clazz, fieldName);
      } else {
         try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            fieldCache.put(clazz, fieldName, field);
            return field;
         } catch (NoSuchFieldException var3) {
            return null;
         }
      }
   }

   public static Constructor<?> getConstructorCached(final Class<?> clazz) {
      if (constructorCache.containsKey(clazz)) {
         return constructorCache.get(clazz);
      } else {
         try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructorCache.put(clazz, constructor);
            return constructor;
         } catch (NoSuchMethodException var2) {
            return null;
         }
      }
   }

   public static Constructor<?> getConstructorCached(final Class<?> clazz, final Class<?>... params) {
      ReflUtil.ConstructorParams constructorParams = new ReflUtil.ConstructorParams(params);
      if (constructorParamCache.contains(clazz, constructorParams)) {
         return (Constructor<?>)constructorParamCache.get(clazz, constructorParams);
      } else {
         try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(params);
            constructor.setAccessible(true);
            constructorParamCache.put(clazz, constructorParams, constructor);
            return constructor;
         } catch (NoSuchMethodException var4) {
            return null;
         }
      }
   }

   private static class ConstructorParams {
      private final Class<?>[] params;

      ConstructorParams(final Class<?>[] params) {
         this.params = params;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ReflUtil.ConstructorParams that = (ReflUtil.ConstructorParams)o;
            return Arrays.deepEquals(this.params, that.params);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Arrays.deepHashCode(this.params);
      }
   }

   private static class MethodParams {
      private final String name;
      private final Class<?>[] params;

      MethodParams(final String name, final Class<?>[] params) {
         this.name = name;
         this.params = params;
      }

      @Override
      public boolean equals(final Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ReflUtil.MethodParams)) {
            return false;
         } else {
            ReflUtil.MethodParams that = (ReflUtil.MethodParams)o;
            if (!that.canEqual(this)) {
               return false;
            } else {
               Object thisName = this.name;
               Object thatName = that.name;
               if (thisName == null) {
                  if (thatName == null) {
                     return Arrays.deepEquals(this.params, that.params);
                  }
               } else if (thisName.equals(thatName)) {
                  return Arrays.deepEquals(this.params, that.params);
               }

               return false;
            }
         }
      }

      boolean canEqual(final Object that) {
         return that instanceof ReflUtil.MethodParams;
      }

      @Override
      public int hashCode() {
         int result = 1;
         Object thisName = this.name;
         result = result * 31 + (thisName == null ? 0 : thisName.hashCode());
         return result * 31 + Arrays.deepHashCode(this.params);
      }
   }

   public static final class NMSVersion implements Comparable<ReflUtil.NMSVersion> {
      private static final Pattern VERSION_PATTERN = Pattern.compile("^v(\\d+)_(\\d+)_R(\\d+)");
      private final int major;
      private final int minor;
      private final int release;

      private NMSVersion(final int major, final int minor, final int release) {
         this.major = major;
         this.minor = minor;
         this.release = release;
      }

      public static ReflUtil.NMSVersion fromString(final String string) {
         Preconditions.checkNotNull(string, "string cannot be null.");
         Matcher matcher = VERSION_PATTERN.matcher(string);
         if (!matcher.matches()) {
            if (!Bukkit.getName().equals("Essentials Fake Server")) {
               throw new IllegalArgumentException(string + " is not in valid version format. e.g. v1_10_R1");
            }

            matcher = VERSION_PATTERN.matcher(ReflUtil.V1_12_R1.toString());
            Preconditions.checkArgument(matcher.matches(), string + " is not in valid version format. e.g. v1_10_R1");
         }

         return new ReflUtil.NMSVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
      }

      public boolean isHigherThan(final ReflUtil.NMSVersion o) {
         return this.compareTo(o) > 0;
      }

      public boolean isHigherThanOrEqualTo(final ReflUtil.NMSVersion o) {
         return this.compareTo(o) >= 0;
      }

      public boolean isLowerThan(final ReflUtil.NMSVersion o) {
         return this.compareTo(o) < 0;
      }

      public boolean isLowerThanOrEqualTo(final ReflUtil.NMSVersion o) {
         return this.compareTo(o) <= 0;
      }

      public int getMajor() {
         return this.major;
      }

      public int getMinor() {
         return this.minor;
      }

      public int getRelease() {
         return this.release;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ReflUtil.NMSVersion that = (ReflUtil.NMSVersion)o;
            return this.major == that.major && this.minor == that.minor && this.release == that.release;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(new Object[]{this.major, this.minor, this.release});
      }

      @Override
      public String toString() {
         return "v" + this.major + "_" + this.minor + "_R" + this.release;
      }

      public int compareTo(final ReflUtil.NMSVersion o) {
         if (this.major < o.major) {
            return -1;
         } else if (this.major > o.major) {
            return 1;
         } else if (this.minor < o.minor) {
            return -1;
         } else {
            return this.minor > o.minor ? 1 : Integer.compare(this.release, o.release);
         }
      }
   }
}
