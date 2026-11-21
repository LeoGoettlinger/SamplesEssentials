package me.byteful.plugin.leveltools.libs.lamp.locales;

import java.util.Locale;
import java.util.ResourceBundle;
import me.byteful.plugin.leveltools.libs.lamp.util.Preconditions;

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
