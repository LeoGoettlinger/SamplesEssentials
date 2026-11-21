package ac.grim.grimac.internal.storage.migrate;

import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApiStatus.Internal
public final class SessionReconstructor {
   private final long gapMs;
   private final SessionReconstructor.LookupLens lookup;
   private final SessionReconstructor.Emit emit;
   private final SessionReconstructor.CheckIdResolver checkIdResolver;
   private final SessionReconstructor.ClientVersionResolver clientVersionResolver;
   private UUID currentPlayer;
   private SessionRecord currentSession;
   private List<SessionReconstructor.ReconstructedViolation> pendingInSession = new ArrayList<>();

   public SessionReconstructor(
      long gapMs,
      SessionReconstructor.LookupLens lookup,
      SessionReconstructor.CheckIdResolver checkIdResolver,
      SessionReconstructor.ClientVersionResolver clientVersionResolver,
      SessionReconstructor.Emit emit
   ) {
      this.gapMs = gapMs;
      this.lookup = lookup;
      this.checkIdResolver = checkIdResolver;
      this.clientVersionResolver = clientVersionResolver;
      this.emit = emit;
   }

   public void accept(V0Reader.V0Violation v) {
      if (this.currentPlayer == null || !this.currentPlayer.equals(v.playerUuid())) {
         this.flush();
         this.currentPlayer = v.playerUuid();
      }

      if (this.currentSession != null
         && v.createdAtEpochMs() - this.currentSession.lastActivityEpochMs() <= this.gapMs
         && this.sameServerContext(this.currentSession, v)) {
         this.currentSession = new SessionRecord(
            this.currentSession.sessionId(),
            this.currentSession.playerUuid(),
            this.currentSession.serverName(),
            this.currentSession.startedEpochMs(),
            Math.max(this.currentSession.lastActivityEpochMs(), v.createdAtEpochMs()),
            this.currentSession.closedAtEpochMs(),
            this.currentSession.grimVersion(),
            this.currentSession.clientBrand(),
            this.currentSession.clientVersion(),
            this.currentSession.serverVersionString(),
            this.currentSession.instanceId(),
            this.currentSession.sessionBlobs()
         );
      } else {
         if (this.currentSession != null) {
            this.flushSession();
         }

         this.currentSession = this.newSession(v);
      }

      String legacyName = this.lookup.checkName(v.checkNameId());
      int checkId = this.checkIdResolver.resolve(legacyName);
      this.pendingInSession
         .add(
            new SessionReconstructor.ReconstructedViolation(
               v.legacyId(), this.currentSession.sessionId(), v.playerUuid(), checkId, v.vl(), v.createdAtEpochMs(), sanitizeVerbose(v.verbose())
            )
         );
   }

   public void flush() {
      if (this.currentSession != null) {
         this.flushSession();
      }

      this.currentSession = null;
      this.currentPlayer = null;
   }

   private void flushSession() {
      this.emit.onSessionBoundary(this.currentSession, List.copyOf(this.pendingInSession));
      this.pendingInSession.clear();
   }

   private SessionRecord newSession(V0Reader.V0Violation v) {
      String clientVersionStr = this.lookup.clientVersion(v.clientVersionId());
      int clientVersionPvn = this.clientVersionResolver.resolve(clientVersionStr);
      return new SessionRecord(
         UUID.randomUUID(),
         v.playerUuid(),
         this.lookup.server(v.serverId()),
         v.createdAtEpochMs(),
         v.createdAtEpochMs(),
         0L,
         this.lookup.grimVersion(v.grimVersionId()),
         this.lookup.clientBrand(v.clientBrandId()),
         clientVersionPvn,
         this.lookup.serverVersion(v.serverVersionId()),
         null,
         List.of()
      );
   }

   private boolean sameServerContext(SessionRecord s, V0Reader.V0Violation v) {
      return s.serverName() == null || s.serverName().equals(this.lookup.server(v.serverId()));
   }

   private static String sanitizeVerbose(String verbose) {
      if (verbose == null) {
         return null;
      } else {
         int marker = verbose.indexOf(" /gl ");
         return marker >= 0 ? verbose.substring(0, marker) : verbose;
      }
   }

   @FunctionalInterface
   public interface CheckIdResolver {
      int resolve(String var1);
   }

   @FunctionalInterface
   public interface ClientVersionResolver {
      int resolve(String var1);
   }

   public interface Emit {
      void onSessionBoundary(SessionRecord var1, List<SessionReconstructor.ReconstructedViolation> var2);
   }

   public interface LookupLens {
      String server(int var1);

      String checkName(int var1);

      String grimVersion(int var1);

      String clientBrand(int var1);

      String clientVersion(int var1);

      String serverVersion(int var1);
   }

   public record ReconstructedViolation(long legacyId, UUID sessionId, UUID playerUuid, int checkId, double vl, long occurredEpochMs, String verbose) {
   }
}
