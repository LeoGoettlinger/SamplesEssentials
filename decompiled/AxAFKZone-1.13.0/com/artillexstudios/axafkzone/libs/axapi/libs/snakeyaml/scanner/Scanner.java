package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.scanner;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.tokens.Token;

public interface Scanner {
   boolean checkToken(Token.ID... var1);

   Token peekToken();

   Token getToken();

   void resetDocumentIndex();
}
