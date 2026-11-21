package com.fastasyncworldedit.core.extension.factory.parser;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
import com.sk89q.worldedit.internal.registry.InputParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public abstract class FaweParser<T> extends InputParser<T> implements AliasedParser {
   protected FaweParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   public static List<Entry<FaweParser.ParseEntry, List<String>>> parse(String toParse) throws InputParseException {
      List<Entry<FaweParser.ParseEntry, List<String>>> keys = new ArrayList<>();
      List<String> inputs = new ArrayList<>();
      List<Boolean> and = new ArrayList<>();
      int last = 0;
      boolean expression = false;

      for (int i = 0; i < toParse.length(); i++) {
         char c = toParse.charAt(i);
         switch (c) {
            case ' ':
            case '&':
            case ',':
               if (!expression) {
                  String result = toParse.substring(last, i);
                  if (result.isEmpty()) {
                     throw new InputParseException(Caption.of("fawe.error.parse.invalid-dangling-character", c));
                  }

                  inputs.add(result);
                  and.add(c == '&' || c == ' ');
                  last = i + 1;
               }
               break;
            case '=':
               expression = true;
               break;
            default:
               if (c == '[') {
                  int next = StringMan.findMatchingBracket(toParse, i);
                  if (next != -1) {
                     i = next;
                  } else {
                     toParse = toParse + "]";
                     i = toParse.length();
                  }

                  expression = false;
               }
         }
      }

      inputs.add(toParse.substring(last));

      for (int i = 0; i < inputs.size(); i++) {
         String full = inputs.get(i);
         String command = full;
         List<String> args = new ArrayList<>();

         while (!command.isEmpty() && command.charAt(command.length() - 1) == ']') {
            int startPos = StringMan.findMatchingBracket(command, command.length() - 1);
            if (startPos == -1) {
               break;
            }

            String arg = command.substring(startPos + 1, command.length() - 1);
            args.add(arg);
            command = full.substring(0, startPos);
         }

         Collections.reverse(args);
         FaweParser.ParseEntry entry = new FaweParser.ParseEntry(full, command, i > 0 ? and.get(i - 1) : false);
         keys.add(new SimpleEntry<>(entry, args));
      }

      return keys;
   }

   protected PlatformCommandManager getPlatform() {
      return PlatformCommandManager.getInstance();
   }

   public static class ParseEntry {
      private final boolean and;
      private final String input;
      private final String full;

      public ParseEntry(String full, String input, boolean type) {
         this.full = full;
         this.input = input;
         this.and = type;
      }

      public boolean isAnd() {
         return this.and;
      }

      public String getInput() {
         return this.input;
      }

      public String getFull() {
         return this.full;
      }

      @Override
      public String toString() {
         return this.input + " | " + this.and;
      }
   }
}
