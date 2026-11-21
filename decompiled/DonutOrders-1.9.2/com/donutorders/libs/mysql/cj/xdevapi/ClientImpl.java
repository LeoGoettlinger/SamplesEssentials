package com.donutorders.libs.mysql.cj.xdevapi;

import com.donutorders.libs.mysql.cj.Messages;
import com.donutorders.libs.mysql.cj.conf.BooleanPropertyDefinition;
import com.donutorders.libs.mysql.cj.conf.ConnectionUrl;
import com.donutorders.libs.mysql.cj.conf.DefaultPropertySet;
import com.donutorders.libs.mysql.cj.conf.HostInfo;
import com.donutorders.libs.mysql.cj.conf.IntegerPropertyDefinition;
import com.donutorders.libs.mysql.cj.conf.PropertySet;
import com.donutorders.libs.mysql.cj.exceptions.CJCommunicationsException;
import com.donutorders.libs.mysql.cj.exceptions.CJException;
import com.donutorders.libs.mysql.cj.exceptions.ExceptionFactory;
import com.donutorders.libs.mysql.cj.exceptions.WrongArgumentException;
import com.donutorders.libs.mysql.cj.protocol.Protocol;
import com.donutorders.libs.mysql.cj.protocol.x.XProtocol;
import com.donutorders.libs.mysql.cj.protocol.x.XProtocolError;
import com.donutorders.libs.mysql.cj.util.StringUtils;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientImpl implements Client, Protocol.ProtocolEventListener {
   private final ClientImpl.PooledXProtocol poisonProtocolMarker = new ClientImpl.PooledXProtocol();
   private boolean isClosed = false;
   private ConnectionUrl connUrl = null;
   private boolean poolingEnabled = true;
   private int maxSize = 25;
   private int maxIdleTime = 0;
   private int queueTimeout = 0;
   private Set<WeakReference<Session>> nonPooledSessions = null;
   private int demotedTimeout = 120000;
   private ConcurrentMap<HostInfo, Long> demotedHosts = null;
   private Set<WeakReference<ClientImpl.PooledXProtocol>> activeProtocols = null;
   private BlockingQueue<ClientImpl.PooledXProtocol> idleProtocols = null;
   private Semaphore availableProtocols;
   private ReadWriteLock clientShutdownLock;
   private SessionFactory sessionFactory = new SessionFactory();

   public ClientImpl(String url, String clientPropsJson) {
      Properties clientProps = StringUtils.isNullOrEmpty(clientPropsJson) ? new Properties() : this.clientPropsFromJson(clientPropsJson);
      this.init(url, clientProps);
   }

   public ClientImpl(String url, Properties clientProps) {
      this.init(url, clientProps != null ? clientProps : new Properties());
   }

   private Properties clientPropsFromJson(String clientPropsJson) {
      Properties props = new Properties();
      DbDoc clientPropsDoc = JsonParser.parseDoc(clientPropsJson);
      JsonValue pooling = clientPropsDoc.remove("pooling");
      if (pooling != null) {
         if (!DbDoc.class.isAssignableFrom(pooling.getClass())) {
            throw new XDevAPIError(String.format("Client option 'pooling' does not support value '%s'.", pooling.toFormattedString()));
         }

         DbDoc poolingDoc = (DbDoc)pooling;
         JsonValue jsonVal = poolingDoc.remove("enabled");
         if (jsonVal != null) {
            if (!JsonLiteral.class.isAssignableFrom(jsonVal.getClass())) {
               if (JsonString.class.isAssignableFrom(jsonVal.getClass())) {
                  throw new XDevAPIError(
                     String.format(
                        "Client option '%s' does not support value '%s'.",
                        Client.ClientProperty.POOLING_ENABLED.getKeyName(),
                        ((JsonString)jsonVal).getString()
                     )
                  );
               }

               throw new XDevAPIError(
                  String.format(
                     "Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_ENABLED.getKeyName(), jsonVal.toFormattedString()
                  )
               );
            }

            JsonLiteral pe = (JsonLiteral)jsonVal;
            if (pe != JsonLiteral.FALSE && pe != JsonLiteral.TRUE) {
               throw new XDevAPIError(
                  String.format(
                     "Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_ENABLED.getKeyName(), jsonVal.toFormattedString()
                  )
               );
            }

            props.setProperty(Client.ClientProperty.POOLING_ENABLED.getKeyName(), pe.value);
         }

         jsonVal = poolingDoc.remove("maxSize");
         if (jsonVal != null) {
            if (!JsonNumber.class.isAssignableFrom(jsonVal.getClass())) {
               if (JsonString.class.isAssignableFrom(jsonVal.getClass())) {
                  throw new XDevAPIError(
                     String.format(
                        "Client option '%s' does not support value '%s'.",
                        Client.ClientProperty.POOLING_MAX_SIZE.getKeyName(),
                        ((JsonString)jsonVal).getString()
                     )
                  );
               }

               throw new XDevAPIError(
                  String.format(
                     "Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_MAX_SIZE.getKeyName(), jsonVal.toFormattedString()
                  )
               );
            }

            props.setProperty(Client.ClientProperty.POOLING_MAX_SIZE.getKeyName(), ((JsonNumber)jsonVal).toString());
         }

         jsonVal = poolingDoc.remove("maxIdleTime");
         if (jsonVal != null) {
            if (!JsonNumber.class.isAssignableFrom(jsonVal.getClass())) {
               if (JsonString.class.isAssignableFrom(jsonVal.getClass())) {
                  throw new XDevAPIError(
                     String.format(
                        "Client option '%s' does not support value '%s'.",
                        Client.ClientProperty.POOLING_MAX_IDLE_TIME.getKeyName(),
                        ((JsonString)jsonVal).getString()
                     )
                  );
               }

               throw new XDevAPIError(
                  String.format(
                     "Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_MAX_IDLE_TIME.getKeyName(), jsonVal.toFormattedString()
                  )
               );
            }

            props.setProperty(Client.ClientProperty.POOLING_MAX_IDLE_TIME.getKeyName(), ((JsonNumber)jsonVal).toString());
         }

         jsonVal = poolingDoc.remove("queueTimeout");
         if (jsonVal != null) {
            if (!JsonNumber.class.isAssignableFrom(jsonVal.getClass())) {
               if (JsonString.class.isAssignableFrom(jsonVal.getClass())) {
                  throw new XDevAPIError(
                     String.format(
                        "Client option '%s' does not support value '%s'.",
                        Client.ClientProperty.POOLING_QUEUE_TIMEOUT.getKeyName(),
                        ((JsonString)jsonVal).getString()
                     )
                  );
               }

               throw new XDevAPIError(
                  String.format(
                     "Client option '%s' does not support value '%s'.", Client.ClientProperty.POOLING_QUEUE_TIMEOUT.getKeyName(), jsonVal.toFormattedString()
                  )
               );
            }

            props.setProperty(Client.ClientProperty.POOLING_QUEUE_TIMEOUT.getKeyName(), ((JsonNumber)jsonVal).toString());
         }

         if (poolingDoc.size() > 0) {
            String key = poolingDoc.keySet().stream().findFirst().get();
            throw new XDevAPIError(String.format("Client option 'pooling.%s' is not recognized as valid.", key));
         }
      }

      if (!clientPropsDoc.isEmpty()) {
         String key = clientPropsDoc.keySet().stream().findFirst().get();
         throw new XDevAPIError(String.format("Client option '%s' is not recognized as valid.", key));
      } else {
         return props;
      }
   }

   private void validateAndInitializeClientProps(Properties clientProps) {
      String propKey = "";
      String propValue = "";
      propKey = Client.ClientProperty.POOLING_ENABLED.getKeyName();
      if (clientProps.containsKey(propKey)) {
         propValue = clientProps.getProperty(propKey);

         try {
            this.poolingEnabled = BooleanPropertyDefinition.booleanFrom(propKey, propValue, null);
         } catch (CJException var8) {
            throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue), var8);
         }
      }

      propKey = Client.ClientProperty.POOLING_MAX_SIZE.getKeyName();
      if (clientProps.containsKey(propKey)) {
         propValue = clientProps.getProperty(propKey);

         try {
            this.maxSize = IntegerPropertyDefinition.integerFrom(propKey, propValue, 1, null);
         } catch (WrongArgumentException var7) {
            throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue), var7);
         }

         if (this.maxSize <= 0) {
            throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue));
         }
      }

      propKey = Client.ClientProperty.POOLING_MAX_IDLE_TIME.getKeyName();
      if (clientProps.containsKey(propKey)) {
         propValue = clientProps.getProperty(propKey);

         try {
            this.maxIdleTime = IntegerPropertyDefinition.integerFrom(propKey, propValue, 1, null);
         } catch (WrongArgumentException var6) {
            throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue), var6);
         }

         if (this.maxIdleTime < 0) {
            throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue));
         }
      }

      propKey = Client.ClientProperty.POOLING_QUEUE_TIMEOUT.getKeyName();
      if (clientProps.containsKey(propKey)) {
         propValue = clientProps.getProperty(propKey);

         try {
            this.queueTimeout = IntegerPropertyDefinition.integerFrom(propKey, propValue, 1, null);
         } catch (WrongArgumentException var5) {
            throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue), var5);
         }

         if (this.queueTimeout < 0) {
            throw new XDevAPIError(String.format("Client option '%s' does not support value '%s'.", propKey, propValue));
         }
      }

      List<String> clientPropsAsString = Stream.of(Client.ClientProperty.values()).map(Client.ClientProperty::getKeyName).collect(Collectors.toList());
      propKey = (String)clientProps.keySet().stream().filter(k -> !clientPropsAsString.contains(k)).findFirst().orElse(null);
      if (propKey != null) {
         throw new XDevAPIError(String.format("Client option '%s' is not recognized as valid.", propKey));
      }
   }

   private void init(String url, Properties clientProps) {
      this.connUrl = this.sessionFactory.parseUrl(url);
      this.validateAndInitializeClientProps(clientProps);
      if (this.poolingEnabled) {
         this.demotedHosts = new ConcurrentHashMap<>();
         this.activeProtocols = new CopyOnWriteArraySet<>();
         this.idleProtocols = new LinkedBlockingQueue<>(this.maxSize);
         this.availableProtocols = new Semaphore(this.maxSize, true);
      } else {
         this.nonPooledSessions = new CopyOnWriteArraySet<>();
      }

      this.clientShutdownLock = new ReentrantReadWriteLock(true);
   }

   @Override
   public Session getSession() {
      return this.poolingEnabled ? this.getPooledSession() : this.getNonPooledSession();
   }

   private Session getNonPooledSession() {
      if (this.isClosed) {
         throw new XDevAPIError("Client is closed.");
      } else {
         this.clientShutdownLock.readLock().lock();

         Session var8;
         try {
            for (WeakReference<Session> ws : this.nonPooledSessions) {
               Session s = ws.get();
               if (s == null || !s.isOpen()) {
                  this.nonPooledSessions.remove(ws);
               }
            }

            Session sess = this.sessionFactory.getSession(this.connUrl);
            this.nonPooledSessions.add(new WeakReference<>(sess));
            var8 = sess;
         } finally {
            this.clientShutdownLock.readLock().unlock();
         }

         return var8;
      }
   }

   private Session getPooledSession() {
      ClientImpl.PooledXProtocol protocol = null;
      List<HostInfo> hostsList = this.connUrl.getHostsList();
      long startTime = System.currentTimeMillis();

      while (protocol == null) {
         if (this.isClosed) {
            throw new XDevAPIError("Client is closed.");
         }

         if (this.queueTimeout != 0 && System.currentTimeMillis() > startTime + this.queueTimeout) {
            if (this.availableProtocols.tryAcquire()) {
               protocol = this.newPooledXProtocol(hostsList);
            }

            if (protocol == null) {
               throw new XDevAPIError("Session can not be obtained within " + this.queueTimeout + " milliseconds.");
            }
         }

         if ((protocol = this.idleProtocols.poll()) != null) {
            protocol = this.validateAndResetPooledXProtocol(protocol, hostsList);
         } else if (this.availableProtocols.tryAcquire()) {
            protocol = this.newPooledXProtocol(hostsList);
         } else if (this.queueTimeout > 0) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long remainingTime = this.queueTimeout - elapsedTime;

            try {
               if (remainingTime > 0L) {
                  protocol = this.idleProtocols.poll(remainingTime, TimeUnit.MILLISECONDS);
                  protocol = this.validateAndResetPooledXProtocol(protocol, hostsList);
               }
            } catch (InterruptedException var15) {
               throw new XDevAPIError("Session can not be obtained within " + this.queueTimeout + " milliseconds.", var15);
            }
         } else {
            try {
               protocol = this.idleProtocols.take();
               this.validateAndResetPooledXProtocol(protocol, hostsList);
            } catch (InterruptedException var14) {
               throw new XDevAPIError("Session can not be obtained.", var14);
            }
         }
      }

      this.clientShutdownLock.readLock().lock();

      try {
         if (this.isClosed) {
            throw new XDevAPIError("Client is closed.");
         }

         this.activeProtocols.add(new WeakReference<>(protocol));
      } finally {
         this.clientShutdownLock.readLock().unlock();
      }

      return new SessionImpl(protocol);
   }

   private ClientImpl.PooledXProtocol newPooledXProtocol(List<HostInfo> hostsList) {
      ClientImpl.PooledXProtocol protocol = null;
      CJException latestException = null;
      List<HostInfo> hostsToRetryAfterwards = new ArrayList<>();

      for (HostInfo hi : hostsList) {
         if (this.demotedHosts.containsKey(hi)) {
            if (System.currentTimeMillis() - this.demotedHosts.get(hi) <= this.demotedTimeout) {
               hostsToRetryAfterwards.add(hi);
               continue;
            }

            this.demotedHosts.remove(hi);
         }

         try {
            protocol = this.newPooledXProtocol(hi);
            break;
         } catch (CJCommunicationsException var9) {
            if (var9.getCause() == null) {
               this.availableProtocols.release();
               throw var9;
            }

            latestException = var9;
            this.demotedHosts.put(hi, System.currentTimeMillis());
         }
      }

      if (protocol == null) {
         for (HostInfo hi : hostsToRetryAfterwards) {
            try {
               protocol = this.newPooledXProtocol(hi);
               this.demotedHosts.remove(hi);
               break;
            } catch (CJCommunicationsException var8) {
               if (var8.getCause() == null) {
                  this.availableProtocols.release();
                  throw var8;
               }

               latestException = var8;
               this.demotedHosts.put(hi, System.currentTimeMillis());
            }
         }
      }

      if (protocol == null) {
         this.availableProtocols.release();
         if (latestException != null) {
            throw (CJCommunicationsException)ExceptionFactory.createException(
               CJCommunicationsException.class, Messages.getString("Session.Create.Failover.0"), latestException
            );
         }
      }

      return protocol;
   }

   private ClientImpl.PooledXProtocol newPooledXProtocol(HostInfo hi) {
      PropertySet pset = new DefaultPropertySet();
      pset.initializeProperties(hi.exposeAsProperties());
      ClientImpl.PooledXProtocol protocol = new ClientImpl.PooledXProtocol(hi, pset, this.maxIdleTime);
      protocol.addListener(this);
      protocol.connect(hi.getUser(), hi.getPassword(), hi.getDatabase());
      return protocol;
   }

   private ClientImpl.PooledXProtocol validateAndResetPooledXProtocol(ClientImpl.PooledXProtocol protocol, List<HostInfo> hostsList) {
      if (protocol == null) {
         return null;
      } else if (protocol == this.poisonProtocolMarker) {
         this.idleProtocols.add(this.poisonProtocolMarker);
         throw new XDevAPIError("Session can not be obtained. Client instance is closing.");
      } else if (!protocol.isOpen()) {
         this.availableProtocols.release();
         return null;
      } else if (!protocol.isHostInfoValid(hostsList)) {
         this.availableProtocols.release();
         this.demotedHosts.remove(protocol.getHostInfo());
         protocol.realClose();
         return null;
      } else if (protocol.isIdleTimeoutReached()) {
         this.availableProtocols.release();
         protocol.realClose();
         return null;
      } else {
         try {
            protocol.reset();
            return protocol;
         } catch (XProtocolError | CJCommunicationsException var4) {
            this.availableProtocols.release();
            return null;
         }
      }
   }

   @Override
   public void close() {
      this.clientShutdownLock.writeLock().lock();

      try {
         if (!this.isClosed) {
            this.isClosed = true;
            if (this.poolingEnabled) {
               this.availableProtocols.drainPermits();
               this.idleProtocols.forEach(ClientImpl.PooledXProtocol::realClose);
               this.idleProtocols.clear();
               this.idleProtocols.add(this.poisonProtocolMarker);
               this.activeProtocols.stream().map(Reference::get).filter(Objects::nonNull).forEach(ClientImpl.PooledXProtocol::realClose);
               this.activeProtocols.clear();
            } else {
               this.nonPooledSessions.stream().map(Reference::get).filter(Objects::nonNull).filter(Session::isOpen).forEach(Session::close);
            }
         }
      } finally {
         this.clientShutdownLock.writeLock().unlock();
      }
   }

   void idleProtocol(ClientImpl.PooledXProtocol protocol) {
      if (!this.isClosed) {
         for (WeakReference<ClientImpl.PooledXProtocol> protocolReference : this.activeProtocols) {
            ClientImpl.PooledXProtocol referencedProtocol = protocolReference.get();
            if (referencedProtocol == null) {
               if (this.activeProtocols.remove(protocolReference)) {
                  this.availableProtocols.release();
               }
            } else if (referencedProtocol == protocol) {
               this.activeProtocols.remove(protocolReference);
               this.idleProtocols.add(referencedProtocol);
            }
         }
      }
   }

   @Override
   public void handleEvent(Protocol.ProtocolEventListener.EventType type, Object info, Throwable reason) {
      switch (type) {
         case SERVER_SHUTDOWN:
            HostInfo hi = ((ClientImpl.PooledXProtocol)info).getHostInfo();
            this.clientShutdownLock.writeLock().lock();

            try {
               List<ClientImpl.PooledXProtocol> toCloseAndRemove = this.idleProtocols
                  .stream()
                  .filter(p -> p.getHostInfo().equalHostPortPair(hi))
                  .collect(Collectors.toList());
               toCloseAndRemove.stream()
                  .peek(ClientImpl.PooledXProtocol::realClose)
                  .peek(this.idleProtocols::remove)
                  .map(ClientImpl.PooledXProtocol::getHostInfo)
                  .sequential()
                  .forEach(this.demotedHosts::remove);
               this.removeActivePooledXProtocol((ClientImpl.PooledXProtocol)info);
               break;
            } finally {
               this.clientShutdownLock.writeLock().unlock();
            }
         case SERVER_CLOSED_SESSION:
            this.clientShutdownLock.writeLock().lock();

            try {
               this.removeActivePooledXProtocol((ClientImpl.PooledXProtocol)info);
            } finally {
               this.clientShutdownLock.writeLock().unlock();
            }
      }
   }

   private void removeActivePooledXProtocol(ClientImpl.PooledXProtocol protocol) {
      for (WeakReference<ClientImpl.PooledXProtocol> protocolReference : this.activeProtocols) {
         ClientImpl.PooledXProtocol referencedProtocol = protocolReference.get();
         if (referencedProtocol == protocol) {
            if (this.activeProtocols.remove(protocolReference)) {
               this.availableProtocols.release();
            }

            protocol.realClose();
            return;
         }
      }
   }

   public class PooledXProtocol extends XProtocol {
      private int maxIdleTime = -1;
      private long idleSince = -1L;
      private HostInfo hostInfo = null;

      PooledXProtocol() {
         super(null, null);
      }

      PooledXProtocol(HostInfo hostInfo, PropertySet propertySet, int maxIdleTime) {
         super(hostInfo, propertySet);
         this.hostInfo = hostInfo;
         this.maxIdleTime = maxIdleTime;
      }

      @Override
      public void close() {
         this.reset();
         this.idleSince = System.currentTimeMillis();
         ClientImpl.this.idleProtocol(this);
      }

      HostInfo getHostInfo() {
         return this.hostInfo;
      }

      boolean isIdleTimeoutReached() {
         return this.maxIdleTime > 0 && this.idleSince > 0L && System.currentTimeMillis() > this.idleSince + this.maxIdleTime;
      }

      boolean isHostInfoValid(List<HostInfo> hostsList) {
         return hostsList.stream().filter(h -> h.equalHostPortPair(this.hostInfo)).findFirst().isPresent();
      }

      void realClose() {
         try {
            super.close();
         } catch (IOException var2) {
            throw new CJCommunicationsException(var2);
         }
      }
   }
}
