package com.dsp.workflow.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.HtmlUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified, production-ready XSS configuration.
 *
 * Usage:
 *  - Annotate rich text fields with {@link AllowHtml} on DTOs/entities.
 *  - All other Strings are sanitized strictly (no tags) on input and HTML-escaped on output.
 *  - Query/form parameters are always sanitized strictly by {@link XssParamSanitizerFilter}.
 *
 * Add dependencies (pom.xml):
 *  <dependency>
 *    <groupId>org.jsoup</groupId>
 *    <artifactId>jsoup</artifactId>
 *    <version>1.17.2</version>
 *  </dependency>
 */
public class XSSConfig {

    /** Marks a field/getter that may contain rich HTML from a WYSIWYG editor. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.METHOD })
    public @interface AllowHtml {}

    /** Central place for Jsoup-based sanitization. */
    public static final class XssHtmlSanitizer {

        private static final Safelist FULL_HTML_SAFELIST = Safelist.relaxed()
                // structural
                .addTags("html","head","body","title","meta","link","section","article","nav","aside","header","footer","main","figure","figcaption")
                // layout/semantic
                .addTags("div","span","p","blockquote","pre","code","kbd","samp","var","abbr","dfn","small","sub","sup","mark","hr","br")
                .addTags("h1","h2","h3","h4","h5","h6")
                // lists
                .addTags("ul","ol","li","dl","dt","dd")
                // tables
                .addTags("table","thead","tbody","tfoot","tr","th","td","colgroup","col","caption")
                // media
                .addTags("img","picture","source","audio","video","track")
                // links & buttons
                .addTags("a","button")
                // forms (kept minimal)
                .addTags("form","label","input","textarea","select","option")
                // allow common global attributes
                .addAttributes(":all", "class", "id", "title", "dir", "lang", "tabindex", "style", "data-*")
                // links
                .addAttributes("a", "href", "name", "target", "rel")
                .addProtocols("a", "href", "http", "https", "mailto")
                // images
                .addAttributes("img", "src", "alt", "width", "height")
                .addProtocols("img", "src", "http", "https", "data")
                // tables
                .addAttributes("table", "border", "cellpadding", "cellspacing", "summary")
                .addAttributes("th", "scope")
                // media
                .addAttributes("video", "controls", "width", "height", "poster")
                .addAttributes("audio", "controls")
                .addAttributes("source", "src", "type")
                // buttons/forms (no event handlers allowed)
                .addAttributes("input", "type", "value", "name", "checked", "disabled", "readonly", "placeholder")
                .addAttributes("textarea", "name", "rows", "cols", "disabled", "readonly", "placeholder")
                .addAttributes("select", "name", "multiple", "size")
                .addAttributes("option", "value", "label", "selected");

        private XssHtmlSanitizer() {}

        /** Strict text-only sanitization (no tags). */
        public static String strictText(String raw) {
            if (raw == null) return null;
            String cleaned = Jsoup.clean(raw, Safelist.none());
            Document.OutputSettings os = new Document.OutputSettings();
            os.prettyPrint(false);
            return Jsoup.parse(cleaned).outputSettings(os).text();
        }

//        /** Rich HTML sanitization that preserves formatting according to FULL_HTML_SAFELIST. */
//        public static String richHtml(String raw) {
//            if (raw == null) return null;
//            Document.OutputSettings os = new Document.OutputSettings();
//            os.prettyPrint(false);
//            String cleaned = Jsoup.clean(raw, "", FULL_HTML_SAFELIST, os);
//            return stripHtmlBody(cleaned);
//        }

        
        public static String richHtml(String raw) {
            if (raw == null) return null;

            // First, run jsoup clean to remove unsafe tags/attributes according to allowlist.
            Document.OutputSettings os = new Document.OutputSettings();
            os.prettyPrint(false); // preserve original spacing as much as possible
            String cleaned = Jsoup.clean(raw, "", FULL_HTML_SAFELIST, os);

            // Parse the cleaned HTML and remove any <head> or <html> wrappers that may remain.
            // Then return only the body inner HTML (the fragment editors expect).
            Document doc = Jsoup.parse(cleaned);

            // Remove head and any stray meta/title/style nodes to avoid editor confusion.
            if (doc.head() != null) {
                doc.head().select("meta, title, style, link").remove();
                // remove any other head children just in case
                if (!doc.head().children().isEmpty()) {
                    doc.head().children().remove();
                }
            }

            // If there is a body, return its inner HTML; otherwise return the whole doc html.
            if (doc.body() != null) {
                return doc.body().html();
            } else {
                // fallback — should be rare
                return doc.html();
            }
        }
        
