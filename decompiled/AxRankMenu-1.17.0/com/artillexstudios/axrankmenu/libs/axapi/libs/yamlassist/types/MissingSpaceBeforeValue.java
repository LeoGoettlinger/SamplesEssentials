package com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types;

import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.error.YAMLException;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.SyntaxError;
import java.util.ArrayList;
import java.util.List;

public class MissingSpaceBeforeValue extends SyntaxError {
   @Override
   public List<String> getSuggestions(YAMLException exception, List<String> fileLines) {
      List<String> suggestions = new ArrayList<>();

      for (int lineNumber = 1; lineNumber <= fileLines.size(); lineNumber++) {
         String line = this.removeIndent(fileLines.get(lineNumber - 1));
         if (!line.startsWith("#") && !line.isEmpty()) {
            if (line.startsWith("-") && !line.startsWith("- ")) {
               suggestions.add("Add a space after the \"-\" at line " + lineNumber + ".");
            } else if (!line.startsWith("- ") && line.contains(":") && !line.contains(": ") && !line.endsWith(":")) {
               if (fileLines.size() != lineNumber) {
                  String nextLine = fileLines.get(lineNumber);
                  if (this.getIndentCount(nextLine) - this.getIndentCount(fileLines.get(lineNumber - 1)) == 2) {
                     suggestions.add("Remove the \"" + line.substring(line.indexOf(58) + 1) + "\" from the end of line " + lineNumber);
                  } else {
                     suggestions.add("Add a space after the \":\" at line " + lineNumber + ".");
                  }
               }
            } else if (!line.contains(":") && !line.contains("-")) {
               suggestions.add("Remove line " + lineNumber + " or add ':' at the end");
            }
         }
      }

      return suggestions;
   }
}
