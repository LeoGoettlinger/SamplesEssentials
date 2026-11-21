package de.oliver.fancynpcs.libs.chatcolorhandler.common.settings;

import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parser;
import de.oliver.fancynpcs.libs.chatcolorhandler.common.parser.Parsers;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class ColorHandlerSettings {
   private UnaryOperator<Parsers> defaultParsers = Parsers::all;

   public UnaryOperator<Parsers> defaultParsers() {
      return this.defaultParsers;
   }

   public ColorHandlerSettings defaultParsers(UnaryOperator<Parsers> defaultParsers) {
      this.defaultParsers = defaultParsers;
      return this;
   }

   public ColorHandlerSettings defaultParsers(List<Parser> defaultParsers) {
      return this.defaultParsers(parsers -> parsers.parsers(defaultParsers));
   }

   public ColorHandlerSettings defaultParsers(Parser... parsers) {
      return this.defaultParsers(Arrays.asList(parsers));
   }
}
