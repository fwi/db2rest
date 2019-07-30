package com.github.fwi.db2restapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.github.fwi.db2rest.RestTableQueries;

@RestController
public class AppTableMappings {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AppTableMappings.class);

	@Autowired
	RequestMappingHandlerMapping requestMapper;

	@RequestMapping("/db2rest")
	public Map<String, Object> rest2dbMappings() {

		var apiInfo = getApiInfo();
		var pathApi = new HashMap<String, ApiInfo>();
		apiInfo.forEach(e -> pathApi.put(e.getPath(), e));
		var paths = apiInfo.stream().map(e -> e.getPath()).sorted().collect(Collectors.toList());
		var pathInfo = new ArrayList<Map<String, Object>>();
		for (var path : paths) {
			var pathApiInfo = pathApi.get(path);
			var pathApiInfoMap = new HashMap<String, Object>();
			pathApiInfoMap.put("path", path);
			var methods = pathApiInfo.getMethods().stream().map(e -> e.toString()).sorted()
				.collect(Collectors.toList());
			if (methods.isEmpty()) {
				continue;
			}
			pathApiInfoMap.put("methods", methods);
			var params = new LinkedList<Map<String, Object>>();
			pathApiInfo.getParameters().forEach(e -> params.add(e.toMap()));
			if (!params.isEmpty()) {
				pathApiInfoMap.put("params", params);
			}
			pathInfo.add(pathApiInfoMap);
		}
		return Collections.singletonMap(RestTableQueries.DATA_KEY, pathInfo);
	}

	final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	List<ApiInfo> getApiInfo() {

		// Copied and adjusted from "list all api" at
		// https://gist.github.com/crivulet/6524887
		// which includes request parameters.

		List<ApiInfo> results = new ArrayList<ApiInfo>();
		Map<RequestMappingInfo, HandlerMethod> methods = requestMapper.getHandlerMethods();

		for (Entry<RequestMappingInfo, HandlerMethod> method : methods.entrySet()) {
			for (String pattern : method.getKey().getPatternsCondition().getPatterns()) {
				List<RequestParameter> parameters = getParameters(method.getValue().getMethodParameters());
				Set<RequestMethod> httpMethods = method.getKey().getMethodsCondition().getMethods();
				results.add(new ApiInfo(pattern, httpMethods, parameters));
			}
		}
		return results;
	}

	List<RequestParameter> getParameters(MethodParameter[] methodParameters) {

		List<RequestParameter> results = new ArrayList<>();
		for (MethodParameter methodParam : methodParameters) {
			RequestParam requestParam = methodParam.getParameterAnnotation(RequestParam.class);
			if (requestParam != null) {
				String parameterName = getName(methodParam);
				
				// requestParam.required() always returns true, not sure why.
				// If no default value is set, required is true.
				
				var defaultValue = requestParam.defaultValue();
				var required = false;
				if (ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue())) {
					defaultValue = null;
					required = true;
				}
				results.add(new RequestParameter(parameterName, required, defaultValue));
			}
		}
		return results;
	}

	String getName(MethodParameter parameter) {

		String name = createNamedValueInfo(parameter);
		name = updateNamedValueInfo(parameter, name);
		return name;
	}

	String createNamedValueInfo(MethodParameter parameter) {

		RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);
		return (annotation != null) ? annotation.value() : StringUtils.EMPTY;
	}

	String updateNamedValueInfo(MethodParameter parameter, String name) {

		if (StringUtils.isNotBlank(name)) {
			return name;
		}
		String originalName = parameter.getParameterName();
		if (StringUtils.isNotBlank(originalName)) {
			return originalName;
		}
		MethodParameter tempParameter = new MethodParameter(parameter);
		tempParameter.initParameterNameDiscovery(parameterNameDiscoverer);
		return tempParameter.getParameterName();
	}

	static class ApiInfo {

		private String path;
		private Set<RequestMethod> methods;
		private List<RequestParameter> parameters;

		public ApiInfo(String path, Set<RequestMethod> methods, List<RequestParameter> parameters) {
			this.path = path;
			this.methods = methods;
			this.parameters = parameters;
		}

		public String getPath() {
			return path;
		}

		public Set<RequestMethod> getMethods() {
			return methods;
		}

		public List<RequestParameter> getParameters() {
			return parameters;
		}

	}

	static class RequestParameter {

		private String value;
		private boolean required;
		private String defaultValue;

		public RequestParameter(String value, boolean required, String defaultValue) {
			this.value = value;
			this.required = required;
			this.defaultValue = defaultValue;
		}

		public String getValue() {
			return value;
		}

		public boolean isRequired() {
			return required;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public Map<String, Object> toMap() {

			var m = new LinkedHashMap<String, Object>();
			m.put("value", getValue());
			if (defaultValue != null) {
				m.put("default", defaultValue);
			}
			m.put("required", Boolean.valueOf(isRequired()));
			return m;
		}

	}

}
