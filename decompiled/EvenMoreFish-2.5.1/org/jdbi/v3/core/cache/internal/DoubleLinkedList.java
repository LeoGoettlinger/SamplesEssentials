package org.jdbi.v3.core.cache.internal;

final class DoubleLinkedList<K, V> {
   final DoubleLinkedList.Node<K, V> rootNode;
   int size = 0;

   DoubleLinkedList() {
      this.rootNode = createNode(null, null);
      this.rootNode.right = this.rootNode;
      this.rootNode.left = this.rootNode;
   }

   void addHead(DoubleLinkedList.Node<K, V> node) {
      if (node != null) {
         if (node.left == null && node.right == null) {
            node.left = this.rootNode;
            node.right = this.rootNode.right;
            node.left.right = node;
            node.right.left = node;
            this.size++;
         } else {
            throw new IllegalStateException("Can not add node twice!");
         }
      }
   }

   DoubleLinkedList.Node<K, V> removeTail() {
      return this.removeNode(this.rootNode.left);
   }

   DoubleLinkedList.Node<K, V> removeNode(DoubleLinkedList.Node<K, V> node) {
      if (node != this.rootNode && node.left != null && node.right != null) {
         node.left.right = node.right;
         node.right.left = node.left;
         node.left = null;
         node.right = null;
         this.size--;
         return node;
      } else {
         return null;
      }
   }

   static <K, V> DoubleLinkedList.Node<K, V> createNode(K key, V value) {
      return new DoubleLinkedList.Node<>(key, value);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("[");

      for (DoubleLinkedList.Node<K, V> node = this.rootNode.right; node != null && node != this.rootNode; node = node.right) {
         sb.append(node.key).append(" = ").append(node.value);
         if (node.right != null) {
            sb.append(", ");
         }
      }

      sb.append(']');
      return sb.toString();
   }

   static final class Node<K, V> {
      DoubleLinkedList.Node<K, V> left = null;
      DoubleLinkedList.Node<K, V> right = null;
      final K key;
      final V value;

      private Node(K key, V value) {
         this.key = key;
         this.value = value;
      }
   }
}
