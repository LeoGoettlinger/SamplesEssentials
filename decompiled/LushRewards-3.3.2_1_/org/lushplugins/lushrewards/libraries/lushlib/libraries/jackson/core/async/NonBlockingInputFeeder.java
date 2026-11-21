package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.async;

public interface NonBlockingInputFeeder {
   boolean needMoreInput();

   void endOfInput();
}
