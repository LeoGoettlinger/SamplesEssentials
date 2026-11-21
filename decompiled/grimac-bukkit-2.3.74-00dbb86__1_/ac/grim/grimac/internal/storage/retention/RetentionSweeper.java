package ac.grim.grimac.internal.storage.retention;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.config.RetentionRule;
import ac.grim.grimac.api.storage.query.Deletes;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class RetentionSweeper {
   private final DataStore store;
   private final Map<Category<?>, RetentionRule> rules;
   private final Logger logger;

   public RetentionSweeper(DataStore store, Map<Category<?>, RetentionRule> rules, Logger logger) {
      this.store = store;
      this.rules = Map.copyOf(rules);
      this.logger = logger;
   }

   public void sweepOnce() {
      for (Entry<Category<?>, RetentionRule> entry : this.rules.entrySet()) {
         Category<?> cat = entry.getKey();
         RetentionRule rule = entry.getValue();
         if (rule.enabled() && rule.maxAgeDays() > 0L && (cat == Categories.SESSION || cat == Categories.VIOLATION)) {
            long maxAgeMs = rule.maxAgeMs();

            try {
               CompletionStage<Void> stage = this.store.delete(cat, Deletes.olderThan(maxAgeMs));
               stage.toCompletableFuture().get(30L, TimeUnit.SECONDS);
            } catch (InterruptedException var8) {
               Thread.currentThread().interrupt();
               this.logger.log(Level.WARNING, "[grim-datastore] retention sweep interrupted");
               return;
            } catch (ExecutionException | TimeoutException | CompletionException var9) {
               this.logger.log(Level.WARNING, "[grim-datastore] retention sweep failed for " + cat.id(), (Throwable)var9);
            }
         }
      }
   }
}
