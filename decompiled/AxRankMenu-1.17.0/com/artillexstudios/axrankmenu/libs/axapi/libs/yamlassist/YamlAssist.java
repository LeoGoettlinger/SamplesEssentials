package com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist;

import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.Yaml;
import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.error.YAMLException;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types.BadIndentation;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types.DoubleMapping;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types.InvalidLine;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types.InvalidList;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types.MissingQuote;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types.MissingSpaceBeforeValue;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types.QuoteWrapRequired;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types.TABIndent;
import com.artillexstudios.axrankmenu.libs.axapi.libs.yamlassist.types.UnknownEscape;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlAssist {
   private static final Map<Class<? extends SyntaxError>, SyntaxError> registeredSyntaxErrors = new HashMap<>();

   public static List<String> getSuggestions(File file) {
      List<String> suggestions = new ArrayList<>();

      try {
         if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
         }

         if (!file.exists()) {
            throw new IllegalStateException("File does not exist");
         }

         FileInputStream input = new FileInputStream(file);

         try {
            new Yaml().load(input);
         } catch (YAMLException var6) {
            YAMLException exception = var6;

            for (SyntaxError possibleError : registeredSyntaxErrors.values()) {
               suggestions.addAll(possibleError.getSuggestions(exception, Files.readAllLines(file.toPath())));
            }
         }

         input.close();
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      return suggestions;
   }

   public static void registerSyntaxError(SyntaxError error) {
      registeredSyntaxErrors.put((Class<? extends SyntaxError>)error.getClass(), error);
   }

   public static <T> T getError(Class<T> clazz) {
      return (T)registeredSyntaxErrors.get(clazz);
   }

   static {
      registerSyntaxError(new DoubleMapping());
      registerSyntaxError(new InvalidList());
      registerSyntaxError(new InvalidLine());
      registerSyntaxError(new MissingQuote());
      registerSyntaxError(new MissingSpaceBeforeValue());
      registerSyntaxError(new QuoteWrapRequired());
      registerSyntaxError(new TABIndent());
      registerSyntaxError(new UnknownEscape());
      registerSyntaxError(new BadIndentation());
   }
}
