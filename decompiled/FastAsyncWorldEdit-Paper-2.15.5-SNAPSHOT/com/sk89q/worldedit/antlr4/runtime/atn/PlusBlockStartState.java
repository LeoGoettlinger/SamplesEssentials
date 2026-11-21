package com.sk89q.worldedit.antlr4.runtime.atn;

public final class PlusBlockStartState extends BlockStartState {
   public PlusLoopbackState loopBackState;

   @Override
   public int getStateType() {
      return 4;
   }
}
