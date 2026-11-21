package eu.kennytv.maintenance.core.hook;

import eu.kennytv.maintenance.api.Maintenance;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.jetbrains.annotations.NotNull;

public final class LuckPermsHook {
   public static <T> void register(Maintenance maintenance) {
      LuckPermsProvider.get().getContextManager().registerCalculator(new LuckPermsHook.MaintenanceCalculator(maintenance));
   }

   public static final class MaintenanceCalculator<T> implements ContextCalculator<T> {
      public static final String MAINTENANCE_KEY = "is-maintenance";
      private final Maintenance maintenance;

      private MaintenanceCalculator(Maintenance maintenance) {
         this.maintenance = maintenance;
      }

      public void calculate(@NotNull T target, @NotNull ContextConsumer consumer) {
         consumer.accept("is-maintenance", Boolean.toString(this.maintenance.isMaintenance()));
      }

      public ContextSet estimatePotentialContexts() {
         return ImmutableContextSet.builder().add("is-maintenance", "true").add("is-maintenance", "false").build();
      }
   }
}
