package kotlin.sequences

import java.util.ArrayList
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.IntrinsicsKt
import kotlin.coroutines.jvm.internal.Boxing
import kotlin.coroutines.jvm.internal.SpillingKt
import kotlin.internal.InlineOnly
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.jvm.functions.Function0
import kotlin.jvm.functions.Function2
import kotlin.random.Random

internal class SequencesKt__SequencesKt : SequencesKt__SequencesJVMKt {
   @InlineOnly
   @JvmStatic
   public inline fun <T> Sequence(crossinline iterator: () -> Iterator<T>): Sequence<T> {
      return new Sequence<T>(iterator) {
         {
            this.$iterator = `$iterator`;
         }

         @Override
         public java.util.Iterator<T> iterator() {
            return this.$iterator.invoke() as MutableIterator<T>;
         }
      };
   }

   @JvmStatic
   public fun <T> Iterator<T>.asSequence(): Sequence<T> {
      return SequencesKt.constrainOnce(new SequencesKt__SequencesKt$asSequence$$inlined$Sequence$1(`$this$asSequence`));
   }

   @JvmStatic
   public fun <T> sequenceOf(vararg elements: T): Sequence<T> {
      return (Sequence<T>)ArraysKt.asSequence(elements);
   }

   @SinceKotlin(version = "2.2")
   @JvmStatic
   public fun <T> sequenceOf(element: T): Sequence<T> {
      return new SequencesKt__SequencesKt$sequenceOf$$inlined$Sequence$1(element);
   }

   @SinceKotlin(version = "2.2")
   @InlineOnly
   @JvmStatic
   public inline fun <T> sequenceOf(): Sequence<T> {
      return SequencesKt.emptySequence();
   }

   @JvmStatic
   public fun <T> emptySequence(): Sequence<T> {
      return EmptySequence.INSTANCE;
   }

   @SinceKotlin(version = "1.3")
   @InlineOnly
   @JvmStatic
   public inline fun <T> Sequence<T>?.orEmpty(): Sequence<T> {
      var var10000: Sequence = `$this$orEmpty`;
      if (`$this$orEmpty` == null) {
         var10000 = SequencesKt.emptySequence();
      }

      return var10000;
   }

   @SinceKotlin(version = "1.3")
   @JvmStatic
   public fun <T> Sequence<T>.ifEmpty(defaultValue: () -> Sequence<T>): Sequence<T> {
      return SequencesKt.sequence((new Function2<SequenceScope<? super T>, Continuation<? super Unit>, Object>(`$this$ifEmpty`, defaultValue, null) {
         Object L$1;
         int label;

         {
            super(2, `$completionx`);
            this.$this_ifEmpty = `$receiver`;
            this.$defaultValue = `$defaultValue`;
         }

         @Override
         public final Object invokeSuspend(Object $result) {
            val `$this$sequence`: SequenceScope = this.L$0 as SequenceScope;
            val var4: Any = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            switch (this.label) {
               case 0: {
                  ResultKt.throwOnFailure(`$result`);
                  val var6: java.util.Iterator = this.$this_ifEmpty.iterator();
                  if (var6.hasNext()) {
                     val var7: Continuation = this;
                     this.L$0 = SpillingKt.nullOutSpilledVariable(`$this$sequence`);
                     this.L$1 = SpillingKt.nullOutSpilledVariable(var6);
                     this.label = 1;
                     if (`$this$sequence`.yieldAll(var6, var7) === var4) {
                        return var4;
                     }
                  } else {
                     val var10001: Sequence = this.$defaultValue.invoke() as Sequence;
                     val var10002: Continuation = this;
                     this.L$0 = SpillingKt.nullOutSpilledVariable(`$this$sequence`);
                     this.L$1 = SpillingKt.nullOutSpilledVariable(var6);
                     this.label = 2;
                     if (`$this$sequence`.yieldAll(var10001, var10002) === var4) {
                        return var4;
                     }
                  }
                  break;
               }
               case 1: {
                  val var5: java.util.Iterator = this.L$1 as java.util.Iterator;
                  ResultKt.throwOnFailure(`$result`);
                  break;
               }
               case 2: {
                  val iterator: java.util.Iterator = this.L$1 as java.util.Iterator;
                  ResultKt.throwOnFailure(`$result`);
                  break;
               }
               default:
                  throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            return Unit.INSTANCE;
         }

         @Override
         public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
            val var3: Function2 = new <anonymous constructor>(this.$this_ifEmpty, this.$defaultValue, `$completion`);
            var3.L$0 = value;
            return var3 as Continuation<Unit>;
         }

         public final Object invoke(SequenceScope<? super T> p1, Continuation<? super Unit> p2) {
            return (this.create(p1, p2) as <unrepresentable>).invokeSuspend(Unit.INSTANCE);
         }
      }) as Function2);
   }

