package com.codeoftheweb.salvo;

import org.apache.catalina.Context;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.stereotype.Component;

@Component
public class SameSiteTomcatCookieProcessorCustomizationBean implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Override
    public void customize(TomcatServletWebServerFactory server) {

        server.getTomcatContextCustomizers().add(new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                DefaultCookieSerializer serializer = new DefaultCookieSerializer();
                Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
                cookieProcessor.setSameSiteCookies(SameSiteCookies.NONE.getValue());
                context.setCookieProcessor(cookieProcessor);
            }
        });
    }
}
