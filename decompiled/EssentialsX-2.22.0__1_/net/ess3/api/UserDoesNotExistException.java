package net.ess3.api;

@Deprecated
public class UserDoesNotExistException extends TranslatableException {
   public UserDoesNotExistException(final String name) {
      super("userDoesNotExist", name);
   }
}
