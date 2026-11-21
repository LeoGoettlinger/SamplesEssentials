package com.earth2me.essentials.libs.configurate.objectmapping.meta;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

final class Localization {
   private Localization() {
   }

   static String key(final ResourceBundle bundle, final String key) {
      try {
         return bundle.getString(key);
      } catch (MissingResourceException var3) {
         return key;
      }
   }
}
