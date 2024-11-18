package net.brianlevine.keycloak.graphql.util;

import io.vertx.core.MultiMap;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.resteasy.reactive.common.util.CookieParser;
import org.jboss.resteasy.reactive.common.util.DateUtil;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Wraps a Vert.x HeadersMultiMap in a jakarta.ws.rs.core.HttpHeaders with a lot of help
 * from RestEasy
 */
public class HttpHeaderWrapper  implements HttpHeaders {
    private final MultiMap headers;
    private final Map<String, Cookie> cookies;

    MultivaluedMap<String, String> jheaders;

    public HttpHeaderWrapper(MultiMap headers) {
        this.headers = headers;
        this.cookies = new HashMap<>();
    }

    @Override
    public List<String> getRequestHeader(String name) {
        return headers.getAll(name);
    }

    @Override
    public String getHeaderString(String name) {
        List<String> vals = getRequestHeader(name);
        if (vals == null)
            return null;
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String val : vals) {
            if (first)
                first = false;
            else
                builder.append(",");
            builder.append(val);
        }
        return builder.toString();
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {

        if (jheaders == null) {
            jheaders = new MultivaluedHashMap<>();

            for (MultivaluedMap.Entry<String, String> entry : headers) {
                jheaders.add(entry.getKey(), entry.getValue());
            }
        }

        return jheaders;
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return List.of();
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        return List.of();
    }

    @Override
    public MediaType getMediaType() {
        return null;
    }

    @Override
    public Locale getLanguage() {
        String obj = getRequestHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE);
        if (obj == null)
            return null;
        return new Locale(obj);
    }

    @Override
    public Map<String, Cookie> getCookies() {
        mergeCookies();
        return Collections.unmodifiableMap(cookies);
    }

    @Override
    public Date getDate() {
        String date = getRequestHeaders().getFirst(DATE);
        if (date == null)
            return null;
        return DateUtil.parseDate(date);
    }

    @Override
    public int getLength() {
        String obj = getRequestHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
        if (obj == null)
            return -1;
        return Integer.parseInt(obj);
    }

    private void mergeCookies() {
        List<String> cookieHeader = getRequestHeaders().get(HttpHeaders.COOKIE);
        if (cookieHeader != null && !cookieHeader.isEmpty()) {
            for (String s : cookieHeader) {
                List<Cookie> list = CookieParser.parseCookies(s);
                for (Cookie cookie : list) {
                    cookies.put(cookie.getName(), cookie);
                }
            }
        }
    }
}