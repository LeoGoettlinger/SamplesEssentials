package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@Deprecated(
   forRemoval = true,
   since = "phase0"
)
@ApiStatus.Experimental
public record TableNames(
   @NotNull String meta, @NotNull String checks, @NotNull String players, @NotNull String sessions, @NotNull String violations, @NotNull String settings
) {
   public static final String DEFAULT_META = "grim_meta";
   public static final String DEFAULT_CHECKS = "grim_checks";
   public static final String DEFAULT_PLAYERS = "grim_players";
   public static final String DEFAULT_SESSIONS = "grim_sessions";
   public static final String DEFAULT_VIOLATIONS = "grim_violations";
   public static final String DEFAULT_SETTINGS = "grim_settings";
   public static final TableNames DEFAULTS = new TableNames("grim_meta", "grim_checks", "grim_players", "grim_sessions", "grim_violations", "grim_settings");

   public TableNames(
      @NotNull String meta, @NotNull String checks, @NotNull String players, @NotNull String sessions, @NotNull String violations, @NotNull String settings
   ) {
      if (meta == null || meta.isBlank()) {
         meta = "grim_meta";
      }

      if (checks == null || checks.isBlank()) {
         checks = "grim_checks";
      }

      if (players == null || players.isBlank()) {
         players = "grim_players";
      }

      if (sessions == null || sessions.isBlank()) {
         sessions = "grim_sessions";
      }

      if (violations == null || violations.isBlank()) {
         violations = "grim_violations";
      }

      if (settings == null || settings.isBlank()) {
         settings = "grim_settings";
      }

      this.meta = meta;
      this.checks = checks;
      this.players = players;
      this.sessions = sessions;
      this.violations = violations;
      this.settings = settings;
   }

   @NotNull
   public static TableNames readFrom(@NotNull BackendConfigSource source) {
      return new TableNames(
         source.getString("tables.meta", "grim_meta"),
         source.getString("tables.checks", "grim_checks"),
         source.getString("tables.players", "grim_players"),
         source.getString("tables.sessions", "grim_sessions"),
         source.getString("tables.violations", "grim_violations"),
         source.getString("tables.settings", "grim_settings")
      );
   }
}
