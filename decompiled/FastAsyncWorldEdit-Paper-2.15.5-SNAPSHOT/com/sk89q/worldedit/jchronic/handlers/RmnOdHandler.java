package com.sk89q.worldedit.jchronic.handlers;

import com.sk89q.worldedit.jchronic.Options;
import com.sk89q.worldedit.jchronic.repeaters.RepeaterMonthName;
import com.sk89q.worldedit.jchronic.tags.OrdinalDay;
import com.sk89q.worldedit.jchronic.utils.Span;
import com.sk89q.worldedit.jchronic.utils.Token;
import java.util.List;

public class RmnOdHandler extends MDHandler {
   @Override
   public Span handle(List<Token> tokens, Options options) {
      return this.handle(tokens.get(0).getTag(RepeaterMonthName.class), tokens.get(1).getTag(OrdinalDay.class), tokens.subList(2, tokens.size()), options);
   }
}
