package com.sk89q.worldedit.jchronic.handlers;

import com.sk89q.worldedit.jchronic.Options;
import com.sk89q.worldedit.jchronic.utils.Span;
import com.sk89q.worldedit.jchronic.utils.Token;
import java.util.List;

public class DummyHandler implements IHandler {
   @Override
   public Span handle(List<Token> tokens, Options options) {
      return null;
   }
}
