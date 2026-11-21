package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.scanner;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.tokens.Token;

public interface Scanner {
   boolean checkToken(Token.ID... var1);

   default boolean checkToken(Token.ID choice) {
      return this.checkToken(choice);
   }

   Token peekToken();

   Token getToken();

   void resetDocumentIndex();
}
