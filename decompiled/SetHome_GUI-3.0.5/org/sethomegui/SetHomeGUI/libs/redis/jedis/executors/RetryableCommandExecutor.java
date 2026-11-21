package org.sethomegui.SetHomeGUI.libs.redis.jedis.executors;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandObject;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Connection;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.VisibleForTesting;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions.JedisConnectionException;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions.JedisException;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.providers.ConnectionProvider;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryableCommandExecutor implements CommandExecutor {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   protected final ConnectionProvider provider;
   protected final int maxAttempts;
   protected final Duration maxTotalRetriesDuration;

   public RetryableCommandExecutor(ConnectionProvider provider, int maxAttempts, Duration maxTotalRetriesDuration) {
      this.provider = provider;
      this.maxAttempts = maxAttempts;
      this.maxTotalRetriesDuration = maxTotalRetriesDuration;
   }

   @Override
   public void close() {
      IOUtils.closeQuietly(this.provider);
   }

   @Override
   public final <T> T executeCommand(CommandObject<T> commandObject) {
      Instant deadline = Instant.now().plus(this.maxTotalRetriesDuration);
      int consecutiveConnectionFailures = 0;
      JedisException lastException = null;

      for (int attemptsLeft = this.maxAttempts; attemptsLeft > 0; attemptsLeft--) {
         Connection connection = null;

         try {
            connection = this.provider.getConnection(commandObject.getArguments());
            return this.execute(connection, commandObject);
         } catch (JedisConnectionException var12) {
            lastException = var12;
            consecutiveConnectionFailures++;
            this.log.debug("Failed connecting to Redis: {}", connection, var12);
            boolean reset = this.handleConnectionProblem(attemptsLeft - 1, consecutiveConnectionFailures, deadline);
            if (reset) {
               consecutiveConnectionFailures = 0;
            }
         } finally {
            if (connection != null) {
               connection.close();
            }
         }

         if (Instant.now().isAfter(deadline)) {
            throw new JedisException("Retry deadline exceeded.");
         }
      }

      JedisException maxAttemptsException = new JedisException("No more attempts left.");
      maxAttemptsException.addSuppressed(lastException);
      throw maxAttemptsException;
   }

   @VisibleForTesting
   protected <T> T execute(Connection connection, CommandObject<T> commandObject) {
      return connection.executeCommand(commandObject);
   }

   private boolean handleConnectionProblem(int attemptsLeft, int consecutiveConnectionFailures, Instant doneDeadline) {
      if (consecutiveConnectionFailures < 2) {
         return false;
      } else {
         this.sleep(getBackoffSleepMillis(attemptsLeft, doneDeadline));
         return true;
      }
   }

   private static long getBackoffSleepMillis(int attemptsLeft, Instant deadline) {
      if (attemptsLeft <= 0) {
         return 0L;
      } else {
         long millisLeft = Duration.between(Instant.now(), deadline).toMillis();
         if (millisLeft < 0L) {
            throw new JedisException("Retry deadline exceeded.");
         } else {
            return millisLeft / (attemptsLeft * (attemptsLeft + 1));
         }
      }
   }

   @VisibleForTesting
   protected void sleep(long sleepMillis) {
      try {
         TimeUnit.MILLISECONDS.sleep(sleepMillis);
      } catch (InterruptedException var4) {
         throw new JedisException(var4);
      }
   }
}
