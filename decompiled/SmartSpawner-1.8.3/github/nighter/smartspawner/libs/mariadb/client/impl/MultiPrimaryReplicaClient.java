package github.nighter.smartspawner.libs.mariadb.client.impl;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.Statement;
import github.nighter.smartspawner.libs.mariadb.client.Client;
import github.nighter.smartspawner.libs.mariadb.client.Completion;
import github.nighter.smartspawner.libs.mariadb.client.util.ClosableLock;
import github.nighter.smartspawner.libs.mariadb.export.Prepare;
import github.nighter.smartspawner.libs.mariadb.message.ClientMessage;
import github.nighter.smartspawner.libs.mariadb.util.log.Logger;
import github.nighter.smartspawner.libs.mariadb.util.log.Loggers;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.List;

public class MultiPrimaryReplicaClient extends MultiPrimaryClient {
   private static final Logger logger = Loggers.getLogger(MultiPrimaryReplicaClient.class);
   protected long waitTimeout;
   private Client replicaClient;
   private Client primaryClient;
   private boolean requestReadOnly;
   private long nextTryReplica = -1L;
   private long nextTryPrimary = -1L;

   public MultiPrimaryReplicaClient(Configuration conf, ClosableLock lock) throws SQLException {
      super(conf, lock);
      this.primaryClient = this.currentClient;
      this.waitTimeout = Long.parseLong(conf.nonMappedOptions().getProperty("waitReconnectTimeout", "30000"));

      try {
         this.replicaClient = this.connectHost(true, false);
      } catch (SQLException var4) {
         this.replicaClient = null;
         this.nextTryReplica = System.currentTimeMillis() + this.waitTimeout;
      }
   }

   private void reconnectIfNeeded() {
      if (!this.closed) {
         if (this.primaryClient == null && this.nextTryPrimary < System.currentTimeMillis()) {
            try {
               this.primaryClient = this.connectHost(false, true);
               this.nextTryPrimary = -1L;
            } catch (SQLException var3) {
               this.nextTryPrimary = System.currentTimeMillis() + this.waitTimeout;
            }
         }

         if (this.replicaClient == null && this.nextTryReplica < System.currentTimeMillis()) {
            try {
               this.replicaClient = this.connectHost(true, true);
               this.nextTryReplica = -1L;
               if (this.requestReadOnly) {
                  this.syncNewState(this.primaryClient);
                  this.currentClient = this.replicaClient;
               }
            } catch (SQLException var2) {
               this.nextTryReplica = System.currentTimeMillis() + this.waitTimeout;
            }
         }
      }
   }

   @Override
   protected Client reConnect() throws SQLException {
      denyList.putIfAbsent(this.currentClient.getHostAddress(), System.currentTimeMillis() + this.deniedListTimeout);
      logger.info("Connection error on {}", this.currentClient.getHostAddress());

      try {
         Client oldClient = this.currentClient;
         if (oldClient.isPrimary()) {
            this.primaryClient = null;
         } else {
            this.replicaClient = null;
         }

         oldClient.getContext().resetPrepareCache();

         try {
            this.currentClient = this.connectHost(this.requestReadOnly, this.requestReadOnly);
            if (this.requestReadOnly) {
               this.nextTryReplica = -1L;
               this.replicaClient = this.currentClient;
            } else {
               this.nextTryPrimary = -1L;
               this.primaryClient = this.currentClient;
            }
         } catch (SQLNonTransientConnectionException var5) {
            if (!this.requestReadOnly) {
               throw new SQLNonTransientConnectionException(
                  String.format("Driver has failed to reconnect master connection after a communications failure with %s", oldClient.getHostAddress()), "08000"
               );
            }

            this.nextTryReplica = System.currentTimeMillis() + this.waitTimeout;
            if (this.primaryClient != null) {
               this.currentClient = this.primaryClient;
            } else {
               try {
                  this.primaryClient = this.connectHost(false, false);
                  this.currentClient = this.primaryClient;
                  this.nextTryPrimary = -1L;
               } catch (SQLNonTransientConnectionException var4) {
                  this.closed = true;
                  throw new SQLNonTransientConnectionException(
                     String.format("Driver has failed to reconnect connection after a communications failure with %s", oldClient.getHostAddress()), "08000"
                  );
               }
            }
         }

         this.syncNewState(oldClient);
         return this.requestReadOnly ? null : oldClient;
      } catch (SQLNonTransientConnectionException var6) {
         this.closed = true;
         if (this.replicaClient != null) {
            this.replicaClient.close();
         }

         throw var6;
      }
   }

