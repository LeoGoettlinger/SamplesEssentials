package kotlin.collections

import java.util.ArrayList
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.IntrinsicsKt
import kotlin.coroutines.jvm.internal.SpillingKt
import kotlin.jvm.functions.Function2

internal fun checkWindowSizeStep(size: Int, step: Int) {
   if (size <= 0 || step <= 0) {
      throw new IllegalArgumentException(
         (if (size != step) "Both size $size and step $step must be greater than zero." else "size $size must be greater than zero.").toString()
      );
   }
}

internal fun <T> Sequence<T>.windowedSequence(size: Int, step: Int, partialWindows: Boolean, reuseBuffer: Boolean): Sequence<List<T>> {
   checkWindowSizeStep(size, step);
   return new SlidingWindowKt$windowedSequence$$inlined$Sequence$1(`$this$windowedSequence`, size, step, partialWindows, reuseBuffer);
}

internal fun <T> windowedIterator(iterator: Iterator<T>, size: Int, step: Int, partialWindows: Boolean, reuseBuffer: Boolean): Iterator<List<T>> {
   return (java.util.Iterator<java.util.List<T>>)(if (!iterator.hasNext())
      EmptyIterator.INSTANCE
      else
      SequencesKt.iterator(
         (
            new Function2<SequenceScope<? super java.util.List<? extends T>>, Continuation<? super Unit>, Object>(
               size, step, iterator, reuseBuffer, partialWindows, null
            ) {
               Object L$1;
               Object L$2;
               Object L$3;
               int I$0;
               int I$1;
               int I$2;
               int label;

               {
                  super(2, `$completionx`);
                  this.$size = `$size`;
                  this.$step = `$step`;
                  this.$iterator = `$iterator`;
                  this.$reuseBuffer = `$reuseBuffer`;
                  this.$partialWindows = `$partialWindows`;
               }

               // $VF: Irreducible bytecode was duplicated to produce valid code
               @Override
               public final Object invokeSuspend(Object $result) {
                  var `$this$iterator`: SequenceScope;
                  var var9: Any;
                  var var10: Int;
                  var var12: Int;
                  var var14: RingBuffer;
                  label154: {
                     var skip: java.util.Iterator;
                     label171: {
                        `$this$iterator` = this.L$0 as SequenceScope;
                        var9 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                        var bufferx: ArrayList;
                        var var19: Int;
                        var var20: java.util.Iterator;
                        switch (this.label) {
                           case 0:
                              ResultKt.throwOnFailure(`$result`);
                              var10 = RangesKt.coerceAtMost(this.$size, 1024);
                              var12 = this.$step - this.$size;
                              if (this.$step - this.$size < 0) {
                                 var14 = new RingBuffer(var10);
                                 skip = this.$iterator;
                                 break label171;
                              }

                              bufferx = new ArrayList(var10);
                              var19 = 0;
                              var20 = this.$iterator;
                              break;
                           case 1: {
                              val skipx: Int = this.I$2;
                              var12 = this.I$1;
                              var10 = this.I$0;
                              val e: Any = this.L$3;
                              var20 = this.L$2 as java.util.Iterator;
                              bufferx = this.L$1 as ArrayList;
                              ResultKt.throwOnFailure(`$result`);
                              if (this.$reuseBuffer) {
                                 bufferx.clear();
                              } else {
                                 bufferx = new ArrayList(this.$size);
                              }

                              var19 = var12;
                              break;
                           }
                           case 2:
                              val skipx: Int = this.I$2;
                              var12 = this.I$1;
                              var10 = this.I$0;
                              val bufferxx: ArrayList = this.L$1 as ArrayList;
                              ResultKt.throwOnFailure(`$result`);
                              return Unit.INSTANCE;
                           case 3: {
                              var12 = this.I$1;
                              var10 = this.I$0;
                              val e: Any = this.L$3;
                              skip = this.L$2 as java.util.Iterator;
                              var14 = this.L$1 as RingBuffer;
                              ResultKt.throwOnFailure(`$result`);
                              var14.removeFirst(this.$step);
                              break label171;
                           }
                           case 4:
                              var12 = this.I$1;
                              var10 = this.I$0;
                              var14 = this.L$1 as RingBuffer;
                              ResultKt.throwOnFailure(`$result`);
                              var14.removeFirst(this.$step);
                              break label154;
                           case 5:
                              var12 = this.I$1;
                              var10 = this.I$0;
                              var14 = this.L$1 as RingBuffer;
                              ResultKt.throwOnFailure(`$result`);
                              return Unit.INSTANCE;
                           default:
                              throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                        }

                        while (var20.hasNext()) {
                           val var22: Any = var20.next();
                           if (var19 > 0) {
                              var19--;
                           } else {
                              bufferx.add(var22);
                              if (bufferx.size() == this.$size) {
                                 val var10002: Continuation = this;
                                 this.L$0 = `$this$iterator`;
                                 this.L$1 = bufferx;
                                 this.L$2 = var20;
                                 this.L$3 = SpillingKt.nullOutSpilledVariable(var22);
                                 this.I$0 = var10;
                                 this.I$1 = var12;
                                 this.I$2 = var19;
                                 this.label = 1;
                                 if (`$this$iterator`.yield(bufferx, var10002) === var9) {
                                    return var9;
                                 }

                                 if (this.$reuseBuffer) {
                                    bufferx.clear();
                                 } else {
                                    bufferx = new ArrayList(this.$size);
                                 }

                                 var19 = var12;
                              }
                           }
                        }

                        if (bufferx.isEmpty() || !this.$partialWindows && bufferx.size() != this.$size) {
                           return Unit.INSTANCE;
                        }

                        val var24: Continuation = this;
                        this.L$0 = SpillingKt.nullOutSpilledVariable(`$this$iterator`);
                        this.L$1 = SpillingKt.nullOutSpilledVariable(bufferx);
                        this.L$2 = null;
                        this.L$3 = null;
                        this.I$0 = var10;
                        this.I$1 = var12;
                        this.I$2 = var19;
                        this.label = 2;
                        if (`$this$iterator`.yield(bufferx, var24) === var9) {
                           return var9;
                        }

                        return Unit.INSTANCE;
                     }

                     while (skip.hasNext()) {
                        val var21: Any = skip.next();
                        var14.add(var21);
                        if (var14.isFull()) {
                           if (var14.size() < this.$size) {
                              var14 = var14.expanded(this.$size);
                           } else {
                              val var10001: java.util.List = if (this.$reuseBuffer) var14 else new ArrayList(var14);
                              val var25: Continuation = this;
                              this.L$0 = `$this$iterator`;
                              this.L$1 = var14;
                              this.L$2 = skip;
                              this.L$3 = SpillingKt.nullOutSpilledVariable(var21);
                              this.I$0 = var10;
                              this.I$1 = var12;
                              this.label = 3;
                              if (`$this$iterator`.yield(var10001, var25) === var9) {
                                 return var9;
                              }

                              var14.removeFirst(this.$step);
                           }
                        }
                     }

                     if (!this.$partialWindows) {
                        return Unit.INSTANCE;
                     }
                  }

                  while (buffer.size() > this.$step) {
                     val var23: java.util.List = if (this.$reuseBuffer) var14 else new ArrayList(var14);
                     val var26: Continuation = this;
                     this.L$0 = `$this$iterator`;
                     this.L$1 = var14;
                     this.L$2 = null;
                     this.L$3 = null;
                     this.I$0 = var10;
                     this.I$1 = var12;
                     this.label = 4;
                     if (`$this$iterator`.yield(var23, var26) === var9) {
                        return var9;
                     }

                     var14.removeFirst(this.$step);
                  }

                  if (!var14.isEmpty()) {
                     val var27: Continuation = this;
                     this.L$0 = SpillingKt.nullOutSpilledVariable(`$this$iterator`);
                     this.L$1 = SpillingKt.nullOutSpilledVariable(var14);
                     this.L$2 = null;
                     this.L$3 = null;
                     this.I$0 = var10;
                     this.I$1 = var12;
                     this.label = 5;
                     if (`$this$iterator`.yield(var14, var27) === var9) {
                        return var9;
                     }
                  }

                  return Unit.INSTANCE;
               }

               @Override
               public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                  val var3: Function2 = new <anonymous constructor>(
                     this.$size, this.$step, this.$iterator, this.$reuseBuffer, this.$partialWindows, `$completion`
                  );
                  var3.L$0 = value;
                  return var3 as Continuation<Unit>;
               }

               public final Object invoke(SequenceScope<? super java.util.List<? extends T>> p1, Continuation<? super Unit> p2) {
                  return (this.create(p1, p2) as <unrepresentable>).invokeSuspend(Unit.INSTANCE);
               }
            }
         ) as Function2
      ));
}
