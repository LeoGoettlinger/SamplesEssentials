package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.util;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.StreamReadConstraints;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.exc.StreamConstraintsException;

public final class ReadConstrainedTextBuffer extends TextBuffer {
   private final StreamReadConstraints _streamReadConstraints;

   public ReadConstrainedTextBuffer(StreamReadConstraints streamReadConstraints, BufferRecycler bufferRecycler) {
      super(bufferRecycler);
      this._streamReadConstraints = streamReadConstraints;
   }

   @Override
   protected void validateStringLength(int length) throws StreamConstraintsException {
      this._streamReadConstraints.validateStringLength(length);
   }
}
