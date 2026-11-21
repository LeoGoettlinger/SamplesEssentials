package de.theredend2000.advancedhunt.lib.hikari.pool;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import de.theredend2000.advancedhunt.lib.hikari.HikariConfig;
import de.theredend2000.advancedhunt.lib.hikari.HikariPoolMXBean;
import de.theredend2000.advancedhunt.lib.hikari.metrics.MetricsTrackerFactory;
import de.theredend2000.advancedhunt.lib.hikari.metrics.PoolStats;
import de.theredend2000.advancedhunt.lib.hikari.metrics.dropwizard.CodahaleHealthChecker;
import de.theredend2000.advancedhunt.lib.hikari.metrics.dropwizard.CodahaleMetricsTrackerFactory;
import de.theredend2000.advancedhunt.lib.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory;
import de.theredend2000.advancedhunt.lib.hikari.util.ClockSource;
import de.theredend2000.advancedhunt.lib.hikari.util.ConcurrentBag;
import de.theredend2000.advancedhunt.lib.hikari.util.SuspendResumeLock;
import de.theredend2000.advancedhunt.lib.hikari.util.UtilityElf;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HikariPool extends PoolBase implements HikariPoolMXBean, ConcurrentBag.IBagStateListener {
   private final Logger logger = LoggerFactory.getLogger(HikariPool.class);
   public static final int POOL_NORMAL = 0;
   public static final int POOL_SUSPENDED = 1;
   public static final int POOL_SHUTDOWN = 2;
   public volatile int poolState;
   private final long aliveBypassWindowMs = Long.getLong("de.theredend2000.advancedhunt.lib.hikari.aliveBypassWindowMs", TimeUnit.MILLISECONDS.toMillis(500L));
   private final long housekeepingPeriodMs = Long.getLong("de.theredend2000.advancedhunt.lib.hikari.housekeeping.periodMs", TimeUnit.SECONDS.toMillis(30L));
   private static final String EVICTED_CONNECTION_MESSAGE = "(connection was evicted)";
   private static final String DEAD_CONNECTION_MESSAGE = "(connection is dead)";
   private final HikariPool.PoolEntryCreator poolEntryCreator = new HikariPool.PoolEntryCreator();
   private final HikariPool.PoolEntryCreator postFillPoolEntryCreator = new HikariPool.PoolEntryCreator("After adding ");
   private final AtomicInteger addConnectionQueueDepth = new AtomicInteger();
   private final ThreadPoolExecutor addConnectionExecutor;
   private final ThreadPoolExecutor closeConnectionExecutor;
   private final ConcurrentBag<PoolEntry> connectionBag = new ConcurrentBag<>(this);
   private final ProxyLeakTaskFactory leakTaskFactory;
   private final SuspendResumeLock suspendResumeLock;
   private final ScheduledExecutorService houseKeepingExecutorService;
   private ScheduledFuture<?> houseKeeperTask;

   public HikariPool(HikariConfig config) {
      super(config);
      this.suspendResumeLock = config.isAllowPoolSuspension() ? new SuspendResumeLock() : SuspendResumeLock.FAUX_LOCK;
      this.houseKeepingExecutorService = this.initializeHouseKeepingExecutorService();
      this.checkFailFast();
      if (config.getMetricsTrackerFactory() != null) {
         this.setMetricsTrackerFactory(config.getMetricsTrackerFactory());
      } else {
         this.setMetricRegistry(config.getMetricRegistry());
      }

      this.setHealthCheckRegistry(config.getHealthCheckRegistry());
      this.handleMBeans(this, true);
      ThreadFactory threadFactory = config.getThreadFactory();
      int maxPoolSize = config.getMaximumPoolSize();
      LinkedBlockingQueue<Runnable> addConnectionQueue = new LinkedBlockingQueue<>(16);
      this.addConnectionExecutor = UtilityElf.createThreadPoolExecutor(
         addConnectionQueue, this.poolName + " connection adder", threadFactory, new HikariPool.CustomDiscardPolicy()
      );
      this.closeConnectionExecutor = UtilityElf.createThreadPoolExecutor(
         maxPoolSize, this.poolName + " connection closer", threadFactory, new CallerRunsPolicy()
      );
      this.leakTaskFactory = new ProxyLeakTaskFactory(config.getLeakDetectionThreshold(), this.houseKeepingExecutorService);
      this.houseKeeperTask = this.houseKeepingExecutorService
         .scheduleWithFixedDelay(new HikariPool.HouseKeeper(), 100L, this.housekeepingPeriodMs, TimeUnit.MILLISECONDS);
      if (Boolean.getBoolean("de.theredend2000.advancedhunt.lib.hikari.blockUntilFilled") && config.getInitializationFailTimeout() > 1L) {
         this.addConnectionExecutor.setMaximumPoolSize(Math.min(16, Runtime.getRuntime().availableProcessors()));
         this.addConnectionExecutor.setCorePoolSize(Math.min(16, Runtime.getRuntime().availableProcessors()));
         long startTime = ClockSource.currentTime();

         while (ClockSource.elapsedMillis(startTime) < config.getInitializationFailTimeout() && this.getTotalConnections() < config.getMinimumIdle()) {
            UtilityElf.quietlySleep(TimeUnit.MILLISECONDS.toMillis(100L));
         }

         this.addConnectionExecutor.setCorePoolSize(1);
         this.addConnectionExecutor.setMaximumPoolSize(1);
      }
   }

   public Connection getConnection() throws SQLException {
      return this.getConnection(this.connectionTimeout);
   }

   public Connection getConnection(long hardTimeout) throws SQLException {
      this.suspendResumeLock.acquire();
      long startTime = ClockSource.currentTime();

      try {
         long timeout = hardTimeout;

         do {
            PoolEntry poolEntry = this.connectionBag.borrow(timeout, TimeUnit.MILLISECONDS);
            if (poolEntry == null) {
               break;
            }

            long now = ClockSource.currentTime();
            if (!poolEntry.isMarkedEvicted()
               && (ClockSource.elapsedMillis(poolEntry.lastAccessed, now) <= this.aliveBypassWindowMs || !this.isConnectionDead(poolEntry.connection))) {
               this.metricsTracker.recordBorrowStats(poolEntry, startTime);
               return poolEntry.createProxyConnection(this.leakTaskFactory.schedule(poolEntry));
            }

            this.closeConnection(poolEntry, poolEntry.isMarkedEvicted() ? "(connection was evicted)" : "(connection is dead)");
            timeout = hardTimeout - ClockSource.elapsedMillis(startTime);
         } while (timeout > 0L);

         this.metricsTracker.recordBorrowTimeoutStats(startTime);
         throw this.createTimeoutException(startTime);
      } catch (InterruptedException var14) {
         Thread.currentThread().interrupt();
         throw new SQLException(this.poolName + " - Interrupted during connection acquisition", var14);
      } finally {
         this.suspendResumeLock.release();
      }
   }

   public synchronized void shutdown() throws InterruptedException {
      try {
         this.poolState = 2;
         if (this.addConnectionExecutor != null) {
            this.logPoolState("Before shutdown ");
            if (this.houseKeeperTask != null) {
               this.houseKeeperTask.cancel(false);
               this.houseKeeperTask = null;
            }

            this.softEvictConnections();
            this.addConnectionExecutor.shutdown();
            if (!this.addConnectionExecutor.awaitTermination(this.getLoginTimeout(), TimeUnit.SECONDS)) {
               this.logger.warn("Timed-out waiting for add connection executor to shutdown");
            }

            this.destroyHouseKeepingExecutorService();
            this.connectionBag.close();
            ThreadPoolExecutor assassinExecutor = UtilityElf.createThreadPoolExecutor(
               this.config.getMaximumPoolSize(), this.poolName + " connection assassinator", this.config.getThreadFactory(), new CallerRunsPolicy()
            );

            try {
               long start = ClockSource.currentTime();

               do {
                  this.abortActiveConnections(assassinExecutor);
                  this.softEvictConnections();
               } while (this.getTotalConnections() > 0 && ClockSource.elapsedMillis(start) < TimeUnit.SECONDS.toMillis(10L));
            } finally {
               assassinExecutor.shutdown();
               if (!assassinExecutor.awaitTermination(10L, TimeUnit.SECONDS)) {
                  this.logger.warn("Timed-out waiting for connection assassin to shutdown");
               }
            }

            this.shutdownNetworkTimeoutExecutor();
            this.closeConnectionExecutor.shutdown();
            if (!this.closeConnectionExecutor.awaitTermination(10L, TimeUnit.SECONDS)) {
               this.logger.warn("Timed-out waiting for close connection executor to shutdown");
            }

            return;
         }
      } finally {
         this.logPoolState("After shutdown ");
         this.handleMBeans(this, false);
         this.metricsTracker.close();
      }
   }

   public void evictConnection(Connection connection) {
      ProxyConnection proxyConnection = (ProxyConnection)connection;
      proxyConnection.cancelLeakTask();

      try {
         this.softEvictConnection(proxyConnection.getPoolEntry(), "(connection evicted by user)", !connection.isClosed());
      } catch (SQLException var4) {
      }
   }

   public void setMetricRegistry(Object metricRegistry) {
      if (metricRegistry != null && UtilityElf.safeIsAssignableFrom(metricRegistry, "com.codahale.metrics.MetricRegistry")) {
         this.setMetricsTrackerFactory(new CodahaleMetricsTrackerFactory((MetricRegistry)metricRegistry));
      } else if (metricRegistry != null && UtilityElf.safeIsAssignableFrom(metricRegistry, "io.micrometer.core.instrument.MeterRegistry")) {
         this.setMetricsTrackerFactory(new MicrometerMetricsTrackerFactory((MeterRegistry)metricRegistry));
      } else {
         this.setMetricsTrackerFactory(null);
      }
   }

   public void setMetricsTrackerFactory(MetricsTrackerFactory metricsTrackerFactory) {
      if (metricsTrackerFactory != null) {
         this.metricsTracker = new PoolBase.MetricsTrackerDelegate(metricsTrackerFactory.create(this.config.getPoolName(), this.getPoolStats()));
      } else {
         this.metricsTracker = new PoolBase.NopMetricsTrackerDelegate();
      }
   }

   public void setHealthCheckRegistry(Object healthCheckRegistry) {
      if (healthCheckRegistry != null) {
         CodahaleHealthChecker.registerHealthChecks(this, this.config, (HealthCheckRegistry)healthCheckRegistry);
      }
   }

   @Override
   public void addBagItem(int waiting) {
      int queueDepth = this.addConnectionQueueDepth.get();
      int countToAdd = waiting - queueDepth;
      if (countToAdd >= 0) {
         this.addConnectionQueueDepth.incrementAndGet();
         this.addConnectionExecutor.submit(this.poolEntryCreator);
      } else {
         this.logger.debug("{} - Add connection elided, waiting={}, adders pending/running={}", this.poolName, waiting, queueDepth);
      }
   }

   @Override
   public int getActiveConnections() {
      return this.connectionBag.getCount(1);
   }

   @Override
   public int getIdleConnections() {
      return this.connectionBag.getCount(0);
   }

   @Override
   public int getTotalConnections() {
      return this.connectionBag.size();
   }

   @Override
   public int getThreadsAwaitingConnection() {
      return this.connectionBag.getWaitingThreadCount();
   }

   @Override
   public void softEvictConnections() {
      this.connectionBag.values().forEach(poolEntry -> this.softEvictConnection(poolEntry, "(connection evicted)", false));
   }

   @Override
   public synchronized void suspendPool() {
      if (this.suspendResumeLock == SuspendResumeLock.FAUX_LOCK) {
         throw new IllegalStateException(this.poolName + " - is not suspendable");
      } else {
         if (this.poolState != 1) {
            this.suspendResumeLock.suspend();
            this.poolState = 1;
         }
      }
   }

   @Override
   public synchronized void resumePool() {
      if (this.poolState == 1) {
         this.poolState = 0;
         this.fillPool(false);
         this.suspendResumeLock.resume();
      }
   }

   void logPoolState(String... prefix) {
      if (this.logger.isDebugEnabled()) {
         this.logger
            .debug(
               "{} - {}stats (total={}, active={}, idle={}, waiting={})",
               this.poolName,
               prefix.length > 0 ? prefix[0] : "",
               this.getTotalConnections(),
               this.getActiveConnections(),
               this.getIdleConnections(),
               this.getThreadsAwaitingConnection()
            );
      }
   }

   @Override
   void recycle(PoolEntry poolEntry) {
      this.metricsTracker.recordConnectionUsage(poolEntry);
      this.connectionBag.requite(poolEntry);
   }

   void closeConnection(PoolEntry poolEntry, String closureReason) {
      if (this.connectionBag.remove(poolEntry)) {
         Connection connection = poolEntry.close();
         this.closeConnectionExecutor.execute(() -> {
            this.quietlyCloseConnection(connection, closureReason);
            if (this.poolState == 0) {
               this.fillPool(false);
            }
         });
      }
   }

   int[] getPoolStateCounts() {
      return this.connectionBag.getStateCounts();
   }

   private PoolEntry createPoolEntry() {
      try {
         PoolEntry poolEntry = this.newPoolEntry();
         long maxLifetime = this.config.getMaxLifetime();
         if (maxLifetime > 0L) {
            long variance = maxLifetime > 10000L ? ThreadLocalRandom.current().nextLong(maxLifetime / 40L) : 0L;
            long lifetime = maxLifetime - variance;
            poolEntry.setFutureEol(this.houseKeepingExecutorService.schedule(new HikariPool.MaxLifetimeTask(poolEntry), lifetime, TimeUnit.MILLISECONDS));
         }

         long keepaliveTime = this.config.getKeepaliveTime();
         if (keepaliveTime > 0L) {
            long variance = ThreadLocalRandom.current().nextLong(keepaliveTime / 10L);
            long heartbeatTime = keepaliveTime - variance;
            poolEntry.setKeepalive(
               this.houseKeepingExecutorService
                  .scheduleWithFixedDelay(new HikariPool.KeepaliveTask(poolEntry), heartbeatTime, heartbeatTime, TimeUnit.MILLISECONDS)
            );
         }

         return poolEntry;
      } catch (PoolBase.ConnectionSetupException var10) {
         if (this.poolState == 0) {
            this.logger.error("{} - Error thrown while acquiring connection from data source", this.poolName, var10.getCause());
            this.lastConnectionFailure.set(var10);
         }
      } catch (Exception var11) {
         if (this.poolState == 0) {
            this.logger.debug("{} - Cannot acquire connection from data source", this.poolName, var11);
         }
      }

      return null;
   }

   private synchronized void fillPool(boolean isAfterAdd) {
      int queueDepth = this.addConnectionQueueDepth.get();
      int countToAdd = this.connectionBag.getWaitingThreadCount() - queueDepth;
      boolean shouldAdd = this.getTotalConnections() < this.config.getMaximumPoolSize()
         && (this.getIdleConnections() < this.config.getMinimumIdle() || countToAdd > this.getIdleConnections());
      if (shouldAdd) {
         this.addConnectionQueueDepth.incrementAndGet();
         this.addConnectionExecutor.submit(isAfterAdd ? this.postFillPoolEntryCreator : this.poolEntryCreator);
      } else if (isAfterAdd) {
         this.logger.debug("{} - Fill pool skipped, pool has sufficient level or currently being filled (queueDepth={}).", this.poolName, queueDepth);
      }
   }

   private void abortActiveConnections(ExecutorService assassinExecutor) {
      for (PoolEntry poolEntry : this.connectionBag.values(1)) {
         Connection connection = poolEntry.close();

         try {
            connection.abort(assassinExecutor);
         } catch (Throwable var9) {
            this.quietlyCloseConnection(connection, "(connection aborted during shutdown)");
         } finally {
            this.connectionBag.remove(poolEntry);
         }
      }
   }

   private void checkFailFast() {
      long initializationTimeout = this.config.getInitializationFailTimeout();
      if (initializationTimeout >= 0L) {
         long startTime = ClockSource.currentTime();

         do {
            PoolEntry poolEntry = this.createPoolEntry();
            if (poolEntry != null) {
               if (this.config.getMinimumIdle() > 0) {
                  this.connectionBag.add(poolEntry);
                  this.logger.info("{} - Added connection {}", this.poolName, poolEntry.connection);
               } else {
                  this.quietlyCloseConnection(poolEntry.close(), "(initialization check complete and minimumIdle is zero)");
               }

               return;
            }

            if (this.getLastConnectionFailure() instanceof PoolBase.ConnectionSetupException) {
               this.throwPoolInitializationException(this.getLastConnectionFailure().getCause());
            }

            UtilityElf.quietlySleep(TimeUnit.SECONDS.toMillis(1L));
         } while (ClockSource.elapsedMillis(startTime) < initializationTimeout);

         if (initializationTimeout > 0L) {
            this.throwPoolInitializationException(this.getLastConnectionFailure());
         }
      }
   }

   private void throwPoolInitializationException(Throwable t) {
      this.logger.error("{} - Exception during pool initialization.", this.poolName, t);
      this.destroyHouseKeepingExecutorService();
      throw new HikariPool.PoolInitializationException(t);
   }

   private boolean softEvictConnection(PoolEntry poolEntry, String reason, boolean owner) {
      poolEntry.markEvicted();
      if (!owner && !this.connectionBag.reserve(poolEntry)) {
         return false;
      } else {
         this.closeConnection(poolEntry, reason);
         return true;
      }
   }

   private ScheduledExecutorService initializeHouseKeepingExecutorService() {
      if (this.config.getScheduledExecutor() == null) {
         ThreadFactory threadFactory = Optional.ofNullable(this.config.getThreadFactory())
            .orElseGet(() -> new UtilityElf.DefaultThreadFactory(this.poolName + " housekeeper", true));
         ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, threadFactory, new DiscardPolicy());
         executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
         executor.setRemoveOnCancelPolicy(true);
         return executor;
      } else {
         return this.config.getScheduledExecutor();
      }
   }

   private void destroyHouseKeepingExecutorService() {
      if (this.config.getScheduledExecutor() == null) {
         this.houseKeepingExecutorService.shutdownNow();
      }
   }

   private PoolStats getPoolStats() {
      return new PoolStats(TimeUnit.SECONDS.toMillis(1L)) {
         @Override
         protected void update() {
            this.pendingThreads = HikariPool.this.getThreadsAwaitingConnection();
            this.idleConnections = HikariPool.this.getIdleConnections();
            this.totalConnections = HikariPool.this.getTotalConnections();
            this.activeConnections = HikariPool.this.getActiveConnections();
            this.maxConnections = HikariPool.this.config.getMaximumPoolSize();
            this.minConnections = HikariPool.this.config.getMinimumIdle();
         }
      };
   }

   private SQLException createTimeoutException(long startTime) {
      this.logPoolState("Timeout failure ");
      this.metricsTracker.recordConnectionTimeout();
      String sqlState = null;
      Exception originalException = this.getLastConnectionFailure();
      if (originalException instanceof SQLException) {
         sqlState = ((SQLException)originalException).getSQLState();
      }

      SQLTransientConnectionException connectionException = new SQLTransientConnectionException(
         this.poolName + " - Connection is not available, request timed out after " + ClockSource.elapsedMillis(startTime) + "ms.", sqlState, originalException
      );
      if (originalException instanceof SQLException) {
         connectionException.setNextException((SQLException)originalException);
      }

      return connectionException;
   }

   private class CustomDiscardPolicy implements RejectedExecutionHandler {
      @Override
      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
         HikariPool.this.addConnectionQueueDepth.decrementAndGet();
      }
   }

   private final class HouseKeeper implements Runnable {
      private volatile long previous = ClockSource.plusMillis(ClockSource.currentTime(), -HikariPool.this.housekeepingPeriodMs);
      private final AtomicReferenceFieldUpdater<PoolBase, String> catalogUpdater = AtomicReferenceFieldUpdater.newUpdater(
         PoolBase.class, String.class, "catalog"
      );

      @Override
      public void run() {
         try {
            HikariPool.this.connectionTimeout = HikariPool.this.config.getConnectionTimeout();
            HikariPool.this.validationTimeout = HikariPool.this.config.getValidationTimeout();
            HikariPool.this.leakTaskFactory.updateLeakDetectionThreshold(HikariPool.this.config.getLeakDetectionThreshold());
            if (HikariPool.this.config.getCatalog() != null && !HikariPool.this.config.getCatalog().equals(HikariPool.this.catalog)) {
               this.catalogUpdater.set(HikariPool.this, HikariPool.this.config.getCatalog());
            }

            long idleTimeout = HikariPool.this.config.getIdleTimeout();
            long now = ClockSource.currentTime();
            if (ClockSource.plusMillis(now, 128L) < ClockSource.plusMillis(this.previous, HikariPool.this.housekeepingPeriodMs)) {
               HikariPool.this.logger
                  .warn(
                     "{} - Retrograde clock change detected (housekeeper delta={}), soft-evicting connections from pool.",
                     HikariPool.this.poolName,
                     ClockSource.elapsedDisplayString(this.previous, now)
                  );
               this.previous = now;
               HikariPool.this.softEvictConnections();
               return;
            }

            if (now > ClockSource.plusMillis(this.previous, 3L * HikariPool.this.housekeepingPeriodMs / 2L)) {
               HikariPool.this.logger
                  .warn(
                     "{} - Thread starvation or clock leap detected (housekeeper delta={}).",
                     HikariPool.this.poolName,
                     ClockSource.elapsedDisplayString(this.previous, now)
                  );
            }

            this.previous = now;
            String afterPrefix = "Pool ";
            if (idleTimeout > 0L && HikariPool.this.config.getMinimumIdle() < HikariPool.this.config.getMaximumPoolSize()) {
               HikariPool.this.logPoolState("Before cleanup ");
               afterPrefix = "After cleanup  ";
               List<PoolEntry> notInUse = HikariPool.this.connectionBag.values(0);
               int toRemove = notInUse.size() - HikariPool.this.config.getMinimumIdle();

               for (PoolEntry entry : notInUse) {
                  if (toRemove > 0 && ClockSource.elapsedMillis(entry.lastAccessed, now) > idleTimeout && HikariPool.this.connectionBag.reserve(entry)) {
                     HikariPool.this.closeConnection(entry, "(connection has passed idleTimeout)");
                     toRemove--;
                  }
               }
            }

            HikariPool.this.logPoolState(afterPrefix);
            HikariPool.this.fillPool(true);
         } catch (Exception var10) {
            HikariPool.this.logger.error("Unexpected exception in housekeeping task", (Throwable)var10);
         }
      }
   }

   private final class KeepaliveTask implements Runnable {
      private final PoolEntry poolEntry;

      KeepaliveTask(PoolEntry poolEntry) {
         this.poolEntry = poolEntry;
      }

      @Override
      public void run() {
         if (HikariPool.this.connectionBag.reserve(this.poolEntry)) {
            if (HikariPool.this.isConnectionDead(this.poolEntry.connection)) {
               HikariPool.this.softEvictConnection(this.poolEntry, "(connection is dead)", true);
               HikariPool.this.addBagItem(HikariPool.this.connectionBag.getWaitingThreadCount());
            } else {
               HikariPool.this.connectionBag.unreserve(this.poolEntry);
               HikariPool.this.logger.debug("{} - keepalive: connection {} is alive", HikariPool.this.poolName, this.poolEntry.connection);
            }
         }
      }
   }

   private final class MaxLifetimeTask implements Runnable {
      private final PoolEntry poolEntry;

      MaxLifetimeTask(PoolEntry poolEntry) {
         this.poolEntry = poolEntry;
      }

      @Override
      public void run() {
         if (HikariPool.this.softEvictConnection(this.poolEntry, "(connection has passed maxLifetime)", false)) {
            HikariPool.this.addBagItem(HikariPool.this.connectionBag.getWaitingThreadCount());
         }
      }
   }

   private final class PoolEntryCreator implements Callable<Boolean> {
      private final String loggingPrefix;

      PoolEntryCreator() {
         this(null);
      }

      PoolEntryCreator(String loggingPrefix) {
         this.loggingPrefix = loggingPrefix;
      }

      public Boolean call() {
         long backoffMs = 10L;
         boolean added = false;

         try {
            for (; this.shouldContinueCreating(); UtilityElf.quietlySleep(backoffMs)) {
               PoolEntry poolEntry = HikariPool.this.createPoolEntry();
               if (poolEntry != null) {
                  added = true;
                  backoffMs = 10L;
                  HikariPool.this.connectionBag.add(poolEntry);
                  HikariPool.this.logger.debug("{} - Added connection {}", HikariPool.this.poolName, poolEntry.connection);
               } else {
                  backoffMs = Math.min(TimeUnit.SECONDS.toMillis(5L), backoffMs * 2L);
                  if (this.loggingPrefix != null) {
                     HikariPool.this.logger.debug("{} - Connection add failed, sleeping with backoff: {}ms", HikariPool.this.poolName, backoffMs);
                  }
               }
            }
         } finally {
            HikariPool.this.addConnectionQueueDepth.decrementAndGet();
            if (added && this.loggingPrefix != null) {
               HikariPool.this.logPoolState(this.loggingPrefix);
            }
         }

         return Boolean.FALSE;
      }

      private synchronized boolean shouldContinueCreating() {
         return HikariPool.this.poolState == 0
            && HikariPool.this.getTotalConnections() < HikariPool.this.config.getMaximumPoolSize()
            && (
               HikariPool.this.getIdleConnections() < HikariPool.this.config.getMinimumIdle()
                  || HikariPool.this.connectionBag.getWaitingThreadCount() > HikariPool.this.getIdleConnections()
            );
      }
   }

   public static class PoolInitializationException extends RuntimeException {
      private static final long serialVersionUID = 929872118275916520L;

      public PoolInitializationException(Throwable t) {
         super("Failed to initialize pool: " + t.getMessage(), t);
      }
   }
}
