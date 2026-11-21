package net.kyori.adventure.util;

public interface Codec<D, E, DX extends Throwable, EX extends Throwable> {
   static <D, E, DX extends Throwable, EX extends Throwable> Codec<D, E, DX, EX> codec(
      final Codec.Decoder<D, E, DX> decoder, final Codec.Encoder<D, E, EX> encoder
   ) {
      return new Codec<D, E, DX, EX>() {
         @Override
         public D decode(final E encoded) throws DX {
            return decoder.decode(encoded);
         }

         @Override
         public E encode(final D decoded) throws EX {
            return encoder.encode(decoded);
         }
      };
   }

   D decode(final E encoded) throws DX;

   E encode(final D decoded) throws EX;

   public interface Decoder<D, E, X extends Throwable> {
      D decode(final E encoded) throws X;
   }

   public interface Encoder<D, E, X extends Throwable> {
      E encode(final D decoded) throws X;
   }
}
