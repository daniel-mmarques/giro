package com.giro.utils;

import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.common.impl.ExtensionsBuilder;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.impl.XSAnyBuilder;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.signature.impl.SignatureBuilder;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;

public class SAML20Utils {

    private static final AuthnRequestBuilder _AuthnRequestBuilder = new AuthnRequestBuilder();
    private static final IssuerBuilder _IssuerBuilder = new IssuerBuilder();
    private static final ExtensionsBuilder _ExtensionsBuilder = new ExtensionsBuilder();
    private static final XSAnyBuilder _XSAnyBuilder = new XSAnyBuilder();
    private static final SignatureBuilder _SignatureBuilder = new SignatureBuilder();

    static {
        try {
            DefaultBootstrap.bootstrap();
        } catch (Exception e) {
        }
    }

    public static AuthnRequest createAuthRequest(String id, DateTime requestInstant, String urlLoginDestination,
                                                 String urlResponseDestination, String providerName, String issuer, boolean createExtensions) {

        AuthnRequest authReq = _AuthnRequestBuilder.buildObject(Saml20Constants.SAML_20_PROTOCOL_NAMESPACE, "AuthnRequest",
                Saml20Constants.SAML_20_PROTOCOL_PREFIX);

        authReq.setID(id);
        authReq.setVersion(SAMLVersion.VERSION_20);
        authReq.setIssueInstant(requestInstant);
        authReq.setDestination(urlLoginDestination);
        authReq.setConsent(Saml20Constants.SAML_20_CONSENT);
        authReq.setProtocolBinding(Saml20Constants.SAML_20_BINDINGS);
        authReq.setAssertionConsumerServiceURL(urlResponseDestination);
        authReq.setProviderName(providerName);

        authReq.setIssuer(createIssuer(issuer));

        if (createExtensions) {
            authReq.setExtensions(createExtensions());
        }

        return authReq;
    }

    public static Extensions createExtensions() {
        Extensions extensions = _ExtensionsBuilder.buildObject(Saml20Constants.SAML_20_PROTOCOL_NAMESPACE, "Extensions",
                Saml20Constants.SAML_20_PROTOCOL_PREFIX);
        return extensions;
    }

    public static Issuer createIssuer(String value) {
        Issuer issuer = _IssuerBuilder.buildObject(Saml20Constants.SAML_20_ASSERTION_NAMESPACE, "Issuer",
                Saml20Constants.SAML_20_ASSERTION_PREFIX);

        issuer.setValue(value);

        return issuer;
    }

    public static String signAuthnRequestAndConvertToBase64String(AuthnRequest authReq) {

        try {
            Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(authReq);
            Element elem = marshaller.marshall(authReq);
            Document document = elem.getOwnerDocument();
            Signer.signObject(authReq.getSignature());
            DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
            LSSerializer serializer = domImplLS.createLSSerializer();
            String str = serializer.writeToString(elem);

            return base64Encode(str);
        } catch (Exception e) {
            throw new RuntimeException("Exception when converting the AuthnRequest to base64 String", e);
        }
    }

    public static Response convertBase64StringToSAML20Response(String responseBase64) {

        try {

            String responseStr = "<?xml version='1.0' encoding='UTF-8'?>" + base64Decode(responseBase64);

            ByteArrayInputStream is = new ByteArrayInputStream(responseStr.getBytes());

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            Element element = documentBuilderFactory.newDocumentBuilder().parse(is).getDocumentElement();

            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);

            return (Response) unmarshaller.unmarshall(element);

        } catch (Exception e) {
            throw new RuntimeException("Exception when converting the base64 String to a standard SAML 2.0 Response",
                    e);
        }
    }

    public static void ValidateSAML20Response(Response response, Path faCertificatePath, int expirationMinutes)
            throws ValidationException {

        final DateTime now = DateTime.now();
        final DateTime responseTimeStamp = response.getIssueInstant();
        final int difMinutes = Minutes.minutesBetween(now, responseTimeStamp).getMinutes();

        if (expirationMinutes < 0) {
            expirationMinutes *= -1;
        }

        if (difMinutes > expirationMinutes || difMinutes < (-1 * expirationMinutes)) {
            throw new ValidationException(
                    "The response timestamp is not valid, please syncronize both servers clocks: { ID="
                            + response.getID() + ", InResponseTo=" + response.getInResponseTo() + ", IssueInstant="
                            + response.getIssueInstant() + ", ServerInstant=" + now + ", DifMinutes=" + difMinutes
                            + " }");
        }

        StatusCode statusCode = response.getStatus().getStatusCode();
        StatusMessage statusMessage = response.getStatus().getStatusMessage();

        if (statusCode == null || !StatusCode.SUCCESS_URI.endsWith(statusCode.getValue())) {
            throw new RuntimeException(formatException(statusCode, statusMessage));
        }

        final SignatureValidator signatureValidator = new SignatureValidator(getCredential(faCertificatePath));
        signatureValidator.validate(response.getSignature());
    }

    public static String base64Encode(String str) {
        if (str == null) {
            return null;
        }

        return Base64.encode(str.getBytes());
    }

    public static String base64Decode(String str) throws Base64DecodingException {
        if (str == null) {
            return null;
        }

        return new String(Base64.decode(str));
    }

    private static String formatException(StatusCode statusCode, StatusMessage statusMessage) {
        StringBuilder message = new StringBuilder();
        message.append("A resposta SAML indica falha de autenticação: ");
        message.append("{ StatusCode = ");
        message.append(statusCode != null ? statusCode.getValue() : "N/A");
        message.append("{ StatusMessage = ");
        message.append(statusMessage != null ? statusMessage.getMessage() : "N/A");
        message.append(" }");

        return message.toString();
    }

    public static XMLObject createFAAALevel(String fAAALevelValue) {
        XSAny fAAALevel = _XSAnyBuilder.buildObject("http://autenticacao.cartaodecidadao.pt/atributos", "FAAALevel",
                "fa");
        fAAALevel.setTextContent(fAAALevelValue);
        return fAAALevel;
    }

    private static Credential createCredential(Path certPath) {
        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(certPath.toFile());
            BasicX509Credential publicCredential = new BasicX509Credential();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fileStream);
            fileStream.close();
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(certificate.getPublicKey().getEncoded());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            java.security.PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            if (publicKey == null) {
                throw new RuntimeException("Not possible to export the public key from the given certificate");
            }
            publicCredential.setPublicKey(publicKey);
            return publicCredential;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(certPath != null
                    ? "Não foi possivel encontrar o certificado com a chave pública do Fornecedor de Autenticação ("
                    .concat(certPath.toString())
                    .concat(")")
                    : "", e);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Não foi possivel carregar o certificado com a chave pública do Fornecedor de Autenticação", e);
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static synchronized Credential getCredential(Path faCertificatePath) {
        if (faCertificatePath == null) {
            throw new RuntimeException(
                    "Não foi possivel encontrar a configuração com o caminho para o certificado com a chave pública do Fornecedor de Autenticação");
        }
        return createCredential(faCertificatePath);
    }

}
