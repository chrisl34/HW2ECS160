package com.ecs160.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.lang.Class;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import redis.clients.jedis.Jedis;





// Assumption - only support int/long/and string values
public class Session {
    private List<Object> objects = new ArrayList<>();
    private Jedis jedisSession;
    public Session() {
        jedisSession = new Jedis("localhost", 6379);
    }


    public void add(Object obj) {
        objects.add(obj);
    }

    public void persistAll()  {
        Class<?> clazz = objects.get(0).getClass();
        if(!clazz.isAnnotationPresent(Persistable.class)) {
            return;
        }
        String key = "";
        for(Object obj : objects) {
            Map<String, String> objectMap = new HashMap<>();
            for(Field field : obj.getClass().getDeclaredFields()) {
                try {
                    if (field.isAnnotationPresent(PersistableField.class)) {
                        field.setAccessible(true);
                        objectMap.put(field.getName(), field.get(obj).toString());
                    }

                    if(field.isAnnotationPresent(PersistableListField.class)) {
                        field.setAccessible(true);
                        List<?> l = (List<?>) field.get(obj);
                        String res = "";
                        for(Object o : l) {
                            for(Field f : o.getClass().getDeclaredFields()) {
                                f.setAccessible(true);
                                if(f.isAnnotationPresent(PersistableId.class)) {
                                    res += f.get(o).toString() + ",";
                                }
                            }
                        }
                        if(res.length() > 0) {
                            res = res.substring(0, res.length() - 1);
                        }
                        objectMap.put(field.getName(), res);
                    }

                    if(field.isAnnotationPresent(PersistableId.class)) {
                        field.setAccessible(true);
                        key = field.get(obj).toString();

                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            jedisSession.hset(key, objectMap);
        }
    }


    public Object load(Object object)  {
        try {
            Class<?> clazz = object.getClass();
            String key = null;
            // Find the field annotated with @PersistableId
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(PersistableId.class)) {
                    f.setAccessible(true);
                    key = f.get(object).toString();
                    break;
                }
            }
            // Retrieve data from Redis
            Map<String, String> map = jedisSession.hgetAll(key);
            // Create a new instance of the class
            Object obj = (Object) clazz.newInstance();
            // Populate fields from Redis data
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (field.isAnnotationPresent(PersistableListField.class)) {
                    // Extract class type from annotation
                    PersistableListField annotation = field.getAnnotation(PersistableListField.class);
                    Class<?> relatedClass = Class.forName(annotation.className());

                    List<Object> relatedObjects = new ArrayList<>();
                    for (String id : fieldValue.split(",")) {
                        try {
                            // Create a new instance of the related class
                            Object instance = relatedClass.newInstance();
                            // Find the @PersistableId field and set the ID
                            for (Field f : relatedClass.getDeclaredFields()) {
                                if (f.isAnnotationPresent(PersistableId.class)) {
                                    f.setAccessible(true);

                                    if (f.getType().equals(Integer.class) || f.getType().equals(int.class)) {
                                        // If the field type is Integer or int, convert the id (String) to Integer
                                        f.set(instance, Integer.parseInt(id));
                                    } else if (f.getType().equals(String.class)) {
                                        // If the field type is String, set the id directly
                                        f.set(instance, id);
                                    }
                                    break; // Found the field, no need to continue looping
                                }
                            }

                            // Recursively load the full object
                            Object fullObject = load(instance);
                            relatedObjects.add(fullObject);

                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    field.set(obj, relatedObjects);
                } else {
                    Object convertedValue;
                    if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                        // Check if fieldValue is empty, if so, assign a default value (e.g., 0)
                        if (fieldValue.isEmpty()) {
                            convertedValue = 0; // or any other default value you prefer
                        } else {
                            convertedValue = Integer.parseInt(fieldValue);
                        }
                    } else {
                        convertedValue = fieldValue; // Keep as String
                    }

                    field.set(obj, convertedValue);

                }
            }
            return obj;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
