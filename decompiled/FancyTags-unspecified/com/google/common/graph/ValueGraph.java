package com.google.common.graph;

import com.google.common.annotations.Beta;
import java.util.Optional;
import java.util.Set;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@Beta
public interface ValueGraph<N, V> extends BaseGraph<N> {
   @Override
   Set<N> nodes();

   @Override
   Set<EndpointPair<N>> edges();

   Graph<N> asGraph();

   @Override
   boolean isDirected();

   @Override
   boolean allowsSelfLoops();

   @Override
   ElementOrder<N> nodeOrder();

   @Override
   ElementOrder<N> incidentEdgeOrder();

   @Override
   Set<N> adjacentNodes(N node);

   @Override
   Set<N> predecessors(N node);

   @Override
   Set<N> successors(N node);

   @Override
   Set<EndpointPair<N>> incidentEdges(N node);

   @Override
   int degree(N node);

   @Override
   int inDegree(N node);

   @Override
   int outDegree(N node);

   @Override
   boolean hasEdgeConnecting(N nodeU, N nodeV);

   @Override
   boolean hasEdgeConnecting(EndpointPair<N> endpoints);

   Optional<V> edgeValue(N nodeU, N nodeV);

   Optional<V> edgeValue(EndpointPair<N> endpoints);

   @CheckForNull
   V edgeValueOrDefault(N nodeU, N nodeV, @CheckForNull V defaultValue);

   @CheckForNull
   V edgeValueOrDefault(EndpointPair<N> endpoints, @CheckForNull V defaultValue);

   @Override
   boolean equals(@CheckForNull Object object);

   @Override
   int hashCode();
}
