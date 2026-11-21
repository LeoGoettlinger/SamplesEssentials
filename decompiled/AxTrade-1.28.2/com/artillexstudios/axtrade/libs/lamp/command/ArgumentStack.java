package com.artillexstudios.axtrade.libs.lamp.command;

import com.artillexstudios.axtrade.libs.lamp.core.LinkedArgumentStack;
import com.artillexstudios.axtrade.libs.lamp.util.QuotedStringTokenizer;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

public interface ArgumentStack extends Deque<String>, List<String>, Cloneable {
   @NotNull
   String join(String var1);

   @NotNull
   String join(@NotNull String var1, int var2);

   String popForParameter(@NotNull CommandParameter var1);

   @NotNull
   @UnmodifiableView
   List<String> asImmutableView();

   @NotNull
   @Unmodifiable
   List<String> asImmutableCopy();

   @NotNull
   ArgumentStack copy();

   @NotNull
   static ArgumentStack parse(@NotNull String... arguments) {
      return (ArgumentStack)(arguments.length == 0 ? empty() : new LinkedArgumentStack(QuotedStringTokenizer.parse(String.join(" ", arguments))));
   }

   @NotNull
   static ArgumentStack parse(@NotNull Collection<String> arguments) {
      return (ArgumentStack)(arguments.size() == 0 ? empty() : new LinkedArgumentStack(QuotedStringTokenizer.parse(String.join(" ", arguments))));
   }

   @NotNull
   static ArgumentStack parseForAutoCompletion(@NotNull String... arguments) {
      return new LinkedArgumentStack(QuotedStringTokenizer.parseForAutoCompletion(String.join(" ", arguments)));
   }

   @NotNull
   static ArgumentStack parseForAutoCompletion(@NotNull Collection<String> arguments) {
      return new LinkedArgumentStack(QuotedStringTokenizer.parseForAutoCompletion(String.join(" ", arguments)));
   }

   @NotNull
   static ArgumentStack copyExact(@NotNull String... arguments) {
      return (ArgumentStack)(arguments.length == 0 ? empty() : new LinkedArgumentStack(arguments));
   }

   @NotNull
   static ArgumentStack copyExact(@NotNull List<String> arguments) {
      return (ArgumentStack)(arguments.size() == 0 ? empty() : new LinkedArgumentStack(arguments));
   }

   @NotNull
   static ArgumentStack empty() {
      return new LinkedArgumentStack();
   }
}
