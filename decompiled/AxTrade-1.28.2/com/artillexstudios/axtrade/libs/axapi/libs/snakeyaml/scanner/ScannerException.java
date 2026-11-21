package com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.scanner;

import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.error.Mark;
import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.error.MarkedYAMLException;

public class ScannerException extends MarkedYAMLException {
   private static final long serialVersionUID = 4782293188600445954L;

   public ScannerException(String context, Mark contextMark, String problem, Mark problemMark, String note) {
      super(context, contextMark, problem, problemMark, note);
   }

   public ScannerException(String context, Mark contextMark, String problem, Mark problemMark) {
      this(context, contextMark, problem, problemMark, null);
   }
}
