package com.magmaguy.elitemobs.magmacore.command.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import org.bukkit.command.CommandSender;

public class DynamicListStringCommandArgument extends ListStringCommandArgument {
   private final Supplier<List<String>> validValuesSupplier;

   public DynamicListStringCommandArgument(Supplier<List<String>> validValuesSupplier, String hint) {
      super(new ArrayList<>(), hint);
      this.validValuesSupplier = validValuesSupplier;
   }

   private List<String> values() {
      List<String> values = this.validValuesSupplier.get();
      return values == null ? List.of() : values;
   }

   @Override
   public boolean matchesInput(String input) {
      return this.values().stream().anyMatch(value -> value.equalsIgnoreCase(input));
   }

   @Override
   public List<String> literals() {
      return this.values();
   }

   @Override
   public List<String> getSuggestions(CommandSender sender, String partialInput) {
      List<String> values = this.values();
      if (values.isEmpty()) {
         return partialInput.isEmpty() ? List.of(this.hint) : List.of();
      } else {
         String lower = partialInput.toLowerCase(Locale.ROOT);
         return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
      }
   }
}
