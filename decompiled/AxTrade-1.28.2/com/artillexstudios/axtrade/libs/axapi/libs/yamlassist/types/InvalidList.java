package com.artillexstudios.axtrade.libs.axapi.libs.yamlassist.types;

import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.error.YAMLException;
import com.artillexstudios.axtrade.libs.axapi.libs.yamlassist.SyntaxError;
import java.util.ArrayList;
import java.util.List;

public class InvalidList extends SyntaxError {
   @Override
   public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
      List<String> suggestions = new ArrayList<>();
      if (exception.getMessage().contains("expected <block end>, but found '-'")) {
         int line1 = Integer.parseInt(exception.getMessage().split(", line ")[1].split(",")[0]);
         int line2 = Integer.parseInt(exception.getMessage().split(", line ")[2].split(",")[0]);
         if (fileLines.get(line2 - 2).endsWith(":")) {
            suggestions.add("List starting at line " + line2 + " seems to be starting at line " + line1 + " already. Make sure indenting is correct.");
         } else {
            suggestions.add("List starting at line " + line2 + " is missing a name.");
         }
      }

      return suggestions;
   }
}
