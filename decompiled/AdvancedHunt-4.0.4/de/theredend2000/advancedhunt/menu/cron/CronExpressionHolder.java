package de.theredend2000.advancedhunt.menu.cron;

import java.util.concurrent.CompletableFuture;

public interface CronExpressionHolder {
   String getExpression();

   void setExpression(String var1);

   void clear();

   CompletableFuture<Void> save();
}
