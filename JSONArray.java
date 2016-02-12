package org.oikw.json;

import java.util.ArrayList;
import java.lang.StringBuilder;

public class JSONArray {
    ArrayList<Object> data = new ArrayList<Object>();

    public JSONArray() {
    }

    public void add(Object o) {
        this.data.add(o);
    }

    public int size() {
        return this.data.size();
    }

    public Object get(int idx) {
        return this.data.get(idx);
    }

    public JSONObject getObject(int idx) {
        return (JSONObject)this.data.get(idx);
    }

    public JSONArray getArray(int idx) {
        return (JSONArray)this.data.get(idx);
    }

    public String getString(int idx) {
        return (String)this.data.get(idx);
    }

    public double getNumber(int idx) {
        return ((Double)this.data.get(idx)).doubleValue();
    }

    public boolean getBoolean(int idx) {
        return ((Boolean)this.data.get(idx)).booleanValue();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean firstKey = true;
        for (Object v : this.data) {
            if (firstKey) {
                firstKey = false;
            } else {
                sb.append(",");
            }
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
        sb.append(']');
        return sb.toString();
    }
}
