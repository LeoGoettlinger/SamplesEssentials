package com.artillexstudios.axafkzone.libs.axapi.collections;

import com.artillexstudios.axafkzone.libs.axapi.utils.mutable.MutableInteger;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

public class NodeCachingLockingQueue<E> {
   private final NodeCachingLockingQueue.Node<E> head = new NodeCachingLockingQueue.Node<>();
   private NodeCachingLockingQueue.Node<E> tail;
   private final ArrayDeque<NodeCachingLockingQueue.Node<E>> nodeCache = new ArrayDeque<>();
   private final ReentrantLock lock = new ReentrantLock();
   private final MutableInteger size = new MutableInteger();

   public NodeCachingLockingQueue() {
      this(0);
   }

   public NodeCachingLockingQueue(int cacheSize) {
      for (int i = 0; i < cacheSize; i++) {
         this.nodeCache.add(new NodeCachingLockingQueue.Node<>());
      }

      this.tail = this.head;
   }

   private NodeCachingLockingQueue.Node<E> getOrCreateNode() {
      NodeCachingLockingQueue.Node<E> node = this.nodeCache.pollFirst();
      return node == null ? new NodeCachingLockingQueue.Node<>() : node;
   }

   private void releaseNode(NodeCachingLockingQueue.Node<E> node) {
      node.item = null;
      node.next = null;
      this.nodeCache.addFirst(node);
      this.size.decrementAndGet();
   }

   public void addAll(Collection<E> collection) {
      this.lock.lock();

      try {
         for (E element : collection) {
            NodeCachingLockingQueue.Node<E> node = this.getOrCreateNode();
            node.item = element;
            this.tail.next = node;
            this.tail = node;
            this.size.incrementAndGet();
         }
      } finally {
         this.lock.unlock();
      }
   }

   public void offer(E item) {
      this.lock.lock();

      try {
         NodeCachingLockingQueue.Node<E> node = this.getOrCreateNode();
         node.item = item;
         this.tail.next = node;
         this.tail = node;
         this.size.incrementAndGet();
      } finally {
         this.lock.unlock();
      }
   }

   public E poll() {
      this.lock.lock();

      E item;
      try {
         NodeCachingLockingQueue.Node<E> first = this.head.next;
         if (first != null) {
            item = first.item;
            this.head.next = first.next;
            if (this.tail == first) {
               this.tail = this.head;
            }

            this.releaseNode(first);
            return item;
         }

         item = null;
      } finally {
         this.lock.unlock();
      }

      return item;
   }

   public boolean isEmpty() {
      return this.head.next == null;
   }

   public int size() {
      return this.size.intValue();
   }

   private static final class Node<E> {
      E item;
      NodeCachingLockingQueue.Node<E> next;
   }
}
