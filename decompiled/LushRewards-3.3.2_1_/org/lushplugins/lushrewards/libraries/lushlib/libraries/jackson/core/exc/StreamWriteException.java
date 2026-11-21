package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.exc;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonLocation;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonProcessingException;

public abstract class StreamWriteException extends JsonProcessingException {
   private static final long serialVersionUID = 2L;
   protected transient JsonGenerator _processor;

   protected StreamWriteException(Throwable rootCause, JsonGenerator g) {
      super(rootCause);
      this._processor = g;
   }

   protected StreamWriteException(String msg, JsonGenerator g) {
      super(msg, (JsonLocation)null);
      this._processor = g;
   }

   protected StreamWriteException(String msg, Throwable rootCause, JsonGenerator g) {
      super(msg, null, rootCause);
      this._processor = g;
   }

   public abstract StreamWriteException withGenerator(JsonGenerator var1);

   public JsonGenerator getProcessor() {
      return this._processor;
   }
}
