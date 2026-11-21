package com.artillexstudios.axafkzone.libs.axapi.libs.yamlassist.types;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.YAMLException;
import com.artillexstudios.axafkzone.libs.axapi.libs.yamlassist.SyntaxError;
import java.util.ArrayList;
import java.util.List;

public class InvalidLine extends SyntaxError {
   @Override
   public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
      List<String> suggestions = new ArrayList<>();

      for (int lineNumber = 1; lineNumber <= fileLines.size(); lineNumber++) {
         String line = this.removeIndent(fileLines.get(lineNumber - 1));
         String previousLine = lineNumber == 1 ? "" : this.removeIndent(fileLines.get(lineNumber - 2));
         if (previousLine.startsWith("-") && !line.startsWith("-") && (line.startsWith("'") || line.startsWith("\""))) {
            String value = "- ";

            for (int i = 0; i < this.getIndentCount(line); i++) {
               value = " " + value;
            }

            suggestions.add("Add \"" + value + "\" at the beginning of line " + lineNumber);
         }
      }

      return suggestions;
   }

   public boolean isLineValid(String line) {
      return !line.startsWith("'") && !line.startsWith("\"");
   }
}
