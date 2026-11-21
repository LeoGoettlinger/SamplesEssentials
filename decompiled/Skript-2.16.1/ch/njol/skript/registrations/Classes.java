package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.PatternedParser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.SerializedVariable;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.yggdrasil.Tag;
import ch.njol.yggdrasil.YggdrasilInputStream;
import ch.njol.yggdrasil.YggdrasilOutputStream;
import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.SequenceInputStream;
import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.properties.Property;

public abstract class Classes {
   @Nullable
   private static ClassInfo<?>[] classInfos = null;
   private static final List<ClassInfo<?>> tempClassInfos = new ArrayList<>();
   private static final HashMap<Class<?>, ClassInfo<?>> exactClassInfos = new HashMap<>();
   private static final HashMap<Class<?>, ClassInfo<?>> superClassInfos = new HashMap<>();
   private static final HashMap<String, ClassInfo<?>> classInfosByCodeName = new HashMap<>();
   private static final Map<String, List<ClassInfo<?>>> registeredLiteralPatterns = new HashMap<>();
   private static final Map<Property<?>, Set<ClassInfo<?>>> CLASS_INFOS_BY_PROPERTY = new HashMap<>();
   private static final byte[] YGGDRASIL_START = new byte[]{89, 103, 103, 0, 0, 1};
   private static final Charset UTF_8 = Charset.forName("UTF-8");

   private Classes() {
   }

   public static <T> void registerClass(ClassInfo<T> info) {
      try {
         Skript.checkAcceptRegistrations();
         if (classInfosByCodeName.containsKey(info.getCodeName())) {
            throw new IllegalArgumentException(
               "Can't register "
                  + info.getC().getName()
                  + " with the code name "
                  + info.getCodeName()
                  + " because that name is already used by "
                  + classInfosByCodeName.get(info.getCodeName())
            );
         }

         if (exactClassInfos.containsKey(info.getC())) {
            throw new IllegalArgumentException(
               "Can't register the class info " + info.getCodeName() + " because the class " + info.getC().getName() + " is already registered"
            );
         }

         if (info.getCodeName().length() > 50) {
            throw new IllegalArgumentException(
               "The codename '" + info.getCodeName() + "' is too long to be saved in a database, the maximum length allowed is 50"
            );
         }

         exactClassInfos.put(info.getC(), info);
         classInfosByCodeName.put(info.getCodeName(), info);
         tempClassInfos.add(info);
         if (info.getParser() instanceof PatternedParser<?> patternedParser) {
            String[] patterns = patternedParser.getPatterns();

            for (String pattern : patterns) {
               registeredLiteralPatterns.computeIfAbsent(pattern, list -> new ArrayList<>()).add(info);
            }
         }
      } catch (RuntimeException var7) {
         if (!SkriptConfig.apiSoftExceptions.value()) {
            throw var7;
         }

         Skript.warning("Ignored an exception due to user configuration: " + var7.getMessage());
      }
   }

   public static void onRegistrationsStop() {
      sortClassInfos();

      for (ClassInfo<?> ci : getClassInfos()) {
         if (ci.getSerializeAs() != null) {
            ClassInfo<?> sa = getExactClassInfo(ci.getSerializeAs());
            if (sa == null) {
               Skript.error(ci.getCodeName() + "'s 'serializeAs' class is not registered");
            } else if (sa.getSerializer() == null) {
               Skript.error(ci.getCodeName() + "'s 'serializeAs' class is not serializable");
            }
         }
      }

      for (ClassInfo<?> cix : getClassInfos()) {
         Serializer<?> s = cix.getSerializer();
         if (s != null) {
            Variables.yggdrasil.registerClassResolver(s);
         }
      }

      EntityData.onRegistrationStop();
   }

