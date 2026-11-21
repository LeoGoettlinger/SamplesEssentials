package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.util.ExceptionUtils;
import ch.njol.skript.util.FileUtils;
import ch.njol.skript.util.Version;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.localization.Localizer;

public class Language {
   public static final int F_PLURAL = 1;
   public static final int F_DEFINITE_ARTICLE = 2;
   public static final int F_INDEFINITE_ARTICLE = 4;
   public static final int NO_ARTICLE_MASK = -7;
   private static String name = "english";
   private static final HashMap<String, String> defaultLanguage = new HashMap<>();
   @Nullable
   private static HashMap<String, String> localizedLanguage = null;
   private static final HashMap<String, Version> langVersion = new HashMap<>();
   private static final Pattern listSplitPattern = Pattern.compile("\\s*,\\s*");
   private static final List<LanguageChangeListener> listeners = new ArrayList<>();
   private static final int[] priorityStartIndices = new int[Language.LanguageListenerPriority.values().length];

   public static String getName() {
      return name;
   }

   @Nullable
   private static String get_i(String key) {
      String value;
      if ((value = defaultLanguage.get(key)) != null) {
         return value;
      } else if (localizedLanguage != null && (value = localizedLanguage.get(key)) != null) {
         return value;
      } else {
         if (Skript.testing()) {
            missingEntryError(key);
         }

         return null;
      }
   }

   public static String get(String key) {
      String s = get_i(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(key.toLowerCase(Locale.ENGLISH)));
      return s == null ? StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(key.toLowerCase(Locale.ENGLISH)) : s;
   }

   @Nullable
   public static String get_(String key) {
      return get_i(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(key.toLowerCase(Locale.ENGLISH)));
   }

   public static void missingEntryError(String key) {
      Skript.error("Missing entry '" + key.toLowerCase(Locale.ENGLISH) + "' in the default/english language file");
   }

   public static String format(String key, Object... args) {
      key = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(key.toLowerCase(Locale.ENGLISH));
      String value = get_i(key);
      if (value == null) {
         return key;
      } else {
         try {
            return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(String.format(value, args));
         } catch (Exception var4) {
            Skript.error("Invalid format string at '" + key + "' in the " + getName() + " language file: " + value);
            return key;
         }
      }
   }

   public static String getSpaced(String key) {
      String s = get(key);
      return s.isEmpty() ? " " : " " + s + " ";
   }

   public static String[] getList(String key) {
      String s = get_i(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(key.toLowerCase(Locale.ENGLISH)));
      if (s == null) {
         return new String[]{key.toLowerCase(Locale.ENGLISH)};
      } else {
         String[] r = listSplitPattern.split(s);

         assert r != null;

         return r;
      }
   }

   public static boolean keyExists(String key) {
      key = key.toLowerCase(Locale.ENGLISH);
      return defaultLanguage.containsKey(key) || localizedLanguage != null && localizedLanguage.containsKey(key);
   }

   public static boolean keyExistsDefault(String key) {
      return defaultLanguage.containsKey(key.toLowerCase(Locale.ENGLISH));
   }

   public static boolean isInitialized() {
      return !defaultLanguage.isEmpty();
   }

   @Nullable
   private static String getSanitizedLanguageDirectory(SkriptAddon addon) {
      Localizer localizer = addon.localizer();
      if (localizer == null) {
         return null;
      } else {
         String languageFileDirectory = localizer.languageFileDirectory();
         if (languageFileDirectory == null) {
            return null;
         } else {
            languageFileDirectory = languageFileDirectory.replace('\\', '/');
            if (languageFileDirectory.startsWith("/")) {
               languageFileDirectory = languageFileDirectory.substring(1);
            }

            if (languageFileDirectory.endsWith("/")) {
               languageFileDirectory = languageFileDirectory.substring(0, languageFileDirectory.length() - 1);
            }

            return languageFileDirectory;
         }
      }
   }

   public static void loadDefault(SkriptAddon addon) {
      String languageFileDirectory = getSanitizedLanguageDirectory(addon);
      if (languageFileDirectory != null) {
         Class<?> source = addon.source();

         assert source != null;

         try {
            try (
               InputStream defaultIs = source.getResourceAsStream("/" + languageFileDirectory + "/default.lang");
               InputStream englishIs = source.getResourceAsStream("/" + languageFileDirectory + "/english.lang");
            ) {
               InputStream defaultLangIs = defaultIs;
               InputStream englishLangIs = englishIs;
               if (defaultIs == null) {
                  if (englishIs == null) {
                     throw new IllegalStateException(addon + " is missing the required default.lang file!");
                  }

                  defaultLangIs = englishIs;
                  englishLangIs = null;
               }

               Map<String, String> def = load(defaultLangIs, "default", false);
               Map<String, String> en = load(englishLangIs, "english", addon instanceof org.skriptlang.skript.Skript);
               String v = def.get("version");
               if (v == null) {
                  Skript.warning("Missing version in default.lang");
               }

               langVersion.put(addon.name(), v == null ? Skript.getVersion() : new Version(v));
               def.remove("version");
               defaultLanguage.putAll(def);
               if (localizedLanguage == null) {
                  localizedLanguage = new HashMap<>();
               }

               localizedLanguage.putAll(en);

               for (LanguageChangeListener l : listeners) {
                  l.onLanguageChange();
               }
            }
         } catch (IOException var16) {
            throw new RuntimeException(var16);
         }
      }
   }

