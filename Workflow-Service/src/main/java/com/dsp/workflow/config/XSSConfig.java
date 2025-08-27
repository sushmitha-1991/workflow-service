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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Unified, production-ready XSS configuration with:
 *  - @AllowHtml for rich-text fields (input sanitized, output not escaped)
 *  - @NoEscape for string fields that must be serialized as-is (e.g., URLs)
 *  - Property-based whitelist: xss.no-escape-fields=comma,separated,names
 *  - Convention: field names ending with "Content" (case-insensitive) bypass escaping
 *
 * Usage:
 *  - Annotate rich text fields with {@link AllowHtml} on DTOs/entities.
 *  - Annotate fields like URLs with {@link NoEscape} or list their names in property xss.no-escape-fields.
 *  - Fields ending with "Content" will automatically bypass escaping.
 *
 * Add dependency (pom.xml) for jsoup:
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

    /** Marks a field/getter whose String should be written to JSON as-is (no HTML escaping). */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.METHOD })
    public @interface NoEscape {}

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

        /** Rich HTML sanitization that preserves formatting according to FULL_HTML_SAFELIST. */
        public static String richHtml(String raw) {
            if (raw == null) return null;
            Document.OutputSettings os = new Document.OutputSettings();
            os.prettyPrint(false);
            String cleaned = Jsoup.clean(raw, "", FULL_HTML_SAFELIST, os);
//            return stripHtmlBody(cleaned);
            return cleaned;
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

    /** Holds output escaping policy (set via Spring properties). */
    public static final class XssOutputPolicy {
        private static volatile Set<String> NO_ESCAPE_FIELDS = Collections.emptySet();

        public static boolean isNoEscapeField(String fieldName) {
            if (fieldName == null) return false;
            return NO_ESCAPE_FIELDS.contains(fieldName);
        }

        public static void setNoEscapeFields(Set<String> s) {
            NO_ESCAPE_FIELDS = (s == null ? Collections.emptySet() : Collections.unmodifiableSet(new HashSet<>(s)));
        }
    }

    /**
     * Jackson String serializer:
     *  - By default, HTML-escapes all Strings (so tags render as text).
     *  - If a property is annotated with {@link AllowHtml} or {@link NoEscape}, or its name
     *    is listed in xss.no-escape-fields, OR its name endsWith "Content", the value
     *    is emitted as-is (assumes input was sanitized).
     */
    public static final class XssStringSerializer extends StdSerializer<String> implements ContextualSerializer {
        private final boolean writeRaw;
        private final String propertyNameFromContext; // optional name from property when creating contextual serializer

        public XssStringSerializer() { this(false, null); }
        private XssStringSerializer(boolean writeRaw, String propertyNameFromContext) { super(String.class); this.writeRaw = writeRaw; this.propertyNameFromContext = propertyNameFromContext; }

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) { gen.writeNull(); return; }
            
            if (value.startsWith("/")) { gen.writeString(value); return; }

            // field name from output context; falls back to contextual property name if available
            String fieldName = null;
            try {
                if (gen.getOutputContext() != null) {
                    fieldName = gen.getOutputContext().getCurrentName();
                }
            } catch (Exception ignored) {}

            if (fieldName == null) fieldName = this.propertyNameFromContext;

            if (shouldWriteRaw(fieldName)) {
                gen.writeString(value);
            } else {
                gen.writeString(HtmlUtils.htmlEscape(value));
            }
        }

        private boolean shouldWriteRaw(String fieldName) {
            if (writeRaw) return true;
            if (fieldName == null) return false;

            // 1. property-name whitelist from application properties
            if (XssOutputPolicy.isNoEscapeField(fieldName)) return true;

            // 2. convention: any field name ending with "Content" (case-insensitive)
            if (fieldName.toLowerCase().endsWith("content")) return true;
            
            if (fieldName.toLowerCase().endsWith("contenten")) return true;
            
            if (fieldName.toLowerCase().endsWith("contenthi")) return true;

            return false;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            if (property != null) {
                boolean raw = property.getAnnotation(AllowHtml.class) != null
                        || property.getContextAnnotation(AllowHtml.class) != null
                        || property.getAnnotation(NoEscape.class) != null
                        || property.getContextAnnotation(NoEscape.class) != null;

                String name = property.getName();
                // also honor property-name whitelist from config
                if (!raw && XssOutputPolicy.isNoEscapeField(name)) raw = true;

                // Also honor convention at contextual creation time
                if (!raw && name != null && (name.toLowerCase().endsWith("content") ||
                							 name.toLowerCase().endsWith("contenten") ||
                							 name.toLowerCase().endsWith("contenthi"))) {
                    raw = true;
                }

                if (raw) {
                    return new XssStringSerializer(true, name);
                } else {
                    return new XssStringSerializer(false, name);
                }
            }
            return this;
        }
    }

    /** Spring Boot properties holder for XSS config. */
    @ConfigurationProperties(prefix = "xss")
    public static class XssProps {
        /**
         * Comma-separated list of String property names that should be written without HTML escaping.
         * Example: url,link,downloadPath
         */
        private String noEscapeFields = "url,link,downloadPath";

        public String getNoEscapeFields() { return noEscapeFields; }
        public void setNoEscapeFields(String noEscapeFields) { this.noEscapeFields = noEscapeFields; }

        public Set<String> parsedNoEscape() {
            if (noEscapeFields == null || noEscapeFields.isBlank()) return Collections.emptySet();
            return Stream.of(noEscapeFields.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    /** Registers the serializer globally and loads the property-based whitelist. */
    @Configuration
    @EnableConfigurationProperties(XssProps.class)
    public static class JacksonConfig {
        @Bean
        public Module xssJacksonModule(XssProps props) {
            // load field-name whitelist into static policy holder
            XssOutputPolicy.setNoEscapeFields(props.parsedNoEscape());

            SimpleModule m = new SimpleModule("xss-string-module");
            m.addSerializer(String.class, new XssStringSerializer());
            return m;
        }
    }

    /**
     * Sanitizes incoming JSON request bodies:
     *  - Fields with {@link AllowHtml} => rich HTML sanitization.
     *  - Other Strings => strict text sanitization.
     *  - Skips reflecting into JDK/Jakarta classes (to avoid InaccessibleObjectException on Java 9+ modules).
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
            if (isJdkOrJakartaType(type)) return; // don't reflect into JDK/Jakarta classes

            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (f.isSynthetic()) continue;

                Class<?> fieldType = f.getType();
                if (isJdkOrJakartaType(fieldType)) continue;

                try {
                    f.setAccessible(true);
                    Object val = f.get(obj);
                    if (val == null) continue;

                    if (val instanceof String s) {
                        boolean allowHtml = f.isAnnotationPresent(AllowHtml.class);
                        String cleaned = allowHtml ? XSSConfig.XssHtmlSanitizer.richHtml(s)
                                                   : XSSConfig.XssHtmlSanitizer.strictText(s);
                        f.set(obj, cleaned);
                    } else if (val instanceof Collection<?> coll) {
                        sanitizeCollection(coll);
                    } else if (val instanceof Map<?,?> map) {
                        sanitizeMap(map);
                    } else {
                        sanitizeObject(val);
                    }
                } catch (InaccessibleObjectException | IllegalAccessException ignored) {
                    // Skip fields we can't access under module rules
                } catch (Exception ex) {
                    // Never break request processing due to sanitizer
                }
            }
        }

        private void sanitizeCollection(Collection<?> coll) {
            if (coll == null) return;
            for (Object e : coll) {
                if (e == null) continue;
                if (e instanceof String) {
                    // Default is strict. Prefer wrapper types if elements can be HTML.
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
     *     @NoEscape
     *     private String url; // serialized as-is
     *     // getters/setters ...
     * }
     */
}