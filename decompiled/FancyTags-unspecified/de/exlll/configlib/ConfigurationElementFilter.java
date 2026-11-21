package de.exlll.configlib;

import java.util.function.Predicate;

public interface ConfigurationElementFilter extends Predicate<ConfigurationElement<?>> {
   default ConfigurationElementFilter and(Predicate<? super ConfigurationElement<?>> other) {
      return element -> this.test(element) && other.test(element);
   }

   static ConfigurationElementFilter byType(Class<?> type) {
      Validator.requireNonNull(type, "type");
      return element -> element.type().equals(type);
   }

   static ConfigurationElementFilter byPostProcessKey(String key) {
      Validator.requireNonNull(key, "post-process key");
      return element -> {
         PostProcess postProcess = element.annotation(PostProcess.class);
         if (postProcess == null) {
            return false;
         } else {
            String actualKey = postProcess.key();
            return actualKey.equals(key);
         }
      };
   }
}
