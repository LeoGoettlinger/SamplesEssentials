package ch.njol.util;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
public interface Closeable extends AutoCloseable {
   @Override
   void close();
}
