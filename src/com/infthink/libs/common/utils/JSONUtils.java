package com.infthink.libs.common.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

/**
 * <pre>
 * String json = "{'id':1, 'flag':['flag1', 'flag2'], 'category':{'id':10, 'name':'category name'}}";
 * JSONUtils jsonUtils = JSONUtils.parse(json);
 * 
 * int id = JSONUtils.asInteger(jsonUtils.opt("id", null), -1);
 *
 * JSONUtils categoryJSONUtils = JSONUtils.valueOf(jsonUtils.opt("category", null));
 * int categoryId = JSONUtils.asInteger(categoryJSONUtils.opt("id", null), -1);
 *
 * String categoryName = JSONUtils.asString(jsonUtils.opt("category.name", null), "");
 *
 * String flag2 = JSONUtils.asString(jsonUtils.opt("flag.1", null), "");
 * </pre>
 */
public class JSONUtils implements IDebuggable {

    private static final String TAG = JSONUtils.class.getSimpleName();

    // a JSONObject, JSONArray, String, Boolean, Integer, Long, Double or JSONObject.NULL.
    private Object mValue = JSONObject.NULL;

    private JSONUtils() {
        // 
    }

    /**
     * @param object JSONObject或者JSONArray对象
     * @return
     */
    public static JSONUtils valueOf(Object object) {
        JSONUtils instance = new JSONUtils();
        instance.mValue = object;
        return instance;
    }

    public static Integer asInteger(Object object, Integer defaultValue) {
        if (object != null) {
            try {
                defaultValue = Integer.parseInt(object.toString());
            } catch (Exception e) {
                if (DEBUG)
                    e.printStackTrace();
            }
        }
        return defaultValue;
    }

    public static Integer asInteger(Object object, int radix, Integer defaultValue) {
        if (object != null) {
            try {
                defaultValue = Integer.parseInt(object.toString(), radix);
            } catch (Exception e) {
                if (DEBUG)
                    e.printStackTrace();
            }
        }
        return defaultValue;
    }

    public static Float asFloat(Object object, Float defaultValue) {
        if (object != null) {
            try {
                defaultValue = Float.parseFloat(object.toString());
            } catch (Exception e) {
                if (DEBUG)
                    e.printStackTrace();
            }
        }
        return defaultValue;
    }

    public static Double asDouble(Object object, Double defaultValue) {
        if (object != null) {
            try {
                defaultValue = Double.parseDouble(object.toString());
            } catch (Exception e) {
                if (DEBUG)
                    e.printStackTrace();
            }
        }
        return defaultValue;
    }

    public static String asString(Object object, String defaultValue) {
        if (object != null) {
            defaultValue = object.toString();
        }
        return defaultValue;
    }

    public static JSONObject asJSONObject(Object jsonObject, JSONObject defaultValue) {
        if (jsonObject != null && jsonObject instanceof JSONObject) {
            defaultValue = (JSONObject) jsonObject;
        }
        return defaultValue;
    }

    public static JSONArray asJSONArray(Object jsonArray, JSONArray defaultValue) {
        if (jsonArray != null && jsonArray instanceof JSONArray) {
            defaultValue = (JSONArray) jsonArray;
        }
        return defaultValue;
    }

    public static JSONUtils parse(String json) {
        JSONUtils instance = new JSONUtils();
        JSONTokener tokener = new JSONTokener(json);
        try {
            instance.mValue = tokener.nextValue();
        } catch (JSONException e) {
            if (DEBUG) {
                Log.d(TAG, String.format("对(%s)构造JSONUtils失败", json));
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * @param path for example: user.3.name.first
     * @return
     */
    public Object opt(String path, Object defaultValue) {
        if (path == null || mValue == null) {
            return opt(defaultValue);
        }
        Object value = mValue;
        String[] nodes = path.split("\\.");
        for (String node : nodes) {
            if (value instanceof JSONObject) {
                JSONObject obj = (JSONObject) value;
                Object v = obj.opt(node);
                value = (v == null ? JSONObject.NULL : v);
                continue;
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                try {
                    Integer index = Integer.decode(node);
                    Object v = array.opt(index);
                    value = (v == null ? JSONObject.NULL : v);
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                    value = JSONObject.NULL;
                }
                continue;
            } else {
                // String, Boolean, Integer, Long, Double or JSONObject.NULL
                break;
            }
        }
        return removeNull(value, defaultValue);
    }

    public Object opt(Object defaultValue) {
        return removeNull(mValue, defaultValue);
    }

    private static Object removeNull(Object value, Object defaultValue) {
        if (JSONObject.NULL.equals(value) || "null".equalsIgnoreCase(value.toString())) {
            return defaultValue;
        } else {
            return value;
        }
    }

}
