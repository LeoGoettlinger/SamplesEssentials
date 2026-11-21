package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers;

import java.util.List;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;

public class ParserTypes {
   public static final String COLOR = "color";
   public static final String SOUND = "sound";
   public static final String PARTICLE = "particle";
   public static final String INTERACTION = "interaction";
   public static final String PLACEHOLDER = "placeholder";
   public static final String TEXT_FORMATTING = "text-formatting";

   public static List<Parser> color() {
      return ChatColorHandler.parsers().ofType("color");
   }

   public static List<Parser> sound() {
      return ChatColorHandler.parsers().ofType("sound");
   }

   public static List<Parser> particle() {
      return ChatColorHandler.parsers().ofType("particle");
   }

   public static List<Parser> interact() {
      return ChatColorHandler.parsers().ofType("interaction");
   }

   public static List<Parser> placeholder() {
      return ChatColorHandler.parsers().ofType("placeholder");
   }

   public static List<Parser> textFormatting() {
      return ChatColorHandler.parsers().ofType("text-formatting");
   }

   public static List<Parser> all() {
      return ChatColorHandler.parsers().getRegisteredParsers();
   }
}
