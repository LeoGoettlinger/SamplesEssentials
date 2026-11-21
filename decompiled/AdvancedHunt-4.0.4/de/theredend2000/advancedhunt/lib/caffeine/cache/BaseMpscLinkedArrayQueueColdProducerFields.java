package de.theredend2000.advancedhunt.lib.caffeine.cache;

abstract class BaseMpscLinkedArrayQueueColdProducerFields<E> extends BaseMpscLinkedArrayQueuePad3<E> {
   protected E[] producerBuffer;
   protected volatile long producerLimit;
   protected long producerMask = this.consumerMask;

   BaseMpscLinkedArrayQueueColdProducerFields(int initialCapacity) {
      super(initialCapacity);
      this.producerBuffer = this.consumerBuffer;
   }
}
