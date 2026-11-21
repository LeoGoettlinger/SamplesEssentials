package com.artillexstudios.axtrade.libs.lamp.locales;

import com.artillexstudios.axtrade.libs.lamp.util.Preconditions;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;

final class SimpleTranslator implements Translator {
   private static final LinkedList<LocaleReader> EMPTY_LIST = new LinkedList<>();
   private final Map<Locale, LinkedList<LocaleReader>> registeredBundles = new HashMap<>();
   private volatile Locale locale = Locale.ENGLISH;

   SimpleTranslator() {
      this.addResourceBundle("lamp");
      if (classExists("org.bukkit.Bukkit")) {
         this.addResourceBundle("lamp-bukkit");
      }

      if (classExists("org.spongepowered.api.Sponge")) {
         this.addResourceBundle("lamp-sponge");
      }

      if (classExists("com.velocitypowered.api.proxy.ProxyServer")) {
         this.addResourceBundle("lamp-velocity");
      }

      if (classExists("net.md_5.bungee.api.ProxyServer")) {
         this.addResourceBundle("lamp-bungee");
      }

      if (classExists("net.dv8tion.jda.api.JDA")) {
         this.addResourceBundle("lamp-jda");
      }
   }

   @NotNull
   @Override
   public String get(@NotNull String key) {
      return this.get(key, this.locale);
   }

   @NotNull
   @Override
   public String get(@NotNull String key, @NotNull Locale locale) {
      Preconditions.notNull(key, "key");
      Preconditions.notNull(locale, "locale");

      for (LocaleReader registeredBundle : this.registeredBundles.getOrDefault(locale, EMPTY_LIST)) {
         if (registeredBundle.containsKey(key)) {
            return registeredBundle.get(key);
         }
      }

      for (LocaleReader registeredBundlex : this.registeredBundles.getOrDefault(this.locale, EMPTY_LIST)) {
         if (registeredBundlex.containsKey(key)) {
            return registeredBundlex.get(key);
         }
      }

      return key;
   }

   @Override
   public void add(@NotNull LocaleReader reader) {
      LinkedList<LocaleReader> list = this.registeredBundles.computeIfAbsent(reader.getLocale(), v -> new LinkedList<>());
      list.push(reader);
   }

   @NotNull
   @Override
   public Locale getLocale() {
      return this.locale;
   }

   @Override
   public void setLocale(@NotNull Locale locale) {
      Preconditions.notNull(locale, "locale");
      this.locale = locale;
   }

   @Override
   public void addResourceBundle(@NotNull String resourceBundle, @NotNull Locale... locales) {
      Preconditions.notNull(resourceBundle, "resource bundle");
      Preconditions.notNull(locales, "locales");

      for (Locale locale : locales) {
         try {
            ResourceBundle bundle = ResourceBundle.getBundle(resourceBundle, locale, this.getClass().getClassLoader(), UTF8Control.INSTANCE);
            this.add(bundle);
         } catch (MissingResourceException var8) {
         }
      }
   }

   @Override
   public void addResourceBundle(@NotNull String resourceBundle) {
      Preconditions.notNull(resourceBundle, "resource bundle");

      for (Locale locale : Locales.getLocales()) {
         try {
            ResourceBundle bundle = ResourceBundle.getBundle(resourceBundle, locale, this.getClass().getClassLoader(), UTF8Control.INSTANCE);
            this.add(bundle);
         } catch (MissingResourceException var5) {
         }
      }
   }

   @Override
   public void add(@NotNull ResourceBundle resourceBundle) {
      Preconditions.notNull(resourceBundle, "resource bundle");
      this.add(LocaleReader.wrap(resourceBundle));
   }

   private static boolean classExists(String name) {
      try {
         Class.forName(name);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }
}
