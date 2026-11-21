package com.sk89q.worldedit.antlr4.runtime.atn;

public final class StarLoopEntryState extends DecisionState {
   public StarLoopbackState loopBackState;
   public boolean isPrecedenceDecision;

   @Override
   public int getStateType() {
      return 10;
   }
}
