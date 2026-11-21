package com.earth2me.essentials.libs.snakeyaml.composer;

import com.earth2me.essentials.libs.snakeyaml.error.Mark;
import com.earth2me.essentials.libs.snakeyaml.error.MarkedYAMLException;

public class ComposerException extends MarkedYAMLException {
   private static final long serialVersionUID = 2146314636913113935L;

   protected ComposerException(String context, Mark contextMark, String problem, Mark problemMark) {
      super(context, contextMark, problem, problemMark);
   }
}
