package de.theredend2000.advancedhunt.lib.cronutils.model.field;

public enum CronFieldName {
   SECOND(0),
   MINUTE(1),
   HOUR(2),
   DAY_OF_MONTH(3),
   MONTH(4),
   DAY_OF_WEEK(5),
   YEAR(6),
   DAY_OF_YEAR(7);

   private int order;

   private CronFieldName(int order) {
      this.order = order;
   }

   public int getOrder() {
      return this.order;
   }
}
