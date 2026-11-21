package kotlin.time

@SinceKotlin(version = "2.3")
@WasExperimental(markerClass = [ExperimentalTime::class])
@JvmName(name = "fromTimeSource")
public fun TimeSource.asClock(origin: Instant): Clock {
   return new Clock(`$this$asClock`, origin) {
      private final TimeMark startMark;

      {
         this.$origin = `$origin`;
         this.startMark = `$receiver`.markNow();
      }

      @Override
      public Instant now() {
         return this.$origin.plus-LRDsOJo(this.startMark.elapsedNow-UwyO8pc());
      }
   };
}
