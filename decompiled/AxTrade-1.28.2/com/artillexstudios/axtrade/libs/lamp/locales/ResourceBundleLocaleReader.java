package com.artillexstudios.axtrade.libs.lamp.locales;

import com.artillexstudios.axtrade.libs.lamp.util.Preconditions;
import java.util.Locale;
import java.util.ResourceBundle;

final class ResourceBundleLocaleReader implements LocaleReader {
   private final ResourceBundle resourceBundle;

   public ResourceBundleLocaleReader(ResourceBundle resourceBundle) {
      this.resourceBundle = Preconditions.notNull(resourceBundle, "resource bundle");
   }

   @Override
   public boolean containsKey(String key) {
      return this.resourceBundle.containsKey(key);
   }

   @Override
   public String get(String key) {
      return this.resourceBundle.getString(key);
   }

   @Override
   public Locale getLocale() {
      return this.resourceBundle.getLocale();
   }
}
