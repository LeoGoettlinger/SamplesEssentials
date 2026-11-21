package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@Beta
public final class Graphs {
   private Graphs() {
   }

   public static <N> boolean hasCycle(Graph<N> graph) {
      int numEdges = graph.edges().size();
      if (numEdges == 0) {
         return false;
      } else if (!graph.isDirected() && numEdges >= graph.nodes().size()) {
         return true;
      } else {
         Map<Object, Graphs.NodeVisitState> visitedNodes = Maps.newHashMapWithExpectedSize(graph.nodes().size());

         for (N node : graph.nodes()) {
            if (subgraphHasCycle(graph, visitedNodes, node, null)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean hasCycle(Network<?, ?> network) {
      return !network.isDirected() && network.allowsParallelEdges() && network.edges().size() > network.asGraph().edges().size()
         ? true
         : hasCycle(network.asGraph());
   }

   private static <N> boolean subgraphHasCycle(Graph<N> graph, Map<Object, Graphs.NodeVisitState> visitedNodes, N node, @CheckForNull N previousNode) {
      Graphs.NodeVisitState state = visitedNodes.get(node);
      if (state == Graphs.NodeVisitState.COMPLETE) {
         return false;
      } else if (state == Graphs.NodeVisitState.PENDING) {
         return true;
      } else {
         visitedNodes.put(node, Graphs.NodeVisitState.PENDING);

         for (N nextNode : graph.successors(node)) {
            if (canTraverseWithoutReusingEdge(graph, nextNode, previousNode) && subgraphHasCycle(graph, visitedNodes, nextNode, node)) {
               return true;
            }
         }

         visitedNodes.put(node, Graphs.NodeVisitState.COMPLETE);
         return false;
      }
   }

   private static boolean canTraverseWithoutReusingEdge(Graph<?> graph, Object nextNode, @CheckForNull Object previousNode) {
      return graph.isDirected() || !Objects.equal(previousNode, nextNode);
   }

   public static <N> Graph<N> transitiveClosure(Graph<N> graph) {
      MutableGraph<N> transitiveClosure = GraphBuilder.from(graph).allowsSelfLoops(true).build();
      if (graph.isDirected()) {
         for (N node : graph.nodes()) {
            for (N reachableNode : reachableNodes(graph, node)) {
               transitiveClosure.putEdge(node, reachableNode);
            }
         }
      } else {
         Set<N> visitedNodes = new HashSet<>();

         for (N node : graph.nodes()) {
            if (!visitedNodes.contains(node)) {
               Set<N> reachableNodes = reachableNodes(graph, node);
               visitedNodes.addAll(reachableNodes);
               int pairwiseMatch = 1;

               for (N nodeU : reachableNodes) {
                  for (N nodeV : Iterables.limit(reachableNodes, pairwiseMatch++)) {
                     transitiveClosure.putEdge(nodeU, nodeV);
                  }
               }
            }
         }
      }

      return transitiveClosure;
   }

   public static <N> Set<N> reachableNodes(Graph<N> graph, N node) {
      Preconditions.checkArgument(graph.nodes().contains(node), "Node %s is not an element of this graph.", node);
      return ImmutableSet.copyOf(Traverser.forGraph(graph).breadthFirst(node));
   }

   public static <N> Graph<N> transpose(Graph<N> graph) {
      if (!graph.isDirected()) {
         return graph;
      } else {
         return (Graph<N>)(graph instanceof Graphs.TransposedGraph ? ((Graphs.TransposedGraph)graph).graph : new Graphs.TransposedGraph<>(graph));
      }
   }

   public static <N, V> ValueGraph<N, V> transpose(ValueGraph<N, V> graph) {
      if (!graph.isDirected()) {
         return graph;
      } else {
         return (ValueGraph<N, V>)(graph instanceof Graphs.TransposedValueGraph
            ? ((Graphs.TransposedValueGraph)graph).graph
            : new Graphs.TransposedValueGraph<>(graph));
      }
   }

   public static <N, E> Network<N, E> transpose(Network<N, E> network) {
      if (!network.isDirected()) {
         return network;
      } else {
         return (Network<N, E>)(network instanceof Graphs.TransposedNetwork
            ? ((Graphs.TransposedNetwork)network).network
            : new Graphs.TransposedNetwork<>(network));
      }
   }

   static <N> EndpointPair<N> transpose(EndpointPair<N> endpoints) {
      return endpoints.isOrdered() ? EndpointPair.ordered(endpoints.target(), endpoints.source()) : endpoints;
   }

   public static <N> MutableGraph<N> inducedSubgraph(Graph<N> graph, Iterable<? extends N> nodes) {
      MutableGraph<N> subgraph = nodes instanceof Collection
         ? GraphBuilder.from(graph).expectedNodeCount(((Collection)nodes).size()).build()
         : GraphBuilder.from(graph).build();

      for (N node : nodes) {
         subgraph.addNode(node);
      }

      for (N node : subgraph.nodes()) {
         for (N successorNode : graph.successors(node)) {
            if (subgraph.nodes().contains(successorNode)) {
               subgraph.putEdge(node, successorNode);
            }
         }
      }

      return subgraph;
   }

   public static <N, V> MutableValueGraph<N, V> inducedSubgraph(ValueGraph<N, V> graph, Iterable<? extends N> nodes) {
      MutableValueGraph<N, V> subgraph = nodes instanceof Collection
         ? ValueGraphBuilder.from(graph).expectedNodeCount(((Collection)nodes).size()).build()
         : ValueGraphBuilder.from(graph).build();

      for (N node : nodes) {
         subgraph.addNode(node);
      }

      for (N node : subgraph.nodes()) {
         for (N successorNode : graph.successors(node)) {
            if (subgraph.nodes().contains(successorNode)) {
               subgraph.putEdgeValue(node, successorNode, java.util.Objects.requireNonNull(graph.edgeValueOrDefault(node, successorNode, null)));
            }
         }
      }

      return subgraph;
   }

   public static <N, E> MutableNetwork<N, E> inducedSubgraph(Network<N, E> network, Iterable<? extends N> nodes) {
      MutableNetwork<N, E> subgraph = nodes instanceof Collection
         ? NetworkBuilder.from(network).expectedNodeCount(((Collection)nodes).size()).build()
         : NetworkBuilder.from(network).build();

      for (N node : nodes) {
         subgraph.addNode(node);
      }

      for (N node : subgraph.nodes()) {
         for (E edge : network.outEdges(node)) {
            N successorNode = network.incidentNodes(edge).adjacentNode(node);
            if (subgraph.nodes().contains(successorNode)) {
               subgraph.addEdge(node, successorNode, edge);
            }
         }
      }

      return subgraph;
   }

   public static <N> MutableGraph<N> copyOf(Graph<N> graph) {
      MutableGraph<N> copy = GraphBuilder.from(graph).expectedNodeCount(graph.nodes().size()).build();

      for (N node : graph.nodes()) {
         copy.addNode(node);
      }

      for (EndpointPair<N> edge : graph.edges()) {
         copy.putEdge(edge.nodeU(), edge.nodeV());
      }

      return copy;
   }

   public static <N, V> MutableValueGraph<N, V> copyOf(ValueGraph<N, V> graph) {
      MutableValueGraph<N, V> copy = ValueGraphBuilder.from(graph).expectedNodeCount(graph.nodes().size()).build();

      for (N node : graph.nodes()) {
         copy.addNode(node);
      }

      for (EndpointPair<N> edge : graph.edges()) {
         copy.putEdgeValue(edge.nodeU(), edge.nodeV(), java.util.Objects.requireNonNull(graph.edgeValueOrDefault(edge.nodeU(), edge.nodeV(), null)));
      }

      return copy;
   }

   public static <N, E> MutableNetwork<N, E> copyOf(Network<N, E> network) {
      MutableNetwork<N, E> copy = NetworkBuilder.from(network).expectedNodeCount(network.nodes().size()).expectedEdgeCount(network.edges().size()).build();

      for (N node : network.nodes()) {
         copy.addNode(node);
      }

      for (E edge : network.edges()) {
         EndpointPair<N> endpointPair = network.incidentNodes(edge);
         copy.addEdge(endpointPair.nodeU(), endpointPair.nodeV(), edge);
      }

      return copy;
   }

   @CanIgnoreReturnValue
   static int checkNonNegative(int value) {
      Preconditions.checkArgument(value >= 0, "Not true that %s is non-negative.", value);
      return value;
   }

   @CanIgnoreReturnValue
   static long checkNonNegative(long value) {
      Preconditions.checkArgument(value >= 0L, "Not true that %s is non-negative.", value);
      return value;
   }

   @CanIgnoreReturnValue
   static int checkPositive(int value) {
      Preconditions.checkArgument(value > 0, "Not true that %s is positive.", value);
      return value;
   }

   @CanIgnoreReturnValue
   static long checkPositive(long value) {
      Preconditions.checkArgument(value > 0L, "Not true that %s is positive.", value);
      return value;
   }

   private static enum NodeVisitState {
      PENDING,
      COMPLETE;
   }

   private static class TransposedGraph<N> extends ForwardingGraph<N> {
      private final Graph<N> graph;

      TransposedGraph(Graph<N> graph) {
         this.graph = graph;
      }

      Graph<N> delegate() {
         return this.graph;
      }

      @Override
      public Set<N> predecessors(N node) {
         return this.delegate().successors(node);
      }

      @Override
      public Set<N> successors(N node) {
         return this.delegate().predecessors(node);
      }

      @Override
      public Set<EndpointPair<N>> incidentEdges(N node) {
         return new IncidentEdgeSet<N>(this, node) {
            @Override
            public Iterator<EndpointPair<N>> iterator() {
               return Iterators.transform(
                  TransposedGraph.this.delegate().incidentEdges(this.node).iterator(),
                  edge -> EndpointPair.of(TransposedGraph.this.delegate(), (N)edge.nodeV(), (N)edge.nodeU())
               );
            }
         };
      }

      @Override
      public int inDegree(N node) {
         return this.delegate().outDegree(node);
      }

      @Override
      public int outDegree(N node) {
         return this.delegate().inDegree(node);
      }

      @Override
      public boolean hasEdgeConnecting(N nodeU, N nodeV) {
         return this.delegate().hasEdgeConnecting(nodeV, nodeU);
      }

      @Override
      public boolean hasEdgeConnecting(EndpointPair<N> endpoints) {
         return this.delegate().hasEdgeConnecting(Graphs.transpose(endpoints));
      }
   }

   private static class TransposedNetwork<N, E> extends ForwardingNetwork<N, E> {
      private final Network<N, E> network;

      TransposedNetwork(Network<N, E> network) {
         this.network = network;
      }

      @Override
      Network<N, E> delegate() {
         return this.network;
      }

      @Override
      public Set<N> predecessors(N node) {
         return this.delegate().successors(node);
      }

      @Override
      public Set<N> successors(N node) {
         return this.delegate().predecessors(node);
      }

      @Override
      public int inDegree(N node) {
         return this.delegate().outDegree(node);
      }

      @Override
      public int outDegree(N node) {
         return this.delegate().inDegree(node);
      }

      @Override
      public Set<E> inEdges(N node) {
         return this.delegate().outEdges(node);
      }

      @Override
      public Set<E> outEdges(N node) {
         return this.delegate().inEdges(node);
      }

      @Override
      public EndpointPair<N> incidentNodes(E edge) {
         EndpointPair<N> endpointPair = this.delegate().incidentNodes(edge);
         return EndpointPair.of(this.network, endpointPair.nodeV(), endpointPair.nodeU());
      }

      @Override
      public Set<E> edgesConnecting(N nodeU, N nodeV) {
         return this.delegate().edgesConnecting(nodeV, nodeU);
      }

      @Override
      public Set<E> edgesConnecting(EndpointPair<N> endpoints) {
         return this.delegate().edgesConnecting(Graphs.transpose(endpoints));
      }

      @Override
      public Optional<E> edgeConnecting(N nodeU, N nodeV) {
         return this.delegate().edgeConnecting(nodeV, nodeU);
      }

      @Override
      public Optional<E> edgeConnecting(EndpointPair<N> endpoints) {
         return this.delegate().edgeConnecting(Graphs.transpose(endpoints));
      }

      @CheckForNull
      @Override
      public E edgeConnectingOrNull(N nodeU, N nodeV) {
         return this.delegate().edgeConnectingOrNull(nodeV, nodeU);
      }

      @CheckForNull
      @Override
      public E edgeConnectingOrNull(EndpointPair<N> endpoints) {
         return this.delegate().edgeConnectingOrNull(Graphs.transpose(endpoints));
      }

      @Override
      public boolean hasEdgeConnecting(N nodeU, N nodeV) {
         return this.delegate().hasEdgeConnecting(nodeV, nodeU);
      }

      @Override
      public boolean hasEdgeConnecting(EndpointPair<N> endpoints) {
         return this.delegate().hasEdgeConnecting(Graphs.transpose(endpoints));
      }
   }

   private static class TransposedValueGraph<N, V> extends ForwardingValueGraph<N, V> {
      private final ValueGraph<N, V> graph;

      TransposedValueGraph(ValueGraph<N, V> graph) {
         this.graph = graph;
      }

      @Override
      ValueGraph<N, V> delegate() {
         return this.graph;
      }

      @Override
      public Set<N> predecessors(N node) {
         return this.delegate().successors(node);
      }

      @Override
      public Set<N> successors(N node) {
         return this.delegate().predecessors(node);
      }

      @Override
      public int inDegree(N node) {
         return this.delegate().outDegree(node);
      }

      @Override
      public int outDegree(N node) {
         return this.delegate().inDegree(node);
      }

      @Override
      public boolean hasEdgeConnecting(N nodeU, N nodeV) {
         return this.delegate().hasEdgeConnecting(nodeV, nodeU);
      }

      @Override
      public boolean hasEdgeConnecting(EndpointPair<N> endpoints) {
         return this.delegate().hasEdgeConnecting(Graphs.transpose(endpoints));
      }

      @Override
      public Optional<V> edgeValue(N nodeU, N nodeV) {
         return this.delegate().edgeValue(nodeV, nodeU);
      }

      @Override
      public Optional<V> edgeValue(EndpointPair<N> endpoints) {
         return this.delegate().edgeValue(Graphs.transpose(endpoints));
      }

      @CheckForNull
      @Override
      public V edgeValueOrDefault(N nodeU, N nodeV, @CheckForNull V defaultValue) {
         return this.delegate().edgeValueOrDefault(nodeV, nodeU, defaultValue);
      }

      @CheckForNull
      @Override
      public V edgeValueOrDefault(EndpointPair<N> endpoints, @CheckForNull V defaultValue) {
         return this.delegate().edgeValueOrDefault(Graphs.transpose(endpoints), defaultValue);
      }
   }
}
