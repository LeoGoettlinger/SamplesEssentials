package kotlin.text

import java.util.regex.Matcher
import kotlin.internal.PlatformImplementationsKt

private class MatcherMatchResult(matcher: Matcher, input: CharSequence) : MatchResult {
   private final val matcher: Matcher
   private final val input: CharSequence

   private final val matchResult: java.util.regex.MatchResult
      private final get() {
         return this.matcher;
      }


   public open val range: IntRange
      public open get() {
         return RegexKt.access$range(this.getMatchResult());
      }


   public open val value: String
      public open get() {
         val var10000: java.lang.String = this.getMatchResult().group();
         return var10000;
      }


   public open val groups: MatchGroupCollection
   private final var groupValues_: List<String>?

   public open val groupValues: List<String>
      public open get() {
         if (this.groupValues_ == null) {
            this.groupValues_ = new AbstractList<java.lang.String>(this) {
               {
                  this.this$0 = `$receiver`;
               }

               @Override
               public int getSize() {
                  return MatcherMatchResult.access$getMatchResult(this.this$0).groupCount() + 1;
               }

               public java.lang.String get(int index) {
                  var var10000: java.lang.String = MatcherMatchResult.access$getMatchResult(this.this$0).group(index);
                  if (var10000 == null) {
                     var10000 = "";
                  }

                  return var10000;
               }
            };
         }

         val var10000: java.util.List = this.groupValues_;
         return var10000;
      }


   init {
      this.matcher = matcher;
      this.input = input;
      this.groups = (
         new MatchNamedGroupCollection(this) {
            {
               this.this$0 = `$receiver`;
            }

            @Override
            public int getSize() {
               return MatcherMatchResult.access$getMatchResult(this.this$0).groupCount() + 1;
            }

            @Override
            public boolean isEmpty() {
               return false;
            }

            @Override
            public java.util.Iterator<MatchGroup> iterator() {
               return SequencesKt.map(CollectionsKt.asSequence(CollectionsKt.getIndices(this as MutableCollection<*>)), <unrepresentable>::iterator$lambda$0)
                  .iterator();
            }

            @Override
            public MatchGroup get(int index) {
               val range: IntRange = RegexKt.access$range(MatcherMatchResult.access$getMatchResult(this.this$0), index);
               val var10000: MatchGroup;
               if (range.getStart() >= 0) {
                  val var10002: java.lang.String = MatcherMatchResult.access$getMatchResult(this.this$0).group(index);
                  var10000 = new MatchGroup(var10002, range);
               } else {
                  var10000 = null;
               }

               return var10000;
            }

            @Override
            public MatchGroup get(java.lang.String name) {
               return PlatformImplementationsKt.IMPLEMENTATIONS.getMatchResultNamedGroup(MatcherMatchResult.access$getMatchResult(this.this$0), name);
            }

            private static final MatchGroup iterator$lambda$0(<unrepresentable> this$0, int it) {
               return `this$0`.get(it);
            }
         }
      ) as MatchGroupCollection;
   }

   public override fun next(): MatchResult? {
      val nextIndex: Int = this.getMatchResult().end() + (if (this.getMatchResult().end() == this.getMatchResult().start()) 1 else 0);
      val var2: MatchResult;
      if (nextIndex <= this.input.length()) {
         val var10000: Matcher = this.matcher.pattern().matcher(this.input);
         var2 = RegexKt.access$findNext(var10000, nextIndex, this.input);
      } else {
         var2 = null;
      }

      return var2;
   }
}
