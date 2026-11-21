package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.NonNullPair;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.util.ClassLoader;

public abstract class Utils {
   public static final Random random = new Random();
   protected static final Deque<Utils.WordEnding> plurals = new LinkedList<>();
   static final ChatColor[] styles = new ChatColor[]{
      ChatColor.BOLD, ChatColor.ITALIC, ChatColor.STRIKETHROUGH, ChatColor.UNDERLINE, ChatColor.MAGIC, ChatColor.RESET
   };
   static final Map<String, String> chat = new HashMap<>();
   static final Map<String, String> englishChat = new HashMap<>();
   private static final Pattern STYLE_PATTERN = Pattern.compile("<([^<>]+)>");
   private static final Pattern UNICODE_PATTERN = Pattern.compile("(?i)u(?:nicode)?:(?<code>[0-9a-f]{4,})");
   private static final Pattern HEX_PATTERN = Pattern.compile("(?i)#{0,2}(?<code>[0-9a-f]{6})");

   private Utils() {
   }

   public static String join(Object[] objects) {
      assert objects != null;

      StringBuilder b = new StringBuilder();

      for (int i = 0; i < objects.length; i++) {
         if (i != 0) {
            b.append(", ");
         }

         b.append(Classes.toString(objects[i]));
      }

      return b.toString();
   }

   public static String join(Iterable<?> objects) {
      assert objects != null;

      StringBuilder b = new StringBuilder();
      boolean first = true;

      for (Object o : objects) {
         if (!first) {
            b.append(", ");
         } else {
            first = false;
         }

         b.append(Classes.toString(o));
      }

      return b.toString();
   }

   public static <T> boolean isEither(@Nullable T compared, @Nullable T... types) {
      return CollectionUtils.contains(types, compared);
   }

