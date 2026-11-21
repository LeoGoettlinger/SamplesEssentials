package de.theredend2000.advancedhunt.lib.kyori.adventure.title;

record TitlePartImpl<T>(String part) implements TitlePart<T> {
   @Override
   public boolean equals(final Object o) {
      return o == this;
   }

   @Override
   public String toString() {
      return "TitlePart." + this.part();
   }
}
