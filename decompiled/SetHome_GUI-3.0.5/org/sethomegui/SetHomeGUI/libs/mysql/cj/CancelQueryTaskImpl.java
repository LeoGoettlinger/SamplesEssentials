package org.sethomegui.SetHomeGUI.libs.mysql.cj;

import java.util.TimerTask;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.HostInfo;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertyKey;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.exceptions.OperationCancelledException;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.a.NativeMessageBuilder;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryAttribute;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetryScope;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpan;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.telemetry.TelemetrySpanName;

public class CancelQueryTaskImpl extends TimerTask implements CancelQueryTask {
   Query queryToCancel;
   Throwable caughtWhileCancelling = null;
   boolean queryTimeoutKillsConnection = false;

   public CancelQueryTaskImpl(Query cancellee) {
      this.queryToCancel = cancellee;
      NativeSession session = (NativeSession)cancellee.getSession();
      this.queryTimeoutKillsConnection = session.getPropertySet().getBooleanProperty(PropertyKey.queryTimeoutKillsConnection).getValue();
   }

   @Override
   public boolean cancel() {
      boolean res = super.cancel();
      this.queryToCancel = null;
      return res;
   }

   @Override
   public void run() {
      Thread cancelThread = new Thread() {
         @Override
         public void run() {
            Query localQueryToCancel = CancelQueryTaskImpl.this.queryToCancel;
            if (localQueryToCancel != null) {
               NativeSession session = (NativeSession)localQueryToCancel.getSession();
               if (session != null) {
                  try {
                     if (CancelQueryTaskImpl.this.queryTimeoutKillsConnection) {
                        localQueryToCancel.setCancelStatus(Query.CancelStatus.CANCELED_BY_TIMEOUT);
                        session.invokeCleanupListeners(new OperationCancelledException(Messages.getString("Statement.ConnectionKilledDueToTimeout")));
                     } else {
                        localQueryToCancel.getCancelTimeoutLock().lock();

                        try {
                           long origConnId = session.getThreadId();
                           HostInfo hostInfo = session.getHostInfo();
                           String database = hostInfo.getDatabase();
                           String user = hostInfo.getUser();
                           String password = hostInfo.getPassword();
                           NativeSession newSession = null;

                           try {
                              newSession = new NativeSession(hostInfo, session.getPropertySet());
                              TelemetrySpan span = newSession.getTelemetryHandler().startSpan(TelemetrySpanName.CANCEL_QUERY);

                              try (TelemetryScope scope = span.makeCurrent()) {
                                 span.setAttribute(TelemetryAttribute.DB_NAME, database);
                                 span.setAttribute(TelemetryAttribute.DB_OPERATION, "KILL");
                                 span.setAttribute(TelemetryAttribute.DB_STATEMENT, "KILL (...)");
                                 span.setAttribute(TelemetryAttribute.DB_SYSTEM, "mysql");
                                 span.setAttribute(TelemetryAttribute.DB_USER, user);
                                 span.setAttribute(TelemetryAttribute.THREAD_ID, Thread.currentThread().getId());
                                 span.setAttribute(TelemetryAttribute.THREAD_NAME, Thread.currentThread().getName());
                                 newSession.connect(hostInfo, user, password, database, 30000, new TransactionEventHandler() {
                                    @Override
                                    public void transactionCompleted() {
                                    }

                                    @Override
                                    public void transactionBegun() {
                                    }
                                 });
                                 newSession.getProtocol()
                                    .sendCommand(
                                       new NativeMessageBuilder(newSession.getServerSession().supportsQueryAttributes())
                                          .buildComQuery(newSession.getSharedSendPacket(), newSession, "KILL QUERY " + origConnId),
                                       false,
                                       0
                                    );
                              } catch (Throwable var83) {
                                 span.setError(var83);
                                 throw var83;
                              } finally {
                                 span.end();
                              }
                           } finally {
                              try {
                                 newSession.forceClose();
                              } catch (Throwable var79) {
                              }
                           }

                           localQueryToCancel.setCancelStatus(Query.CancelStatus.CANCELED_BY_TIMEOUT);
                        } finally {
                           localQueryToCancel.getCancelTimeoutLock().unlock();
                        }
                     }
                  } catch (Throwable var87) {
                     CancelQueryTaskImpl.this.caughtWhileCancelling = var87;
                  } finally {
                     CancelQueryTaskImpl.this.setQueryToCancel(null);
                  }
               }
            }
         }
      };
      cancelThread.start();
   }

   @Override
   public Throwable getCaughtWhileCancelling() {
      return this.caughtWhileCancelling;
   }

   @Override
   public void setCaughtWhileCancelling(Throwable caughtWhileCancelling) {
      this.caughtWhileCancelling = caughtWhileCancelling;
   }

   @Override
   public Query getQueryToCancel() {
      return this.queryToCancel;
   }

   @Override
   public void setQueryToCancel(Query queryToCancel) {
      this.queryToCancel = queryToCancel;
   }
}