   public static Pair<String, Integer> getAmount(String s) {
      if (s.matches("\\d+ of .+")) {
         return new Pair<>(s.split(" ", 3)[2], parseInt(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.split(" ", 2)[0])));
      } else if (s.matches("\\d+ .+")) {
         return new Pair<>(s.split(" ", 2)[1], parseInt(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.split(" ", 2)[0])));
      } else {
         return s.matches("an? .+") ? new Pair<>(s.split(" ", 2)[1], 1) : new Pair<>(s, -1);
      }
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public static Class<?>[] getClasses(Plugin plugin, String basePackage, String... subPackages) throws IOException {
      List<Class<?>> classes = new ArrayList<>();
      ClassLoader loader = ClassLoader.builder()
         .basePackage(basePackage)
         .addSubPackages(subPackages)
         .deep(true)
         .initialize(true)
         .forEachClass(classes::add)
         .build();
      File jarFile = getFile(plugin);
      if (jarFile != null) {
         loader.loadClasses(plugin.getClass(), jarFile);
      } else {
         loader.loadClasses(plugin.getClass());
      }

      return classes.toArray(new Class[0]);
   }

   @Nullable
   public static File getFile(Plugin plugin) {
      try {
         Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
         getFile.setAccessible(true);
         return (File)getFile.invoke(plugin);
      } catch (NoSuchMethodException var2) {
         Skript.outdatedError(var2);
      } catch (IllegalArgumentException var3) {
         Skript.outdatedError(var3);
      } catch (IllegalAccessException var4) {
         assert false;
      } catch (SecurityException var5) {
         throw new RuntimeException(var5);
      } catch (InvocationTargetException var6) {
         throw new RuntimeException(var6.getCause());
      }

      return null;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.14"
   )
   public static NonNullPair<String, Boolean> getEnglishPlural(String word) {
      Utils.PluralResult result = isPlural(word);
      return new NonNullPair<>(result.updated, result.plural);
   }

   public static Utils.PluralResult isPlural(String word) {
      Preconditions.checkNotNull(word, "word cannot be null");
      if (word.isEmpty()) {
         return new Utils.PluralResult("", false);
      } else if (couldBeSingular(word)) {
         return new Utils.PluralResult(word, false);
      } else {
         for (Utils.WordEnding ending : plurals) {
            if (!ending.isCompleteWord() || word.length() == ending.plural().length()) {
               if (word.endsWith(ending.plural())) {
                  return new Utils.PluralResult(word.substring(0, word.length() - ending.plural().length()) + ending.singular(), true);
               }

               if (word.endsWith(ending.plural().toUpperCase(Locale.ENGLISH))) {
                  return new Utils.PluralResult(
                     word.substring(0, word.length() - ending.plural().length()) + ending.singular().toUpperCase(Locale.ENGLISH), true
                  );
               }
            }
         }

         return new Utils.PluralResult(word, false);
      }
   }

   private static boolean couldBeSingular(String word) {
      for (Utils.WordEnding ending : plurals) {
         if (!ending.singular().isBlank()
            && (!ending.isCompleteWord() || ending.singular().length() == word.length())
            && (word.endsWith(ending.singular()) || word.toLowerCase().endsWith(ending.singular()))) {
            return true;
         }
      }

      return false;
   }

   public static void addPluralOverride(String singular, String plural) {
      plurals.addFirst(new Utils.WordEnding(singular, plural, true));
   }

   public static String toEnglishPlural(String word) {
      assert word != null && word.length() != 0;

      for (Utils.WordEnding ending : plurals) {
         if ((!ending.isCompleteWord() || word.length() == ending.singular().length()) && word.endsWith(ending.singular())) {
            return word.substring(0, word.length() - ending.singular().length()) + ending.plural();
         }
      }

      assert false;

      return word + "s";
   }

   public static String toEnglishPlural(String s, boolean p) {
      return p ? toEnglishPlural(s) : s;
   }

   public static String a(String s) {
      return a(s, false);
   }

   public static String A(String s) {
      return a(s, true);
   }

   public static String a(String s, boolean capA) {
      assert s != null && s.length() != 0;

      if ("aeiouAEIOU".indexOf(s.charAt(0)) != -1) {
         return capA ? "An " + s : "an " + s;
      } else {
         return capA ? "A " + s : "a " + s;
      }
   }

   public static CompletableFuture<ByteArrayDataInput> sendPluginMessage(String channel, String... data) {
      return sendPluginMessage(channel, r -> true, data);
   }

   public static CompletableFuture<ByteArrayDataInput> sendPluginMessage(Player player, String channel, String... data) {
      return sendPluginMessage(player, channel, r -> true, data);
   }

   public static CompletableFuture<ByteArrayDataInput> sendPluginMessage(String channel, Predicate<ByteArrayDataInput> messageVerifier, String... data) throws IllegalStateException {
      Player firstPlayer = (Player)Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
      if (firstPlayer == null) {
         throw new IllegalStateException("There are no players online");
      } else {
         return sendPluginMessage(firstPlayer, channel, messageVerifier, data);
      }
   }

   public static CompletableFuture<ByteArrayDataInput> sendPluginMessage(
      Player player, String channel, Predicate<ByteArrayDataInput> messageVerifier, String... data
   ) {
      CompletableFuture<ByteArrayDataInput> completableFuture = new CompletableFuture<>();
      Skript skript = Skript.getInstance();
      Messenger messenger = Bukkit.getMessenger();
      messenger.registerOutgoingPluginChannel(skript, channel);
      PluginMessageListener listener = (sendingChannel, sendingPlayer, message) -> {
         ByteArrayDataInput input = ByteStreams.newDataInput(message);
         if (channel.equals(sendingChannel)
            && sendingPlayer == player
            && !completableFuture.isDone()
            && !completableFuture.isCancelled()
            && messageVerifier.test(input)) {
            completableFuture.complete(input);
         }
      };
      messenger.registerIncomingPluginChannel(skript, channel, listener);
      completableFuture.whenComplete((r, ex) -> messenger.unregisterIncomingPluginChannel(skript, channel, listener));
      Bukkit.getScheduler().scheduleSyncDelayedTask(skript, () -> {
         if (!completableFuture.isDone()) {
            completableFuture.cancel(true);
         }
      }, 1200L);
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      Stream.of(data).forEach(out::writeUTF);
      player.sendPluginMessage(Skript.getInstance(), channel, out.toByteArray());
      return completableFuture;
   }

   @Deprecated(
      since = "2.15",
      forRemoval = true
   )
   @Nullable
   public static String getChatStyle(String s) {
      SkriptColor color = SkriptColor.fromName(s);
      return color != null ? color.getFormattedChat() : chat.get(s);
   }

   @Deprecated(
      since = "2.15",
      forRemoval = true
   )
   @NotNull
   public static String replaceChatStyles(String message) {
      return message.isEmpty() ? message : replaceChatStyle(message.replace("<<none>>", ""));
   }

   @Deprecated(
      since = "2.15",
      forRemoval = true
   )
   @NotNull
   public static String replaceEnglishChatStyles(String message) {
      return message.isEmpty() ? message : replaceChatStyle(message);
   }

   @Deprecated(
      since = "2.15",
      forRemoval = true
   )
   @NotNull
   private static String replaceChatStyle(String message) {
      String m = StringUtils.replaceAll(Matcher.quoteReplacement(message), STYLE_PATTERN, matcher -> {
         SkriptColor color = SkriptColor.fromName(matcher.group(1));
         if (color != null) {
            return color.getFormattedChat();
         } else {
            String tag = matcher.group(1).toLowerCase(Locale.ENGLISH);
            String f = englishChat.get(tag);
            if (f != null) {
               return f;
            } else {
               if (tag.startsWith("#")) {
                  ChatColor chatColor = parseHexColor(tag);
                  if (chatColor != null) {
                     return chatColor.toString();
                  }
               } else if (tag.startsWith("u:") || tag.startsWith("unicode:")) {
                  String character = parseUnicode(tag);
                  if (character != null) {
                     return character;
                  }
               }

               return matcher.group();
            }
         }
      });
      if (!message.equals(m)) {
         m = m.replace("\\$", "$").replace("\\\\", "\\");
      }

      return ChatColor.translateAlternateColorCodes('&', m);
   }

   @Deprecated(
      since = "2.15",
      forRemoval = true
   )
   @Nullable
   public static String parseUnicode(String string) {
      Matcher matcher = UNICODE_PATTERN.matcher(string);
      if (!matcher.matches()) {
         return null;
      } else {
         try {
            return Character.toString(Integer.parseInt(matcher.group("code"), 16));
         } catch (IllegalArgumentException var3) {
            return null;
         }
      }
   }

   @Deprecated(
      since = "2.15",
      forRemoval = true
   )
   @Nullable
   public static ChatColor parseHexColor(String string) {
      Matcher matcher = HEX_PATTERN.matcher(string);
      if (!matcher.matches()) {
         return null;
      } else {
         try {
            return ChatColor.of("#" + matcher.group("code"));
         } catch (IllegalArgumentException var3) {
            return null;
         }
      }
   }

   public static int random(int start, int end) {
      if (end <= start) {
         throw new IllegalArgumentException("end (" + end + ") must be > start (" + start + ")");
      } else {
         return start + random.nextInt(end - start);
      }
   }

   public static Class<?> getSuperType(Class<?>... classes) {
      return highestDenominator(Object.class, classes);
   }

   @SafeVarargs
   public static <Found, Type extends Found> Class<Found> highestDenominator(Class<? super Found> bestGuess, @NotNull Class<? extends Type>... classes) {
      assert classes.length > 0;

      Class<?> chosen = classes[0];

      label70:
      for (Class<?> checking : classes) {
         assert !checking.isArray() && !checking.isPrimitive() : "%s has no super".formatted(checking.getSimpleName());

         if (!chosen.isAssignableFrom(checking)) {
            Class<?> superType = checking;

            while (superType == Object.class || !superType.isAssignableFrom(chosen)) {
               if ((superType = superType.getSuperclass()) == null) {
                  for (Class<?> anInterface : checking.getInterfaces()) {
                     superType = highestDenominator(Object.class, anInterface, chosen);
                     if (superType != Object.class) {
                        chosen = superType;
                        continue label70;
                     }
                  }

                  return (Class<Found>)bestGuess;
               }
            }

            chosen = superType;
         }
      }

      if (!bestGuess.isAssignableFrom(chosen)) {
         return (Class<Found>)bestGuess;
      } else {
         return (Class<Found>)(chosen == Cloneable.class ? bestGuess : (chosen == Object.class ? bestGuess : chosen));
      }
   }

   public static int parseInt(String s) {
      assert s.matches("-?\\d+");

      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException var2) {
         return s.startsWith("-") ? Integer.MIN_VALUE : Integer.MAX_VALUE;
      }
   }

   public static long parseLong(String s) {
      assert s.matches("-?\\d+");

      try {
         return Long.parseLong(s);
      } catch (NumberFormatException var2) {
         return s.startsWith("-") ? Long.MIN_VALUE : Long.MAX_VALUE;
      }
   }

   public static Class<?> classForName(String name) {
      try {
         return Class.forName(name);
      } catch (ClassNotFoundException var3) {
         throw new RuntimeException("Class not found!");
      }
   }

   public static <T> int findLastIndex(List<T> list, Predicate<T> checker) {
      int lastIndex = -1;

      for (int i = 0; i < list.size(); i++) {
         if (checker.test(list.get(i))) {
            lastIndex = i;
         }
      }

      return lastIndex;
   }

   public static boolean isInteger(Number... numbers) {
      for (Number number : numbers) {
         if (Double.class.isAssignableFrom(number.getClass()) || Float.class.isAssignableFrom(number.getClass())) {
            return false;
         }
      }

      return true;
   }

   @Internal
   public static int loadedRemovedClassWarning(Class<?> source) {
      Logger logger = Skript.getInstance().getLogger();
      Exception exception = new Exception();
      exception.fillInStackTrace();
      StackTraceElement[] stackTrace = exception.getStackTrace();
      StackTraceElement caller = stackTrace[2];

      String authors;
      String name;
      try {
         Class<?> callingClass = Class.forName(caller.getClassName());
         JavaPlugin plugin = JavaPlugin.getProvidingPlugin(callingClass);
         name = plugin.getDescription().getFullName();
         authors = String.valueOf(plugin.getDescription().getAuthors());
      } catch (IllegalArgumentException | ClassCastException | ClassNotFoundException var9) {
         name = caller.getClassLoaderName();
         authors = "(unknown)";
      }

      logger.log(
         Level.SEVERE,
         String.format(
            "\n\nWARNING!\n\nAn addon attempted to load a deprecated/outdated/removed '%s' class.\n\nThe plugin '%s' tried to use a class that has been deprecated/removed in this version of Skript.\nPlease make sure you are using the latest supported version of the addon.\n\nIf there are no supported versions, you should contact the author(s): %s, and ask them to update it.\n\n(This addon may not work correctly on this version of Skript.)\n\n",
            source.getSimpleName(),
            name,
            authors
         )
      );
      return 0;
   }

   public static boolean isValidUUID(String uuid) {
      if (uuid == null || uuid.length() != 36) {
         return false;
      } else if (uuid.charAt(8) == '-' && uuid.charAt(13) == '-' && uuid.charAt(18) == '-' && uuid.charAt(23) == '-') {
         for (int i = 0; i < 36; i++) {
            if (i != 8 && i != 13 && i != 18 && i != 23) {
               char c = uuid.charAt(i);
               if ((c < '0' || c > '9') && (c < 'a' || c > 'f') && (c < 'A' || c > 'F')) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static Class<?> getComponentType(Class<?> cls) {
      return cls != null && cls.isArray() ? cls.componentType() : cls;
   }

   static {
      plurals.add(new Utils.WordEnding("axe", "axes"));
      plurals.add(new Utils.WordEnding("x", "xes"));
      plurals.add(new Utils.WordEnding("ay", "ays"));
      plurals.add(new Utils.WordEnding("ey", "eys"));
      plurals.add(new Utils.WordEnding("iy", "iys"));
      plurals.add(new Utils.WordEnding("oy", "oys"));
      plurals.add(new Utils.WordEnding("uy", "uys"));
      plurals.add(new Utils.WordEnding("kie", "kies"));
      plurals.add(new Utils.WordEnding("zombie", "zombies", true));
      plurals.add(new Utils.WordEnding("y", "ies"));
      plurals.add(new Utils.WordEnding("wife", "wives", true));
      plurals.add(new Utils.WordEnding("life", "lives"));
      plurals.add(new Utils.WordEnding("knife", "knives", true));
      plurals.add(new Utils.WordEnding("ive", "ives"));
      plurals.add(new Utils.WordEnding("lf", "lves"));
      plurals.add(new Utils.WordEnding("thief", "thieves", true));
      plurals.add(new Utils.WordEnding("ief", "iefs"));
      plurals.add(new Utils.WordEnding("hoof", "hooves"));
      plurals.add(new Utils.WordEnding("fe", "ves"));
      plurals.add(new Utils.WordEnding("h", "hes"));
      plurals.add(new Utils.WordEnding("man", "men"));
      plurals.add(new Utils.WordEnding("ui", "uis"));
      plurals.add(new Utils.WordEnding("api", "apis"));
      plurals.add(new Utils.WordEnding("us", "i"));
      plurals.add(new Utils.WordEnding("hoe", "hoes", true));
      plurals.add(new Utils.WordEnding("toe", "toes", true));
      plurals.add(new Utils.WordEnding("foe", "foes", true));
      plurals.add(new Utils.WordEnding("woe", "woes", true));
      plurals.add(new Utils.WordEnding("o", "oes"));
      plurals.add(new Utils.WordEnding("alias", "aliases", true));
      plurals.add(new Utils.WordEnding("gas", "gases", true));
      plurals.add(new Utils.WordEnding("child", "children"));
      plurals.add(new Utils.WordEnding("sheep", "sheep", true));
      plurals.add(new Utils.WordEnding("", "s"));
      Language.addListener(() -> {
         boolean english = englishChat.isEmpty();
         chat.clear();

         for (ChatColor style : styles) {
            for (String s : Language.getList("chat styles." + style.name())) {
               chat.put(s.toLowerCase(Locale.ENGLISH), style.toString());
               if (english) {
                  englishChat.put(s.toLowerCase(Locale.ENGLISH), style.toString());
               }
            }
         }
      });
   }

   public record PluralResult(String updated, boolean plural) {
   }

   protected record WordEnding(String singular, String plural, boolean isCompleteWord) {
      public WordEnding(String singular, String plural) {
         this(singular, plural, false);
      }

      @Override
      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else {
            return !(object instanceof Utils.WordEnding ending)
               ? false
               : Objects.equals(this.singular, ending.singular) && Objects.equals(this.plural, ending.plural);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.singular, this.plural);
      }
   }
}
