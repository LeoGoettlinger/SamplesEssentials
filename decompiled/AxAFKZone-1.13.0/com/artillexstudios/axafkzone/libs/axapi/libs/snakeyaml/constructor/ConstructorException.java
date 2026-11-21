package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.constructor;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.Mark;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.MarkedYAMLException;

public class ConstructorException extends MarkedYAMLException {
   private static final long serialVersionUID = -8816339931365239910L;

   protected ConstructorException(String context, Mark contextMark, String problem, Mark problemMark, Throwable cause) {
      super(context, contextMark, problem, problemMark, cause);
   }

   protected ConstructorException(String context, Mark contextMark, String problem, Mark problemMark) {
      this(context, contextMark, problem, problemMark, null);
   }
}
