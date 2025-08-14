/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

@Configuration
public class XssProtectionConfig {

	/**
	 * Safelist for fields where full HTML is allowed.
	 */
	private static final Safelist FULL_HTML_SAFELIST = Safelist.relaxed()
			// structural / document
			.addTags("html", "head", "body", "title", "meta", "link", "style")

			// headings & text formatting
			.addTags("h1", "h2", "h3", "h4", "h5", "h6", "p", "div", "span", "br", "hr", "strong", "b", "em", "i", "u",
					"small", "mark", "sub", "sup", "del", "ins", "cite", "q", "abbr", "address")

			// lists
			.addTags("ul", "ol", "li", "dl", "dt", "dd")

			// tables
			.addTags("table", "thead", "tbody", "tfoot", "tr", "th", "td", "caption", "colgroup", "col")

			// media & figures
			.addTags("img", "figure", "figcaption", "picture", "source", "audio", "video", "track")

			// links & anchors
			.addTags("a", "nav")

			// layout / semantic elements
			.addTags("section", "article", "aside", "header", "footer", "main", "figure", "figcaption", "details",
					"summary")

			// forms (if you need user inputs inside trusted HTML)
			.addTags("form", "input", "textarea", "select", "option", "label", "button", "fieldset", "legend")

			// code & preformatted
			.addTags("pre", "code", "kbd", "samp", "var", "blockquote")

//		    // optional (use carefully) — iframe is powerful and potentially risky; restrict attributes
//		    .addTags("iframe")

			// Allow commonly useful attributes on elements
			.addAttributes(":all", "class", "style", "id", "title", "role", "hidden")
			.addAttributes("html", "lang", "dir").addAttributes("a", "href", "name", "rel", "target", "download")
			.addAttributes("img", "src", "alt", "title", "width", "height", "loading")
			.addAttributes("video", "src", "controls", "width", "height", "poster")
			.addAttributes("audio", "src", "controls").addAttributes("source", "src", "type")
			.addAttributes("iframe", "src", "width", "height", "frameborder", "allow", "allowfullscreen", "loading",
					"referrerpolicy", "sandbox")
			.addAttributes("form", "action", "method", "enctype", "name", "target")
			.addAttributes("input", "type", "name", "value", "placeholder", "checked", "disabled", "readonly",
					"maxlength")
			.addAttributes("textarea", "rows", "cols", "name", "placeholder")
			.addAttributes("select", "name", "multiple", "size")
			.addAttributes("option", "value", "selected", "disabled").addAttributes("table", "summary")
			.addAttributes("th", "scope", "colspan", "rowspan").addAttributes("td", "colspan", "rowspan")
			.addAttributes("col", "span").addAttributes("link", "rel", "href", "type", "media")
			.addAttributes("meta", "charset", "name", "content", "http-equiv").addAttributes("style", "type")

			// ARIA and some common data attributes (explicit)
			.addAttributes(":all", "aria-hidden", "aria-label", "aria-labelledby", "aria-describedby", "data-toggle",
					"data-target", "data-id")

			// enforce type for style tags and allow http/https for URLs
			.addEnforcedAttribute("style", "type", "text/css").addProtocols("a", "href", "http", "https", "mailto")
			.addProtocols("img", "src", "http", "https").addProtocols("src", "http", "https");

	/**
	 * Outgoing JSON serialization sanitization.
	 */
	@Bean
	public Module xssEscapeModule() {
		SimpleModule module = new SimpleModule();
		module.addSerializer(String.class, new JsonSerializer<>() {
			@Override
			public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
				gen.writeString(sanitizeHtml(value));
			}
		});
		return module;
	}

	/**
	 * Strict XSS cleaning for form params and query params.
	 */
	@Bean
	public FilterRegistrationBean<Filter> xssFilterRegistration() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new XssFilter());
		registration.addUrlPatterns("/*");
		registration.setName("XssFilter");
		registration.setOrder(1);
		return registration;
	}

	private static class XssFilter implements Filter {
		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
		}
	}

	/**
	 * Wrapper for cleaning query params and form data.
	 */
	private static class XssRequestWrapper extends HttpServletRequestWrapper {
		public XssRequestWrapper(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getParameter(String name) {
			return sanitizeStrict(super.getParameter(name));
		}

		@Override
		public String[] getParameterValues(String name) {
			String[] values = super.getParameterValues(name);
			if (values == null)
				return null;
			String[] sanitizedValues = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				sanitizedValues[i] = sanitizeStrict(values[i]);
			}
			return sanitizedValues;
		}

		private String sanitizeStrict(String value) {
			if (value == null)
				return null;
			if (!containsHtmlLikeContent(value))
				return value; // Skip if no HTML-like chars
			return Jsoup.clean(value, Safelist.none()); // Remove all HTML tags
		}
	}

	/**
	 * RequestBodyAdvice for JSON payloads.
	 */
	@ControllerAdvice
	public static class XssRequestBodyAdvice extends RequestBodyAdviceAdapter {
		@Override
		public boolean supports(MethodParameter methodParameter, Type targetType,
				Class<? extends HttpMessageConverter<?>> converterType) {
			return true;
		}

		@Override
		public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
				Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
			sanitizeObject(body, new IdentityHashMap<>());
			return body;
		}

		private void sanitizeObject(Object obj, Map<Object, Boolean> visited) {
			if (obj == null || visited.containsKey(obj))
				return;
			visited.put(obj, true);

			// If it's a String, sanitize and stop.
			if (obj instanceof String)
				return; // strings are handled at field assignment time

			Class<?> clazz = obj.getClass();

			// ⛔ Do not descend into JDK/framework types
			if (!shouldDescendInto(clazz))
				return;

			for (Field field : clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()))
					continue; // skip static
				field.setAccessible(true);
				try {
					Object value = field.get(obj);

					if (value instanceof String str) {
						field.set(obj, sanitizeHtml(str));
						continue;
					}

					// Skip recursion into JDK/framework types
					if (value != null && shouldDescendInto(value.getClass())) {
						sanitizeObject(value, visited);
					}
				} catch (IllegalAccessException ignored) {
				}
			}
		}

		private boolean shouldDescendInto(Class<?> type) {
			if (type.isPrimitive() || type.isEnum())
				return false;
			Package p = type.getPackage();
			String name = (p != null) ? p.getName() : "";
			// Don’t reflect into JDK or common framework libs
			return !(name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jakarta.")
					|| name.startsWith("org.springframework.") || name.startsWith("com.fasterxml.") // Jackson
					|| name.startsWith("org.jsoup.")); // Jsoup, etc.
		}

	}

	/**
	 * HTML sanitization (full HTML allowed).
	 */
	private static String sanitizeHtml(String value) {
		if (value == null)
			return null;
		if (!containsHtmlLikeContent(value))
			return value; // Skip if no HTML-like chars
		return Jsoup.clean(value, FULL_HTML_SAFELIST);
	}

	/**
	 * Simple heuristic to skip Jsoup if string has no HTML-like characters.
	 */
	private static boolean containsHtmlLikeContent(String value) {
		return value.contains("<") || value.contains(">");
	}

	private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		return clazz.isPrimitive() || clazz == Boolean.class || clazz == Byte.class || clazz == Character.class
				|| clazz == Short.class || clazz == Integer.class || clazz == Long.class || clazz == Float.class
				|| clazz == Double.class;
	}
}
