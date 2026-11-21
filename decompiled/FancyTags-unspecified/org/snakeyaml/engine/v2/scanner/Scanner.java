package org.snakeyaml.engine.v2.scanner;

import java.util.Iterator;
import org.snakeyaml.engine.v2.tokens.Token;

public interface Scanner extends Iterator<Token> {
   boolean checkToken(Token.ID... var1);

   Token peekToken();

   Token next();

   void resetDocumentIndex();
}