   @JvmStatic
   public fun <T> Sequence<Sequence<T>>.flatten(): Sequence<T> {
      return flatten$SequencesKt__SequencesKt(`$this$flatten`, SequencesKt__SequencesKt::flatten$lambda$0$SequencesKt__SequencesKt);
   }

   @JvmName(name = "flattenSequenceOfIterable")
   @JvmStatic
   public fun <T> Sequence<Iterable<T>>.flatten(): Sequence<T> {
      return flatten$SequencesKt__SequencesKt(`$this$flatten`, SequencesKt__SequencesKt::flatten$lambda$1$SequencesKt__SequencesKt);
   }

   @JvmStatic
   private fun <T, R> Sequence<T>.flatten(iterator: (T) -> Iterator<R>): Sequence<R> {
      return (Sequence<R>)(if (`$this$flatten` is TransformingSequence)
         (`$this$flatten` as TransformingSequence).flatten$kotlin_stdlib(iterator)
         else
         new FlatteningSequence<>(`$this$flatten`, SequencesKt__SequencesKt::flatten$lambda$2$SequencesKt__SequencesKt, iterator));
   }

   @JvmStatic
   public fun <T, R> Sequence<Pair<T, R>>.unzip(): Pair<List<T>, List<R>> {
      val listT: ArrayList = new ArrayList();
      val listR: ArrayList = new ArrayList();

      for (Pair pair : $this$unzip) {
         listT.add(pair.getFirst());
         listR.add(pair.getSecond());
      }

      return TuplesKt.to(listT, listR);
   }

   @SinceKotlin(version = "1.4")
   @JvmStatic
   public fun <T> Sequence<T>.shuffled(): Sequence<T> {
      return SequencesKt.shuffled(`$this$shuffled`, Random.Default);
   }

   @SinceKotlin(version = "1.4")
   @JvmStatic
   public fun <T> Sequence<T>.shuffled(random: Random): Sequence<T> {
      return SequencesKt.sequence((new Function2<SequenceScope<? super T>, Continuation<? super Unit>, Object>(`$this$shuffled`, random, null) {
         Object L$1;
         Object L$2;
         Object L$3;
         int I$0;
         int label;

         {
            super(2, `$completionx`);
            this.$this_shuffled = `$receiver`;
            this.$random = `$random`;
         }

         // $VF: Irreducible bytecode was duplicated to produce valid code
         @Override
         public final Object invokeSuspend(Object $result) {
            val `$this$sequence`: SequenceScope = this.L$0 as SequenceScope;
            val var7: Any = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            var buffer: java.util.List;
            switch (this.label) {
               case 0:
                  ResultKt.throwOnFailure(`$result`);
                  buffer = SequencesKt.toMutableList(this.$this_shuffled);
                  break;
               case 1:
                  val j: Int = this.I$0;
                  val valuex: Any = this.L$3;
                  val last: Any = this.L$2;
                  buffer = this.L$1 as java.util.List;
                  ResultKt.throwOnFailure(`$result`);
                  break;
               default:
                  throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            while (!buffer.isEmpty()) {
               val var8: Int = this.$random.nextInt(buffer.size());
               val var9: Any = CollectionsKt.removeLast(buffer);
               val var10: Any = if (var8 < buffer.size()) buffer.set(var8, var9) else var9;
               val var10002: Continuation = this;
               this.L$0 = `$this$sequence`;
               this.L$1 = buffer;
               this.L$2 = SpillingKt.nullOutSpilledVariable(var9);
               this.L$3 = SpillingKt.nullOutSpilledVariable(var10);
               this.I$0 = var8;
               this.label = 1;
               if (`$this$sequence`.yield(var10, var10002) === var7) {
                  return var7;
               }
            }

            return Unit.INSTANCE;
         }

         @Override
         public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
            val var3: Function2 = new <anonymous constructor>(this.$this_shuffled, this.$random, `$completion`);
            var3.L$0 = value;
            return var3 as Continuation<Unit>;
         }

         public final Object invoke(SequenceScope<? super T> p1, Continuation<? super Unit> p2) {
            return (this.create(p1, p2) as <unrepresentable>).invokeSuspend(Unit.INSTANCE);
         }
      }) as Function2);
   }

