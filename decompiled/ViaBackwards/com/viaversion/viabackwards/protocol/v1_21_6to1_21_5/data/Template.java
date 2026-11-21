package com.viaversion.viabackwards.protocol.v1_21_6to1_21_5.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.Map;

public record Template(List<String> segments, List<String> variables) {
   private static final int MAX_LENGTH = 2000000;

   public static Template fromString(String string) {
      Builder<String> segmentsBuilder = ImmutableList.builder();
      Builder<String> variablesBuilder = ImmutableList.builder();
      int currentIndex = 0;
      int dollarIndex = string.indexOf(36);

      while (dollarIndex != -1) {
         if (dollarIndex != string.length() - 1 && string.charAt(dollarIndex + 1) == '(') {
            segmentsBuilder.add(string.substring(currentIndex, dollarIndex));
            int closingParenIndex = string.indexOf(41, dollarIndex + 1);
            if (closingParenIndex == -1) {
               throw new IllegalArgumentException("Unterminated macro variable");
            }

            String variableName = string.substring(dollarIndex + 2, closingParenIndex);
            if (!isValidVariableName(variableName)) {
               throw new IllegalArgumentException("Invalid macro variable name '" + variableName + "'");
            }

            variablesBuilder.add(variableName);
            currentIndex = closingParenIndex + 1;
            dollarIndex = string.indexOf(36, currentIndex);
         } else {
            dollarIndex = string.indexOf(36, dollarIndex + 1);
         }
      }

      if (currentIndex == 0) {
         throw new IllegalArgumentException("No variables in macro");
      } else {
         if (currentIndex != string.length()) {
            segmentsBuilder.add(string.substring(currentIndex));
         }

         return new Template(segmentsBuilder.build(), variablesBuilder.build());
      }
   }

   private static boolean isValidVariableName(String string) {
      for (int i = 0; i < string.length(); i++) {
         char c = string.charAt(i);
         if (!Character.isLetterOrDigit(c) && c != '_') {
            return false;
         }
      }

      return true;
   }

   public String instantiate(Map<String, String> map) {
      return this.substitute(this.variables().stream().map(string -> map.getOrDefault(string, "")).toList());
   }

   private String substitute(List<String> list) {
      StringBuilder out = new StringBuilder();

      for (int i = 0; i < this.variables.size(); i++) {
         out.append(this.segments.get(i)).append(list.get(i));
         if (out.length() > 2000000) {
            throw new IllegalArgumentException("Output too long (> 2000000)");
         }
      }

      if (this.segments.size() > this.variables.size()) {
         out.append(this.segments.get(this.segments.size() - 1));
         if (out.length() > 2000000) {
            throw new IllegalArgumentException("Output too long (> 2000000)");
         }
      }

      return out.toString();
   }
}
