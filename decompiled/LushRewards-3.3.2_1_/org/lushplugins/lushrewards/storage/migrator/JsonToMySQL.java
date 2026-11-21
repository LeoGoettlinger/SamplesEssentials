package org.lushplugins.lushrewards.storage.migrator;

public class JsonToMySQL extends Migrator {
   public JsonToMySQL(String name) {
      super(name);
   }

   @Override
   public boolean convert() {
      return false;
   }
}
