package github.nighter.smartspawner.language.section;

import github.nighter.smartspawner.language.file.LocaleData;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class MessageLanguageSection {
   private static final Map<String, String> EMPTY_PLACEHOLDERS = Collections.emptyMap();
   private final Supplier<LocaleData> localeSupplier;
   private final BiFunction<String, Map<String, String>, String> formatter;
   private final BiFunction<String, Map<String, String>, String> placeholderFormatter;

   public MessageLanguageSection(
      Supplier<LocaleData> localeSupplier,
      BiFunction<String, Map<String, String>, String> formatter,
      BiFunction<String, Map<String, String>, String> placeholderFormatter
   ) {
      this.localeSupplier = localeSupplier;
      this.formatter = formatter;
      this.placeholderFormatter = placeholderFormatter;
   }

   public String message(String key, Map<String, String> placeholders) {
      if (!this.isEnabled(key)) {
         return null;
      } else {
         String message = this.resolve(key + ".message");
         return message == null ? "Missing message: " + key : this.formatter.apply(this.locale().messages().getString("prefix", "") + message, placeholders);
      }
   }

   public String messageWithoutPrefix(String key, Map<String, String> placeholders) {
      if (!this.isEnabled(key)) {
         return null;
      } else {
         String message = this.resolve(key + ".message");
         return message == null ? "Missing message: " + key : this.formatter.apply(message, placeholders);
      }
   }

   public String consoleMessage(String key, Map<String, String> placeholders) {
      if (!this.isEnabled(key)) {
         return null;
      } else {
         String message = this.resolve(key + ".message");
         return message == null ? "Missing message: " + key : this.placeholderFormatter.apply(message, placeholders);
      }
   }

   public String title(String key, Map<String, String> placeholders) {
      return this.isEnabled(key) ? this.raw(key + ".title", placeholders) : null;
   }

   public String subtitle(String key, Map<String, String> placeholders) {
      return this.isEnabled(key) ? this.raw(key + ".subtitle", placeholders) : null;
   }

   public String actionBar(String key, Map<String, String> placeholders) {
      return this.isEnabled(key) ? this.raw(key + ".action_bar", placeholders) : null;
   }

   public String sound(String key) {
      return this.isEnabled(key) ? this.resolve(key + ".sound") : null;
   }

   public String raw(String path, Map<String, String> placeholders) {
      String message = this.resolve(path);
      return message == null ? null : this.formatter.apply(message, placeholders);
   }

   public boolean contains(String key) {
      return this.locale().commandMessages().contains(key) || this.locale().messages().contains(key);
   }

   public String commandConfig(String path, String defaultValue, Map<String, String> placeholders) {
      String value = this.locale().commandMessages().getString(path);
      return value == null ? defaultValue : this.formatter.apply(value, placeholders);
   }

   public String commandConfig(String path, String defaultValue) {
      return this.commandConfig(path, defaultValue, EMPTY_PLACEHOLDERS);
   }

   private String resolve(String path) {
      String value = this.locale().commandMessages().getString(path);
      return value != null ? value : this.locale().messages().getString(path);
   }

   private boolean isEnabled(String key) {
      return !this.locale().commandMessages().contains(key)
         ? this.locale().messages().getBoolean(key + ".enabled", true)
         : this.locale().commandMessages().getBoolean(key + ".enabled", true);
   }

   private LocaleData locale() {
      return this.localeSupplier.get();
   }
}
