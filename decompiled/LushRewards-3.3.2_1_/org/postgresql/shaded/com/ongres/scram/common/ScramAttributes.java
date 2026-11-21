package org.postgresql.shaded.com.ongres.scram.common;

import java.util.HashMap;
import java.util.Map;
import org.postgresql.shaded.com.ongres.scram.common.exception.ScramParseException;
import org.postgresql.shaded.com.ongres.scram.common.util.CharAttribute;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public enum ScramAttributes implements CharAttribute {
   USERNAME('n'),
   AUTHZID('a'),
   NONCE('r'),
   CHANNEL_BINDING('c'),
   SALT('s'),
   ITERATION('i'),
   CLIENT_PROOF('p'),
   SERVER_SIGNATURE('v'),
   ERROR('e');

   private final char attributeChar;
   private static final Map<Character, ScramAttributes> REVERSE_MAPPING = new HashMap<>();

   private ScramAttributes(char attributeChar) {
      this.attributeChar = Preconditions.checkNotNull(attributeChar, "attributeChar");
   }

   @Override
   public char getChar() {
      return this.attributeChar;
   }

   public static ScramAttributes byChar(char c) throws ScramParseException {
      if (!REVERSE_MAPPING.containsKey(c)) {
         throw new ScramParseException("Attribute with char '" + c + "' does not exist");
      } else {
         return REVERSE_MAPPING.get(c);
      }
   }

   static {
      for (ScramAttributes scramAttribute : values()) {
         REVERSE_MAPPING.put(scramAttribute.getChar(), scramAttribute);
      }
   }
}
