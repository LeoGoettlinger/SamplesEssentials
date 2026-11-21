package org.jsoup.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.jspecify.annotations.NullMarked;

@Deprecated
@Documented
@NullMarked
@Retention(RetentionPolicy.CLASS)
public @interface NonnullByDefault {
}
