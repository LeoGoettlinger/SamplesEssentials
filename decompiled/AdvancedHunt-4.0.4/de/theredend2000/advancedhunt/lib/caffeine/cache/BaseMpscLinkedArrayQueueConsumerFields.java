package de.theredend2000.advancedhunt.lib.caffeine.cache;

abstract class BaseMpscLinkedArrayQueueConsumerFields<E> extends BaseMpscLinkedArrayQueuePad2<E> {
   protected E[] consumerBuffer;
   protected long consumerIndex;
   protected long consumerMask;

   BaseMpscLinkedArrayQueueConsumerFields(int initialCapacity) {
      Caffeine.requireArgument(initialCapacity >= 2, "Initial capacity must be 2 or more");
      int p2capacity = Caffeine.ceilingPowerOfTwo(initialCapacity);
      long mask = p2capacity - 1L << 1;
      E[] buffer = this.allocate(p2capacity + 1);
      this.consumerBuffer = buffer;
      this.consumerMask = mask;
   }

   E[] allocate(int capacity) {
      return (E[])(new Object[capacity]);
   }
}
