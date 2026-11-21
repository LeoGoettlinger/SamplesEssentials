package ch.njol.skript.aliases;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class AliasesParser {
   private static final Message m_empty_name = new Message("aliases.empty name");
   private static final ArgsMessage m_invalid_variation_section = new ArgsMessage("aliases.invalid variation section");
   private static final Message m_unexpected_section = new Message("aliases.unexpected section");
   private static final Message m_useless_variation = new Message("aliases.useless variation");
   private static final ArgsMessage m_not_enough_brackets = new ArgsMessage("aliases.not enough brackets");
   private static final ArgsMessage m_too_many_brackets = new ArgsMessage("aliases.too many brackets");
   private static final ArgsMessage m_unknown_variation = new ArgsMessage("aliases.unknown variation");
   private static final ArgsMessage m_invalid_minecraft_id = new ArgsMessage("aliases.invalid minecraft id");
   private static final Message m_empty_alias = new Message("aliases.empty alias");
   protected final AliasesProvider provider;
   private final Map<String, Function<String, Boolean>> conditions;

   public AliasesParser(AliasesProvider provider) {
      this.provider = provider;
      this.conditions = new HashMap<>();
   }

   public void load(SectionNode root) {
      Skript.debug(
         "Loading aliases node " + root.getKey() + " from " + root.getConfig().getFileName() + " (" + this.provider.getAliasCount() + " aliases loaded)"
      );

      for (Node node : root) {
         String key = node.getKey();
         if (key == null) {
            Skript.error(m_empty_name.toString());
         } else if (node instanceof SectionNode) {
            AliasesProvider.VariationGroup vars = this.loadVariations((SectionNode)node);
            if (vars != null) {
               String groupName = node.getKey();

               assert groupName != null;

               this.provider.addVariationGroup(groupName, vars);
            } else {
               Skript.error(m_invalid_variation_section.toString(key));
            }
         } else if (!(node instanceof EntryNode)) {
            Skript.error(m_unexpected_section.toString());
         } else if (this.conditions.containsKey(key)) {
            boolean success = this.conditions.get(key).apply(((EntryNode)node).getValue());
            if (!success) {
               Skript.debug("Condition " + key + " was NOT met; not loading more");
               return;
            }
         } else {
            String value = ((EntryNode)node).getValue();
            this.loadAlias(key, value);
         }
      }
   }

   protected Map<String, String> parseBlockStates(String input) {
      Map<String, String> parsed = new HashMap<>();
      int pos = 0;

      while (pos != -1) {
         int comma = input.indexOf(44, pos);
         String pair;
         if (comma == -1) {
            pair = input.substring(pos);
            pos = -1;
         } else {
            pair = input.substring(pos, comma);
            pos = comma + 1;
         }

         String[] parts = pair.split("=");
         parsed.put(parts[0], parts[1]);
      }

      return parsed;
   }

   @Nullable
   protected AliasesProvider.VariationGroup loadVariations(SectionNode root) {
      String name = root.getKey();

      assert name != null;

      if (name.startsWith("{") && name.endsWith("}")) {
         AliasesProvider.VariationGroup vars = new AliasesProvider.VariationGroup();

         for (Node node : root) {
            String pattern = node.getKey();

            assert pattern != null;

            List<String> keys = this.parseKeyPattern(pattern);
            AliasesProvider.Variation var = this.parseVariation(((EntryNode)node).getValue());
            boolean useful = false;

            for (String key : keys) {
               assert key != null;

               if (key.equals("{default}")) {
                  key = "";
                  useful = true;
               }

               vars.put(key, var);
            }

            if (!useful && var.getId() == null && var.getTags().isEmpty() && var.getBlockStates().isEmpty()) {
               Skript.warning(m_useless_variation.toString());
            }
         }

         return vars;
      } else {
         return null;
      }
   }

   protected AliasesProvider.Variation parseVariation(String item) {
      String trimmed = item.trim();

      assert trimmed != null;

      int firstBracket = trimmed.indexOf(123);
      String id;
      Map<String, Object> tags;
      if (firstBracket == -1) {
         id = trimmed;
         tags = new HashMap<>();
      } else {
         if (firstBracket == 0) {
            throw new AssertionError("missing space between id and tags in " + trimmed);
         }

         id = trimmed.substring(0, firstBracket - 1);
         int jsonEndIndex = trimmed.indexOf("} [");
         String json;
         if (jsonEndIndex == -1) {
            json = trimmed.substring(firstBracket);
         } else {
            json = trimmed.substring(firstBracket, jsonEndIndex + 1);
            id = id + trimmed.substring(jsonEndIndex + 2);
         }

         json = "[" + json.substring(1, json.length() - 1) + "]";
         tags = Collections.singletonMap("components", json);
      }

      int stateIndex = id.indexOf(91);
      String typeName;
      Map<String, String> blockStates;
      if (stateIndex != -1) {
         if (stateIndex == 0) {
            throw new AssertionError("missing id or - in " + id);
         }

         typeName = id.substring(0, stateIndex);
         String statesInput = id.substring(stateIndex + 1, id.length() - 1);

         assert statesInput != null;

         blockStates = this.parseBlockStates(statesInput);
      } else {
         typeName = id;
         blockStates = new HashMap<>();
      }

      if (typeName.equals("-")) {
         typeName = null;
      }

      return new AliasesProvider.Variation(typeName, typeName == null ? -1 : typeName.indexOf(45), tags, blockStates);
   }

   protected List<String> parseKeyPattern(String name) {
      List<String> versions = new ArrayList<>();
      boolean simple = true;
      AliasesParser.IntStack optionals = new AliasesParser.IntStack(4);
      AliasesParser.IntStack choices = new AliasesParser.IntStack(4);
      int i = 0;

      while (i < name.length()) {
         int c = name.codePointAt(i);
         if (c == 91) {
            optionals.push(i);
            simple = false;
         } else if (c == 40) {
            choices.push(i);
            simple = false;
         } else if (c == 93) {
            int start;
            try {
               start = optionals.pop();
            } catch (ArrayIndexOutOfBoundsException var13) {
               Skript.error(m_too_many_brackets.toString(i, ']'));
               return versions;
            }

            versions.addAll(this.parseKeyPattern(name.substring(0, start) + name.substring(start + 1, i) + name.substring(i + 1)));
            versions.addAll(this.parseKeyPattern(name.substring(0, start) + name.substring(i + 1)));
         } else if (c == 41) {
            int start;
            try {
               start = choices.pop();
            } catch (ArrayIndexOutOfBoundsException var12) {
               Skript.error(m_too_many_brackets.toString(i, ')'));
               return versions;
            }

            int optionStart = start;
            int nested = 0;

            for (int j = start + 1; j < i; j += Character.charCount(c)) {
               c = name.codePointAt(j);
               if (c == 40 || c == 91) {
                  nested++;
               } else if (c == 41 || c == 93) {
                  nested--;
               } else if (c == 124 && nested == 0) {
                  versions.addAll(this.parseKeyPattern(name.substring(0, start) + name.substring(optionStart + 1, j) + name.substring(i + 1)));
                  optionStart = j;
               }
            }

            assert nested == 0;

            versions.addAll(this.parseKeyPattern(name.substring(0, start) + name.substring(optionStart + 1, i) + name.substring(i + 1)));
         }

         i += Character.charCount(c);
      }

      if (optionals.isEmpty() && choices.isEmpty()) {
         if (simple) {
            versions.add(name);
         }

         return versions;
      } else {
         if (!optionals.isEmpty()) {
            i = optionals.pop();
         } else {
            i = choices.pop();
         }

         char errorChar = (char)name.codePointAt(i);
         Skript.error(m_not_enough_brackets.toString(i, errorChar));
         optionals.clear();
         choices.clear();
         return versions;
      }
   }

   protected Map<String, AliasesProvider.Variation> parseKeyVariations(String name) {
      int varStart = -1;
      int varEnd = 0;
      List<AliasesParser.PatternSlot> slots = new ArrayList<>();
      int i = 0;

      while (i < name.length()) {
         int c = name.codePointAt(i);
         if (c == 123) {
            varStart = i;
            String part = name.substring(varEnd, i);

            assert part != null;

            slots.add(new AliasesParser.PatternSlot(part));
         } else if (c == 125) {
            if (varStart == -1) {
               Skript.error(m_not_enough_brackets.toString());
            } else {
               String varName = name.substring(varStart, i + 1);

               assert varName != null;

               AliasesProvider.VariationGroup vars = this.provider.getVariationGroup(varName);
               if (vars == null) {
                  Skript.error(m_unknown_variation.toString(varName));
                  varStart = -1;
                  varEnd = i + 1;
               } else {
                  slots.add(new AliasesParser.VariationSlot(vars));
                  varStart = -1;
                  varEnd = i + 1;
               }
            }
         }

         i += Character.charCount(name.codePointAt(i));
      }

      String part = name.substring(varEnd);

      assert part != null;

      slots.add(new AliasesParser.PatternSlot(part));
      if (varStart != -1) {
         Skript.error(m_not_enough_brackets.toString());
      }

      Map<String, AliasesProvider.Variation> variations = new LinkedHashMap<>();
      if (slots.size() == 1) {
         AliasesParser.PatternSlot slot = slots.get(0);
         if (!(slot instanceof AliasesParser.VariationSlot)) {
            variations.put(this.fixName(name), new AliasesProvider.Variation(null, -1, Collections.emptyMap(), Collections.emptyMap()));
            return variations;
         }
      }

      int count;
      int incremented;
      do {
         count = slots.size();
         incremented = 0;
         StringBuilder pattern = new StringBuilder();
         String id = null;
         int insertPoint = -1;
         Map<String, Object> tags = new HashMap<>();
         Map<String, String> states = new HashMap<>();

         for (int ix = 0; ix < count; ix++) {
            AliasesParser.PatternSlot slot = slots.get(ix);
            if (slot instanceof AliasesParser.VariationSlot varSlot) {
               pattern.append(varSlot.getName());
               AliasesProvider.Variation var = varSlot.getVariation();
               String varId = var.getId();
               if (varId != null) {
                  id = varId;
               }

               if (var.getInsertPoint() != -1) {
                  insertPoint = var.getInsertPoint();
               }

               tags.putAll(var.getTags());
               states.putAll(var.getBlockStates());
               if (ix == incremented && varSlot.increment()) {
                  incremented++;
               }
            } else {
               if (ix == incremented) {
                  incremented++;
               }

               pattern.append(slot.content);
            }
         }

         variations.put(this.fixName(pattern.toString()), new AliasesProvider.Variation(id, insertPoint, tags, states));
      } while (incremented != count);

      return variations;
   }

   protected void loadAlias(String name, String data) {
      List<String> patterns = this.parseKeyPattern(name);
      Map<String, AliasesProvider.Variation> variations = new LinkedHashMap<>();

      for (String pattern : patterns) {
         assert pattern != null;

         variations.putAll(this.parseKeyVariations(pattern));
      }

      int start = 0;

      for (int indexStart = 0; start - 1 != data.length(); indexStart = start) {
         int comma = StringUtils.indexOfOutsideGroup(data, ',', '{', '}', indexStart);
         if (comma == -1) {
            if (indexStart == 0) {
               String item = data.trim();

               assert item != null;

               this.loadSingleAlias(variations, item);
               break;
            }

            comma = data.length();
         }

         String item = data.substring(start, comma).trim();

         assert item != null;

         this.loadSingleAlias(variations, item);
         start = comma + 1;
      }
   }

   protected NonNullPair<String, String> getAliasPlural(String name) {
      int marker = name.indexOf(166);
      if (marker == -1) {
         String trimmed = name.trim();

         assert trimmed != null;

         return new NonNullPair<>(trimmed, trimmed);
      } else {
         int pluralEnd = -1;

         for (int i = marker; i < name.length(); i++) {
            int c = name.codePointAt(i);
            if (Character.isWhitespace(c)) {
               pluralEnd = i;
               break;
            }

            i += Character.charCount(c);
         }

         if (pluralEnd == -1) {
            String singular = name.substring(0, marker);
            String plural = singular + name.substring(marker + 1);
            singular = singular.trim();
            plural = plural.trim();

            assert singular != null;

            assert plural != null;

            return new NonNullPair<>(singular, plural);
         } else {
            String base = name.substring(0, marker);
            String singular = base + name.substring(pluralEnd);
            String plural = base + name.substring(marker + 1);
            singular = singular.trim();
            plural = plural.trim();

            assert singular != null;

            assert plural != null;

            return new NonNullPair<>(singular, plural);
         }
      }
   }

   protected void loadSingleAlias(Map<String, AliasesProvider.Variation> variations, String item) {
      AliasesProvider.Variation base = this.parseVariation(item);

      for (Entry<String, AliasesProvider.Variation> entry : variations.entrySet()) {
         String name = entry.getKey();

         assert name != null;

         AliasesProvider.Variation var = entry.getValue();

         assert var != null;

         AliasesProvider.Variation merged = base.merge(var);
         String id = merged.getId();
         if (id == null) {
            Skript.warning(m_empty_alias.toString());
         } else {
            id = id.toLowerCase(Locale.ENGLISH).intern();

            assert id != null;

            try {
               NonNullPair<String, Integer> plain = Noun.stripGender(name, name);
               NonNullPair<String, String> forms = this.getAliasPlural(plain.getFirst());
               this.provider
                  .addAlias(
                     new AliasesProvider.AliasName(forms.getFirst(), forms.getSecond(), plain.getSecond()), id, merged.getTags(), merged.getBlockStates()
                  );
            } catch (InvalidMinecraftIdException var12) {
               Skript.error(m_invalid_minecraft_id.toString(var12.getId()));
            }
         }
      }
   }

   protected String fixName(String name) {
      if (name.isEmpty()) {
         return "";
      } else {
         StringBuilder sb = new StringBuilder(name.length());
         int firstNonWhitespace = -1;
         int lastNonWhitespace = -1;
         int lastWhitespace = -1;
         int stripped = 0;

         for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c > ' ') {
               int adjustedIndex = i - stripped;
               if (firstNonWhitespace == -1) {
                  firstNonWhitespace = adjustedIndex;
               }

               lastNonWhitespace = adjustedIndex;
            } else {
               int oldLastWhitespace = lastWhitespace;
               lastWhitespace = i;
               if (oldLastWhitespace != -1 && oldLastWhitespace == i - 1 || i < name.length() - 1 && name.charAt(i + 1) == 166) {
                  stripped++;
                  continue;
               }
            }

            sb.append(c);
         }

         return sb.substring(firstNonWhitespace, lastNonWhitespace + 1);
      }
   }

   public void registerCondition(String name, Function<String, Boolean> condition) {
      this.conditions.put(name, condition);
   }

   private static class IntStack {
      private int[] ints;
      private int pos;

      public IntStack(int capacity) {
         this.ints = new int[capacity];
         this.pos = 0;
      }

      public void push(int value) {
         if (this.pos == this.ints.length - 1) {
            this.enlargeArray();
         }

         this.ints[this.pos++] = value;
      }

      public int pop() {
         return this.ints[--this.pos];
      }

      public boolean isEmpty() {
         return this.pos == 0;
      }

      private void enlargeArray() {
         int[] newArray = new int[this.ints.length * 2];
         System.arraycopy(this.ints, 0, newArray, 0, this.ints.length);
         this.ints = newArray;
      }

      public void clear() {
         this.pos = 0;
      }
   }

   protected static class PatternSlot {
      public final String content;

      public PatternSlot(String content) {
         this.content = content;
      }
   }

   protected static class VariationSlot extends AliasesParser.PatternSlot {
      public final AliasesProvider.VariationGroup vars;
      private int counter;

      public VariationSlot(AliasesProvider.VariationGroup vars) {
         super("");
         this.vars = vars;
      }

      public String getName() {
         return this.vars.keys.get(this.counter);
      }

      public AliasesProvider.Variation getVariation() {
         return this.vars.values.get(this.counter);
      }

      public boolean increment() {
         this.counter++;
         if (this.counter == this.vars.keys.size()) {
            this.counter = 0;
            return true;
         } else {
            return false;
         }
      }
   }
}