   public static boolean load(String name) {
      name = name.toLowerCase(Locale.ENGLISH);
      localizedLanguage = new HashMap<>();
      boolean exists = load(Skript.instance(), name, true);

      for (SkriptAddon addon : Skript.instance().addons()) {
         exists |= load(addon, name, false);
      }

      if (!exists) {
         if (name.equals("english")) {
            throw new RuntimeException("English language is missing (english.lang)");
         } else {
            load("english");
            return false;
         }
      } else {
         Language.name = name;

         for (LanguageChangeListener l : listeners) {
            l.onLanguageChange();
         }

         return true;
      }
   }

   private static boolean load(SkriptAddon addon, String name, boolean tryUpdate) {
      String languageFileDirectory = getSanitizedLanguageDirectory(addon);
      if (languageFileDirectory == null) {
         return false;
      } else {
         Class<?> source = addon.source();
         if (name.equals("english")) {
            try (InputStream is = source.getResourceAsStream("/" + languageFileDirectory + "/default.lang")) {
               if (is == null) {
                  return true;
               }
            } catch (IOException var18) {
               throw new RuntimeException(var18);
            }
         }

         Map<String, String> l;
         try (InputStream isx = source.getResourceAsStream("/" + languageFileDirectory + "/" + name + ".lang")) {
            l = load(isx, name, tryUpdate);
         } catch (IOException var15) {
            throw new RuntimeException(var15);
         }

         String dataFileDirectory = addon.localizer().dataFileDirectory();
         if (dataFileDirectory != null) {
            File file = new File(dataFileDirectory, File.separator + name + ".lang");

            try {
               if (file.exists()) {
                  l.putAll(load(new FileInputStream(file), name, tryUpdate));
               }
            } catch (FileNotFoundException var16) {
               assert false;
            }
         }

         if (l.isEmpty()) {
            return false;
         } else {
            if (!l.containsKey("version")) {
               Skript.error(addon + "'s language file " + name + ".lang does not provide a version number!");
            } else {
               try {
                  Version v = new Version(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(l.get("version")));
                  Version lv = langVersion.get(addon.name());

                  assert lv != null;

                  if (v.isSmallerThan(lv)) {
                     Skript.warning(addon + "'s language file " + name + ".lang is outdated, some messages will be english.");
                  }
               } catch (IllegalArgumentException var13) {
                  Skript.error("Illegal version syntax in " + addon + "'s language file " + name + ".lang: " + var13.getLocalizedMessage());
               }
            }

            l.remove("version");
            if (localizedLanguage != null) {
               for (Entry<String, String> entry : l.entrySet()) {
                  String key = entry.getKey();
                  String value = entry.getValue();
                  if (defaultLanguage.containsKey(key)) {
                     Skript.warning("'" + key + "' is part of the default language file, and can therefore not be modified in a localized language file.");
                  } else {
                     localizedLanguage.put(key, value);
                  }
               }
            } else {
               assert false : addon + "; " + name;
            }

            return true;
         }
      }
   }

   private static Map<String, String> load(@Nullable InputStream in, String name, boolean tryUpdate) {
      if (in == null) {
         return new HashMap<>();
      } else {
         try {
            Config langConfig = new Config(in, name + ".lang", false, false, ":");
            String langVersion = langConfig.getValue("version");
            if (tryUpdate && (langVersion == null || Skript.getVersion().compareTo(new Version(langVersion)) != 0)) {
               String langFileName = "lang/" + name + ".lang";
               InputStream newConfigIn = Skript.getInstance().getResource(langFileName);
               if (newConfigIn == null) {
                  Skript.error("The lang file '" + name + ".lang' is outdated, but Skript couldn't find the newest version of it in its jar.");
                  return new HashMap<>();
               }

               Config newLangConfig = new Config(newConfigIn, "Skript.jar/" + langFileName, false, false, ":");
               newConfigIn.close();
               File langFile = new File(Skript.getInstance().getDataFolder(), langFileName);
               if (!newLangConfig.compareValues(langConfig, "version")) {
                  File langFileBackup = FileUtils.backup(langFile);
                  newLangConfig.getMainNode().set("version", Skript.getVersion().toString());
                  langConfig = newLangConfig;
                  newLangConfig.save(langFile);
                  Skript.info(
                     "The lang file '"
                        + name
                        + ".lang' has been updated to the latest version. A backup of your old lang file has been created as "
                        + langFileBackup.getName()
                  );
               } else {
                  langConfig.getMainNode().set("version", Skript.getVersion().toString());
                  langConfig.save(langFile);
               }
            }

            return langConfig.toMap(".");
         } catch (IOException var19) {
            Skript.exception(var19, "Could not load the language file '" + name + ".lang': " + ExceptionUtils.toString(var19));
            return new HashMap<>();
         } finally {
            try {
               in.close();
            } catch (IOException var18) {
            }
         }
      }
   }

   public static void addListener(LanguageChangeListener listener) {
      addListener(listener, Language.LanguageListenerPriority.NORMAL);
   }

   public static void addListener(LanguageChangeListener listener, Language.LanguageListenerPriority priority) {
      listeners.add(priorityStartIndices[priority.ordinal()], listener);

      for (int i = priority.ordinal() + 1; i < Language.LanguageListenerPriority.values().length; i++) {
         priorityStartIndices[i]++;
      }

      if (isInitialized()) {
         listener.onLanguageChange();
      }
   }

   public static enum LanguageListenerPriority {
      EARLIEST,
      NORMAL,
      LATEST;
   }
}
