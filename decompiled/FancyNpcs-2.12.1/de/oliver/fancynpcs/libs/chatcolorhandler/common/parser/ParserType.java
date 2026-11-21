package de.oliver.fancynpcs.libs.chatcolorhandler.common.parser;

public enum ParserType {
   COLOR("color"),
   SOUND("sound"),
   PARTICLE("particle"),
   INTERACTION("interaction"),
   PLACEHOLDER("placeholder"),
   TEXT_FORMATTING("text-formatting");

   private final String id;

   private ParserType(String id) {
      this.id = id;
   }

   public String id() {
      return this.id;
   }

   public boolean equals(String id) {
      return this.id.equals(id);
   }
}
