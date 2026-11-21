package org.jsoup.nodes;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jsoup.helper.Validate;
import org.jspecify.annotations.Nullable;

public class NodeIterator<T extends Node> implements Iterator<T> {
   private Node root;
   @Nullable
   private T next;
   private Node current;
   private Node previous;
   @Nullable
   private Node currentParent;
   private final Class<T> type;

   public NodeIterator(Node start, Class<T> type) {
      Validate.notNull(start);
      Validate.notNull(type);
      this.type = type;
      this.restart(start);
   }

   public static NodeIterator<Node> from(Node start) {
      return new NodeIterator<>(start, Node.class);
   }

   public void restart(Node start) {
      if (this.type.isInstance(start)) {
         this.next = (T)start;
      }

      this.root = this.previous = this.current = start;
      this.currentParent = this.current.parent();
   }

   @Override
   public boolean hasNext() {
      this.maybeFindNext();
      return this.next != null;
   }

   public T next() {
      this.maybeFindNext();
      if (this.next == null) {
         throw new NoSuchElementException();
      } else {
         T result = this.next;
         this.previous = this.current;
         this.current = this.next;
         this.currentParent = this.current.parent();
         this.next = null;
         return result;
      }
   }

   private void maybeFindNext() {
      if (this.next == null) {
         if (this.currentParent != null && !this.current.hasParent()) {
            this.current = this.previous;
         }

         this.next = this.findNextNode();
      }
   }

   @Nullable
   private T findNextNode() {
      Node node = this.current;

      do {
         if (node.childNodeSize() > 0) {
            node = node.childNode(0);
         } else if (this.root.equals(node)) {
            node = null;
         } else if (node.nextSibling() != null) {
            node = node.nextSibling();
         } else {
            do {
               node = node.parent();
               if (node == null || this.root.equals(node)) {
                  return null;
               }
            } while (node.nextSibling() == null);

            node = node.nextSibling();
         }

         if (node == null) {
            return null;
         }
      } while (!this.type.isInstance(node));

      return (T)node;
   }

   @Override
   public void remove() {
      this.current.remove();
   }
}
