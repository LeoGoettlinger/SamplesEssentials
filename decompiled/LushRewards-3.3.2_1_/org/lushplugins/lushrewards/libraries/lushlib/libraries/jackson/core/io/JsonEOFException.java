package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParseException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;

public class JsonEOFException extends JsonParseException {
   private static final long serialVersionUID = 1L;
   protected final JsonToken _token;

   public JsonEOFException(JsonParser p, JsonToken token, String msg) {
      super(p, msg);
      this._token = token;
   }

   public JsonToken getTokenBeingDecoded() {
      return this._token;
   }
}
