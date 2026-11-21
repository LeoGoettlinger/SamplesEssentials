package org.sethomegui.SetHomeGUI.libs.redis.jedis.mcf;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateSupplier;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Connection;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.Experimental;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.providers.MultiClusterPooledConnectionProvider;

@Experimental
public class CircuitBreakerFailoverConnectionProvider extends CircuitBreakerFailoverBase {
   public CircuitBreakerFailoverConnectionProvider(MultiClusterPooledConnectionProvider provider) {
      super(provider);
   }

   public Connection getConnection() {
      MultiClusterPooledConnectionProvider.Cluster cluster = this.provider.getCluster();
      DecorateSupplier<Connection> supplier = Decorators.ofSupplier(() -> this.handleGetConnection(cluster));
      supplier.withRetry(cluster.getRetry());
      supplier.withCircuitBreaker(cluster.getCircuitBreaker());
      supplier.withFallback(this.provider.getFallbackExceptionList(), e -> this.handleClusterFailover(cluster.getCircuitBreaker()));
      return (Connection)supplier.decorate().get();
   }

   private Connection handleGetConnection(MultiClusterPooledConnectionProvider.Cluster cluster) {
      Connection connection = cluster.getConnection();
      connection.ping();
      return connection;
   }

   private Connection handleClusterFailover(CircuitBreaker circuitBreaker) {
      this.clusterFailover(circuitBreaker);
      return this.getConnection();
   }
}
