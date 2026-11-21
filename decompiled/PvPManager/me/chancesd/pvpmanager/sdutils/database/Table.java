package me.chancesd.pvpmanager.sdutils.database;

public class Table {
   private final String name;
   private final String usage;

   public Table(String name, String usage) {
      this.name = name;
      this.usage = usage;
   }

   public String getName() {
      return this.name;
   }

   public String getUsage() {
      return " (" + this.usage + ")";
   }

   public String getValues() {
      StringBuilder v = new StringBuilder();
      String[] a = this.usage.split(",");
      int i = 0;

      for (String b : a) {
         i++;
         String[] c = b.split(" ");
         String f = c[0] != null && !c[0].isEmpty() ? c[0] : c[1];
         String next = "";
         if (f != null) {
            next = f + (i <= a.length - 1 ? "," : "");
         }

         v.append(next);
      }

      return "(" + v + ")";
   }
}