   @Override
   public List<Completion> execute(
      ClientMessage message,
      Statement stmt,
      int fetchSize,
      long maxRows,
      int resultSetConcurrency,
      int resultSetType,
      boolean closeOnCompletion,
      boolean canRedo
   ) throws SQLException {
      this.reconnectIfNeeded();
      return super.execute(message, stmt, fetchSize, maxRows, resultSetConcurrency, resultSetType, closeOnCompletion, canRedo);
   }

   @Override
   public List<Completion> executePipeline(
      ClientMessage[] messages,
      Statement stmt,
      int fetchSize,
      long maxRows,
      int resultSetConcurrency,
      int resultSetType,
      boolean closeOnCompletion,
      boolean canRedo
   ) throws SQLException {
      this.reconnectIfNeeded();
      return super.executePipeline(messages, stmt, fetchSize, maxRows, resultSetConcurrency, resultSetType, closeOnCompletion, canRedo);
   }

   @Override
   public void readStreamingResults(
      List<Completion> completions, int fetchSize, long maxRows, int resultSetConcurrency, int resultSetType, boolean closeOnCompletion
   ) throws SQLException {
      this.reconnectIfNeeded();
      super.readStreamingResults(completions, fetchSize, maxRows, resultSetConcurrency, resultSetType, closeOnCompletion);
   }

   @Override
   public void closePrepare(Prepare prepare) throws SQLException {
      this.reconnectIfNeeded();
      super.closePrepare(prepare);
   }

   @Override
   public void close() throws SQLException {
      if (!this.closed) {
         this.closed = true;

         try {
            if (this.primaryClient != null) {
               this.primaryClient.close();
            }
         } catch (SQLException var3) {
         }

         try {
            if (this.replicaClient != null) {
               this.replicaClient.close();
            }
         } catch (SQLException var2) {
         }

         this.primaryClient = null;
         this.replicaClient = null;
      }
   }

   @Override
   public void setReadOnly(boolean readOnly) throws SQLException {
      if (this.closed) {
         throw new SQLNonTransientConnectionException("Connection is closed", "08000", 1220);
      } else {
         if (readOnly) {
            if (!this.requestReadOnly) {
               Client oldCli = this.currentClient;
               if (this.replicaClient != null) {
                  this.currentClient = this.replicaClient;
                  if (oldCli != null && oldCli != this.currentClient) {
                     this.syncNewState(oldCli);
                  }
               } else if (this.nextTryReplica < System.currentTimeMillis()) {
                  try {
                     this.replicaClient = this.connectHost(true, true);
                     this.currentClient = this.replicaClient;
                     if (oldCli != null && oldCli != this.currentClient) {
                        this.syncNewState(oldCli);
                     }
                  } catch (SQLException var5) {
                     this.nextTryReplica = System.currentTimeMillis() + this.waitTimeout;
                  }
               }
            }
         } else if (this.requestReadOnly) {
            Client oldCli = this.currentClient;
            if (this.primaryClient != null) {
               this.currentClient = this.primaryClient;
               if (oldCli != null && oldCli != this.currentClient) {
                  this.syncNewState(oldCli);
               }
            } else if (this.nextTryPrimary < System.currentTimeMillis()) {
               try {
                  this.primaryClient = this.connectHost(false, false);
                  this.nextTryPrimary = -1L;
                  this.currentClient = this.primaryClient;
                  if (oldCli != null && oldCli != this.currentClient) {
                     this.syncNewState(oldCli);
                  }
               } catch (SQLException var4) {
                  this.nextTryPrimary = System.currentTimeMillis() + this.waitTimeout;
                  throw new SQLNonTransientConnectionException("Driver has failed to reconnect a primary connection", "08000");
               }
            }
         }

         this.requestReadOnly = readOnly;
      }
   }

   @Override
   public void setSocketTimeout(int milliseconds) throws SQLException {
      this.reconnectIfNeeded();
      super.setSocketTimeout(milliseconds);
   }

   @Override
   public boolean isPrimary() {
      return this.getHostAddress().primary;
   }

   @Override
   public void reset() {
      if (this.replicaClient != null) {
         this.replicaClient.getContext().resetStateFlag();
         this.replicaClient.getContext().resetPrepareCache();
      }

      if (this.primaryClient != null) {
         this.primaryClient.getContext().resetStateFlag();
         this.primaryClient.getContext().resetPrepareCache();
      }
   }
}
