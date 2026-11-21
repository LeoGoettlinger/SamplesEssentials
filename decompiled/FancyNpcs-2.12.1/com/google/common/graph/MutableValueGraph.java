package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@Beta
public interface MutableValueGraph<N, V> extends ValueGraph<N, V> {
   @CanIgnoreReturnValue
   boolean addNode(N node);

   @CheckForNull
   @CanIgnoreReturnValue
   V putEdgeValue(N nodeU, N nodeV, V value);

   @CheckForNull
   @CanIgnoreReturnValue
   V putEdgeValue(EndpointPair<N> endpoints, V value);

   @CanIgnoreReturnValue
   boolean removeNode(N node);

   @CheckForNull
   @CanIgnoreReturnValue
   V removeEdge(N nodeU, N nodeV);

   @CheckForNull
   @CanIgnoreReturnValue
   V removeEdge(EndpointPair<N> endpoints);
}
