package github.nighter.smartspawner.language.section;

import github.nighter.smartspawner.language.cache.LanguageCache;
import github.nighter.smartspawner.language.file.LocaleData;
import github.nighter.smartspawner.language.format.NumberFormatter;
import github.nighter.smartspawner.language.format.PlaceholderFormatter;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public final class FormattingLanguageSection {
   private final Supplier<LocaleData> localeSupplier;
   private final BooleanSupplier enabled;
   private final LanguageCache cache;
   private final PlaceholderFormatter placeholderFormatter;
   private final BiFunction<String, Map<String, String>, String> formatter;

   public FormattingLanguageSection(
      Supplier<LocaleData> localeSupplier,
      BooleanSupplier enabled,
      LanguageCache cache,
      PlaceholderFormatter placeholderFormatter,
      BiFunction<String, Map<String, String>, String> formatter
   ) {
      this.localeSupplier = localeSupplier;
      this.enabled = enabled;
      this.cache = cache;
      this.placeholderFormatter = placeholderFormatter;
      this.formatter = formatter;
   }

   public String number(double number) {
      YamlConfiguration config = this.enabled.getAsBoolean() ? this.localeSupplier.get().formatting() : new YamlConfiguration();
      return new NumberFormatter(config).format(number);
   }

   public String mobName(EntityType type) {
      if (type != null && type != EntityType.UNKNOWN) {
         String cacheKey = "mob_name|" + type.name();
         String cached = this.cache.getCachedEntityName(cacheKey);
         if (cached != null) {
            return cached;
         } else {
            String result = null;
            if (this.enabled.getAsBoolean()) {
               result = this.localeSupplier.get().formatting().getString("mob_names." + type.name());
               if (result != null) {
                  result = this.formatter.apply(result, null);
               }
            }

            if (result == null) {
               result = this.placeholderFormatter.formatEnumName(type.name());
            }

            this.cache.putCachedEntityName(cacheKey, result);
            return result;
         }
      } else {
         return "Unknown";
      }
   }
}