   @SuppressFBWarnings({"LI_LAZY_INIT_STATIC"})
   private static void sortClassInfos() {
      assert Classes.classInfos == null;

      if (!Skript.testing() && SkriptConfig.addonSafetyChecks.value()) {
         removeNullElements();
      }

      for (ClassInfo<?> ci : tempClassInfos) {
         Set<String> before = ci.before();
         if (before != null && !before.isEmpty()) {
            for (ClassInfo<?> ci2 : tempClassInfos) {
               if (before.contains(ci2.getCodeName())) {
                  ci2.after().add(ci.getCodeName());
                  before.remove(ci2.getCodeName());
                  if (before.isEmpty()) {
                     break;
                  }
               }
            }
         }
      }

      for (ClassInfo<?> cix : tempClassInfos) {
         for (ClassInfo<?> ci2x : tempClassInfos) {
            if (cix != ci2x && cix.getC().isAssignableFrom(ci2x.getC())) {
               cix.after().add(ci2x.getCodeName());
            }
         }
      }

      for (ClassInfo<?> cix : tempClassInfos) {
         Set<String> s = new HashSet<>();
         Set<String> before = cix.before();
         if (before != null) {
            for (String b : before) {
               if (getClassInfoNoError(b) == null) {
                  s.add(b);
               }
            }

            before.removeAll(s);
         }

         for (String a : cix.after()) {
            if (getClassInfoNoError(a) == null) {
               s.add(a);
            }
         }

         cix.after().removeAll(s);
         if (!s.isEmpty() && Skript.testing()) {
            Skript.warning(s.size() + " dependency/ies could not be resolved for " + cix + ": " + StringUtils.join(s, ", "));
         }
      }

      List<ClassInfo<?>> classInfos = new ArrayList<>(tempClassInfos.size());
      boolean changed = true;

      while (changed) {
         changed = false;

         for (int i = 0; i < tempClassInfos.size(); i++) {
            ClassInfo<?> cix = tempClassInfos.get(i);
            if (cix.after().isEmpty()) {
               classInfos.add(cix);
               tempClassInfos.remove(i);
               i--;

               for (ClassInfo<?> ci2xx : tempClassInfos) {
                  ci2xx.after().remove(cix.getCodeName());
               }

               changed = true;
            }
         }
      }

      Classes.classInfos = classInfos.toArray(new ClassInfo[classInfos.size()]);
      if (!tempClassInfos.isEmpty()) {
         StringBuilder bx = new StringBuilder();

         for (ClassInfo<?> c : tempClassInfos) {
            if (bx.length() != 0) {
               bx.append(", ");
            }

            bx.append(c.getCodeName() + " (after: " + StringUtils.join(c.after(), ", ") + ")");
         }

         throw new IllegalStateException("ClassInfos with circular dependencies detected: " + bx.toString());
      } else {
         if (Skript.debug()) {
            StringBuilder bx = new StringBuilder();

            for (ClassInfo<?> cix : classInfos) {
               if (bx.length() != 0) {
                  bx.append(", ");
               }

               bx.append(cix.getCodeName());
            }

            Skript.info("All registered classes in order: " + bx.toString());
         }
      }
   }

   private static void removeNullElements() {
      Iterator<ClassInfo<?>> it = tempClassInfos.iterator();

      while (it.hasNext()) {
         ClassInfo<?> ci = it.next();
         if (ci.getC() == null) {
            it.remove();
         }
      }
   }

   private static void checkAllowClassInfoInteraction() {
      if (Skript.isAcceptRegistrations()) {
         throw new IllegalStateException("Cannot use classinfos until registration is over");
      }
   }

   @Nullable
   @Unmodifiable
   public static List<ClassInfo<?>> getPatternInfos(String pattern) {
      pattern = pattern.toLowerCase(Locale.ENGLISH);
      List<ClassInfo<?>> infos = registeredLiteralPatterns.get(pattern);
      return infos != null ? Collections.unmodifiableList(infos) : null;
   }

