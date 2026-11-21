package com.sk89q.worldedit.command.util;

import com.fastasyncworldedit.core.util.MathMan;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.registry.NamespacedRegistry;
import com.sk89q.worldedit.registry.Registry;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SuggestionHelper {
   private SuggestionHelper() {
   }

   public static Stream<String> getBlockCategorySuggestions(String tag, boolean allowRandom) {
      if (!tag.isEmpty() && !tag.equals("#")) {
         if (!tag.startsWith("##")) {
            return Stream.empty();
         } else if (tag.equals("##")) {
            return Stream.concat(
               allowRandom ? Stream.of("##*") : Stream.empty(), getNamespacedRegistrySuggestions(BlockCategory.REGISTRY, tag.substring(2)).map(s -> "##" + s)
            );
         } else if (tag.equals("##*") && allowRandom) {
            return getNamespacedRegistrySuggestions(BlockCategory.REGISTRY, tag.substring(3)).map(s -> "##*" + s);
         } else {
            boolean wild = tag.startsWith("##*") && allowRandom;
            return getNamespacedRegistrySuggestions(BlockCategory.REGISTRY, tag.substring(wild ? 3 : 2)).map(s -> (wild ? "##*" : "##") + s);
         }
      } else {
         return allowRandom ? Stream.of("##", "##*") : Stream.of("##");
      }
   }

   public static Stream<String> getBlockPropertySuggestions(String blockTypeString, BlockType type, String props) {
      if (type == null) {
         return Stream.empty();
      } else if (!props.toLowerCase(Locale.ROOT).equals(props)) {
         return Stream.empty();
      } else {
         Map<String, ? extends Property<?>> propertyMap = type.getPropertyMap();
         Set<String> matchedProperties = new HashSet<>();
         String[] propParts = props.split(",", -1);

         for (int i = 0; i < propParts.length; i++) {
            String[] propVal = propParts[i].split("=");
            String matchProp = propVal[0].toLowerCase(Locale.ROOT);
            if (i == propParts.length - 1) {
               String previous = Arrays.stream(propParts, 0, propParts.length - 1).collect(Collectors.joining(",")) + (propParts.length == 1 ? "" : ",");
               String lastValidInput = (blockTypeString + "[" + previous).toLowerCase(Locale.ROOT);
               if (propVal.length == 1) {
                  List<? extends Property<?>> matchingProps = propertyMap.entrySet()
                     .stream()
                     .filter(p -> !matchedProperties.contains(p.getKey()) && p.getKey().startsWith(matchProp))
                     .map(Entry::getValue)
                     .collect(Collectors.toList());
                  switch (matchingProps.size()) {
                     case 0:
                        return propertyMap.keySet().stream().filter(p -> !matchedProperties.contains(p)).map(propx -> lastValidInput + propx + "=");
                     case 1:
                        return matchingProps.get(0)
                           .getValues()
                           .stream()
                           .map(val -> lastValidInput + matchingProps.get(0).getName() + "=" + val.toString().toLowerCase(Locale.ROOT));
                     default:
                        return matchingProps.stream().map(p -> lastValidInput + p.getName() + "=");
                  }
               }

               Property<?> prop = (Property<?>)propertyMap.get(matchProp);
               if (prop == null) {
                  return propertyMap.keySet().stream().map(p -> lastValidInput + p);
               }

               List<String> values = prop.getValues().stream().map(v -> v.toString().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
               String matchVal = propVal[1].toLowerCase(Locale.ROOT);
               List<String> matchingVals = values.stream().filter(val -> val.startsWith(matchVal)).collect(Collectors.toList());
               if (matchingVals.isEmpty()) {
                  return values.stream().map(val -> lastValidInput + prop.getName() + "=" + val);
               }

               if (matchingVals.size() == 1 && matchingVals.get(0).equals(matchVal)) {
                  String currProp = lastValidInput + prop.getName() + "=" + matchVal;
                  if (matchingVals.size() < values.size()) {
                     return Stream.of(currProp + "] ", currProp + ",");
                  }

                  return Stream.of(currProp + "] ");
               }

               return matchingVals.stream().map(val -> lastValidInput + prop.getName() + "=" + val);
            }

            if (propVal.length != 2) {
               return Stream.empty();
            }

            Property<?> propx = (Property<?>)propertyMap.get(matchProp);
            if (propx == null) {
               return Stream.empty();
            }

            try {
               propx.getValueFor(propVal[1]);
               matchedProperties.add(propx.getName());
            } catch (IllegalArgumentException var16) {
               return Stream.empty();
            }
         }

         return Stream.empty();
      }
   }

   public static <V extends Keyed> Stream<String> getRegistrySuggestions(Registry<V> registry, String input) {
      return registry instanceof NamespacedRegistry
         ? getNamespacedRegistrySuggestions((NamespacedRegistry<V>)registry, input)
         : org.enginehub.piston.converter.SuggestionHelper.limitByPrefix(registry.keySet().stream(), input).stream();
   }

   public static <V extends Keyed> Stream<String> getNamespacedRegistrySuggestions(NamespacedRegistry<V> registry, String input) {
      if (input.isEmpty() || input.equals(":")) {
         Set<String> namespaces = registry.getKnownNamespaces();
         if (namespaces.size() == 1) {
            int def = namespaces.iterator().next().length() + 1;
            return registry.keySet().stream().map(s -> s.substring(def));
         } else {
            return namespaces.stream().map(s -> s + ":");
         }
      } else if (input.startsWith(":")) {
         String term = input.substring(1).toLowerCase(Locale.ROOT);
         Predicate<String> search = org.enginehub.piston.converter.SuggestionHelper.byPrefix(term);
         return registry.keySet().stream().filter(s -> search.test(s.substring(s.indexOf(58) + 1)));
      } else if (input.indexOf(58) < 0) {
         String lowerSearch = input.toLowerCase(Locale.ROOT);
         String defKey = registry.getDefaultNamespace() + ":" + lowerSearch;
         int defLength = registry.getDefaultNamespace().length() + 1;
         return Stream.concat(
            registry.keySet().stream().filter(s -> s.startsWith(defKey)).map(s -> s.substring(defLength)),
            registry.getKnownNamespaces().stream().filter(n -> n.startsWith(lowerSearch)).map(n -> n + ":")
         );
      } else {
         Predicate<String> search = org.enginehub.piston.converter.SuggestionHelper.byPrefix(input.toLowerCase(Locale.ROOT));
         return registry.keySet().stream().filter(search);
      }
   }

   public static Stream<String> suggestPositiveDoubles(String argumentInput) {
      if (argumentInput.isEmpty()) {
         return Stream.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
      } else if (isDouble(argumentInput)) {
         Stream<String> numbers = Stream.of("", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
         if (argumentInput.indexOf(46) == -1) {
            numbers = Stream.concat(numbers, Stream.of("."));
         }

         return numbers.map(s -> argumentInput + s);
      } else {
         return Stream.empty();
      }
   }

   public static Stream<String> suggestPositiveIntegers(String argumentInput) {
      if (argumentInput.isEmpty()) {
         return IntStream.rangeClosed(1, 9).mapToObj(Integer::toString);
      } else {
         return MathMan.isInteger(argumentInput) ? IntStream.rangeClosed(0, 9).mapToObj(Integer::toString).map(s -> argumentInput + s) : Stream.empty();
      }
   }

   public static Stream<String> suggestBoolean(String argumentInput) {
      if (argumentInput.isEmpty()) {
         return Stream.of("true", "false");
      } else if ("true".startsWith(argumentInput)) {
         return Stream.of("true");
      } else {
         return "false".startsWith(argumentInput) ? Stream.of("false") : Stream.empty();
      }
   }

   private static boolean isDouble(String input) {
      boolean point = false;

      for (char c : input.toCharArray()) {
         if (!Character.isDigit(c)) {
            if (c != '.' || point) {
               return false;
            }

            point = true;
         }
      }

      return true;
   }
}
