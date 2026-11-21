package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonParser;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.JsonToken;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationContext;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.std.StdScalarDeserializer;

public class NioPathDeserializer extends StdScalarDeserializer<Path> {
   private static final long serialVersionUID = 1L;
   private static final boolean areWindowsFilePathsSupported;

   public NioPathDeserializer() {
      super(Path.class);
   }

   public Path deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      if (!p.hasToken(JsonToken.VALUE_STRING)) {
         return (Path)ctxt.handleUnexpectedToken(Path.class, p);
      } else {
         String value = p.getText();
         if (value.indexOf(58) < 0) {
            return Paths.get(value);
         } else if (areWindowsFilePathsSupported && value.length() >= 2 && Character.isLetter(value.charAt(0)) && value.charAt(1) == ':') {
            return Paths.get(value);
         } else {
            URI uri;
            try {
               uri = new URI(value);
            } catch (URISyntaxException var12) {
               return (Path)ctxt.handleInstantiationProblem(this.handledType(), value, var12);
            }

            try {
               return Paths.get(uri);
            } catch (FileSystemNotFoundException var10) {
               FileSystemNotFoundException cause = var10;

               try {
                  String scheme = uri.getScheme();

                  for (FileSystemProvider provider : ServiceLoader.load(FileSystemProvider.class)) {
                     if (provider.getScheme().equalsIgnoreCase(scheme)) {
                        return provider.getPath(uri);
                     }
                  }

                  return (Path)ctxt.handleInstantiationProblem(this.handledType(), value, cause);
               } catch (ServiceConfigurationError var9) {
                  var9.addSuppressed(var10);
                  return (Path)ctxt.handleInstantiationProblem(this.handledType(), value, var9);
               }
            } catch (Exception var11) {
               return (Path)ctxt.handleInstantiationProblem(this.handledType(), value, var11);
            }
         }
      }
   }

   static {
      boolean isWindowsRootFound = false;

      for (File file : File.listRoots()) {
         String path = file.getPath();
         if (path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
            isWindowsRootFound = true;
            break;
         }
      }

      areWindowsFilePathsSupported = isWindowsRootFound;
   }
}
