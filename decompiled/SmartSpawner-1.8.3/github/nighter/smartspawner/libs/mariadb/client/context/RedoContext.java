package github.nighter.smartspawner.libs.mariadb.client.context;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.HostAddress;
import github.nighter.smartspawner.libs.mariadb.client.PrepareCache;
import github.nighter.smartspawner.libs.mariadb.client.impl.TransactionSaver;
import github.nighter.smartspawner.libs.mariadb.export.ExceptionFactory;
import github.nighter.smartspawner.libs.mariadb.message.ClientMessage;
import github.nighter.smartspawner.libs.mariadb.message.client.RedoableClientMessage;
import github.nighter.smartspawner.libs.mariadb.message.server.InitialHandshakePacket;

public class RedoContext extends BaseContext {
   private final TransactionSaver transactionSaver;

   public RedoContext(
      HostAddress hostAddress,
      InitialHandshakePacket handshake,
      long clientCapabilities,
      Configuration conf,
      ExceptionFactory exceptionFactory,
      PrepareCache prepareCache,
      Boolean loopbackAddress,
      Runnable connectionCloser
   ) {
      super(hostAddress, handshake, clientCapabilities, conf, exceptionFactory, prepareCache, loopbackAddress, connectionCloser);
      this.transactionSaver = new TransactionSaver(conf.transactionReplaySize());
   }

   @Override
   public void setServerStatus(int serverStatus) {
      this.serverStatus = serverStatus;
      if ((serverStatus & 1) == 0) {
         this.transactionSaver.clear();
      }
   }

   public void saveRedo(ClientMessage msg) {
      if ((this.serverStatus & 1) != 0) {
         if (msg instanceof RedoableClientMessage) {
            RedoableClientMessage redoMsg = (RedoableClientMessage)msg;
            redoMsg.saveParameters();
            this.transactionSaver.add(redoMsg);
         }
      }
   }

   public void saveRedo(ClientMessage[] msgs) {
      for (ClientMessage msg : msgs) {
         this.saveRedo(msg);
      }
   }

   public TransactionSaver getTransactionSaver() {
      return this.transactionSaver;
   }
}
