package com.hgxx.whiteboard.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by ly on 03/05/2017.
 */

public class JsonUtils {
    public static <T> JSONObject obj2json(T obj){
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        JSONObject jsonObject = new JSONObject();

        for(Field f: fields){
            f.setAccessible(true);
            try {
                jsonObject.put(f.getName(), String.valueOf(f.get(obj)));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
}
