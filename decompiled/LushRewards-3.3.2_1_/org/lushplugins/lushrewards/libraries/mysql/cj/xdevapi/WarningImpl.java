package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

public class WarningImpl implements Warning {
   private org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Warning message;

   public WarningImpl(org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Warning message) {
      this.message = message;
   }

   @Override
   public int getLevel() {
      return this.message.getLevel();
   }

   @Override
   public long getCode() {
      return this.message.getCode();
   }

   @Override
   public String getMessage() {
      return this.message.getMessage();
   }
}
