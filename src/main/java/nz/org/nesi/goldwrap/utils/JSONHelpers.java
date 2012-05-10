package nz.org.nesi.goldwrap.utils;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

public class JSONHelpers {
	private static ObjectMapper mapper = null;

	public static <T> T convertFromJSONString(String json, Class<T> objectType) {
		System.out.println(json);
		try {
			return getMapper().readValue(json, objectType);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static String convertToJSONString(Object o) {

		try {
			return getMapper().writeValueAsString(o);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static <T> T extractObject(Class<T> dtoClass, String line) {
		int index = line.indexOf("{");
		String temp = line.substring(index);
		System.out.println(temp);
		T result = JSONHelpers.convertFromJSONString(temp, dtoClass);
		return result;
	}

	private static ObjectMapper getMapper() {
		if (mapper == null) {
			AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
			AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
			AnnotationIntrospector pair = new AnnotationIntrospector.Pair(
					primary, secondary);

			mapper = new ObjectMapper();
			AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
			// make deserializer use JAXB annotations (only)
			mapper.getDeserializationConfig().withAnnotationIntrospector(pair);
			// make serializer use JAXB annotations (only)
			mapper.getSerializationConfig().withAnnotationIntrospector(pair);

		}
		return mapper;
	}
}
