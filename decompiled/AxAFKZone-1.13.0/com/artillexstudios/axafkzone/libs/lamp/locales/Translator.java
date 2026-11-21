package com.artillexstudios.axafkzone.libs.lamp.locales;

import java.util.Locale;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NotNull;

public interface Translator {
   @NotNull
   static Translator create() {
      return new SimpleTranslator();
   }

   @NotNull
   String get(@NotNull String var1);

   @NotNull
   String get(@NotNull String var1, @NotNull Locale var2);

   void add(@NotNull LocaleReader var1);

   void add(@NotNull ResourceBundle var1);

   void addResourceBundle(@NotNull String var1, @NotNull Locale... var2);

   void addResourceBundle(@NotNull String var1);

   @NotNull
   Locale getLocale();

   void setLocale(@NotNull Locale var1);
}
