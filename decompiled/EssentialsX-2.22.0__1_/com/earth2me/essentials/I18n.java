package com.earth2me.essentials;

import com.earth2me.essentials.adventure.AdventureUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.ess3.api.II18n;
import org.jetbrains.annotations.NotNull;

public class I18n implements II18n {
   private static final String MESSAGES = "messages";
   private static final Pattern NODOUBLEMARK = Pattern.compile("''");
   private static final ExecutorService BUNDLE_LOADER_EXECUTOR = Executors.newFixedThreadPool(2);
   private static final ResourceBundle NULL_BUNDLE = new ResourceBundle() {
      @Override
      public Enumeration<String> getKeys() {
         return null;
      }

      @Override
      protected Object handleGetObject(@NotNull final String key) {
         return null;
      }
   };
   private static I18n instance;
   private final transient Locale defaultLocale = Locale.getDefault();
   private final transient ResourceBundle defaultBundle;
   private final transient net.ess3.api.IEssentials ess;
   private transient Locale currentLocale = this.defaultLocale;
   private final transient Map<Locale, ResourceBundle> loadedBundles = new ConcurrentHashMap<>();
   private final transient List<Locale> loadingBundles = new ArrayList<>();
   private transient ResourceBundle localeBundle;
   private final transient Map<Locale, Map<String, MessageFormat>> messageFormatCache = new HashMap<>();

