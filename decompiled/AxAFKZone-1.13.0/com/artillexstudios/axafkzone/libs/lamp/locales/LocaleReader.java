package com.artillexstudios.axafkzone.libs.lamp.locales;

import java.util.Locale;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;

public interface LocaleReader {
   boolean containsKey(String var1);

   String get(String var1);

   Locale getLocale();

   @NotNull
   static LocaleReader wrap(@NotNull ResourceBundle bundle) {
      return new ResourceBundleLocaleReader(bundle);
   }
}
