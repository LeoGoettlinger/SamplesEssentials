package me.byteful.plugin.leveltools.libs.crunch.functional;

import me.byteful.plugin.leveltools.libs.crunch.token.Token;
import me.byteful.plugin.leveltools.libs.crunch.token.TokenType;
import me.byteful.plugin.leveltools.libs.crunch.token.Value;

public class ArgumentList implements Token {
   private final Value[] arguments;

   public ArgumentList(Value[] arguments) {
      this.arguments = arguments;
   }

   public Value[] getArguments() {
      return this.arguments;
   }

   @Override
   public TokenType getType() {
      return TokenType.ARGUMENT_LIST;
   }
}
