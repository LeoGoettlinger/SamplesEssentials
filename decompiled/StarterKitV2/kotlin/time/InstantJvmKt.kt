package kotlin.time

import kotlin.internal.PlatformImplementationsKt

private final val systemClock: Clock = PlatformImplementationsKt.IMPLEMENTATIONS.getSystemClock()

internal fun systemClockNow(): Instant {
   return systemClock.now();
}

internal fun serializedInstant(instant: Instant): Any {
   return new InstantSerialized(instant.getEpochSeconds(), instant.getNanosecondsOfSecond());
}
