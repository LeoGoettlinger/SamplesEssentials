package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.List;
import java.util.Map;

@ApiStatus.Experimental
public record DataStoreConfig(
   Map<Category<?>, String> routing,
   Map<String, BackendConfig> backends,
   SessionConfig session,
   OwnershipConfig ownership,
   WritePathConfig writePath,
   Map<Category<?>, RetentionRule> retention,
   MigrationConfig migration,
   List<String> nameResolutionChain,
   HistoryConfig history,
   String serverName
) {
   public DataStoreConfig(
      Map<Category<?>, String> routing,
      Map<String, BackendConfig> backends,
      SessionConfig session,
      OwnershipConfig ownership,
      WritePathConfig writePath,
      Map<Category<?>, RetentionRule> retention,
      MigrationConfig migration,
      List<String> nameResolutionChain,
      HistoryConfig history,
      String serverName
   ) {
      routing = routing == null ? Map.of() : Map.copyOf(routing);
      backends = backends == null ? Map.of() : Map.copyOf(backends);
      session = session == null ? SessionConfig.defaults() : session;
      ownership = ownership == null ? OwnershipConfig.defaults() : ownership;
      writePath = writePath == null ? WritePathConfig.defaults() : writePath;
      retention = retention == null ? Map.of() : Map.copyOf(retention);
      migration = migration == null ? MigrationConfig.defaults() : migration;
      nameResolutionChain = nameResolutionChain == null ? List.of() : List.copyOf(nameResolutionChain);
      history = history == null ? HistoryConfig.defaults() : history;
      serverName = serverName == null ? "Unknown" : serverName;
      this.routing = routing;
      this.backends = backends;
      this.session = session;
      this.ownership = ownership;
      this.writePath = writePath;
      this.retention = retention;
      this.migration = migration;
      this.nameResolutionChain = nameResolutionChain;
      this.history = history;
      this.serverName = serverName;
   }
}
