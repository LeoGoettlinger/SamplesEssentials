package com.sk89q.worldedit.antlr4.runtime.atn;

import com.sk89q.worldedit.antlr4.runtime.TokenStream;

public class ContextSensitivityInfo extends DecisionEventInfo {
   public ContextSensitivityInfo(int decision, ATNConfigSet configs, TokenStream input, int startIndex, int stopIndex) {
      super(decision, configs, input, startIndex, stopIndex, true);
   }
}
