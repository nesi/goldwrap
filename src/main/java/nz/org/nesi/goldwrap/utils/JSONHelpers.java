package nz.org.nesi.goldwrap.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nz.org.nesi.goldwrap.domain.Machine;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

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

	private static JsonFactory f = new MappingJsonFactory();

	public static void main(String[] args) throws Exception {

		List<Machine> objects = readJSONfile(new File(
				"/home/markus/.goldwrap/machines.json"), Machine.class);

		for (Machine m : objects) {
			System.out.println(m.getName());
		}

	}

	public static <T> List<T> getFromJSONCollection(String jsonString,
			final Class<T> type) throws Exception {
		try {
			return getMapper().readValue(
					jsonString,
					TypeFactory.defaultInstance().constructCollectionType(
							ArrayList.class, type));
		} catch (JsonMappingException e) {
			T m = convertFromJSONString(jsonString, type);
			List<T> l = new ArrayList<T>();
			l.add(m);
			return l;
		}
	}

	public static <T> List<T> readJSONfile(File file, Class<T> type)
			throws Exception {

		String txt = Files.toString(file, Charsets.UTF_8);

		return getFromJSONCollection(txt, type);

	}
}
