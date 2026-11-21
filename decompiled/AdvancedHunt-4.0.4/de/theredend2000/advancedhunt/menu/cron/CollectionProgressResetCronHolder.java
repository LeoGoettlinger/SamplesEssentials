package de.theredend2000.advancedhunt.menu.cron;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.model.Collection;
import java.util.concurrent.CompletableFuture;

final class CollectionProgressResetCronHolder implements CronExpressionHolder {
   private final Main plugin;
   private final Collection collection;

   CollectionProgressResetCronHolder(Main plugin, Collection collection) {
      this.plugin = plugin;
      this.collection = collection;
   }

   @Override
   public String getExpression() {
      return this.collection.getProgressResetCron();
   }

   @Override
   public void setExpression(String expression) {
      this.collection.setProgressResetCron(expression);
   }

   @Override
   public void clear() {
      this.collection.setProgressResetCron(null);
   }

   @Override
   public CompletableFuture<Void> save() {
      return this.plugin.getCollectionManager().saveCollection(this.collection);
   }
}