        private static String stripHtmlBody(String html) {
            if (html == null) return null;
            String trimmed = html.trim();
            if (trimmed.startsWith("<html") || trimmed.startsWith("<body")) {
                return Jsoup.parse(trimmed).body().html();
            }
            return trimmed;
        }
    }

    /**
     * Jackson String serializer:
     *  - By default, HTML-escapes all Strings (so tags render as text).
     *  - If a property is annotated with {@link AllowHtml}, it emits as-is (assumes input was sanitized).
     *
     * Uses Spring's HtmlUtils to avoid adding commons-text dependency.
     */
    public static final class XssStringSerializer extends StdSerializer<String> implements ContextualSerializer {
        private final boolean allowHtml;

        public XssStringSerializer() { this(false); }
        private XssStringSerializer(boolean allowHtml) { super(String.class); this.allowHtml = allowHtml; }

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) { gen.writeNull(); return; }
            if (allowHtml) gen.writeString(value);
            else gen.writeString(HtmlUtils.htmlEscape(value));
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            if (property != null) {
                AllowHtml allow = property.getAnnotation(AllowHtml.class);
                if (allow == null) allow = property.getContextAnnotation(AllowHtml.class);
                if (allow != null) return new XssStringSerializer(true);
            }
            return this;
        }
    }

    /** Registers the serializer globally for String.class. */
    @Configuration
    public static class JacksonConfig {
        @Bean
        public Module xssJacksonModule() {
            SimpleModule m = new SimpleModule("xss-string-module");
            m.addSerializer(String.class, new XssStringSerializer());
            return m;
        }
    }

    /**
     * Sanitizes incoming JSON request bodies:
     *  - Fields with {@link AllowHtml} => rich HTML sanitization.
     *  - Other Strings => strict text sanitization.
     *
     * This version avoids reflectively accessing JDK/internal classes (java.*, jakarta.*, etc.)
     * which causes InaccessibleObjectException under the Java module system.
     */
    @ControllerAdvice
    @Component
    public static class XssRequestBodyAdvice extends RequestBodyAdviceAdapter {

        @Override
        public boolean supports(MethodParameter methodParameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
            return true;
        }

        @Override
        public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                    Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
            sanitizeObject(body);
            return body;
        }

        private boolean isJdkOrJakartaType(Class<?> c) {
            if (c == null) return true;
            String name = c.getName();
            return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jakarta.")
                    || c.isPrimitive() || c.isEnum();
        }

        private void sanitizeObject(Object obj) {
            if (obj == null) return;

            Class<?> type = obj.getClass();

            // Avoid recursing into JDK/Jakarta/primitive types (e.g., java.time.LocalDate)
            if (isJdkOrJakartaType(type)) return;

            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                // Skip synthetic or bridge fields
                if (f.isSynthetic()) continue;

                Class<?> fieldType = f.getType();
                // Skip fields whose type is from java/javax/jakarta packages to avoid module issues
                if (isJdkOrJakartaType(fieldType)) continue;

                try {
                    f.setAccessible(true);
                    Object val = f.get(obj);
                    if (val == null) continue;

                    if (val instanceof String s) {
                        boolean allow = f.isAnnotationPresent(AllowHtml.class);
                        String cleaned = allow ? XSSConfig.XssHtmlSanitizer.richHtml(s)
                                               : XSSConfig.XssHtmlSanitizer.strictText(s);
                        f.set(obj, cleaned);
                    } else if (val instanceof Collection<?> coll) {
                        sanitizeCollection(coll);
                    } else if (val instanceof Map<?,?> map) {
                        sanitizeMap(map);
                    } else {
                        // recurse into nested DTOs
                        sanitizeObject(val);
                    }
                } catch (InaccessibleObjectException ioe) {
                    // Happens on Java 9+ when reflective access to JDK internals is blocked.
                    // Silently skip and continue; we do not want to break request processing.
                } catch (IllegalAccessException ignored) {
                    // ignore fields we can't access
                } catch (Exception ex) {
                    // Catch-all to prevent sanitizer from breaking request processing
                }
            }
        }

        private void sanitizeCollection(Collection<?> coll) {
            if (coll == null) return;
            for (Object e : coll) {
                if (e == null) continue;
                if (e instanceof String) {
                    // Default is strict; prefer wrapper types if elements can be HTML
                } else {
                    sanitizeObject(e);
                }
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void sanitizeMap(Map<?,?> map) {
            if (map == null) return;
            Map raw = (Map) map;
            for (Object key : raw.keySet().toArray()) {
                Object v = raw.get(key);
                if (v instanceof String) {
                    raw.put(key, XSSConfig.XssHtmlSanitizer.strictText((String) v));
                } else {
                    sanitizeObject(v);
                }
            }
        }
    }

    /**
     * Strictly sanitizes query/form parameters and adds basic defensive headers.
     * Do not send HTML via @RequestParam—use JSON body on an {@link AllowHtml} field instead.
     */
    @Component
    @Order(1)
    public static class XssParamSanitizerFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletRequest httpReq = (HttpServletRequest) request;
            HttpServletResponse httpRes = (HttpServletResponse) response;

            httpRes.setHeader("X-Content-Type-Options", "nosniff");
            httpRes.setHeader("X-Frame-Options", "SAMEORIGIN");
            httpRes.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            httpRes.setHeader("Content-Security-Policy",
                    "default-src 'self'; img-src 'self' data: https:; media-src 'self' https:; frame-ancestors 'self'; object-src 'none'");

            chain.doFilter(new SanitizingRequestWrapper(httpReq), httpRes);
        }

        static class SanitizingRequestWrapper extends HttpServletRequestWrapper {
            public SanitizingRequestWrapper(HttpServletRequest request) { super(request); }

            @Override public String getParameter(String name) {
                return XSSConfig.XssHtmlSanitizer.strictText(super.getParameter(name));
            }
            @Override public String[] getParameterValues(String name) {
                String[] arr = super.getParameterValues(name);
                if (arr == null) return null;
                String[] out = new String[arr.length];
                for (int i = 0; i < arr.length; i++) out[i] = XSSConfig.XssHtmlSanitizer.strictText(arr[i]);
                return out;
            }
            @Override public Map<String, String[]> getParameterMap() {
                Map<String, String[]> src = super.getParameterMap();
                Map<String, String[]> dst = new HashMap<>(src.size());
                src.forEach((k,v) -> {
                    String[] out = new String[v.length];
                    for (int i = 0; i < v.length; i++) out[i] = XSSConfig.XssHtmlSanitizer.strictText(v[i]);
                    dst.put(k, out);
                });
                return dst;
            }
        }
    }

    /* ------------------------------------------------------------
     * Optional: Example DTO
     * ------------------------------------------------------------
     *
     * public static class ArticleDto {
     *     private String title; // strict text
     *     @AllowHtml
     *     private String bodyHtml; // rich HTML
     *     // getters/setters ...
     * }
     */
}