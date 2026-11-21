package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.tokens;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.Mark;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.YAMLException;
import java.util.List;

public final class DirectiveToken<T> extends Token {
   private final String name;
   private final List<T> value;

   public DirectiveToken(String name, List<T> value, Mark startMark, Mark endMark) {
      super(startMark, endMark);
      this.name = name;
      if (value != null && value.size() != 2) {
         throw new YAMLException("Two strings must be provided instead of " + value.size());
      } else {
         this.value = value;
      }
   }

   public String getName() {
      return this.name;
   }

   public List<T> getValue() {
      return this.value;
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.Directive;
   }
}
