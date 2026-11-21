package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.parser.match;

import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.parser.Token;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.parser.TokenType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class TokenListProducingMatchedTokenConsumer extends MatchedTokenConsumer<List<Token>> {
   private List<Token> result = null;

   public TokenListProducingMatchedTokenConsumer(@NotNull final String input) {
      super(input);
   }

   @Override
   public void accept(final int start, final int end, @NotNull final TokenType tokenType) {
      super.accept(start, end, tokenType);
      if (this.result == null) {
         this.result = new ArrayList<>();
      }

      this.result.add(new Token(start, end, tokenType));
   }

   @NotNull
   public List<Token> result() {
      return this.result == null ? Collections.emptyList() : this.result;
   }
}
