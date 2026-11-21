package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor;

import java.util.Collections;
import java.util.List;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;

public class ChatColorHandlerSettings {
   private List<Parser> defaultParsers = Collections.emptyList();
   private boolean debug = false;

   public List<Parser> defaultParsers() {
      ChatColorHandler.ensureInitialised();
      return this.defaultParsers;
   }

   public ChatColorHandlerSettings defaultParsers(List<Parser> parsers) {
      this.defaultParsers = parsers;
      return this;
   }

   public boolean debug() {
      return this.debug;
   }

   public ChatColorHandlerSettings debug(boolean debug) {
      this.debug = debug;
      return this;
   }
}
