package com.artillexstudios.axrankmenu.libs.axapi.placeholders;

import java.util.List;

public enum DefaultPlaceholderFormatter implements PlaceholderFormatter {
   INSTANCE;

   @Override
   public List<String> format(String placeholder) {
      if (!placeholder.startsWith("%")) {
         placeholder = "%" + placeholder;
      }

      if (!placeholder.endsWith("%")) {
         placeholder = placeholder + "%";
      }

      return List.of(placeholder);
   }
}
