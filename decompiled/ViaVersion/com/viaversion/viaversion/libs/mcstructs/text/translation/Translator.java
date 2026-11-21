package com.viaversion.viaversion.libs.mcstructs.text.translation;

import javax.annotation.Nullable;

@FunctionalInterface
public interface Translator {
   BasicTranslator GLOBAL = new BasicTranslator();

   @Nullable
   String translate(String var1);

   default String translateOrKey(String key) {
      String translation = this.translate(key);
      return translation != null ? translation : key;
   }
}
