package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.PatternedParser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegistryParser<R extends Keyed> extends PatternedParser<R> {
   private final Registry<R> registry;
   private final String languageNode;
   private final Consumer<R> parseCallback;
   private final Map<R, String> names = new HashMap<>();
   private final Map<String, R> parseMap = new HashMap<>();
   private String[] patterns;

   public RegistryParser(Registry<R> registry, String languageNode) {
      this(registry, languageNode, ignored -> {});
   }

   public RegistryParser(Registry<R> registry, String languageNode, Consumer<R> parseCallback) {
      assert !languageNode.isEmpty() && !languageNode.endsWith(".") : languageNode;

      this.registry = registry;
      this.languageNode = languageNode;
      this.parseCallback = parseCallback;
      this.refresh();
      Language.addListener(this::refresh);
   }

   private void refresh() {
      this.names.clear();
      this.parseMap.clear();

      for (R registryObject : this.registry) {
         NamespacedKey namespacedKey = registryObject.getKey();
         String namespace = namespacedKey.getNamespace();
         String key = namespacedKey.getKey();
         String keyWithSpaces = key.replace("_", " ");
         this.parseMap.put(namespacedKey.toString(), registryObject);
         String languageKey;
         if (namespace.equalsIgnoreCase("minecraft")) {
            this.parseMap.put(keyWithSpaces, registryObject);
            languageKey = this.languageNode + "." + key;
         } else {
            languageKey = namespacedKey.toString();
         }

         String[] options = Language.getList(languageKey);
         if (options.length != 1 || !options[0].equals(languageKey.toLowerCase(Locale.ENGLISH))) {
            for (String option : options) {
               option = option.toLowerCase(Locale.ENGLISH);
               NonNullPair<String, Integer> strippedOption = Noun.stripGender(option, languageKey);
               String first = strippedOption.getFirst();
               Integer second = strippedOption.getSecond();
               this.names.putIfAbsent(registryObject, first);
               this.parseMap.put(first, registryObject);
               if (second != -1) {
                  this.parseMap.put(Noun.getArticleWithSpace(second, 4) + first, registryObject);
               }
            }
         } else if (namespace.equalsIgnoreCase("minecraft")) {
            this.names.put(registryObject, keyWithSpaces);
         } else {
            this.names.put(registryObject, namespacedKey.toString());
         }
      }

      this.patterns = this.parseMap.keySet().stream().filter(pattern -> !pattern.startsWith("minecraft:")).sorted().toArray(String[]::new);
   }

   @Nullable
   public R parse(String input, @NotNull ParseContext context) {
      R element = this.parseMap.get(input.toLowerCase(Locale.ENGLISH));
      if (element != null) {
         this.parseCallback.accept(element);
      }

      return element;
   }

   @NotNull
   public String toString(R object, int flags) {
      return this.names.get(object);
   }

   @NotNull
   public String toVariableNameString(R object) {
      return this.toString(object, 0);
   }

   @Override
   public String[] getPatterns() {
      return this.patterns;
   }
}
