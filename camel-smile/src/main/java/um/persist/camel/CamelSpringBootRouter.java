package um.persist.camel;

import um.persist.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;

import javax.net.ssl.SSLContext;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.jetty.JettyHttpComponent;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.springframework.stereotype.Component;

import io.netty.handler.codec.http.HttpScheme;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 *
 * @author UM FERI
 * @date NOV 2020
 * @description Camel main class used for testing of SSL and web servers
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */

@Component
public class CamelSpringBootRouter extends RouteBuilder {

	/*
	 * 1*private Endpoint setupSSLConext(CamelContext camelContext) throws Exception
	 * {
	 * 
	 * KeyStoreParameters keyStoreParameters = new KeyStoreParameters(); // Change
	 * this path to point to your truststore/keystore as jks files
	 * keyStoreParameters.setResource("/etc/ssl/demo.jks");
	 * keyStoreParameters.setPassword("password");
	 * 
	 * KeyManagersParameters keyManagersParameters = new KeyManagersParameters();
	 * keyManagersParameters.setKeyStore(keyStoreParameters);
	 * keyManagersParameters.setKeyPassword("password");
	 * 
	 * TrustManagersParameters trustManagersParameters = new
	 * TrustManagersParameters();
	 * trustManagersParameters.setKeyStore(keyStoreParameters);
	 * 
	 * SSLContextParameters sslContextParameters = new SSLContextParameters();
	 * sslContextParameters.setKeyManagers(keyManagersParameters);
	 * sslContextParameters.setTrustManagers(trustManagersParameters);
	 * 
	 * HttpComponent httpComponent = camelContext.getComponent("https",
	 * HttpComponent.class);
	 * httpComponent.setSslContextParameters(sslContextParameters); //This is
	 * important to make your cert skip CN/Hostname checks
	 * //httpComponent.setX509HostnameVerifier(new AllowAllHostnameVerifier());
	 * //This is important to make your cert skip CN/Hostname checks
	 * httpComponent.setX509HostnameVerifier(new X509HostnameVerifier() {
	 * 
	 * @Override public void verify(String s, SSLSocket sslSocket) throws
	 * IOException {
	 * 
	 * }
	 * 
	 * @Override public void verify(String s, X509Certificate x509Certificate)
	 * throws SSLException {
	 * 
	 * }
	 * 
	 * @Override public void verify(String s, String[] strings, String[] strings1)
	 * throws SSLException {
	 * 
	 * }
	 * 
	 * @Override public boolean verify(String s, SSLSession sslSession) { //I don't
	 * mind just return true for all or you can add your own logic return true; }
	 * });
	 * 
	 * return httpComponent.createEndpoint("https:localhost"); }*1
	 */

	/*private void configureJetty() {
		KeyStoreParameters ksp = new KeyStoreParameters();
		ksp.setResource("/etc/ssl/demo.jks");
		ksp.setPassword("password");
		KeyManagersParameters kmp = new KeyManagersParameters();
		kmp.setKeyStore(ksp);
		kmp.setKeyPassword("password");
		SSLContextParameters scp = new SSLContextParameters();
		scp.setKeyManagers(kmp);
		JettyHttpComponent jettyComponent = getContext().getComponent("jetty", JettyHttpComponent.class);
		jettyComponent.setSslContextParameters(scp);
	}

	private void configureHttp4() {
		KeyStoreParameters ksp = new KeyStoreParameters();
		ksp.setResource("/etc/ssl/demo.jks");
		ksp.setPassword("password");
		TrustManagersParameters tmp = new TrustManagersParameters();
		tmp.setKeyStore(ksp);
		SSLContextParameters scp = new SSLContextParameters();
		scp.setTrustManagers(tmp);
		HttpComponent httpComponent = getContext().getComponent("https", HttpComponent.class);
		httpComponent.setSslContextParameters(scp);
	}*/
	
	
	@Override
	public void configure() throws Exception {

		/*
		 * configureJetty(); configureHttp4();
		 * from("jetty:https://0.0.0.0:8080/api-doc/?matchOnUriPrefix=true") .to(
		 * "https://google.com/?q=ssl&bridgeEndpoint=true&throwExceptionOnFailure=false"
		 * );
		 */

		/*
		 * 1*Endpoint httpsEndpoint = setupSSLConext(getContext());
		 * 
		 * from("timer:demo") .to(httpsEndpoint) .choice()
		 * .when(simple("${headers.CamelHttpResponseCode} == 200")) .log("Success")
		 * .otherwise() .log("Failed");*1
		 */

	}

}
