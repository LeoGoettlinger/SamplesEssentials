package ac.grim.grimac.internal.storage.copy;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.model.SessionRecord;
import ac.grim.grimac.api.storage.model.ViolationRecord;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.internal.storage.backend.memory.InMemoryBackend;
import ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackend;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongConsumer;

@ApiStatus.Internal
public final class BackendToBackendCopier {
   private final Backend src;
   private final Backend dst;
   private final int pageSize;

   public BackendToBackendCopier(@NotNull Backend src, @NotNull Backend dst) {
      this(src, dst, 500);
   }

   public BackendToBackendCopier(@NotNull Backend src, @NotNull Backend dst, int pageSize) {
      if (src == dst) {
         throw new IllegalArgumentException("source and destination cannot be the same backend");
      } else {
         this.src = src;
         this.dst = dst;
         this.pageSize = Math.max(50, pageSize);
      }
   }

   public BackendToBackendCopier.Result run(LongConsumer onBatch) throws BackendException {
      long start = System.currentTimeMillis();
      this.copyCheckCatalog();
      Set<UUID> players = this.collectPlayersBySessionWalk();
      List<PlayerIdentity> identities = new ArrayList<>(players.size());

      for (UUID u : players) {
         Page<PlayerIdentity> p = this.src.read(Categories.PLAYER_IDENTITY, Queries.getPlayerIdentity(u));
         if (!p.items().isEmpty()) {
            identities.add(p.items().get(0));
         }
      }

      if (!identities.isEmpty()) {
         this.dst.bulkImport(Categories.PLAYER_IDENTITY, identities);
      }

      long copiedSessions = 0L;
      long copiedViolations = 0L;
      List<SessionRecord> sessionBatch = new ArrayList<>(this.pageSize);
      List<ViolationRecord> violationBatch = new ArrayList<>(this.pageSize);

      for (UUID player : players) {
         Cursor sCursor = null;

         do {
            Page<SessionRecord> sp = this.src.read(Categories.SESSION, Queries.listSessionsByPlayer(player, this.pageSize, sCursor));

            for (SessionRecord s : sp.items()) {
               sessionBatch.add(s);
               if (sessionBatch.size() >= this.pageSize) {
                  this.dst.bulkImport(Categories.SESSION, sessionBatch);
                  copiedSessions += sessionBatch.size();
                  sessionBatch.clear();
               }

               Cursor vCursor = null;

               do {
                  Page<ViolationRecord> vp = this.src.read(Categories.VIOLATION, Queries.listViolationsInSession(s.sessionId(), this.pageSize, vCursor));

                  for (ViolationRecord v : vp.items()) {
                     violationBatch.add(v);
                     if (violationBatch.size() >= this.pageSize) {
                        this.dst.bulkImport(Categories.VIOLATION, violationBatch);
                        copiedViolations += violationBatch.size();
                        violationBatch.clear();
                        onBatch.accept(copiedViolations);
                     }
                  }

                  vCursor = vp.nextCursor();
               } while (vCursor == null);
            }

            sCursor = sp.nextCursor();
         } while (sCursor == null);
      }

      if (!sessionBatch.isEmpty()) {
         this.dst.bulkImport(Categories.SESSION, sessionBatch);
         copiedSessions += sessionBatch.size();
      }

      if (!violationBatch.isEmpty()) {
         this.dst.bulkImport(Categories.VIOLATION, violationBatch);
         copiedViolations += violationBatch.size();
         onBatch.accept(copiedViolations);
      }

      return new BackendToBackendCopier.Result(copiedSessions, copiedViolations, identities.size(), System.currentTimeMillis() - start);
   }

   private void copyCheckCatalog() throws BackendException {
      for (CheckCatalogRow row : this.src.checkCatalog().loadAll()) {
         this.dst.checkCatalog().upsert(row);
      }
   }

   private Set<UUID> collectPlayersBySessionWalk() throws BackendException {
      Set<UUID> out = new LinkedHashSet<>();
      if (this.src instanceof SqliteBackend sq) {
         try {
            try (
               Connection c = DriverManager.getConnection(sq.jdbcUrl());
               Statement s = c.createStatement();
               ResultSet rs = s.executeQuery("SELECT uuid FROM " + sq.tableNames().players());
            ) {
               while (rs.next()) {
                  byte[] bytes = rs.getBytes(1);
                  if (bytes != null && bytes.length == 16) {
                     ByteBuffer bb = ByteBuffer.wrap(bytes);
                     out.add(new UUID(bb.getLong(), bb.getLong()));
                  }
               }
            }

            return out;
         } catch (SQLException var14) {
            throw new BackendException("failed to enumerate source players", var14);
         }
      } else if (this.src instanceof InMemoryBackend mem) {
         out.addAll(mem.knownPlayerUuids());
         return out;
      } else {
         throw new BackendException("backend " + this.src.id() + " does not expose a player-enumeration hatch; cannot use as copy source");
      }
   }

   public void dropSource() throws BackendException {
      if (this.src instanceof SqliteBackend sq) {
         synchronized (sq.writeMutexForCopier()) {
            Connection conn = sq.writeConnection();
            if (conn == null) {
               throw new BackendException("source backend not initialised");
            } else {
               boolean wasAutoCommit = true;

               try {
                  wasAutoCommit = conn.getAutoCommit();
                  if (wasAutoCommit) {
                     conn.setAutoCommit(false);
                  }

                  try (Statement s = conn.createStatement()) {
                     TableNames t = sq.tableNames();
                     s.executeUpdate("DELETE FROM " + t.violations());
                     s.executeUpdate("DELETE FROM " + t.sessions());
                     s.executeUpdate("DELETE FROM " + t.players());
                  }

                  conn.commit();
               } catch (SQLException var23) {
                  try {
                     conn.rollback();
                  } catch (SQLException var20) {
                  }

                  throw new BackendException("failed to drop source data", var23);
               } finally {
                  try {
                     if (wasAutoCommit) {
                        conn.setAutoCommit(true);
                     }
                  } catch (SQLException var19) {
                  }
               }
            }
         }
      } else if (this.src instanceof InMemoryBackend mem) {
         mem.wipeAllForCopier();
      } else {
         throw new BackendException("backend " + this.src.id() + " does not support in-place wipe from the copier");
      }
   }

   public record Result(long sessions, long violations, long players, long elapsedMs) {
   }
}
