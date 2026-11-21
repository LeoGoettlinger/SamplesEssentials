package revxrsal.commands.process;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.command.CommandParameter;

@FunctionalInterface
public interface ParameterNamingStrategy {
   @NotNull
   String getName(@NotNull CommandParameter var1);

   static ParameterNamingStrategy identity() {
      return parameter -> parameter.getJavaParameter().getName();
   }

   static ParameterNamingStrategy lowerCaseWithSeparator(String separator) {
      return parameter -> BuiltInNamingStrategies.separateCamelCase(parameter.getName(), separator).toLowerCase(Locale.ENGLISH);
   }

   static ParameterNamingStrategy lowerCaseWithSpace() {
      return lowerCaseWithSeparator(" ");
   }

   static ParameterNamingStrategy upperCamelCase() {
      return parameter -> BuiltInNamingStrategies.upperCaseFirstLetter(parameter.getName());
   }

   static ParameterNamingStrategy upperCamelCaseWithSpace() {
      return upperCamelCaseWithSeparator(" ");
   }

   static ParameterNamingStrategy upperCamelCaseWithSeparator(String separator) {
      return parameter -> BuiltInNamingStrategies.upperCaseFirstLetter(BuiltInNamingStrategies.separateCamelCase(parameter.getName(), separator));
   }
}