   public static List<ClassInfo<?>> getClassInfos() {
      checkAllowClassInfoInteraction();
      ClassInfo<?>[] ci = classInfos;
      return ci == null ? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(ci));
   }

   public static ClassInfo<?> getClassInfo(String codeName) {
      ClassInfo<?> ci = classInfosByCodeName.get(codeName);
      if (ci == null) {
         throw new SkriptAPIException("No class info found for " + codeName);
      } else {
         return ci;
      }
   }

   @Nullable
   public static ClassInfo<?> getClassInfoNoError(@Nullable String codeName) {
      return classInfosByCodeName.get(codeName);
   }

   @Nullable
   public static <T> ClassInfo<T> getExactClassInfo(@Nullable Class<T> c) {
      return (ClassInfo<T>)exactClassInfos.get(c);
   }

   @Contract(
      pure = true,
      value = "!null -> !null"
   )
   public static <T> ClassInfo<? super T> getSuperClassInfo(Class<T> c) {
      assert c != null;

      ClassInfo<? super T> info = getExactClassInfo(c);
      if (info != null) {
         return info;
      } else {
         info = (ClassInfo<? super T>)superClassInfos.get(c);
         if (info != null) {
            return info;
         } else {
            for (ClassInfo<?> ci : getClassInfos()) {
               if (ci.getC().isAssignableFrom(c)) {
                  if (!Skript.isAcceptRegistrations()) {
                     superClassInfos.put(c, ci);
                  }

                  return (ClassInfo<? super T>)ci;
               }
            }

            assert false;

            return null;
         }
      }
   }

   public static ClassInfo<?> getSuperClassInfo(Class<?>... classes) {
      return getSuperClassInfo(Utils.getSuperType(classes));
   }

   public static <T> List<ClassInfo<? super T>> getAllSuperClassInfos(Class<T> c) {
      assert c != null;

      checkAllowClassInfoInteraction();
      List<ClassInfo<? super T>> list = new ArrayList<>();

      for (ClassInfo<?> ci : getClassInfos()) {
         if (ci.getC().isAssignableFrom(c)) {
            list.add((ClassInfo<? super T>)ci);
         }
      }

      return list;
   }

   @Internal
   public static void hasProperty(@NotNull Property<?> property, @NotNull ClassInfo<?> classInfo) {
      Preconditions.checkNotNull(property, "property cannot be null");
      Preconditions.checkNotNull(classInfo, "classInfo cannot be null");
      CLASS_INFOS_BY_PROPERTY.computeIfAbsent(property, key -> new HashSet<>()).add(classInfo);
   }

   @NotNull
   public static Set<ClassInfo<?>> getClassInfosByProperty(@NotNull Property<?> property) {
      Preconditions.checkNotNull(property, "property cannot be null");
      return CLASS_INFOS_BY_PROPERTY.getOrDefault(property, Collections.emptySet());
   }

   public static Class<?> getClass(String codeName) {
      checkAllowClassInfoInteraction();
      return getClassInfo(codeName).getC();
   }

   @Nullable
   public static ClassInfo<?> getClassInfoFromUserInput(String name) {
      checkAllowClassInfoInteraction();
      name = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(name.toLowerCase(Locale.ENGLISH));

      for (ClassInfo<?> ci : getClassInfos()) {
         Pattern[] uip = ci.getUserInputPatterns();
         if (uip != null) {
            for (Pattern pattern : uip) {
               if (pattern.matcher(name).matches()) {
                  return ci;
               }
            }
         }
      }

      return null;
   }

   @Nullable
   public static Class<?> getClassFromUserInput(String name) {
      checkAllowClassInfoInteraction();
      ClassInfo<?> ci = getClassInfoFromUserInput(name);
      return ci == null ? null : ci.getC();
   }

   @Nullable
   public static DefaultExpression<?> getDefaultExpression(String codeName) {
      checkAllowClassInfoInteraction();
      return getClassInfo(codeName).getDefaultExpression();
   }

   @Nullable
   public static <T> DefaultExpression<T> getDefaultExpression(Class<T> c) {
      checkAllowClassInfoInteraction();
      ClassInfo<T> ci = getExactClassInfo(c);
      return ci == null ? null : ci.getDefaultExpression();
   }

   public static Object clone(Object obj) {
      if (obj == null) {
         return null;
      } else if (!obj.getClass().isArray()) {
         ClassInfo classInfo = getSuperClassInfo(obj.getClass());
         return classInfo.clone(obj);
      } else {
         int length = Array.getLength(obj);
         Object clone = Array.newInstance(obj.getClass().getComponentType(), length);

         for (int i = 0; i < length; i++) {
            Array.set(clone, i, clone(Array.get(obj, i)));
         }

         return clone;
      }
   }

   @Nullable
   public static String getExactClassName(Class<?> c) {
      checkAllowClassInfoInteraction();
      ClassInfo<?> ci = exactClassInfos.get(c);
      return ci == null ? null : ci.getCodeName();
   }

   @Nullable
   public static <T> T parseSimple(String s, Class<T> c, ParseContext context) {
      ParseLogHandler log = SkriptLogger.startParseLogHandler();

      try {
         for (ClassInfo<?> info : getClassInfos()) {
            Parser<?> parser = info.getParser();
            if (parser != null && parser.canParse(context) && c.isAssignableFrom(info.getC())) {
               log.clear();
               T t = (T)parser.parse(s, context);
               if (t != null) {
                  log.printLog();
                  return t;
               }
            }
         }

         log.printError();
         return null;
      } finally {
         log.stop();
      }
   }

   @Nullable
   public static <T> T parse(String s, Class<T> c, ParseContext context) {
      ParseLogHandler log = SkriptLogger.startParseLogHandler();

      try {
         T t = parseSimple(s, c, context);
         if (t != null) {
            log.printLog();
            return t;
         } else {
            for (ConverterInfo<?, ?> conv : org.skriptlang.skript.lang.converter.Converters.getConverterInfos()) {
               if ((context != ParseContext.COMMAND && context != ParseContext.PARSE || (conv.getFlags() & 8) == 0) && c.isAssignableFrom(conv.getTo())) {
                  log.clear();
                  Object object = parseSimple(s, conv.getFrom(), context);
                  if (object != null) {
                     t = (T)((Converter<Object, ?>)conv.getConverter()).convert(object);
                     if (t != null) {
                        log.printLog();
                        return t;
                     }
                  }
               }
            }

            log.printError();
            return null;
         }
      } finally {
         log.stop();
      }
   }

   @Nullable
   public static <T> Parser<? extends T> getParser(Class<T> to) {
      checkAllowClassInfoInteraction();
      ClassInfo<?>[] classInfos = Classes.classInfos;
      if (classInfos == null) {
         return null;
      } else {
         for (int i = classInfos.length - 1; i >= 0; i--) {
            ClassInfo<?> ci = classInfos[i];
            if (to.isAssignableFrom(ci.getC()) && ci.getParser() != null) {
               return (Parser<? extends T>)ci.getParser();
            }
         }

         for (ConverterInfo<?, ?> conv : org.skriptlang.skript.lang.converter.Converters.getConverterInfos()) {
            if (to.isAssignableFrom(conv.getTo())) {
               for (int ix = classInfos.length - 1; ix >= 0; ix--) {
                  ClassInfo<?> ci = classInfos[ix];
                  Parser<?> parser = ci.getParser();
                  if (conv.getFrom().isAssignableFrom(ci.getC()) && parser != null) {
                     return createConvertedParser(parser, (Converter<?, ? extends T>)conv.getConverter());
                  }
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public static <T> Parser<? extends T> getExactParser(Class<T> c) {
      if (Skript.isAcceptRegistrations()) {
         for (ClassInfo<?> ci : tempClassInfos) {
            if (ci.getC() == c) {
               return (Parser<? extends T>)ci.getParser();
            }
         }

         return null;
      } else {
         ClassInfo<T> cix = getExactClassInfo(c);
         return cix == null ? null : cix.getParser();
      }
   }

   private static <F, T> Parser<T> createConvertedParser(final Parser<?> parser, final Converter<F, T> converter) {
      return new Parser<T>() {
         @Nullable
         @Override
         public T parse(String s, ParseContext context) {
            Object f = parser.parse(s, context);
            return f == null ? null : converter.convert((F)f);
         }

         @Override
         public String toString(T o, int flags) {
            throw new UnsupportedOperationException();
         }

         @Override
         public String toVariableNameString(T o) {
            throw new UnsupportedOperationException();
         }
      };
   }

   public static String toString(@Nullable Object o) {
      return toString(o, StringMode.MESSAGE, 0);
   }

   public static String getDebugMessage(@Nullable Object o) {
      return toString(o, StringMode.DEBUG, 0);
   }

   public static <T> String toString(@Nullable T o, StringMode mode) {
      return toString(o, mode, 0);
   }

   private static <T> String toString(@Nullable T o, StringMode mode, int flags) {
      assert flags == 0 || mode == StringMode.MESSAGE;

      if (o == null) {
         return Language.get("none");
      } else if (o.getClass().isArray()) {
         if (o.length == 0) {
            return Language.get("none");
         } else {
            StringBuilder b = new StringBuilder();
            boolean first = true;

            for (Object i : (Object[])o) {
               if (!first) {
                  b.append(", ");
               }

               b.append(toString(i, mode, flags));
               first = false;
            }

            return "[" + b.toString() + "]";
         }
      } else {
         for (ClassInfo<?> ci : getClassInfos()) {
            Parser<?> parser = ci.getParser();
            if (parser != null && ci.getC().isInstance(o)) {
               return mode == StringMode.MESSAGE
                  ? ((Parser<T>)parser).toString(o, flags)
                  : (
                     mode == StringMode.DEBUG
                        ? "[" + ci.getCodeName() + ":" + ((Parser<T>)parser).toString(o, mode) + "]"
                        : ((Parser<T>)parser).toString(o, mode)
                  );
            }
         }

         return mode == StringMode.VARIABLE_NAME ? "object:" + o : o + "";
      }
   }

   public static String toString(Object[] os, int flags, boolean and) {
      return toString(os, and, null, StringMode.MESSAGE, flags);
   }

   public static String toString(Object[] os, int flags, @Nullable ChatColor c) {
      return toString(os, true, c, StringMode.MESSAGE, flags);
   }

   public static String toString(Object[] os, boolean and) {
      return toString(os, and, null, StringMode.MESSAGE, 0);
   }

   public static String toString(Object[] os, boolean and, StringMode mode) {
      return toString(os, and, null, mode, 0);
   }

   private static String toString(Object[] os, boolean and, @Nullable ChatColor c, StringMode mode, int flags) {
      if (os.length == 0) {
         return toString(null);
      } else if (os.length == 1) {
         return toString(os[0], mode, flags);
      } else {
         StringBuilder b = new StringBuilder();

         for (int i = 0; i < os.length; i++) {
            if (i != 0) {
               if (c != null) {
                  b.append(c.toString());
               }

               if (i == os.length - 1) {
                  b.append(and ? " and " : " or ");
               } else {
                  b.append(", ");
               }
            }

            b.append(toString(os[i], mode, flags));
         }

         return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(b.toString());
      }
   }

   private static byte[] getYggdrasilStart(ClassInfo<?> c) throws NotSerializableException {
      assert Enum.class.isAssignableFrom(Kleenean.class) && Tag.getType(Kleenean.class) == Tag.T_ENUM : Tag.getType(Kleenean.class);

      Tag t = Tag.getType(c.getC());

      assert t.isWrapper() || t == Tag.T_STRING || t == Tag.T_OBJECT || t == Tag.T_ENUM;

      byte[] cn = t != Tag.T_OBJECT && t != Tag.T_ENUM ? null : Variables.yggdrasil.getID(c.getC()).getBytes(UTF_8);
      byte[] r = new byte[YGGDRASIL_START.length + 1 + (cn == null ? 0 : 1 + cn.length)];

      int i;
      for (i = 0; i < YGGDRASIL_START.length; i++) {
         r[i] = YGGDRASIL_START[i];
      }

      r[i++] = t.tag;
      if (cn != null) {
         r[i++] = (byte)cn.length;

         for (int j = 0; j < cn.length; j++) {
            r[i++] = cn[j];
         }
      }

      assert i == r.length;

      return r;
   }

   @Nullable
   public static SerializedVariable.Value serialize(@Nullable Object object) {
      if (object == null) {
         return null;
      } else {
         assert Bukkit.isPrimaryThread();

         ClassInfo<?> classInfo = getSuperClassInfo(object.getClass());
         if (classInfo.getSerializeAs() != null) {
            classInfo = getExactClassInfo(classInfo.getSerializeAs());
            if (classInfo == null) {
               assert false : object.getClass();

               return null;
            }

            object = org.skriptlang.skript.lang.converter.Converters.convert(object, classInfo.getC());
            if (object == null) {
               assert false : classInfo.getCodeName();

               return null;
            }
         }

         Serializer<?> serializer = classInfo.getSerializer();
         if (serializer == null) {
            return null;
         } else {
            assert !serializer.mustSyncDeserialization() || Bukkit.isPrimaryThread();

            try {
               ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
               YggdrasilOutputStream yggdrasilOutputStream = Variables.yggdrasil.newOutputStream(byteOutputStream);
               yggdrasilOutputStream.writeObject(object);
               yggdrasilOutputStream.flush();
               yggdrasilOutputStream.close();
               byte[] byteArray = byteOutputStream.toByteArray();
               byte[] start = getYggdrasilStart(classInfo);

               for (int i = 0; i < start.length; i++) {
                  assert byteArray[i] == start[i] : object
                     + " ("
                     + classInfo.getC().getName()
                     + "); "
                     + Arrays.toString(start)
                     + ", "
                     + Arrays.toString(byteArray);
               }

               byte[] byteArrayCopy = new byte[byteArray.length - start.length];
               System.arraycopy(byteArray, start.length, byteArrayCopy, 0, byteArrayCopy.length);
               Object deserialized;

               assert equals(object, deserialized = deserialize(classInfo, new ByteArrayInputStream(byteArrayCopy))) : object
                  + " ("
                  + object.getClass()
                  + ") != "
                  + deserialized
                  + " ("
                  + (deserialized == null ? null : deserialized.getClass())
                  + "): "
                  + Arrays.toString(byteArray);

               return new SerializedVariable.Value(classInfo.getCodeName(), byteArrayCopy);
            } catch (IOException var9) {
               Skript.exception(var9);
               return null;
            }
         }
      }
   }

   private static boolean equals(@Nullable Object o, @Nullable Object d) {
      if (o instanceof Chunk) {
         if (!(d instanceof Chunk)) {
            return false;
         } else {
            Chunk c1 = (Chunk)o;
            Chunk c2 = (Chunk)d;
            return c1.getWorld().equals(c2.getWorld()) && c1.getX() == c2.getX() && c1.getZ() == c2.getZ();
         }
      } else {
         return o == null ? d == null : o.equals(d);
      }
   }

   @Nullable
   public static Object deserialize(ClassInfo<?> type, byte[] value) {
      return deserialize(type, new ByteArrayInputStream(value));
   }

   @Nullable
   public static Object deserialize(String type, byte[] value) {
      ClassInfo<?> ci = getClassInfoNoError(type);
      return ci == null ? null : deserialize(ci, new ByteArrayInputStream(value));
   }

   @Nullable
   public static Object deserialize(ClassInfo<?> type, InputStream value) {
      Serializer<?> s;

      assert (s = type.getSerializer()) != null && (!s.mustSyncDeserialization() || Bukkit.isPrimaryThread()) : type
         + "; "
         + s
         + "; "
         + Bukkit.isPrimaryThread();

      YggdrasilInputStream in = null;

      Object var5;
      try {
         value = new SequenceInputStream(new ByteArrayInputStream(getYggdrasilStart(type)), value);
         in = Variables.yggdrasil.newInputStream(value);
         return in.readObject();
      } catch (IOException var19) {
         if (Skript.testing()) {
            var19.printStackTrace();
         }

         var5 = null;
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException var18) {
            }
         }

         try {
            value.close();
         } catch (IOException var17) {
         }
      }

      return var5;
   }

   @Deprecated(
      since = "2.3.0",
      forRemoval = true
   )
   @Nullable
   public static Object deserialize(String type, String value) {
      assert Bukkit.isPrimaryThread();

      ClassInfo<?> ci = getClassInfoNoError(type);
      if (ci == null) {
         return null;
      } else {
         Serializer<?> s = ci.getSerializer();
         return s == null ? null : s.deserialize(value);
      }
   }
}
