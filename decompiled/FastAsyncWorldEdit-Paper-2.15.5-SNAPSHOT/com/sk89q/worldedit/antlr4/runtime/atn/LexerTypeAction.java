package com.sk89q.worldedit.antlr4.runtime.atn;

import com.sk89q.worldedit.antlr4.runtime.Lexer;
import com.sk89q.worldedit.antlr4.runtime.misc.MurmurHash;

public class LexerTypeAction implements LexerAction {
   private final int type;

   public LexerTypeAction(int type) {
      this.type = type;
   }

   public int getType() {
      return this.type;
   }

   @Override
   public LexerActionType getActionType() {
      return LexerActionType.TYPE;
   }

   @Override
   public boolean isPositionDependent() {
      return false;
   }

   @Override
   public void execute(Lexer lexer) {
      lexer.setType(this.type);
   }

   @Override
   public int hashCode() {
      int hash = MurmurHash.initialize();
      hash = MurmurHash.update(hash, this.getActionType().ordinal());
      hash = MurmurHash.update(hash, this.type);
      return MurmurHash.finish(hash, 2);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else {
         return !(obj instanceof LexerTypeAction) ? false : this.type == ((LexerTypeAction)obj).type;
      }
   }

   @Override
   public String toString() {
      return String.format("type(%d)", this.type);
   }
}
