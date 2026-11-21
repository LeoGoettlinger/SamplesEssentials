package ch.njol.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class EnumParser<E extends Enum<E>> extends PatternedParser<E> implements org.skriptlang.skript.lang.converter.Converter<String, E> {
   private final Class<E> enumClass;
   private final String languageNode;
   private final Consumer<E> parseCallback;
   private String[] names;
   protected final Map<String, E> parseMap = new HashMap<>();
   private String[] patterns;

   public EnumParser(Class<E> enumClass, String languageNode) {
      this(enumClass, languageNode, ignored -> {});
   }

   public EnumParser(Class<E> enumClass, String languageNode, Consumer<E> parseCallback) {
      assert enumClass.isEnum() : enumClass;

      assert !languageNode.isEmpty() && !languageNode.endsWith(".") : languageNode;

      this.enumClass = enumClass;
      this.languageNode = languageNode;
      this.parseCallback = parseCallback;
      this.refresh();
      Language.addListener(this::refresh);
   }

   void refresh() {
      E[] constants = this.enumClass.getEnumConstants();
      this.names = new String[constants.length];
      this.parseMap.clear();

      for (E constant : constants) {
         String key = this.languageNode + "." + constant.name();
         int ordinal = constant.ordinal();
         String[] options = Language.getList(key);

         for (String option : options) {
            option = option.toLowerCase(Locale.ENGLISH);
            if (options.length == 1 && option.equals(key.toLowerCase(Locale.ENGLISH))) {
               String[] splitKey = key.split("\\.");
               String newKey = splitKey[1].replace('_', ' ').toLowerCase(Locale.ENGLISH) + " " + splitKey[0];
               this.parseMap.put(newKey, constant);
               Skript.debug("Missing lang enum constant for '" + key + "'. Using '" + newKey + "' for now.");
            } else {
               NonNullPair<String, Integer> strippedOption = Noun.stripGender(option, key);
               String first = strippedOption.getFirst();
               Integer second = strippedOption.getSecond();
               Noun.PluralPair singlePlural = Noun.parsePlural(first);
               String single = singlePlural.singular();
               String plural = singlePlural.plural();
               if (this.names[ordinal] == null) {
                  this.names[ordinal] = single;
               }

               this.parseMap.put(single, constant);
               if (!plural.isEmpty()) {
                  this.parseMap.put(plural, constant);
               }

               if (second != -1) {
                  this.parseMap.put(Noun.getArticleWithSpace(second, 4) + single, constant);
               }
            }
         }
      }

      this.patterns = this.parseMap.keySet().toArray(String[]::new);
   }

   @Nullable
   public E parse(String string, ParseContext context) {
      E element = this.parseMap.get(string.toLowerCase(Locale.ENGLISH));
      if (element != null) {
         this.parseCallback.accept(element);
      }

      return element;
   }

   @Nullable
   public E convert(String string) {
      return this.parse(string, ParseContext.DEFAULT);
   }

   public String toVariableNameString(E object) {
      return this.toString(object, 0);
   }

   @Override
   public String[] getPatterns() {
      return this.patterns;
   }

   public String toString(E object, int flags) {
      String name = this.names[object.ordinal()];
      return name != null ? name : object.name();
   }
}
