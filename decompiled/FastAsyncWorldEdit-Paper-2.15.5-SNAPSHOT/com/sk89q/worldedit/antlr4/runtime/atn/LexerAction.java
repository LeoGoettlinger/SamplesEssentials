package com.sk89q.worldedit.antlr4.runtime.atn;

import com.sk89q.worldedit.antlr4.runtime.Lexer;

public interface LexerAction {
   LexerActionType getActionType();

   boolean isPositionDependent();

   void execute(Lexer var1);
}
