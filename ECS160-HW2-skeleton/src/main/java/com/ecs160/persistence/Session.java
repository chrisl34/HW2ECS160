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
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(PersistableId.class)) {
                    f.setAccessible(true);
                    key = f.get(object).toString();
                    break;
                }
            }
            Map<String, String> map = jedisSession.hgetAll(key);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                if (field.isAnnotationPresent(PersistableListField.class)) {
                    PersistableListField annotation = field.getAnnotation(PersistableListField.class);
                    Class<?> subClass = Class.forName(annotation.className());
                    if(fieldValue.length() == 0) {
                        field.set(object, new ArrayList<>());
                        continue;
                    }
                    List<Object> responses = new ArrayList<>();
                    for (String id : fieldValue.split(",")) {
                        try {
                            Object instance = subClass.newInstance();
                            for (Field f : subClass.getDeclaredFields()) {
                                if (f.isAnnotationPresent(PersistableId.class)) {
                                    f.setAccessible(true);
                                    if (f.getType().equals(Integer.class) || f.getType().equals(int.class)) {
                                        f.set(instance, Integer.parseInt(id));
                                    } else if (f.getType().equals(String.class)) {
                                        f.set(instance, id);
                                    }
                                    break;
                                }
                            }
                            Object fullObject = load(instance);
                            responses.add(fullObject);

                        } catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    field.set(object, responses);
                } else {
                    Object convertedValue;
                    if (field.getType().equals(Integer.class)) {
                        if (fieldValue.isEmpty()) {
                            convertedValue = 0;
                        } else {
                            convertedValue = Integer.parseInt(fieldValue);
                        }
                    } else {
                        convertedValue = fieldValue;
                    }

                    field.set(object, convertedValue);

                }
            }
            return object;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
