package net.thenextlvl.protect.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.thenextlvl.protect.commands.exceptions.ComponentCommandExceptionType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EnumArgumentType<E extends Enum<E>> implements CustomArgumentType<E, String> {
   private final Class<E> enumClass;

   public EnumArgumentType(Class<E> enumClass) {
      this.enumClass = enumClass;
   }

   public E parse(StringReader reader) throws CommandSyntaxException {
      String type = (String)this.getNativeType().parse(reader);

      try {
         return Enum.valueOf(this.enumClass, type.toUpperCase(Locale.ROOT).replace('-', '_'));
      } catch (IllegalArgumentException var4) {
         throw new ComponentCommandExceptionType(Component.text("No such enum constant: " + type)).createWithContext(reader);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      Arrays.stream(this.enumClass.getEnumConstants())
         .map(e -> e.name().toLowerCase(Locale.ROOT).replace('_', '-'))
         .<String>map(StringArgumentType::escapeIfRequired)
         .filter(s -> s.contains(builder.getRemaining()))
         .forEach(builder::suggest);
      return builder.buildFuture();
   }

   public ArgumentType<String> getNativeType() {
      return StringArgumentType.string();
   }
}
