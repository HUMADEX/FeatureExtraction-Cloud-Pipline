package um.persist.camel;

import org.apache.camel.spi.annotations.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import um.persist.*;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author UM FERI
 * @date NOV 2020
 * @description Camel config for web servers and redirects
 *
 */

@Configuration
public class CamelSpringBootWebConfig {
	
	/*@Bean
	public TomcatServletWebServerFactory httpsRedirectConfig() {
		return new TomcatServletWebServerFactory () {
			@Override
			protected void postProcessContext(Context context) {
				SecurityConstraint securityConstraint = new SecurityConstraint();
				securityConstraint.setUserConstraint("CONFIDENTIAL");
				SecurityCollection collection = new SecurityCollection();
				collection.addPattern("/*");
				securityConstraint.addCollection(collection);
				context.addConstraint(securityConstraint);
			}
		};
	}*/
	/*
	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				SecurityConstraint securityConstraint = new SecurityConstraint();
				securityConstraint.setUserConstraint("CONFIDENTIAL");
				SecurityCollection collection = new SecurityCollection();
				collection.addPattern("/*");
				securityConstraint.addCollection(collection);
				context.addConstraint(securityConstraint);
			}
		};
		tomcat.addAdditionalTomcatConnectors(redirectConnector());
		return tomcat;
	}

	private Connector redirectConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("http");
		connector.setPort(8081);
		connector.setSecure(false);
		connector.setRedirectPort(8080);
		return connector;
	}
	*/
	
	/*//@Component("")
	public class HttpsConfig implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestedPort = String.valueOf(request.getServerPort()); //if you're not behind a proxy
        //String requestedPort = request.getHeader("X-Forwarded-Port"); // I'm behind a proxy on Heroku

        if (requestedPort != null && requestedPort.equals("80")) { // This will still allow requests on :8080
            response.sendRedirect("https://" + request.getServerName() + request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            return false;
        }
        return true;
    }

	}
	
	@Configuration
	public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HttpsConfig());
    }
	}*/
	

}