/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.appwork.storage.TypeRef;
import org.appwork.storage.simplejson.JSonArray;
import org.appwork.storage.simplejson.JSonNode;
import org.appwork.storage.simplejson.JSonObject;
import org.appwork.storage.simplejson.JSonValue;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author thomas
 * 
 */
public class JSonMapper {

    /**
     * @param value
     * @param type
     * @return
     */
    public static Object cast(Object v, final Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                v = ((Boolean) v).booleanValue();
            } else if (type == char.class) {
                v = (char) ((Long) v).byteValue();
            } else if (type == byte.class) {
                v = ((Long) v).byteValue();
            } else if (type == short.class) {
                v = ((Long) v).shortValue();
            } else if (type == int.class) {
                v = ((Long) v).intValue();
            } else if (type == long.class) {
                v = ((Long) v).longValue();
            } else if (type == float.class) {
                v = ((Double) v).floatValue();
            } else if (type == double.class) {
                //
                v = ((Double) v).doubleValue();

            }
        } else if (type == Boolean.class) {
            v = ((Boolean) v).booleanValue();
        } else if (type == Character.class) {
            v = (char) ((Long) v).byteValue();
        } else if (type == Byte.class) {
            v = ((Long) v).byteValue();
        } else if (type == Short.class) {
            v = ((Long) v).shortValue();
        } else if (type == Integer.class) {
            v = ((Long) v).intValue();
        } else if (type == Long.class) {
            v = ((Long) v).longValue();
        } else if (type == Float.class) {
            v = ((Double) v).floatValue();
        } else if (type == Double.class) {
            //
            v = ((Double) v).doubleValue();

        }

        return v;
    }

    private boolean                          ignorePrimitiveNullMapping    = false;

    private boolean                          ignoreIllegalArgumentMappings = false;

    /**
     * @param value
     * @param type
     * @return
     */
    private boolean                          ignoreIllegalEnumMappings     = false;

    private HashMap<Class<?>, TypeMapper<?>> typeMapper;

    public JSonMapper() {

        typeMapper = new HashMap<Class<?>, TypeMapper<?>>();
        addMapper(File.class, new FileMapper());
        addMapper(Class.class, new ClassMapper());
        addMapper(URL.class, new URLMapper());
        addMapper(Date.class, new DateMapper());
    }

    /**
     * @param <T>
     * @param class1
     * @param fileMapper
     */
    public <T> void addMapper(Class<T> class1, TypeMapper<T> fileMapper) {
        typeMapper.put(class1, fileMapper);

    }

    /**
     * @param obj
     * @return
     * @throws MapperException
     */
    @SuppressWarnings("unchecked")
    public JSonNode create(final Object obj) throws MapperException {
        try {

            if (obj == null) { return new JSonValue(null); }
            final Class<? extends Object> clazz = obj.getClass();
            TypeMapper<?> mapper;
            if (clazz.isPrimitive()) {
                if (clazz == boolean.class) {
                    return new JSonValue((Boolean) obj);
                } else if (clazz == char.class) {
                    return new JSonValue(0 + ((Character) obj).charValue());
                } else if (clazz == byte.class) {
                    return new JSonValue(((Byte) obj).longValue());
                } else if (clazz == short.class) {
                    return new JSonValue(((Short) obj).longValue());
                } else if (clazz == int.class) {
                    return new JSonValue(((Integer) obj).longValue());
                } else if (clazz == long.class) {
                    return new JSonValue(((Long) obj).longValue());
                } else if (clazz == float.class) {
                    return new JSonValue(((Float) obj).doubleValue());
                } else if (clazz == double.class) { return new JSonValue(((Double) obj).doubleValue()); }
            } else if (clazz.isEnum()) {
                return new JSonValue(obj + "");
            } else if (obj instanceof Boolean) {
                return new JSonValue(((Boolean) obj).booleanValue());
            } else if (obj instanceof Character) {
                return new JSonValue(0 + ((Character) obj).charValue());
            } else if (obj instanceof Byte) {
                return new JSonValue(((Byte) obj).longValue());
            } else if (obj instanceof Short) {
                return new JSonValue(((Short) obj).longValue());
            } else if (obj instanceof Integer) {
                return new JSonValue(((Integer) obj).longValue());
            } else if (obj instanceof Long) {
                return new JSonValue(((Long) obj).longValue());
            } else if (obj instanceof Float) {
                return new JSonValue(((Float) obj).doubleValue());
            } else if (obj instanceof Double) {
                return new JSonValue(((Double) obj).doubleValue());

            } else if (obj instanceof String) {
                return new JSonValue((String) obj);
            } else if (obj instanceof Map) {

                final JSonObject ret = new JSonObject();
                Entry<Object, Object> next;
                for (final Iterator<Entry<Object, Object>> it = ((Map<Object, Object>) obj).entrySet().iterator(); it.hasNext();) {
                    next = it.next();
                    if (!(next.getKey() instanceof String)) { throw new MapperException("Map keys have to be Strings: " + clazz); }
                    ret.put(next.getKey().toString(), this.create(next.getValue()));
                }
                return ret;
            } else if (obj instanceof List) {
                final JSonArray ret = new JSonArray();
                for (final Object o : (List<?>) obj) {
                    ret.add(this.create(o));
                }
                return ret;
            } else if (clazz.isArray()) {
                final JSonArray ret = new JSonArray();
                for (int i = 0; i < Array.getLength(obj); i++) {
                    ret.add(this.create(Array.get(obj, i)));
                }
                return ret;
            } else if (obj instanceof Class) {
                return new JSonValue(((Class<?>) obj).getName());
            } else if ((mapper = typeMapper.get(clazz)) != null) {
                return mapper.map(obj);
            } else/* if (obj instanceof Storable) */{
                final ClassCache cc = ClassCache.getClassCache(clazz);
                final JSonObject ret = new JSonObject();
                for (final Getter g : cc.getGetter()) {

                    ret.put(g.getKey(), this.create(g.getValue(obj)));
                }
                return ret;

            }
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        } catch (final SecurityException e) {

            e.printStackTrace();
        } catch (final NoSuchMethodException e) {

            e.printStackTrace();
        }

        return null;
    }

    public boolean isIgnoreIllegalArgumentMappings() {
        return this.ignoreIllegalArgumentMappings;
    }

    public boolean isIgnoreIllegalEnumMappings() {
        return this.ignoreIllegalEnumMappings;
    }

    /**
     * if json maps null to a primitive field
     * 
     * @return
     */
    public boolean isIgnorePrimitiveNullMapping() {
        return this.ignorePrimitiveNullMapping;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object jsonToObject(final JSonNode json, final Type type) {
        final ClassCache cc;
        try {
            Class<?> clazz = null;
            if (type instanceof ParameterizedTypeImpl) {
                clazz = ((ParameterizedTypeImpl) type).getRawType();
            } else if (type instanceof Class) {
                clazz = (Class) type;
            }

            TypeMapper<?> tm = typeMapper.get(clazz);
            if (tm != null) {
              
                return tm.reverseMap(json); 
              
                }
            if (json instanceof JSonValue) {
                switch (((JSonValue) json).getType()) {
                case BOOLEAN:
                case DOUBLE:
                case LONG:
                    if (type instanceof Class) {
                        return JSonMapper.cast(((JSonValue) json).getValue(), (Class) type);
                    } else {
                        return ((JSonValue) json).getValue();

                    }

                case STRING:
                    if (type instanceof Class && ((Class<?>) type).isEnum()) {
                        try {
                            return Enum.valueOf((Class<Enum>) type, ((JSonValue) json).getValue() + "");
                        } catch (final IllegalArgumentException e) {
                            if (this.isIgnoreIllegalArgumentMappings() || this.isIgnoreIllegalEnumMappings()) { return null; }
                            throw e;
                        }
                    } else {
                        return ((JSonValue) json).getValue();

                    }

                case NULL:
                    return null;

                }
            }

            if (type instanceof Class) {
                clazz = (Class<?>) type;
                if (List.class.isAssignableFrom(clazz)) {
                    final List<Object> inst = (List<Object>) clazz.newInstance();
                    final JSonArray obj = (JSonArray) json;
                    final Type gs = clazz.getGenericSuperclass();
                    final Type gType;
                    if (gs instanceof ParameterizedTypeImpl) {
                        gType = ((ParameterizedTypeImpl) gs).getActualTypeArguments()[0];
                    } else {
                        gType = void.class;
                    }
                    for (final JSonNode n : obj) {
                        inst.add(this.jsonToObject(n, gType));
                    }
                    return inst;
                } else if (Map.class.isAssignableFrom(clazz)) {
                    final Map<String, Object> inst = (Map<String, Object>) clazz.newInstance();
                    final JSonObject obj = (JSonObject) json;
                    final Type gs = clazz.getGenericSuperclass();
                    final Type gType;
                    if (gs instanceof ParameterizedTypeImpl) {
                        gType = ((ParameterizedTypeImpl) gs).getActualTypeArguments()[1];
                    } else {
                        gType = void.class;
                    }

                    Entry<String, JSonNode> next;
                    for (final Iterator<Entry<String, JSonNode>> it = obj.entrySet().iterator(); it.hasNext();) {
                        next = it.next();
                        inst.put(next.getKey(), this.jsonToObject(next.getValue(), gType));
                    }

                    return inst;

                } else if (clazz.isArray()) {
                    final JSonArray obj = (JSonArray) json;
                    final Object arr = Array.newInstance(clazz.getComponentType(), obj.size());
                    for (int i = 0; i < obj.size(); i++) {
                        final Object v = this.jsonToObject(obj.get(i), clazz.getComponentType());

                        Array.set(arr, i, v);

                    }
                    return arr;
                } else {
                    if (json instanceof JSonArray) {

                        final ArrayList<Object> inst = new ArrayList<Object>();
                        final JSonArray obj = (JSonArray) json;
                        final Type gs = clazz.getGenericSuperclass();
                        final Type gType;
                        if (gs instanceof ParameterizedTypeImpl) {
                            gType = ((ParameterizedTypeImpl) gs).getActualTypeArguments()[0];
                        } else {
                            gType = void.class;
                        }
                        for (final JSonNode n : obj) {
                            inst.add(this.jsonToObject(n, gType));
                        }
                        return inst;

                    } else {
                        final JSonObject obj = (JSonObject) json;
                        cc = ClassCache.getClassCache(clazz);

                        final Object inst = cc.getInstance();
                        JSonNode value;
                        Object v;
                        for (final Setter s : cc.getSetter()) {

                            value = obj.get(s.getKey());
                            if (value == null) {
                                continue;
                            }
                            v = this.jsonToObject(value, s.getType());
                            try {
                                s.setValue(inst, v);
                            } catch (final IllegalArgumentException e) {
                                if (this.isIgnoreIllegalArgumentMappings()) {
                                    continue;
                                } else if (v == null && this.isIgnorePrimitiveNullMapping()) {
                                    continue;
                                }
                                throw e;
                            }

                        }

                        return inst;
                    }
                }
            } else if (type instanceof ParameterizedTypeImpl) {
                final ParameterizedTypeImpl pType = (ParameterizedTypeImpl) type;
                if (List.class.isAssignableFrom(pType.getRawType())) {
                    final List<Object> inst = (List<Object>) pType.getRawType().newInstance();
                    final JSonArray obj = (JSonArray) json;
                    for (final JSonNode n : obj) {
                        inst.add(this.jsonToObject(n, pType.getActualTypeArguments()[0]));
                    }
                    return inst;
                } else if (Map.class.isAssignableFrom(pType.getRawType())) {
                    final Map<String, Object> inst = (Map<String, Object>) pType.getRawType().newInstance();
                    final JSonObject obj = (JSonObject) json;
                    Entry<String, JSonNode> next;
                    for (final Iterator<Entry<String, JSonNode>> it = obj.entrySet().iterator(); it.hasNext();) {
                        next = it.next();
                        inst.put(next.getKey(), this.jsonToObject(next.getValue(), pType.getActualTypeArguments()[1]));
                    }
                    return inst;
                } else {
                    System.err.println("TYPE?!");
                }
            } else {
                System.err.println("TYPE?!");
            }
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param <T>
     * @param json
     * @param typeRef
     */
    @SuppressWarnings("unchecked")
    public <T> T jsonToObject(final JSonNode json, final TypeRef<T> type) {

        return (T) this.jsonToObject(json, type.getType());
    }

    public void setIgnoreIllegalArgumentMappings(final boolean ignoreIllegalArgumentMappings) {
        this.ignoreIllegalArgumentMappings = ignoreIllegalArgumentMappings;
    }

    public void setIgnoreIllegalEnumMappings(final boolean ignoreIllegalEnumMappings) {
        this.ignoreIllegalEnumMappings = ignoreIllegalEnumMappings;
    }

    public void setIgnorePrimitiveNullMapping(final boolean ignoreIllegalNullArguments) {
        this.ignorePrimitiveNullMapping = ignoreIllegalNullArguments;
    }
}
