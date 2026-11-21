package net.ess3.api;

@Deprecated
public class NoLoanPermittedException extends TranslatableException {
   public NoLoanPermittedException() {
      super("negativeBalanceError");
   }
}
