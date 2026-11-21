package com.google.common.graph;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
interface GraphConnections<N, V> {
   Set<N> adjacentNodes();

   Set<N> predecessors();

   Set<N> successors();

   Iterator<EndpointPair<N>> incidentEdgeIterator(N thisNode);

   @CheckForNull
   V value(N node);

   void removePredecessor(N node);

   @CheckForNull
   @CanIgnoreReturnValue
   V removeSuccessor(N node);

   void addPredecessor(N node, V value);

   @CheckForNull
   @CanIgnoreReturnValue
   V addSuccessor(N node, V value);
}
