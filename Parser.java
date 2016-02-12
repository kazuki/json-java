package org.oikw.json;

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException;

import org.oikw.json.Tokenizer.TokenType;

public class Parser {
    public static Object parse(Reader reader) throws IOException, IllegalArgumentException {
        Tokenizer tokenizer = new Tokenizer(reader);
        TokenType type = tokenizer.next();
        ArrayDeque<Object[]> stack = new ArrayDeque<Object[]>();
        Object ret = null;
        State state = State.None;
        String key = null;
        Object value = null;

        if (type == TokenType.BeginObject) {
            value = ret = new JSONObject();
            state = State.Key;
        } else if (type == TokenType.BeginArray) {
            value = ret = new JSONArray();
            state = State.Value;
        } else {
            throw new IllegalArgumentException();
        }

        while ((type = tokenizer.next()) != TokenType.EOS) {
            switch (state) {
            case Key:
                if (type == TokenType.String) {
                    key = tokenizer.getStringToken();
                    state = State.NameSeparator;
                } else if (type == TokenType.EndObject) {
                    if (stack.size() == 0)
                        return ret;
                    Object[] e = stack.pop();
                    state = (State)e[0];
                    key = (String)e[1];
                    value = e[2];
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case NameSeparator:
                if (type != TokenType.NameSeparator)
                    throw new IllegalArgumentException();
                state = State.Value;
                break;
            case ArrayValueSeparator:
                if (type == TokenType.ValueSeparator) {
                    state = State.Value;
                } else if (type == TokenType.EndArray) {
                    if (stack.size() == 0)
                        return ret;
                    Object[] e = stack.pop();
                    state = (State)e[0];
                    key = (String)e[1];
                    value = e[2];
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case ObjectValueSeparator:
                if (type == TokenType.ValueSeparator) {
                    state = State.Key;
                } else if (type == TokenType.EndObject) {
                    if (stack.size() == 0)
                        return ret;
                    Object[] e = stack.pop();
                    state = (State)e[0];
                    key = (String)e[1];
                    value = e[2];
                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case Value:
                Object tmp = null;
                if (type == TokenType.BeginObject || type == TokenType.BeginArray) {
                    if (type == TokenType.BeginObject) {
                        tmp = new JSONObject();
                        state = State.Key;
                    } else {
                        tmp = new JSONArray();
                        state = State.Value;
                    }
                    if (key == null) {
                        stack.push(new Object[]{State.ArrayValueSeparator, key, value});
                        ((JSONArray)value).add(tmp);
                    } else {
                        stack.push(new Object[]{State.ObjectValueSeparator, key, value});
                        ((JSONObject)value).put(key, tmp);
                    }
                    key = null;
                    value = tmp;
                    break;
                } else if (type == TokenType.EndArray) {
                    if (stack.size() == 0)
                        return ret;
                    Object[] e = stack.pop();
                    state = (State)e[0];
                    key = (String)e[1];
                    value = e[2];
                } else {
                    switch (type) {
                    case String:
                        tmp = tokenizer.getStringToken();
                        break;
                    case Number:
                        tmp = Double.valueOf(tokenizer.getNumberToken());
                        break;
                    case True:
                        tmp = Boolean.TRUE;
                        break;
                    case False:
                        tmp = Boolean.FALSE;
                        break;
                    case Null:
                        tmp = null;
                        break;
                    default:
                        throw new IllegalArgumentException();
                    }
                    if (key == null) {
                        ((JSONArray)value).add(tmp);
                        state = State.ArrayValueSeparator;
                    } else {
                        ((JSONObject)value).put(key, tmp);
                        state = State.ObjectValueSeparator;
                    }
                    break;
                }
            }
        }

        throw new IllegalArgumentException();
    }

    public static void escapeString(String src, StringBuilder dst) {
        for (int i = 0; i < src.length(); ++i) {
            int c = src.codePointAt(i);
            if (c == 0x20 || c == 0x21 ||
                (c >= 0x23 && c <= 0x5b) ||
                (c >= 0x5d && c <= 0x10ffff)) {
                // unescaped
                dst.append((char)c);
            } else if (c == 0x22) {
                dst.append("\\\"");
            } else if (c == 0x5c) {
                dst.append("\\\\");
            } else if (c == 0x08) {
                dst.append("\\b");
            } else if (c == 0x0c) {
                dst.append("\\f");
            } else if (c == 0x0a) {
                dst.append("\\n");
            } else if (c == 0x0d) {
                dst.append("\\r");
            } else if (c == 0x09) {
                dst.append("\\t");
            } else {
                dst.append("\\u");
                String t = Integer.toHexString(c);
                if (t.length() == 1)
                    dst.append("000");
                else if (t.length() == 2)
                    dst.append("00");
                else if (t.length() == 3)
                    dst.append("0");
                dst.append(t);
            }
        }
    }

    enum State {
        None,
        Key,
        Value,
        NameSeparator,
        ArrayValueSeparator,
        ObjectValueSeparator
    }
}
