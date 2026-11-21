package su.nightexpress.nightcore.commands.argument.type;

import java.util.List;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.commands.SuggestionsProvider;
import su.nightexpress.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.nightcore.commands.argument.ArgumentType;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.bridge.RegistryType;

public class EnchantmentArgumentType implements ArgumentType<Enchantment>, SuggestionsProvider {
   private static final List<String> EXAMPLES = BukkitThing.getAsStrings(RegistryType.ENCHANTMENT);

   @NonNull
   public Enchantment parse(@NonNull CommandContextBuilder contextBuilder, @NonNull String string) throws CommandSyntaxException {
      Enchantment enchantment = BukkitThing.getEnchantment(string);
      if (enchantment == null) {
         throw CommandSyntaxException.custom(CoreLang.COMMAND_SYNTAX_INVALID_ENCHANTMENT);
      } else {
         return enchantment;
      }
   }

   @NonNull
   @Override
   public List<String> suggest(@NonNull ArgumentReader reader, @NonNull CommandContext context) {
      return EXAMPLES;
   }
}
