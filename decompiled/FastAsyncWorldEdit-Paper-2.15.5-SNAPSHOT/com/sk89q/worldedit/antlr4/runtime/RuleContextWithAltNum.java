package com.sk89q.worldedit.antlr4.runtime;

public class RuleContextWithAltNum extends ParserRuleContext {
   public int altNum;

   public RuleContextWithAltNum() {
      this.altNum = 0;
   }

   public RuleContextWithAltNum(ParserRuleContext parent, int invokingStateNumber) {
      super(parent, invokingStateNumber);
   }

   @Override
   public int getAltNumber() {
      return this.altNum;
   }

   @Override
   public void setAltNumber(int altNum) {
      this.altNum = altNum;
   }
}