   public I18n(final net.ess3.api.IEssentials ess) {
      this.ess = ess;
      this.defaultBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH, new I18n.UTF8PropertiesControl());
      this.localeBundle = this.defaultBundle;
   }

   public static String tlLiteral(final String tlKey, final Object... objects) {
      return instance == null ? "" : tlLocale(instance.currentLocale, tlKey, objects);
   }

   public static String tlLocale(final Locale locale, final String tlKey, final Object... objects) {
      if (instance == null) {
         return "";
      } else {
         return objects.length == 0 ? NODOUBLEMARK.matcher(instance.translate(locale, tlKey)).replaceAll("'") : instance.format(locale, tlKey, objects);
      }
   }

   public static String capitalCase(final String input) {
      return input != null && !input.isEmpty() ? input.toUpperCase(Locale.ENGLISH).charAt(0) + input.toLowerCase(Locale.ENGLISH).substring(1) : input;
   }

   public void onEnable() {
      instance = this;
   }

   public void onDisable() {
      instance = null;
   }

   @Override
   public Locale getCurrentLocale() {
      return this.currentLocale;
   }

   private ResourceBundle getBundle(final Locale locale) {
      if (this.loadedBundles.containsKey(locale)) {
         return this.loadedBundles.get(locale);
      } else {
         synchronized (this.loadingBundles) {
            if (!this.loadingBundles.contains(locale)) {
               this.loadingBundles.add(locale);
               BUNDLE_LOADER_EXECUTOR.submit(() -> {
                  this.blockingLoadBundle(locale);
                  synchronized (this.loadingBundles) {
                     this.loadingBundles.remove(locale);
                  }
               });
            }
         }

         return this.defaultBundle;
      }
   }

   public void blockingLoadBundle(final Locale locale) {
      if (!this.loadedBundles.containsKey(locale)) {
         ResourceBundle bundle;
         try {
            bundle = ResourceBundle.getBundle(
               "messages", locale, new I18n.FileResClassLoader(I18n.class.getClassLoader(), this.ess), new I18n.UTF8PropertiesControl()
            );
         } catch (MissingResourceException var6) {
            try {
               bundle = ResourceBundle.getBundle("messages", locale, new I18n.UTF8PropertiesControl());
            } catch (MissingResourceException var5) {
               bundle = NULL_BUNDLE;
            }
         }

         this.loadedBundles.put(locale, bundle);
      }
   }

   private String translate(final Locale locale, final String string) {
      try {
         try {
            return this.getBundle(locale).getString(string);
         } catch (MissingResourceException var4) {
            return this.localeBundle.getString(string);
         }
      } catch (MissingResourceException var5) {
         if (this.ess != null && this.ess.getSettings().isDebug()) {
            this.ess
               .getLogger()
               .log(
                  Level.WARNING,
                  String.format("Missing translation key \"%s\" in translation file %s", var5.getKey(), this.localeBundle.getLocale().toString()),
                  (Throwable)var5
               );
         }

         return this.defaultBundle.getString(string);
      }
   }

   private String format(final Locale locale, final String string, final Object... objects) {
      String format = this.translate(locale, string);
      MessageFormat messageFormat = this.messageFormatCache.computeIfAbsent(locale, l -> new HashMap<>()).get(format);
      if (messageFormat == null) {
         try {
            messageFormat = new MessageFormat(format);
         } catch (IllegalArgumentException var7) {
            this.ess.getLogger().log(Level.SEVERE, "Invalid Translation key for '" + string + "': " + var7.getMessage());
            format = format.replaceAll("\\{(\\D*?)}", "\\[$1\\]");
            messageFormat = new MessageFormat(format);
         }

         this.messageFormatCache.get(locale).put(format, messageFormat);
      }

      Object[] processedArgs = mutateArgs(
         objects,
         arg -> arg instanceof AdventureUtil.ParsedPlaceholder
            ? arg.toString()
            : this.ess.getAdventureFacet().legacyToMini(this.ess.getAdventureFacet().escapeTags(arg.toString()))
      );
      return messageFormat.format(processedArgs).replace(' ', ' ');
   }

   public static Object[] mutateArgs(final Object[] objects, final Function<Object, String> mutator) {
      Object[] args = new Object[objects.length];

      for (int i = 0; i < objects.length; i++) {
         Object object = objects[i];
         if (!(object instanceof Number) && !(object instanceof Date) && object != null) {
            args[i] = mutator.apply(object);
         } else {
            args[i] = object;
         }
      }

      return args;
   }

   public void updateLocale(final String loc) {
      if (loc != null && !loc.isEmpty()) {
         this.currentLocale = getLocale(loc);
      }

      ResourceBundle.clearCache();
      this.loadedBundles.clear();
      this.messageFormatCache.clear();
      this.ess.getLogger().log(Level.INFO, String.format("Using locale %s", this.currentLocale.toString()));

      try {
         this.localeBundle = ResourceBundle.getBundle("messages", this.currentLocale, new I18n.UTF8PropertiesControl());
      } catch (MissingResourceException var3) {
         this.localeBundle = NULL_BUNDLE;
      }
   }

   public static Locale getLocale(final String loc) {
      if (loc != null && !loc.isEmpty()) {
         String[] parts = loc.split("[_.]");
         if (parts.length == 1) {
            return new Locale(parts[0]);
         } else if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
         } else {
            return parts.length == 3 ? new Locale(parts[0], parts[1], parts[2]) : instance.currentLocale;
         }
      } else {
         return instance.currentLocale;
      }
   }

   private static class FileResClassLoader extends ClassLoader {
      private final transient File messagesFolder;

      FileResClassLoader(final ClassLoader classLoader, final net.ess3.api.IEssentials ess) {
         super(classLoader);
         this.messagesFolder = new File(ess.getDataFolder(), "messages");
         this.messagesFolder.mkdirs();
      }

      @Override
      public URL getResource(final String string) {
         File file = new File(this.messagesFolder, string);
         if (file.exists()) {
            try {
               return file.toURI().toURL();
            } catch (MalformedURLException var4) {
            }
         }

         return null;
      }

      @Override
      public InputStream getResourceAsStream(final String string) {
         File file = new File(this.messagesFolder, string);
         if (file.exists()) {
            try {
               return new FileInputStream(file);
            } catch (FileNotFoundException var4) {
            }
         }

         return null;
      }
   }

   private static final class UTF8PropertiesControl extends Control {
      private UTF8PropertiesControl() {
      }

      @Override
      public ResourceBundle newBundle(final String baseName, final Locale locale, final String format, final ClassLoader loader, final boolean reload) throws IOException {
         String resourceName = this.toResourceName(this.toBundleName(baseName, locale), "properties");
         ResourceBundle bundle = null;
         InputStream stream = null;
         if (reload) {
            URL url = loader.getResource(resourceName);
            if (url != null) {
               URLConnection connection = url.openConnection();
               if (connection != null) {
                  connection.setUseCaches(false);
                  stream = connection.getInputStream();
               }
            }
         } else {
            stream = loader.getResourceAsStream(resourceName);
         }

         if (stream != null) {
            try {
               bundle = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
            } finally {
               stream.close();
            }
         }

         return bundle;
      }

      @Override
      public Locale getFallbackLocale(String baseName, Locale locale) {
         if (baseName != null && locale != null) {
            return null;
         } else {
            throw new NullPointerException();
         }
      }
   }
}
