package org.postgresql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLXML;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.xml.DefaultPGXmlFactoryFactory;
import org.postgresql.xml.PGXmlFactoryFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class PgSQLXML implements SQLXML {
   private final ResourceLock lock = new ResourceLock();
   private final BaseConnection conn;
   @Nullable
   private String data;
   private boolean initialized;
   private boolean active;
   private boolean freed;
   @Nullable
   private ByteArrayOutputStream byteArrayOutputStream;
   @Nullable
   private StringWriter stringWriter;
   @Nullable
   private DOMResult domResult;

   public PgSQLXML(BaseConnection conn) {
      this(conn, null, false);
   }

   public PgSQLXML(BaseConnection conn, @Nullable String data) {
      this(conn, data, true);
   }

   private PgSQLXML(BaseConnection conn, @Nullable String data, boolean initialized) {
      this.conn = conn;
      this.data = data;
      this.initialized = initialized;
      this.active = false;
      this.freed = false;
   }

   private PGXmlFactoryFactory getXmlFactoryFactory() throws SQLException {
      return (PGXmlFactoryFactory)(this.conn != null ? this.conn.getXmlFactoryFactory() : DefaultPGXmlFactoryFactory.INSTANCE);
   }

   @Override
   public void free() {
      ResourceLock ignore = this.lock.obtain();

      try {
         this.freed = true;
         this.data = null;
      } catch (Throwable var5) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   @Nullable
   @Override
   public InputStream getBinaryStream() throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      InputStream var7;
      label46: {
         try {
            this.checkFreed();
            this.ensureInitialized();
            if (this.data == null) {
               var7 = null;
               break label46;
            }

            try {
               var7 = new ByteArrayInputStream(this.conn.getEncoding().encode(this.data));
            } catch (IOException var5) {
               throw new PSQLException("Failed to re-encode xml data.", PSQLState.DATA_ERROR, var5);
            }
         } catch (Throwable var6) {
            if (ignore != null) {
               try {
                  ignore.close();
               } catch (Throwable var4) {
                  var6.addSuppressed(var4);
               }
            }

            throw var6;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var7;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var7;
   }

   @Nullable
   @Override
   public Reader getCharacterStream() throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      Reader var6;
      label43: {
         try {
            this.checkFreed();
            this.ensureInitialized();
            if (this.data == null) {
               var6 = null;
               break label43;
            }

            var6 = new StringReader(this.data);
         } catch (Throwable var5) {
            if (ignore != null) {
               try {
                  ignore.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (ignore != null) {
            ignore.close();
         }

         return var6;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var6;
   }

   @Nullable
   @Override
   public <T extends Source> T getSource(@Nullable Class<T> sourceClass) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      Source var14;
      label96: {
         Source var17;
         label97: {
            label98: {
               label99: {
                  DOMSource var7;
                  try {
                     label100: {
                        this.checkFreed();
                        this.ensureInitialized();
                        String data = this.data;
                        if (data == null) {
                           var14 = null;
                           break label96;
                        }

                        try {
                           if (sourceClass == null || DOMSource.class.equals(sourceClass)) {
                              DocumentBuilder builder = this.getXmlFactoryFactory().newDocumentBuilder();
                              InputSource input = new InputSource(new StringReader(data));
                              DOMSource domSource = new DOMSource(builder.parse(input));
                              var7 = domSource;
                              break label100;
                           }

                           if (SAXSource.class.equals(sourceClass)) {
                              XMLReader reader = this.getXmlFactoryFactory().createXMLReader();
                              InputSource is = new InputSource(new StringReader(data));
                              var17 = sourceClass.cast(new SAXSource(reader, is));
                              break label97;
                           }

                           if (StreamSource.class.equals(sourceClass)) {
                              var14 = sourceClass.cast(new StreamSource(new StringReader(data)));
                              break label98;
                           }

                           if (StAXSource.class.equals(sourceClass)) {
                              XMLInputFactory xif = this.getXmlFactoryFactory().newXMLInputFactory();
                              XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(data));
                              var17 = sourceClass.cast(new StAXSource(xsr));
                              break label99;
                           }
                        } catch (Exception var9) {
                           throw new PSQLException(GT.tr("Unable to decode xml data."), PSQLState.DATA_ERROR, var9);
                        }

                        throw new PSQLException(GT.tr("Unknown XML Source class: {0}", sourceClass), PSQLState.INVALID_PARAMETER_TYPE);
                     }
                  } catch (Throwable var10) {
                     if (ignore != null) {
                        try {
                           ignore.close();
                        } catch (Throwable var8) {
                           var10.addSuppressed(var8);
                        }
                     }

                     throw var10;
                  }

                  if (ignore != null) {
                     ignore.close();
                  }

                  return (T)var7;
               }

               if (ignore != null) {
                  ignore.close();
               }

               return (T)var17;
            }

            if (ignore != null) {
               ignore.close();
            }

            return (T)var14;
         }

         if (ignore != null) {
            ignore.close();
         }

         return (T)var17;
      }

      if (ignore != null) {
         ignore.close();
      }

      return (T)var14;
   }

   @Nullable
   @Override
   public String getString() throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      String var2;
      try {
         this.checkFreed();
         this.ensureInitialized();
         var2 = this.data;
      } catch (Throwable var5) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var2;
   }

   @Override
   public OutputStream setBinaryStream() throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      ByteArrayOutputStream var2;
      try {
         this.checkFreed();
         this.initialize();
         this.active = true;
         this.byteArrayOutputStream = new ByteArrayOutputStream();
         var2 = this.byteArrayOutputStream;
      } catch (Throwable var5) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var2;
   }

   @Override
   public Writer setCharacterStream() throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      StringWriter var2;
      try {
         this.checkFreed();
         this.initialize();
         this.active = true;
         this.stringWriter = new StringWriter();
         var2 = this.stringWriter;
      } catch (Throwable var5) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (ignore != null) {
         ignore.close();
      }

      return var2;
   }

   @Override
   public <T extends Result> T setResult(Class<T> resultClass) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      Result var15;
      label83: {
         Result var12;
         label84: {
            Result var6;
            label85: {
               try {
                  this.checkFreed();
                  this.initialize();
                  if (resultClass != null && !DOMResult.class.equals(resultClass)) {
                     if (SAXResult.class.equals(resultClass)) {
                        try {
                           SAXTransformerFactory transformerFactory = this.getXmlFactoryFactory().newSAXTransformerFactory();
                           TransformerHandler transformerHandler = transformerFactory.newTransformerHandler();
                           this.stringWriter = new StringWriter();
                           transformerHandler.setResult(new StreamResult(this.stringWriter));
                           this.active = true;
                           var15 = resultClass.cast(new SAXResult(transformerHandler));
                           break label83;
                        } catch (TransformerException var8) {
                           throw new PSQLException(GT.tr("Unable to create SAXResult for SQLXML."), PSQLState.UNEXPECTED_ERROR, var8);
                        }
                     }

                     if (StreamResult.class.equals(resultClass)) {
                        this.stringWriter = new StringWriter();
                        this.active = true;
                        var12 = resultClass.cast(new StreamResult(this.stringWriter));
                        break label84;
                     }

                     if (StAXResult.class.equals(resultClass)) {
                        StringWriter stringWriter = new StringWriter();
                        this.stringWriter = stringWriter;

                        try {
                           XMLOutputFactory xof = this.getXmlFactoryFactory().newXMLOutputFactory();
                           XMLStreamWriter xsw = xof.createXMLStreamWriter(stringWriter);
                           this.active = true;
                           var6 = resultClass.cast(new StAXResult(xsw));
                           break label85;
                        } catch (XMLStreamException var9) {
                           throw new PSQLException(GT.tr("Unable to create StAXResult for SQLXML"), PSQLState.UNEXPECTED_ERROR, var9);
                        }
                     }

                     throw new PSQLException(GT.tr("Unknown XML Result class: {0}", resultClass), PSQLState.INVALID_PARAMETER_TYPE);
                  }

                  this.domResult = new DOMResult();
                  this.active = true;
                  var12 = this.domResult;
               } catch (Throwable var10) {
                  if (ignore != null) {
                     try {
                        ignore.close();
                     } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                     }
                  }

                  throw var10;
               }

               if (ignore != null) {
                  ignore.close();
               }

               return (T)var12;
            }

            if (ignore != null) {
               ignore.close();
            }

            return (T)var6;
         }

         if (ignore != null) {
            ignore.close();
         }

         return (T)var12;
      }

      if (ignore != null) {
         ignore.close();
      }

      return (T)var15;
   }

   @Override
   public void setString(String value) throws SQLException {
      ResourceLock ignore = this.lock.obtain();

      try {
         this.checkFreed();
         this.initialize();
         this.data = value;
      } catch (Throwable var6) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   private void checkFreed() throws SQLException {
      if (this.freed) {
         throw new PSQLException(GT.tr("This SQLXML object has already been freed."), PSQLState.OBJECT_NOT_IN_STATE);
      }
   }

   private void ensureInitialized() throws SQLException {
      if (!this.initialized) {
         throw new PSQLException(GT.tr("This SQLXML object has not been initialized, so you cannot retrieve data from it."), PSQLState.OBJECT_NOT_IN_STATE);
      } else if (this.active) {
         if (this.byteArrayOutputStream != null) {
            try {
               this.data = this.conn.getEncoding().decode(this.byteArrayOutputStream.toByteArray());
            } catch (IOException var18) {
               throw new PSQLException(
                  GT.tr("Failed to convert binary xml data to encoding: {0}.", this.conn.getEncoding().name()), PSQLState.DATA_ERROR, var18
               );
            } finally {
               this.byteArrayOutputStream = null;
               this.active = false;
            }
         } else if (this.stringWriter != null) {
            this.data = this.stringWriter.toString();
            this.stringWriter = null;
            this.active = false;
         } else if (this.domResult != null) {
            DOMResult domResult = this.domResult;

            try {
               TransformerFactory factory = this.getXmlFactoryFactory().newTransformerFactory();
               Transformer transformer = factory.newTransformer();
               DOMSource domSource = new DOMSource(domResult.getNode());
               StringWriter stringWriter = new StringWriter();
               StreamResult streamResult = new StreamResult(stringWriter);
               transformer.transform(domSource, streamResult);
               this.data = stringWriter.toString();
            } catch (TransformerException var16) {
               throw new PSQLException(GT.tr("Unable to convert DOMResult SQLXML data to a string."), PSQLState.DATA_ERROR, var16);
            } finally {
               DOMResult var20 = null;
               this.active = false;
            }
         }
      }
   }

   private void initialize() throws SQLException {
      if (this.initialized) {
         throw new PSQLException(GT.tr("This SQLXML object has already been initialized, so you cannot manipulate it further."), PSQLState.OBJECT_NOT_IN_STATE);
      } else {
         this.initialized = true;
      }
   }
}
