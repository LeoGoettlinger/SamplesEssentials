package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.backend.BackendV2;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.kind.EventStream;
import ac.grim.grimac.api.storage.kind.KeyValueScoped;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.MigrationContext;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class V2BackendBootstrap {
   private V2BackendBootstrap() {
   }

   @NotNull
   public static V2BackendBootstrap.Result install(
      @NotNull Map<Category<?>, V2BackendBootstrap.Binding<?>> bindings,
      @NotNull BackendV2 backend,
      @NotNull MigrationContext migrationCtx,
      @NotNull V2Routes.Builder routesBuilder,
      @NotNull Logger logger
   ) {
      List<String> failures = new ArrayList<>();

      for (Entry<Category<?>, V2BackendBootstrap.Binding<?>> entry : bindings.entrySet()) {
         Category<?> category = entry.getKey();
         V2BackendBootstrap.Binding<?> binding = entry.getValue();
         DataKind kind = binding.kind();
         StoreId storeId = binding.storeId();
         boolean eventOk = kind.eventType().isAssignableFrom(category.eventType());
         boolean recordOk = kind instanceof KeyValueScoped || category.queryResultType() == kind.recordType();
         if (eventOk && recordOk) {
            boolean expectsTimeseriesLegacy = category.requiredCapabilities().contains(Capability.TIMESERIES_APPEND);
            boolean expectsTimeseriesV2 = category.requiredCapabilities().contains(Capability.KIND_EVENT_STREAM);
            boolean expectsTimeseries = expectsTimeseriesLegacy || expectsTimeseriesV2;
            boolean isTimeseries = kind instanceof EventStream;
            if (expectsTimeseries != isTimeseries) {
               failures.add(
                  "kind family mismatch: category "
                     + category.id()
                     + (
                        expectsTimeseries
                           ? " requires an EventStream kind (TIMESERIES_APPEND) but got "
                           : " requires a non-EventStream kind but got an EventStream: "
                     )
                     + kind.name()
               );
               logger.warning(
                  () -> "[v2-bootstrap] "
                     + category.id()
                     + " ↔ "
                     + kind.name()
                     + " kind-family mismatch (expectsTimeseries="
                     + expectsTimeseries
                     + ", isTimeseries="
                     + isTimeseries
                     + ") — skipping binding"
               );
            } else {
               KindAdapter adapter;
               try {
                  adapter = (KindAdapter)backend.adapterFor(kind)
                     .orElseThrow(() -> new IllegalStateException("backend " + backend.id() + " does not advertise an adapter for kind " + kind.name()));
               } catch (Exception var25) {
                  failures.add("adapterFor " + kind.name() + " on " + backend.id() + ": " + var25.getMessage());
                  logger.log(Level.WARNING, "[v2-bootstrap] adapterFor " + kind.name() + " on " + backend.id() + " failed", (Throwable)var25);
                  continue;
               }

               try {
                  adapter.ensureStore(storeId, kind);
               } catch (Exception var24) {
                  failures.add("ensureStore " + kind.name() + " on " + storeId + ": " + var24.getMessage());
                  logger.log(Level.WARNING, "[v2-bootstrap] ensureStore " + kind.name() + " on " + storeId + " failed", (Throwable)var24);
                  continue;
               }

               List<Migration> migs = adapter.migrations(kind);
               logger.info(() -> "[v2-bootstrap] " + storeId + " (" + kind.name() + "): " + migs.size() + " migration(s)");
               boolean migrationFailed = false;
               Iterator var21 = migs.iterator();

               while (true) {
                  if (var21.hasNext()) {
                     Migration m = (Migration)var21.next();

                     try {
                        logger.fine(() -> "[v2-bootstrap]   apply " + m.getClass().getSimpleName() + " " + m.fromVersion() + " → " + m.toVersion());
                        m.apply(migrationCtx, storeId, kind);
                        logger.fine(() -> "[v2-bootstrap]   OK    " + m.getClass().getSimpleName());
                        continue;
                     } catch (Exception var26) {
                        failures.add("migration " + m.getClass().getSimpleName() + " on " + storeId + ": " + var26.getMessage());
                        logger.log(
                           Level.WARNING,
                           "[v2-bootstrap] migration "
                              + m.getClass().getSimpleName()
                              + " on "
                              + storeId
                              + " FAILED — stopping migration chain for binding, not registering route",
                           (Throwable)var26
                        );
                        migrationFailed = true;
                     }
                  }

                  if (!migrationFailed) {
                     registerRoute(routesBuilder, category, storeId, kind, backend);
                  }
                  break;
               }
            }
         } else {
            failures.add(
               "type mismatch: category "
                  + category.id()
                  + " (event="
                  + category.eventType().getSimpleName()
                  + ", queryResult="
                  + category.queryResultType().getSimpleName()
                  + ") does not match kind "
                  + kind.name()
                  + " (event="
                  + kind.eventType().getSimpleName()
                  + ", record="
                  + kind.recordType().getSimpleName()
                  + ")"
            );
            logger.warning(() -> "[v2-bootstrap] " + category.id() + " ↔ " + kind.name() + " type mismatch — skipping binding");
         }
      }

      return new V2BackendBootstrap.Result(failures);
   }

   private static void registerRoute(
      @NotNull V2Routes.Builder routes, @NotNull Category category, @NotNull StoreId storeId, @NotNull DataKind kind, @NotNull BackendV2 backend
   ) {
      routes.register(category, storeId, kind, backend);
   }

   public record Binding<K extends DataKind<?, ?>>(@NotNull StoreId storeId, @NotNull K kind) {
   }

   public record Result(@NotNull List<String> failures) {
      public boolean ok() {
         return this.failures.isEmpty();
      }
   }
}
