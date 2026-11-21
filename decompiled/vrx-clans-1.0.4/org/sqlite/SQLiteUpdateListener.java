package org.sqlite;

public interface SQLiteUpdateListener {
   void onUpdate(SQLiteUpdateListener.Type var1, String var2, String var3, long var4);

   public static enum Type {
      INSERT,
      DELETE,
      UPDATE;
   }
}
