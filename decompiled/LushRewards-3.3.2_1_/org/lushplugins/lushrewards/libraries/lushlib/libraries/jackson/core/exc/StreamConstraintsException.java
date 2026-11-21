package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.exc;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonLocation;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonProcessingException;

public class StreamConstraintsException extends JsonProcessingException {
   private static final long serialVersionUID = 2L;

   public StreamConstraintsException(String msg) {
      super(msg);
   }

   public StreamConstraintsException(String msg, JsonLocation loc) {
      super(msg, loc);
   }
}
