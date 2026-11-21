package com.sk89q.worldedit.jchronic.handlers;

import com.sk89q.worldedit.jchronic.Options;
import com.sk89q.worldedit.jchronic.utils.Span;
import com.sk89q.worldedit.jchronic.utils.Token;
import java.util.List;

public class ORSRHandler extends ORRHandler {
   @Override
   public Span handle(List<Token> tokens, Options options) {
      Span outerSpan = Handler.getAnchor(tokens.subList(3, 4), options);
      return this.handle(tokens.subList(0, 2), outerSpan, options);
   }
}
