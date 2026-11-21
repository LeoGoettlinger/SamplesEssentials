package org.sethomegui.SetHomeGUI.libs.redis.jedis.mcf;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.State;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.Experimental;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions.JedisConnectionException;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.providers.MultiClusterPooledConnectionProvider;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.IOUtils;

@Experimental
public class CircuitBreakerFailoverBase implements AutoCloseable {
   private final Lock lock = new ReentrantLock(true);
   protected final MultiClusterPooledConnectionProvider provider;

   public CircuitBreakerFailoverBase(MultiClusterPooledConnectionProvider provider) {
      this.provider = provider;
   }

   @Override
   public void close() {
      IOUtils.closeQuietly(this.provider);
   }

   protected void clusterFailover(CircuitBreaker circuitBreaker) {
      this.lock.lock();

      try {
         if (!State.FORCED_OPEN.equals(circuitBreaker.getState())) {
            circuitBreaker.transitionToForcedOpenState();
            int activeMultiClusterIndex = this.provider.incrementActiveMultiClusterIndex();
            this.provider.runClusterFailoverPostProcessor(activeMultiClusterIndex);
         } else if (this.provider.isLastClusterCircuitBreakerForcedOpen()) {
            throw new JedisConnectionException(
               "Cluster/database endpoint could not failover since the MultiClusterClientConfig was not provided with an additional cluster/database endpoint according to its prioritized sequence. If applicable, consider failing back OR restarting with an available cluster/database endpoint"
            );
         }
      } finally {
         this.lock.unlock();
      }
   }
}
