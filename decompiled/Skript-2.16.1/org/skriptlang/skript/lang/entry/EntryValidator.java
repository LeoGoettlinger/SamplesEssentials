package org.skriptlang.skript.lang.entry;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

public class EntryValidator {
   private static final Function<String, String> DEFAULT_UNEXPECTED_ENTRY_MESSAGE = key -> "Unexpected entry '"
      + key
      + "'. Check whether it's spelled correctly or remove it";
   private static final Function<String, String> DEFAULT_MISSING_REQUIRED_ENTRY_MESSAGE = key -> "Required entry '" + key + "' is missing";
   private final List<EntryData<?>> entryData;
   @Nullable
   private final Predicate<Node> unexpectedNodeTester;
   private final Function<String, String> unexpectedEntryMessage;
   private final Function<String, String> missingRequiredEntryMessage;

   public static EntryValidator.EntryValidatorBuilder builder() {
      return new EntryValidator.EntryValidatorBuilder();
   }

   protected EntryValidator(
      List<EntryData<?>> entryData,
      @Nullable Predicate<Node> unexpectedNodeTester,
      @Nullable Function<String, String> unexpectedEntryMessage,
      @Nullable Function<String, String> missingRequiredEntryMessage
   ) {
      this.entryData = entryData;
      this.unexpectedNodeTester = unexpectedNodeTester;
      this.unexpectedEntryMessage = unexpectedEntryMessage != null ? unexpectedEntryMessage : DEFAULT_UNEXPECTED_ENTRY_MESSAGE;
      this.missingRequiredEntryMessage = missingRequiredEntryMessage != null ? missingRequiredEntryMessage : DEFAULT_MISSING_REQUIRED_ENTRY_MESSAGE;
   }

   public List<EntryData<?>> getEntryData() {
      return Collections.unmodifiableList(this.entryData);
   }

   @Nullable
   public Predicate<Node> getUnexpectedNodeTester() {
      return this.unexpectedNodeTester;
   }

   public Function<String, String> getUnexpectedEntryMessage() {
      return this.unexpectedEntryMessage;
   }

   public Function<String, String> getMissingRequiredEntryMessage() {
      return this.missingRequiredEntryMessage;
   }

   @Nullable
   public EntryContainer validate(SectionNode sectionNode) {
      List<EntryData<?>> entries = new ArrayList<>(this.entryData);
      Map<String, Collection<Node>> handledNodes = new HashMap<>();
      List<Node> unhandledNodes = new ArrayList<>();
      boolean ok = true;

      label59:
      for (Node node : sectionNode) {
         if (node.getKey() != null) {
            Iterator<EntryData<?>> iterator = entries.iterator();

            while (iterator.hasNext()) {
               EntryData<?> data = iterator.next();
               if (data.canCreateWith(node)) {
                  Collection<Node> nodes = handledNodes.computeIfAbsent(data.getKey(), k -> new LinkedList<>());
                  nodes.add(node);
                  if (!data.supportsMultiple()) {
                     iterator.remove();
                  }
                  continue label59;
               }
            }

            if (this.unexpectedNodeTester != null && !this.unexpectedNodeTester.test(node)) {
               unhandledNodes.add(node);
            } else {
               ok = false;
               Skript.error(this.unexpectedEntryMessage.apply(ScriptLoader.replaceOptions(node.getKey())));
            }
         }
      }

      for (EntryData<?> entryData : entries) {
         if ((!entryData.supportsMultiple() || !handledNodes.containsKey(entryData.getKey())) && !entryData.isOptional()) {
            Skript.error(this.missingRequiredEntryMessage.apply(entryData.getKey()));
            ok = false;
         }
      }

      return !ok ? null : new EntryContainer(sectionNode, this, handledNodes, unhandledNodes);
   }

   public static class EntryValidatorBuilder {
      public static final String DEFAULT_ENTRY_SEPARATOR = ": ";
      private final List<EntryData<?>> entryData = new ArrayList<>();
      private String entrySeparator = ": ";
      @Nullable
      private Predicate<Node> unexpectedNodeTester;
      @Nullable
      private Function<String, String> unexpectedEntryMessage;
      @Nullable
      private Function<String, String> missingRequiredEntryMessage;

      protected EntryValidatorBuilder() {
      }

      @Nullable
      protected Function<String, String> getMissingRequiredEntryMessage() {
         return this.missingRequiredEntryMessage;
      }

      @Nullable
      protected Function<String, String> getUnexpectedEntryMessage() {
         return this.unexpectedEntryMessage;
      }

      @Nullable
      protected Predicate<Node> getUnexpectedNodeTester() {
         return this.unexpectedNodeTester;
      }

      protected List<EntryData<?>> getEntryData() {
         return this.entryData;
      }

      protected String getEntrySeparator() {
         return this.entrySeparator;
      }

      public EntryValidator.EntryValidatorBuilder entrySeparator(String separator) {
         this.entrySeparator = separator;
         return this;
      }

      public EntryValidator.EntryValidatorBuilder unexpectedNodeTester(Predicate<Node> unexpectedNodeTester) {
         this.unexpectedNodeTester = unexpectedNodeTester;
         return this;
      }

      public EntryValidator.EntryValidatorBuilder unexpectedEntryMessage(Function<String, String> unexpectedEntryMessage) {
         this.unexpectedEntryMessage = unexpectedEntryMessage;
         return this;
      }

      public EntryValidator.EntryValidatorBuilder missingRequiredEntryMessage(Function<String, String> message) {
         this.missingRequiredEntryMessage = message;
         return this;
      }

      public EntryValidator.EntryValidatorBuilder addEntry(String key, @Nullable String defaultValue, boolean optional) {
         return this.addEntry(key, defaultValue, optional, false);
      }

      public EntryValidator.EntryValidatorBuilder addEntry(String key, @Nullable String defaultValue, boolean optional, boolean multiple) {
         this.entryData.add(new KeyValueEntryData<String>(key, defaultValue, optional, multiple) {
            protected String getValue(String value) {
               return value;
            }

            @Override
            public String getSeparator() {
               return EntryValidatorBuilder.this.entrySeparator;
            }
         });
         return this;
      }

      public EntryValidator.EntryValidatorBuilder addSection(String key, boolean optional) {
         return this.addSection(key, optional, false);
      }

      public EntryValidator.EntryValidatorBuilder addSection(String key, boolean optional, boolean multiple) {
         this.entryData.add(new SectionEntryData(key, null, optional, multiple));
         return this;
      }

      public EntryValidator.EntryValidatorBuilder addEntryData(EntryData<?> entryData) {
         this.entryData.add(entryData);
         return this;
      }

      public EntryValidator build() {
         return new EntryValidator(this.entryData, this.unexpectedNodeTester, this.unexpectedEntryMessage, this.missingRequiredEntryMessage);
      }
   }
}
