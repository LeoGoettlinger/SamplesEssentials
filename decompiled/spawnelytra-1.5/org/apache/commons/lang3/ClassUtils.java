package org.apache.commons.lang3;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ClassUtils {
   private static final int MAX_DIMENSIONS = 255;
   private static final Comparator<Class<?>> COMPARATOR = (o1, o2) -> Objects.compare(getName(o1), getName(o2), String::compareTo);
   public static final char PACKAGE_SEPARATOR_CHAR = '.';
   public static final String PACKAGE_SEPARATOR = String.valueOf('.');
   public static final char INNER_CLASS_SEPARATOR_CHAR = '$';
   public static final String INNER_CLASS_SEPARATOR = String.valueOf('$');
   private static final Map<String, Class<?>> NAME_PRIMITIVE_MAP = new HashMap<>();
   private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap<>();
   private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP = new HashMap<>();
   private static final Map<String, String> ABBREVIATION_MAP;
   private static final Map<String, String> REVERSE_ABBREVIATION_MAP;

   public static Comparator<Class<?>> comparator() {
      return COMPARATOR;
   }

   public static List<String> convertClassesToClassNames(List<Class<?>> classes) {
      return classes == null ? null : classes.stream().map(e -> getName((Class<?>)e, null)).collect(Collectors.toList());
   }

   public static List<Class<?>> convertClassNamesToClasses(List<String> classNames) {
      if (classNames == null) {
         return null;
      } else {
         List<Class<?>> classes = new ArrayList<>(classNames.size());
         classNames.forEach(className -> {
            try {
               classes.add(Class.forName(className));
            } catch (Exception var3) {
               classes.add(null);
            }
         });
         return classes;
      }
   }

   public static String getAbbreviatedName(Class<?> cls, int lengthHint) {
      return cls == null ? "" : getAbbreviatedName(cls.getName(), lengthHint);
   }

   public static String getAbbreviatedName(String className, int lengthHint) {
      if (lengthHint <= 0) {
         throw new IllegalArgumentException("len must be > 0");
      } else if (className == null) {
         return "";
      } else if (className.length() <= lengthHint) {
         return className;
      } else {
         char[] abbreviated = className.toCharArray();
         int target = 0;
         int source = 0;

         while (source < abbreviated.length) {
            int runAheadTarget = target;

            while (source < abbreviated.length && abbreviated[source] != '.') {
               abbreviated[runAheadTarget++] = abbreviated[source++];
            }

            if (useFull(runAheadTarget, source, abbreviated.length, lengthHint) || ++target > runAheadTarget) {
               target = runAheadTarget;
            }

            if (source < abbreviated.length) {
               abbreviated[target++] = abbreviated[source++];
            }
         }

         return new String(abbreviated, 0, target);
      }
   }

   public static List<Class<?>> getAllInterfaces(Class<?> cls) {
      if (cls == null) {
         return null;
      } else {
         LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<>();
         getAllInterfaces(cls, interfacesFound);
         return new ArrayList<>(interfacesFound);
      }
   }

   private static void getAllInterfaces(Class<?> cls, HashSet<Class<?>> interfacesFound) {
      while (cls != null) {
         Class<?>[] interfaces = cls.getInterfaces();

         for (Class<?> i : interfaces) {
            if (interfacesFound.add(i)) {
               getAllInterfaces(i, interfacesFound);
            }
         }

         cls = cls.getSuperclass();
      }
   }

   public static List<Class<?>> getAllSuperclasses(Class<?> cls) {
      if (cls == null) {
         return null;
      } else {
         List<Class<?>> classes = new ArrayList<>();

         for (Class<?> superclass = cls.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
            classes.add(superclass);
         }

         return classes;
      }
   }

   public static String getCanonicalName(Class<?> cls) {
      return getCanonicalName(cls, "");
   }

   public static String getCanonicalName(Class<?> cls, String valueIfNull) {
      if (cls == null) {
         return valueIfNull;
      } else {
         String canonicalName = cls.getCanonicalName();
         return canonicalName == null ? valueIfNull : canonicalName;
      }
   }

   public static String getCanonicalName(Object object) {
      return getCanonicalName(object, "");
   }

   public static String getCanonicalName(Object object, String valueIfNull) {
      if (object == null) {
         return valueIfNull;
      } else {
         String canonicalName = object.getClass().getCanonicalName();
         return canonicalName == null ? valueIfNull : canonicalName;
      }
   }

   private static String getCanonicalName(String name) {
      String className = StringUtils.deleteWhitespace(name);
      if (className == null) {
         return null;
      } else {
         int dim = 0;

         while (className.charAt(dim) == '[') {
            if (++dim > 255) {
               throw new IllegalArgumentException(String.format("Maximum array dimension %d exceeded", 255));
            }
         }

         if (dim < 1) {
            return className;
         } else {
            className = className.substring(dim);
            if (className.startsWith("L")) {
               className = className.substring(1, className.endsWith(";") ? className.length() - 1 : className.length());
            } else if (!className.isEmpty()) {
               className = REVERSE_ABBREVIATION_MAP.get(className.substring(0, 1));
            }

            StringBuilder canonicalClassNameBuffer = new StringBuilder(className.length() + dim * 2);
            canonicalClassNameBuffer.append(className);

            for (int i = 0; i < dim; i++) {
               canonicalClassNameBuffer.append("[]");
            }

            return canonicalClassNameBuffer.toString();
         }
      }
   }

   public static Class<?> getClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
      return getClass(classLoader, className, true);
   }

   public static Class<?> getClass(ClassLoader classLoader, String className, boolean initialize) throws ClassNotFoundException {
      String next = className;
      int lastDotIndex = -1;

      while (true) {
         try {
            Class<?> clazz = getPrimitiveClass(next);
            return clazz != null ? clazz : Class.forName(toCanonicalName(next), initialize, classLoader);
         } catch (ClassNotFoundException var6) {
            lastDotIndex = next.lastIndexOf(46);
            if (lastDotIndex != -1) {
               next = next.substring(0, lastDotIndex) + '$' + next.substring(lastDotIndex + 1);
            }

            if (lastDotIndex == -1) {
               throw new ClassNotFoundException(next);
            }
         }
      }
   }

   public static Class<?> getClass(String className) throws ClassNotFoundException {
      return getClass(className, true);
   }

   public static Class<?> getClass(String className, boolean initialize) throws ClassNotFoundException {
      ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
      ClassLoader loader = contextCL == null ? ClassUtils.class.getClassLoader() : contextCL;
      return getClass(loader, className, initialize);
   }

   public static <T> Class<T> getComponentType(Class<T[]> cls) {
      return (Class<T>)(cls == null ? null : cls.getComponentType());
   }

   public static String getName(Class<?> cls) {
      return getName(cls, "");
   }

   public static String getName(Class<?> cls, String valueIfNull) {
      return cls == null ? valueIfNull : cls.getName();
   }

   public static String getName(Object object) {
      return getName(object, "");
   }

   public static String getName(Object object, String valueIfNull) {
      return object == null ? valueIfNull : object.getClass().getName();
   }

   public static String getPackageCanonicalName(Class<?> cls) {
      return cls == null ? "" : getPackageCanonicalName(cls.getName());
   }

   public static String getPackageCanonicalName(Object object, String valueIfNull) {
      return object == null ? valueIfNull : getPackageCanonicalName(object.getClass().getName());
   }

   public static String getPackageCanonicalName(String name) {
      return getPackageName(getCanonicalName(name));
   }

   public static String getPackageName(Class<?> cls) {
      return cls == null ? "" : getPackageName(cls.getName());
   }

   public static String getPackageName(Object object, String valueIfNull) {
      return object == null ? valueIfNull : getPackageName(object.getClass());
   }

   public static String getPackageName(String className) {
      if (StringUtils.isEmpty(className)) {
         return "";
      } else {
         while (className.charAt(0) == '[') {
            className = className.substring(1);
         }

         if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
            className = className.substring(1);
         }

         int i = className.lastIndexOf(46);
         return i == -1 ? "" : className.substring(0, i);
      }
   }

   static Class<?> getPrimitiveClass(String className) {
      return NAME_PRIMITIVE_MAP.get(className);
   }

   public static Method getPublicMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
      Method declaredMethod = cls.getMethod(methodName, parameterTypes);
      if (isPublic(declaredMethod.getDeclaringClass())) {
         return declaredMethod;
      } else {
         List<Class<?>> candidateClasses = new ArrayList<>(getAllInterfaces(cls));
         candidateClasses.addAll(getAllSuperclasses(cls));

         for (Class<?> candidateClass : candidateClasses) {
            if (isPublic(candidateClass)) {
               Method candidateMethod;
               try {
                  candidateMethod = candidateClass.getMethod(methodName, parameterTypes);
               } catch (NoSuchMethodException var9) {
                  continue;
               }

               if (Modifier.isPublic(candidateMethod.getDeclaringClass().getModifiers())) {
                  return candidateMethod;
               }
            }
         }

         throw new NoSuchMethodException("Can't find a public method for " + methodName + " " + ArrayUtils.toString(parameterTypes));
      }
   }

   public static String getShortCanonicalName(Class<?> cls) {
      return cls == null ? "" : getShortCanonicalName(cls.getCanonicalName());
   }

   public static String getShortCanonicalName(Object object, String valueIfNull) {
      return object == null ? valueIfNull : getShortCanonicalName(object.getClass().getCanonicalName());
   }

   public static String getShortCanonicalName(String canonicalName) {
      return getShortClassName(getCanonicalName(canonicalName));
   }

   public static String getShortClassName(Class<?> cls) {
      return cls == null ? "" : getShortClassName(cls.getName());
   }

   public static String getShortClassName(Object object, String valueIfNull) {
      return object == null ? valueIfNull : getShortClassName(object.getClass());
   }

   public static String getShortClassName(String className) {
      if (StringUtils.isEmpty(className)) {
         return "";
      } else {
         StringBuilder arrayPrefix = new StringBuilder();
         if (className.startsWith("[")) {
            while (className.charAt(0) == '[') {
               className = className.substring(1);
               arrayPrefix.append("[]");
            }

            if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
               className = className.substring(1, className.length() - 1);
            }

            if (REVERSE_ABBREVIATION_MAP.containsKey(className)) {
               className = REVERSE_ABBREVIATION_MAP.get(className);
            }
         }

         int lastDotIdx = className.lastIndexOf(46);
         int innerIdx = className.indexOf(36, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
         String out = className.substring(lastDotIdx + 1);
         if (innerIdx != -1) {
            out = out.replace('$', '.');
         }

         return out + arrayPrefix;
      }
   }

   public static String getSimpleName(Class<?> cls) {
      return getSimpleName(cls, "");
   }

   public static String getSimpleName(Class<?> cls, String valueIfNull) {
      return cls == null ? valueIfNull : cls.getSimpleName();
   }

   public static String getSimpleName(Object object) {
      return getSimpleName(object, "");
   }

   public static String getSimpleName(Object object, String valueIfNull) {
      return object == null ? valueIfNull : object.getClass().getSimpleName();
   }

   public static Iterable<Class<?>> hierarchy(Class<?> type) {
      return hierarchy(type, ClassUtils.Interfaces.EXCLUDE);
   }

   public static Iterable<Class<?>> hierarchy(Class<?> type, ClassUtils.Interfaces interfacesBehavior) {
      Iterable<Class<?>> classes = () -> {
         final AtomicReference<Class<?>> next = new AtomicReference<>(type);
         return new Iterator<Class<?>>() {
            @Override
            public boolean hasNext() {
               return next.get() != null;
            }

            public Class<?> next() {
               return next.getAndUpdate(Class::getSuperclass);
            }

            @Override
            public void remove() {
               throw new UnsupportedOperationException();
            }
         };
      };
      return interfacesBehavior != ClassUtils.Interfaces.INCLUDE ? classes : () -> {
         final Set<Class<?>> seenInterfaces = new HashSet<>();
         final Iterator<Class<?>> wrapped = classes.iterator();
         return new Iterator<Class<?>>() {
            Iterator<Class<?>> interfaces = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
               return this.interfaces.hasNext() || wrapped.hasNext();
            }

            public Class<?> next() {
               if (this.interfaces.hasNext()) {
                  Class<?> nextInterface = this.interfaces.next();
                  seenInterfaces.add(nextInterface);
                  return nextInterface;
               } else {
                  Class<?> nextSuperclass = wrapped.next();
                  Set<Class<?>> currentInterfaces = new LinkedHashSet<>();
                  this.walkInterfaces(currentInterfaces, nextSuperclass);
                  this.interfaces = currentInterfaces.iterator();
                  return nextSuperclass;
               }
            }

            @Override
            public void remove() {
               throw new UnsupportedOperationException();
            }

            private void walkInterfaces(Set<Class<?>> addTo, Class<?> c) {
               for (Class<?> iface : c.getInterfaces()) {
                  if (!seenInterfaces.contains(iface)) {
                     addTo.add(iface);
                  }

                  this.walkInterfaces(addTo, iface);
               }
            }
         };
      };
   }

   public static boolean isAssignable(Class<?> cls, Class<?> toClass) {
      return isAssignable(cls, toClass, true);
   }

   public static boolean isAssignable(Class<?> cls, Class<?> toClass, boolean autoboxing) {
      if (toClass == null) {
         return false;
      } else if (cls == null) {
         return !toClass.isPrimitive();
      } else {
         if (autoboxing) {
            if (cls.isPrimitive() && !toClass.isPrimitive()) {
               cls = primitiveToWrapper(cls);
               if (cls == null) {
                  return false;
               }
            }

            if (toClass.isPrimitive() && !cls.isPrimitive()) {
               cls = wrapperToPrimitive(cls);
               if (cls == null) {
                  return false;
               }
            }
         }

         if (cls.equals(toClass)) {
            return true;
         } else if (cls.isPrimitive()) {
            if (!toClass.isPrimitive()) {
               return false;
            } else if (int.class.equals(cls)) {
               return long.class.equals(toClass) || float.class.equals(toClass) || double.class.equals(toClass);
            } else if (long.class.equals(cls)) {
               return float.class.equals(toClass) || double.class.equals(toClass);
            } else if (boolean.class.equals(cls)) {
               return false;
            } else if (double.class.equals(cls)) {
               return false;
            } else if (float.class.equals(cls)) {
               return double.class.equals(toClass);
            } else if (!char.class.equals(cls) && !short.class.equals(cls)) {
               return !byte.class.equals(cls)
                  ? false
                  : short.class.equals(toClass)
                     || int.class.equals(toClass)
                     || long.class.equals(toClass)
                     || float.class.equals(toClass)
                     || double.class.equals(toClass);
            } else {
               return int.class.equals(toClass) || long.class.equals(toClass) || float.class.equals(toClass) || double.class.equals(toClass);
            }
         } else {
            return toClass.isAssignableFrom(cls);
         }
      }
   }

   public static boolean isAssignable(Class<?>[] classArray, Class<?>... toClassArray) {
      return isAssignable(classArray, toClassArray, true);
   }

   public static boolean isAssignable(Class<?>[] classArray, Class<?>[] toClassArray, boolean autoboxing) {
      if (!ArrayUtils.isSameLength((Object[])classArray, (Object[])toClassArray)) {
         return false;
      } else {
         classArray = ArrayUtils.nullToEmpty(classArray);
         toClassArray = ArrayUtils.nullToEmpty(toClassArray);

         for (int i = 0; i < classArray.length; i++) {
            if (!isAssignable(classArray[i], toClassArray[i], autoboxing)) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isInnerClass(Class<?> cls) {
      return cls != null && cls.getEnclosingClass() != null;
   }

   public static boolean isPrimitiveOrWrapper(Class<?> type) {
      return type == null ? false : type.isPrimitive() || isPrimitiveWrapper(type);
   }

   public static boolean isPrimitiveWrapper(Class<?> type) {
      return WRAPPER_PRIMITIVE_MAP.containsKey(type);
   }

   public static boolean isPublic(Class<?> cls) {
      return Modifier.isPublic(cls.getModifiers());
   }

   public static Class<?>[] primitivesToWrappers(Class<?>... classes) {
      if (classes == null) {
         return null;
      } else if (classes.length == 0) {
         return classes;
      } else {
         Class<?>[] convertedClasses = new Class[classes.length];
         Arrays.setAll(convertedClasses, i -> primitiveToWrapper(classes[i]));
         return convertedClasses;
      }
   }

   public static Class<?> primitiveToWrapper(Class<?> cls) {
      Class<?> convertedClass = cls;
      if (cls != null && cls.isPrimitive()) {
         convertedClass = PRIMITIVE_WRAPPER_MAP.get(cls);
      }

      return convertedClass;
   }

   private static String toCanonicalName(String className) {
      String canonicalName = StringUtils.deleteWhitespace(className);
      Objects.requireNonNull(canonicalName, "className");
      String arrayMarker = "[]";
      if (canonicalName.endsWith("[]")) {
         StringBuilder classNameBuffer = new StringBuilder();

         while (canonicalName.endsWith("[]")) {
            canonicalName = canonicalName.substring(0, canonicalName.length() - 2);
            classNameBuffer.append("[");
         }

         String abbreviation = ABBREVIATION_MAP.get(canonicalName);
         if (abbreviation != null) {
            classNameBuffer.append(abbreviation);
         } else {
            classNameBuffer.append("L").append(canonicalName).append(";");
         }

         canonicalName = classNameBuffer.toString();
      }

      return canonicalName;
   }

   public static Class<?>[] toClass(Object... array) {
      if (array == null) {
         return null;
      } else if (array.length == 0) {
         return ArrayUtils.EMPTY_CLASS_ARRAY;
      } else {
         Class<?>[] classes = new Class[array.length];
         Arrays.setAll(classes, i -> array[i] == null ? null : array[i].getClass());
         return classes;
      }
   }

   private static boolean useFull(int runAheadTarget, int source, int originalLength, int desiredLength) {
      return source >= originalLength || runAheadTarget + originalLength - source <= desiredLength;
   }

   public static Class<?>[] wrappersToPrimitives(Class<?>... classes) {
      if (classes == null) {
         return null;
      } else if (classes.length == 0) {
         return classes;
      } else {
         Class<?>[] convertedClasses = new Class[classes.length];
         Arrays.setAll(convertedClasses, i -> wrapperToPrimitive(classes[i]));
         return convertedClasses;
      }
   }

   public static Class<?> wrapperToPrimitive(Class<?> cls) {
      return WRAPPER_PRIMITIVE_MAP.get(cls);
   }

   static {
      NAME_PRIMITIVE_MAP.put(boolean.class.getName(), boolean.class);
      NAME_PRIMITIVE_MAP.put(byte.class.getName(), byte.class);
      NAME_PRIMITIVE_MAP.put(char.class.getName(), char.class);
      NAME_PRIMITIVE_MAP.put(double.class.getName(), double.class);
      NAME_PRIMITIVE_MAP.put(float.class.getName(), float.class);
      NAME_PRIMITIVE_MAP.put(int.class.getName(), int.class);
      NAME_PRIMITIVE_MAP.put(long.class.getName(), long.class);
      NAME_PRIMITIVE_MAP.put(short.class.getName(), short.class);
      NAME_PRIMITIVE_MAP.put(void.class.getName(), void.class);
      PRIMITIVE_WRAPPER_MAP.put(boolean.class, Boolean.class);
      PRIMITIVE_WRAPPER_MAP.put(byte.class, Byte.class);
      PRIMITIVE_WRAPPER_MAP.put(char.class, Character.class);
      PRIMITIVE_WRAPPER_MAP.put(short.class, Short.class);
      PRIMITIVE_WRAPPER_MAP.put(int.class, Integer.class);
      PRIMITIVE_WRAPPER_MAP.put(long.class, Long.class);
      PRIMITIVE_WRAPPER_MAP.put(double.class, Double.class);
      PRIMITIVE_WRAPPER_MAP.put(float.class, Float.class);
      PRIMITIVE_WRAPPER_MAP.put(void.class, void.class);
      PRIMITIVE_WRAPPER_MAP.forEach((primitiveClass, wrapperClass) -> {
         if (!primitiveClass.equals(wrapperClass)) {
            WRAPPER_PRIMITIVE_MAP.put((Class<?>)wrapperClass, (Class<?>)primitiveClass);
         }
      });
      Map<String, String> map = new HashMap<>();
      map.put(int.class.getName(), "I");
      map.put(boolean.class.getName(), "Z");
      map.put(float.class.getName(), "F");
      map.put(long.class.getName(), "J");
      map.put(short.class.getName(), "S");
      map.put(byte.class.getName(), "B");
      map.put(double.class.getName(), "D");
      map.put(char.class.getName(), "C");
      ABBREVIATION_MAP = Collections.unmodifiableMap(map);
      REVERSE_ABBREVIATION_MAP = Collections.unmodifiableMap(map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey)));
   }

   public static enum Interfaces {
      INCLUDE,
      EXCLUDE;
   }
}
