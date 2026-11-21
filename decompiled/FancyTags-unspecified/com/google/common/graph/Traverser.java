package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.DoNotMock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import javax.annotation.CheckForNull;

@DoNotMock("Call forGraph or forTree, passing a lambda or a Graph with the desired edges (built with GraphBuilder)")
@ElementTypesAreNonnullByDefault
@Beta
public abstract class Traverser<N> {
   private final SuccessorsFunction<N> successorFunction;

   private Traverser(SuccessorsFunction<N> successorFunction) {
      this.successorFunction = Preconditions.checkNotNull(successorFunction);
   }

   public static <N> Traverser<N> forGraph(SuccessorsFunction<N> graph) {
      return new Traverser<N>(graph) {
         @Override
         Traverser.Traversal<N> newTraversal() {
            return Traverser.Traversal.inGraph(graph);
         }
      };
   }

   public static <N> Traverser<N> forTree(SuccessorsFunction<N> tree) {
      if (tree instanceof BaseGraph) {
         Preconditions.checkArgument(((BaseGraph)tree).isDirected(), "Undirected graphs can never be trees.");
      }

      if (tree instanceof Network) {
         Preconditions.checkArgument(((Network)tree).isDirected(), "Undirected networks can never be trees.");
      }

      return new Traverser<N>(tree) {
         @Override
         Traverser.Traversal<N> newTraversal() {
            return Traverser.Traversal.inTree(tree);
         }
      };
   }

   public final Iterable<N> breadthFirst(N startNode) {
      return this.breadthFirst(ImmutableSet.of(startNode));
   }

   public final Iterable<N> breadthFirst(Iterable<? extends N> startNodes) {
      final ImmutableSet<N> validated = this.validate(startNodes);
      return new Iterable<N>() {
         @Override
         public Iterator<N> iterator() {
            return Traverser.this.newTraversal().breadthFirst(validated.iterator());
         }
      };
   }

   public final Iterable<N> depthFirstPreOrder(N startNode) {
      return this.depthFirstPreOrder(ImmutableSet.of(startNode));
   }

   public final Iterable<N> depthFirstPreOrder(Iterable<? extends N> startNodes) {
      final ImmutableSet<N> validated = this.validate(startNodes);
      return new Iterable<N>() {
         @Override
         public Iterator<N> iterator() {
            return Traverser.this.newTraversal().preOrder(validated.iterator());
         }
      };
   }

   public final Iterable<N> depthFirstPostOrder(N startNode) {
      return this.depthFirstPostOrder(ImmutableSet.of(startNode));
   }

   public final Iterable<N> depthFirstPostOrder(Iterable<? extends N> startNodes) {
      final ImmutableSet<N> validated = this.validate(startNodes);
      return new Iterable<N>() {
         @Override
         public Iterator<N> iterator() {
            return Traverser.this.newTraversal().postOrder(validated.iterator());
         }
      };
   }

   abstract Traverser.Traversal<N> newTraversal();

   private ImmutableSet<N> validate(Iterable<? extends N> startNodes) {
      ImmutableSet<N> copy = ImmutableSet.copyOf(startNodes);

      for (N node : copy) {
         this.successorFunction.successors(node);
      }

      return copy;
   }

   private static enum InsertionOrder {
      FRONT {
         @Override
         <T> void insertInto(Deque<T> deque, T value) {
            deque.addFirst(value);
         }
      },
      BACK {
         @Override
         <T> void insertInto(Deque<T> deque, T value) {
            deque.addLast(value);
         }
      };

      private InsertionOrder() {
      }

      abstract <T> void insertInto(Deque<T> deque, T value);
   }

   private abstract static class Traversal<N> {
      final SuccessorsFunction<N> successorFunction;

      Traversal(SuccessorsFunction<N> successorFunction) {
         this.successorFunction = successorFunction;
      }

      static <N> Traverser.Traversal<N> inGraph(SuccessorsFunction<N> graph) {
         final Set<N> visited = new HashSet<>();
         return new Traverser.Traversal<N>(graph) {
            @CheckForNull
            @Override
            N visitNext(Deque<Iterator<? extends N>> horizon) {
               Iterator<? extends N> top = horizon.getFirst();

               while (top.hasNext()) {
                  N element = (N)top.next();
                  Objects.requireNonNull(element);
                  if (visited.add(element)) {
                     return element;
                  }
               }

               horizon.removeFirst();
               return null;
            }
         };
      }

      static <N> Traverser.Traversal<N> inTree(SuccessorsFunction<N> tree) {
         return new Traverser.Traversal<N>(tree) {
            @CheckForNull
            @Override
            N visitNext(Deque<Iterator<? extends N>> horizon) {
               Iterator<? extends N> top = horizon.getFirst();
               if (top.hasNext()) {
                  return Preconditions.checkNotNull((N)top.next());
               } else {
                  horizon.removeFirst();
                  return null;
               }
            }
         };
      }

      final Iterator<N> breadthFirst(Iterator<? extends N> startNodes) {
         return this.topDown(startNodes, Traverser.InsertionOrder.BACK);
      }

      final Iterator<N> preOrder(Iterator<? extends N> startNodes) {
         return this.topDown(startNodes, Traverser.InsertionOrder.FRONT);
      }

      private Iterator<N> topDown(Iterator<? extends N> startNodes, Traverser.InsertionOrder order) {
         final Deque<Iterator<? extends N>> horizon = new ArrayDeque<>();
         horizon.add(startNodes);
         return new AbstractIterator<N>() {
            @CheckForNull
            @Override
            protected N computeNext() {
               do {
                  N next = Traversal.this.visitNext(horizon);
                  if (next != null) {
                     Iterator<? extends N> successors = Traversal.this.successorFunction.successors(next).iterator();
                     if (successors.hasNext()) {
                        order.insertInto(horizon, successors);
                     }

                     return next;
                  }
               } while (!horizon.isEmpty());

               return (N)this.endOfData();
            }
         };
      }

      final Iterator<N> postOrder(Iterator<? extends N> startNodes) {
         final Deque<N> ancestorStack = new ArrayDeque<>();
         final Deque<Iterator<? extends N>> horizon = new ArrayDeque<>();
         horizon.add(startNodes);
         return new AbstractIterator<N>() {
            @CheckForNull
            @Override
            protected N computeNext() {
               for (N next = Traversal.this.visitNext(horizon); next != null; next = Traversal.this.visitNext(horizon)) {
                  Iterator<? extends N> successors = Traversal.this.successorFunction.successors(next).iterator();
                  if (!successors.hasNext()) {
                     return next;
                  }

                  horizon.addFirst(successors);
                  ancestorStack.push(next);
               }

               return (N)(!ancestorStack.isEmpty() ? ancestorStack.pop() : this.endOfData());
            }
         };
      }

      @CheckForNull
      abstract N visitNext(Deque<Iterator<? extends N>> horizon);
   }
}
