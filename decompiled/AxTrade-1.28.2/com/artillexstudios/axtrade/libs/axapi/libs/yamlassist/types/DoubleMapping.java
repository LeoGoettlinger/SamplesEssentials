package com.artillexstudios.axtrade.libs.axapi.libs.yamlassist.types;

import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.error.YAMLException;
import com.artillexstudios.axtrade.libs.axapi.libs.yamlassist.SyntaxError;
import java.util.ArrayList;
import java.util.List;

public class DoubleMapping extends SyntaxError {
   @Override
   public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
      List<String> suggestions = new ArrayList<>();
      String[] arr = exception.getMessage().split(", line ");
      if (arr.length == 1) {
         return suggestions;
      } else {
         int line = Integer.parseInt(arr[1].split(",")[0]);
         String text = fileLines.get(line - 1).split("#")[0];
         if (exception.getMessage().contains("mapping values are not allowed here") && text.endsWith(":") && this.getIndentCount(text) % 2 == 0) {
            suggestions.add("Remove the last : from line " + line + ".");
         }

         return suggestions;
      }
   }
}
