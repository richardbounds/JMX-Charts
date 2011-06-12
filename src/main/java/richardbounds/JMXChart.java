package richardbounds;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class JMXChart implements JSONAware {
	
	private final String name;
	private final String attribute;
	private final String path;
	public JMXChart(String name, String attribute, String path) {
		super();
		this.name = name;
		this.attribute = attribute;
		this.path = path;
	}
	public String getName() {
		return name;
	}
	public String getAttribute() {
		return attribute;
	}
	public String getPath() {
		return path;
	}
	
	public String toJSONString(){
         StringBuffer sb = new StringBuffer();
         
         sb.append("{");
         
         sb.append("\"" + JSONObject.escape("name") + "\"");
         sb.append(":");
         sb.append("\"" + JSONObject.escape(name) + "\"");
         
         sb.append(",");
         
         sb.append("\"" + JSONObject.escape("attribute") + "\"");
         sb.append(":");
         sb.append("\"" + JSONObject.escape(attribute) + "\"");
 
         if (path != null) {
	         sb.append(",");
	         
	         sb.append("\"" + JSONObject.escape("path") + "\"");
	         sb.append(":");
	         sb.append("\"" + JSONObject.escape(path) + "\"");
         }
         sb.append("}");
         
         return sb.toString();
 }
	

}
