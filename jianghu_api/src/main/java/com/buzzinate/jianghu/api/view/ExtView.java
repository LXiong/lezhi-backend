package com.buzzinate.jianghu.api.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.node.ObjectNode;

import com.buzzinate.common.model.Article;

@JsonSerialize(using=ExtViewSer.class)
public class ExtView<T> {
	private T object;
	private Map<String, Object> params = null;
	
	private ExtView(T object, Map<String, Object> params) {
		this.object = object;
		this.params = params;
	}
	
	public T getObject() {
		return object;
	}
	
	public Map<String, Object> getParams() {
		return params;
	}
	
	public static <T> ExtView<T> combine(T object, Object... args) {
		return new ExtView<T>(object, from(args));
	}
	
	public static Map<String, Object> from(Object... args) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (int i = 0; i + 1 < args.length; i += 2) {
			String key = (String) args[i];
			result.put(key, args[i+1]);
		}
		return result;
	}
	
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		Article a = new Article();
		a.setId(20);
		a.setFrom("test");
		//a.setHash("abc".getBytes());
		
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("likeCount", 20);
		
		ExtView<Article> v = new ExtView<Article>(a, params);
		
		ObjectMapper om = new ObjectMapper();
		om.writeValue(System.out, v);
	}
}

class ExtViewSer extends JsonSerializer<ExtView> {
	private ObjectMapper om = new ObjectMapper();

	@Override
	public void serialize(ExtView value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		ObjectNode r = om.valueToTree(value.getObject());
		ObjectNode o = om.valueToTree(value.getParams());
		r.putAll(o);
		om.writeTree(jgen, r);
	}
}