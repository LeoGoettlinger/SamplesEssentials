package org.lushplugins.lushrewards.libraries.mysql.cj;

import java.net.SocketAddress;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.HostInfo;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertyKey;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.RuntimeProperty;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.CJOperationNotSupportedException;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.exceptions.ExceptionInterceptor;
import org.lushplugins.lushrewards.libraries.mysql.cj.log.Log;
import org.lushplugins.lushrewards.libraries.mysql.cj.log.LogFactory;
import org.lushplugins.lushrewards.libraries.mysql.cj.log.NullLogger;
import org.lushplugins.lushrewards.libraries.mysql.cj.log.ProfilerEventHandler;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Message;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.Protocol;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ServerSession;
import org.lushplugins.lushrewards.libraries.mysql.cj.util.Util;

public abstract class CoreSession implements Session {
   protected PropertySet propertySet;
   protected ExceptionInterceptor exceptionInterceptor;
   protected transient Log log;
   protected static final Log NULL_LOGGER = new NullLogger("MySQL");
   protected transient Protocol<? extends Message> protocol;
   protected MessageBuilder<? extends Message> messageBuilder;
   protected long connectionCreationTimeMillis = 0L;
   protected HostInfo hostInfo = null;
   protected RuntimeProperty<Boolean> gatherPerfMetrics;
   protected RuntimeProperty<String> characterEncoding;
   protected RuntimeProperty<Boolean> disconnectOnExpiredPasswords;
   protected RuntimeProperty<Boolean> cacheServerConfiguration;
   protected RuntimeProperty<Boolean> autoReconnect;
   protected RuntimeProperty<Boolean> autoReconnectForPools;
   protected RuntimeProperty<Boolean> maintainTimeStats;
   protected int sessionMaxRows = -1;
   private ProfilerEventHandler eventSink;

   public CoreSession(HostInfo hostInfo, PropertySet propSet) {
      this.connectionCreationTimeMillis = System.currentTimeMillis();
      this.hostInfo = hostInfo;
      this.propertySet = propSet;
      this.gatherPerfMetrics = this.getPropertySet().getBooleanProperty(PropertyKey.gatherPerfMetrics);
      this.characterEncoding = this.getPropertySet().getStringProperty(PropertyKey.characterEncoding);
      this.disconnectOnExpiredPasswords = this.getPropertySet().getBooleanProperty(PropertyKey.disconnectOnExpiredPasswords);
      this.cacheServerConfiguration = this.getPropertySet().getBooleanProperty(PropertyKey.cacheServerConfiguration);
      this.autoReconnect = this.getPropertySet().getBooleanProperty(PropertyKey.autoReconnect);
      this.autoReconnectForPools = this.getPropertySet().getBooleanProperty(PropertyKey.autoReconnectForPools);
      this.maintainTimeStats = this.getPropertySet().getBooleanProperty(PropertyKey.maintainTimeStats);
      this.log = LogFactory.getLogger(this.getPropertySet().getStringProperty(PropertyKey.logger).getStringValue(), "MySQL");
   }

   @Override
   public void changeUser(String user, String password, String database) {
      this.sessionMaxRows = -1;
      this.protocol.changeUser(user, password, database);
   }

   @Override
   public PropertySet getPropertySet() {
      return this.propertySet;
   }

   @Override
   public ExceptionInterceptor getExceptionInterceptor() {
      return this.exceptionInterceptor;
   }

   @Override
   public void setExceptionInterceptor(ExceptionInterceptor exceptionInterceptor) {
      this.exceptionInterceptor = exceptionInterceptor;
   }

   @Override
   public Log getLog() {
      return this.log;
   }

   @Override
   public HostInfo getHostInfo() {
      return this.hostInfo;
   }

   @Override
   public <M extends Message> MessageBuilder<M> getMessageBuilder() {
      return (MessageBuilder<M>)this.messageBuilder;
   }

   @Override
   public ServerSession getServerSession() {
      return this.protocol.getServerSession();
   }

   @Override
   public boolean versionMeetsMinimum(int major, int minor, int subminor) {
      return this.protocol.versionMeetsMinimum(major, minor, subminor);
   }

   @Override
   public long getThreadId() {
      return this.protocol.getServerSession().getCapabilities().getThreadId();
   }

   @Override
   public void quit() {
      if (this.eventSink != null) {
         this.eventSink.destroy();
         this.eventSink = null;
      }
   }

   @Override
   public void forceClose() {
      if (this.eventSink != null) {
         this.eventSink.destroy();
         this.eventSink = null;
      }
   }

   @Override
   public boolean isSetNeededForAutoCommitMode(boolean autoCommitFlag) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public ProfilerEventHandler getProfilerEventHandler() {
      if (this.eventSink == null) {
         synchronized (this) {
            if (this.eventSink == null) {
               this.eventSink = Util.getInstance(
                  ProfilerEventHandler.class,
                  this.propertySet.getStringProperty(PropertyKey.profilerEventHandler).getStringValue(),
                  null,
                  null,
                  this.exceptionInterceptor
               );
               this.eventSink.init(this.log);
            }
         }
      }

      return this.eventSink;
   }

   @Override
   public boolean isSSLEstablished() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public SocketAddress getRemoteSocketAddress() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void addListener(Session.SessionEventListener l) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void removeListener(Session.SessionEventListener l) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public String getIdentifierQuoteString() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public DataStoreMetadata getDataStoreMetadata() {
      return new DataStoreMetadataImpl(this);
   }

   @Override
   public String getQueryTimingUnits() {
      return this.protocol.getQueryTimingUnits();
   }
}
