package org.enginehub.linbus.format.snbt.impl.reader;

public sealed interface SnbtToken
   permits SnbtToken.CompoundStart,
   SnbtToken.CompoundEnd,
   SnbtToken.ListLikeStart,
   SnbtToken.ListLikeEnd,
   SnbtToken.EntrySeparator,
   SnbtToken.ListTypeSeparator,
   SnbtToken.Separator,
   SnbtToken.Text {
   public static enum CompoundEnd implements SnbtToken {
      INSTANCE;

      @Override
      public String toString() {
         return "'}'";
      }
   }

   public static enum CompoundStart implements SnbtToken {
      INSTANCE;

      @Override
      public String toString() {
         return "'{'";
      }
   }

   public static enum EntrySeparator implements SnbtToken {
      INSTANCE;

      @Override
      public String toString() {
         return "':'";
      }
   }

   public static enum ListLikeEnd implements SnbtToken {
      INSTANCE;

      @Override
      public String toString() {
         return "']'";
      }
   }

   public static enum ListLikeStart implements SnbtToken {
      INSTANCE;

      @Override
      public String toString() {
         return "'['";
      }
   }

   public static enum ListTypeSeparator implements SnbtToken {
      INSTANCE;

      @Override
      public String toString() {
         return "';'";
      }
   }

   public static enum Separator implements SnbtToken {
      INSTANCE;

      @Override
      public String toString() {
         return "','";
      }
   }

   public record Text(boolean quoted, String content) implements SnbtToken {
   }
}