   @JvmStatic
   internal fun <T, C, R> flatMapIndexed(source: Sequence<T>, transform: (Int, T) -> C, iterator: (C) -> Iterator<R>): Sequence<R> {
      return SequencesKt.sequence((new Function2<SequenceScope<? super R>, Continuation<? super Unit>, Object>(source, transform, iterator, null) {
         Object L$1;
         Object L$2;
         Object L$3;
         int I$0;
         int label;

         {
            super(2, `$completionx`);
            this.$source = `$source`;
            this.$transform = `$transform`;
            this.$iterator = `$iterator`;
         }

         // $VF: Irreducible bytecode was duplicated to produce valid code
         @Override
         public final Object invokeSuspend(Object $result) {
            val `$this$sequence`: SequenceScope = this.L$0 as SequenceScope;
            val var8: Any = IntrinsicsKt.getCOROUTINE_SUSPENDED();
            var index: Int;
            var var4: java.util.Iterator;
            switch (this.label) {
               case 0:
                  ResultKt.throwOnFailure(`$result`);
                  index = 0;
                  var4 = this.$source.iterator();
                  break;
               case 1:
                  index = this.I$0;
                  val result: Any = this.L$3;
                  val element: Any = this.L$2;
                  var4 = this.L$1 as java.util.Iterator;
                  ResultKt.throwOnFailure(`$result`);
                  break;
               default:
                  throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }

            while (var4.hasNext()) {
               val var9: Any = var4.next();
               val var7: Int = index++;
               if (var7 < 0) {
                  CollectionsKt.throwIndexOverflow();
               }

               val var10: Any = this.$transform.invoke(Boxing.boxInt(var7), var9);
               val var11: java.util.Iterator = this.$iterator.invoke(var10) as java.util.Iterator;
               val var10002: Continuation = this;
               this.L$0 = `$this$sequence`;
               this.L$1 = var4;
               this.L$2 = SpillingKt.nullOutSpilledVariable(var9);
               this.L$3 = SpillingKt.nullOutSpilledVariable(var10);
               this.I$0 = index;
               this.label = 1;
               if (`$this$sequence`.yieldAll(var11, var10002) === var8) {
                  return var8;
               }
            }

            return Unit.INSTANCE;
         }

         @Override
         public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
            val var3: Function2 = new <anonymous constructor>(this.$source, this.$transform, this.$iterator, `$completion`);
            var3.L$0 = value;
            return var3 as Continuation<Unit>;
         }

         public final Object invoke(SequenceScope<? super R> p1, Continuation<? super Unit> p2) {
            return (this.create(p1, p2) as <unrepresentable>).invokeSuspend(Unit.INSTANCE);
         }
      }) as Function2);
   }

   @JvmStatic
   public fun <T> Sequence<T>.constrainOnce(): Sequence<T> {
      return (Sequence<T>)(if (`$this$constrainOnce` is ConstrainedOnceSequence) `$this$constrainOnce` else new ConstrainedOnceSequence(`$this$constrainOnce`));
   }

   @JvmStatic
   public fun <T : Any> generateSequence(nextFunction: () -> T?): Sequence<T> {
      return (Sequence<T>)SequencesKt.constrainOnce(
         new GeneratorSequence<>(nextFunction, SequencesKt__SequencesKt::generateSequence$lambda$0$SequencesKt__SequencesKt)
      );
   }

   @LowPriorityInOverloadResolution
   @JvmStatic
   public fun <T : Any> generateSequence(seed: T?, nextFunction: (T) -> T?): Sequence<T> {
      return (Sequence<T>)(if (seed == null)
         EmptySequence.INSTANCE
         else
         new GeneratorSequence(SequencesKt__SequencesKt::generateSequence$lambda$1$SequencesKt__SequencesKt, nextFunction));
   }

   @JvmStatic
   public fun <T : Any> generateSequence(seedFunction: () -> T?, nextFunction: (T) -> T?): Sequence<T> {
      return new GeneratorSequence(seedFunction, nextFunction);
   }

   @JvmStatic
   fun `flatten$lambda$0$SequencesKt__SequencesKt`(it: Sequence): java.util.Iterator {
      return it.iterator();
   }

   @JvmStatic
   fun `flatten$lambda$1$SequencesKt__SequencesKt`(it: java.lang.Iterable): java.util.Iterator {
      return it.iterator();
   }

   @JvmStatic
   fun `flatten$lambda$2$SequencesKt__SequencesKt`(it: Any): Any {
      return it;
   }

   @JvmStatic
   fun `generateSequence$lambda$0$SequencesKt__SequencesKt`(`$nextFunction`: Function0, it: Any): Any {
      return `$nextFunction`.invoke();
   }

   @JvmStatic
   fun `generateSequence$lambda$1$SequencesKt__SequencesKt`(`$seed`: Any): Any {
      return `$seed`;
   }

   open fun SequencesKt__SequencesKt() {
   }
}
