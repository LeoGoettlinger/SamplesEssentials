package com.sk89q.worldedit.antlr4.runtime;

import com.sk89q.worldedit.antlr4.runtime.atn.ATNConfig;
import com.sk89q.worldedit.antlr4.runtime.atn.ATNConfigSet;
import com.sk89q.worldedit.antlr4.runtime.dfa.DFA;
import com.sk89q.worldedit.antlr4.runtime.misc.Interval;
import java.util.BitSet;

public class DiagnosticErrorListener extends BaseErrorListener {
   protected final boolean exactOnly;

   public DiagnosticErrorListener() {
      this(true);
   }

   public DiagnosticErrorListener(boolean exactOnly) {
      this.exactOnly = exactOnly;
   }

   @Override
   public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
      if (!this.exactOnly || exact) {
         String format = "reportAmbiguity d=%s: ambigAlts=%s, input='%s'";
         String decision = this.getDecisionDescription(recognizer, dfa);
         BitSet conflictingAlts = this.getConflictingAlts(ambigAlts, configs);
         String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
         String message = String.format(format, decision, conflictingAlts, text);
         recognizer.notifyErrorListeners(message);
      }
   }

   @Override
   public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
      String format = "reportAttemptingFullContext d=%s, input='%s'";
      String decision = this.getDecisionDescription(recognizer, dfa);
      String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
      String message = String.format(format, decision, text);
      recognizer.notifyErrorListeners(message);
   }

   @Override
   public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
      String format = "reportContextSensitivity d=%s, input='%s'";
      String decision = this.getDecisionDescription(recognizer, dfa);
      String text = recognizer.getTokenStream().getText(Interval.of(startIndex, stopIndex));
      String message = String.format(format, decision, text);
      recognizer.notifyErrorListeners(message);
   }

   protected String getDecisionDescription(Parser recognizer, DFA dfa) {
      int decision = dfa.decision;
      int ruleIndex = dfa.atnStartState.ruleIndex;
      String[] ruleNames = recognizer.getRuleNames();
      if (ruleIndex >= 0 && ruleIndex < ruleNames.length) {
         String ruleName = ruleNames[ruleIndex];
         return ruleName != null && !ruleName.isEmpty() ? String.format("%d (%s)", decision, ruleName) : String.valueOf(decision);
      } else {
         return String.valueOf(decision);
      }
   }

   protected BitSet getConflictingAlts(BitSet reportedAlts, ATNConfigSet configs) {
      if (reportedAlts != null) {
         return reportedAlts;
      } else {
         BitSet result = new BitSet();

         for (ATNConfig config : configs) {
            result.set(config.alt);
         }

         return result;
      }
   }
}
