package net.ess3.api;

@Deprecated
public class InvalidNameException extends Exception {
   private static final long serialVersionUID = 1485321420293663139L;

   public InvalidNameException(final Throwable thrwbl) {
      super(thrwbl);
   }
}
