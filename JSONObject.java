package org.oikw.json;

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.StringBuilder;
import java.util.Map;
import java.util.HashMap;

public class JSONObject {
    HashMap<String, Object> data = new HashMap<String, Object>();
    
    public JSONObject() {
    }

    public JSONObject put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public int size() {
        return this.data.size();
    }

    public boolean containsKey(String key) {
        return this.data.containsKey(key);
    }

    public Object get(String key) {
        return this.data.get(key);
    }

    public JSONObject getObject(String key) {
        return (JSONObject)this.data.get(key);
    }

    public JSONArray getArray(String key) {
        return (JSONArray)this.data.get(key);
    }

    public String getString(String key) {
        return (String)this.data.get(key);
    }

    public double getNumber(String key) {
        return ((Double)this.data.get(key)).doubleValue();
    }

    public boolean getBoolean(String key) {
        return ((Boolean)this.data.get(key)).booleanValue();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean firstKey = true;
        for(Map.Entry<String, Object> e : this.data.entrySet()) {
            if (firstKey) {
                sb.append('"');
                firstKey = false;
            } else {
                sb.append(",\"");
            }
            Parser.escapeString(e.getKey(), sb);
            sb.append("\":");
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof String) {
                sb.append('"');
                Parser.escapeString((String)v, sb);
                sb.append('"');
            } else {
                sb.append(v.toString());
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
