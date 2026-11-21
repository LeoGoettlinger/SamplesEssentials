package org.evenmorefish.fish.libs.daisylib.messages.replacer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.Builder;
import org.evenmorefish.fish.libs.daisylib.messages.ObjectProcessor;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentSingleMessage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Replacer {
   private static final Pattern PATTERN = Pattern.compile("\\{([^{}]*)}");
   private final Map<String, Object> replacements = new HashMap<>();

   private Replacer() {
   }

   public static Replacer replacer() {
      return new Replacer();
   }

   public Replacer addReplacement(@NonNull String string, @Nullable Object replacement) {
      this.replacements.put(string, replacement);
      return this;
   }

   public Replacer addReplacements(@NonNull Map<String, ?> replacements) {
      this.replacements.putAll((Map<? extends String, ? extends Object>)replacements);
      return this;
   }

   public Replacer merge(Replacer... others) {
      for (Replacer other : others) {
         if (other != this) {
            this.replacements.putAll(other.replacements);
         }
      }

      return this;
   }

   public Replacer clear() {
      this.replacements.clear();
      return this;
   }

   public Component apply(@NonNull Component component) {
      return this.replacements.isEmpty() ? component : this.apply(component, this.getResolver());
   }

   public List<Component> apply(@NonNull List<Component> components) {
      if (this.replacements.isEmpty()) {
         return components;
      } else {
         TagResolver resolver = this.getResolver();
         return components.stream().map(component -> this.apply(component, resolver)).toList();
      }
   }

   private Component apply(Component component, TagResolver resolver) {
      MiniMessage mm = MiniMessage.miniMessage();
      String string = (String)mm.serialize(component);
      return mm.deserialize(this.processVariables(string), resolver);
   }

   public List<Component> applyWithListInsertion(@NonNull List<Component> components) {
      List<Component> newList = new ArrayList<>();

      for (Component component : components) {
         boolean replaced = false;

         for (Entry<String, Object> entry : this.replacements.entrySet()) {
            ComponentSingleMessage single = ComponentMessage.componentMessage(component);
            if (single.contains(entry.getKey())) {
               newList.addAll(ObjectProcessor.process(entry.getValue()));
               replaced = true;
               break;
            }
         }

         if (!replaced) {
            newList.add(component);
         }
      }

      return newList;
   }

   private TagResolver getResolver() {
      Builder builder = TagResolver.builder();
      this.replacements.forEach((variable, replacement) -> {
         Component parsed = Component.join(JoinConfiguration.commas(true), ObjectProcessor.process(replacement));
         builder.resolver(TagResolver.resolver(this.getVariableName(variable), Tag.selfClosingInserting(parsed)));
      });
      return builder.build();
   }

   private String processVariables(String string) {
      Matcher matcher = PATTERN.matcher(string);
      StringBuilder result = new StringBuilder();

      while (matcher.find()) {
         String exactMatch = matcher.group();
         if (this.replacements.containsKey(exactMatch)) {
            String name = exactMatch.substring(1, exactMatch.length() - 1);
            String replacement = "<" + name + ">";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
         } else {
            matcher.appendReplacement(result, Matcher.quoteReplacement(exactMatch));
         }
      }

      matcher.appendTail(result);
      return result.toString();
   }

   private String getVariableName(String string) {
      return PATTERN.matcher(string).replaceAll("$1");
   }
}
