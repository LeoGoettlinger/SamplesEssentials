package org.lushplugins.lushrewards.libraries.mysql.cj;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.HostInfo;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.log.Log;
import org.lushplugins.lushrewards.libraries.mysql.cj.log.ProfilerEventHandler;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ResultBuilder;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ServerSession;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.Row;

public interface Session {
   PropertySet getPropertySet();

   <M extends Message> MessageBuilder<M> getMessageBuilder();

   void changeUser(String var1, String var2, String var3);

   ExceptionInterceptor getExceptionInterceptor();

   void setExceptionInterceptor(ExceptionInterceptor var1);

   void quit();

   void forceClose();

   boolean versionMeetsMinimum(int var1, int var2, int var3);

   long getThreadId();

   boolean isSetNeededForAutoCommitMode(boolean var1);

   Log getLog();

   ProfilerEventHandler getProfilerEventHandler();

   HostInfo getHostInfo();

   String getQueryTimingUnits();

   ServerSession getServerSession();

   boolean isSSLEstablished();

   SocketAddress getRemoteSocketAddress();

   String getProcessHost();

   void addListener(Session.SessionEventListener var1);

   void removeListener(Session.SessionEventListener var1);

   boolean isClosed();

   String getIdentifierQuoteString();

   DataStoreMetadata getDataStoreMetadata();

   default <M extends Message, R, RES> RES query(M message, Predicate<Row> rowFilter, Function<Row, R> rowMapper, Collector<R, ?, RES> collector) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default <M extends Message, R extends QueryResult> R query(M message, ResultBuilder<R> resultBuilder) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   default <M extends Message, R extends QueryResult> CompletableFuture<R> queryAsync(M message, ResultBuilder<R> resultBuilder) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   public interface SessionEventListener {
      void handleNormalClose();

      void handleReconnect();

      void handleCleanup(Throwable var1);
   }
}
