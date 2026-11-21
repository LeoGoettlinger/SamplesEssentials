package com.earth2me.essentials.libs.snakeyaml.scanner;

import com.earth2me.essentials.libs.snakeyaml.tokens.Token;

public interface Scanner {
   boolean checkToken(Token.ID... var1);

   Token peekToken();

   Token getToken();

   void resetDocumentIndex();
}
