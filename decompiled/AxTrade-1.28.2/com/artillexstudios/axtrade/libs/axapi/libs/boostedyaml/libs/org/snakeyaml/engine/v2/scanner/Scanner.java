package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.scanner;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.Token;
import java.util.Iterator;

public interface Scanner extends Iterator<Token> {
   boolean checkToken(Token.ID... var1);

   Token peekToken();

   Token next();

   void resetDocumentIndex();
}
