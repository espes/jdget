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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.appwork.storage.simplejson.JSonArray;
import org.appwork.storage.simplejson.JSonNode;
import org.appwork.storage.simplejson.JSonObject;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author thomas
 * 
 */
public class JSonMapper {

    public JSonMapper() {

    }

    /**
     * @param obj
     * @return
     * @throws MapperException
     */
    @SuppressWarnings("unchecked")
    public JSonNode create(final Object obj) throws MapperException {
        try {
            if (obj == null) { return new JSonObject(null); }
            final Class<? extends Object> clazz = obj.getClass();
            if (clazz.isPrimitive()) {
                if (clazz == boolean.class) {
                    return new JSonObject((Boolean) obj);
                } else if (clazz == char.class) {
                    return new JSonObject(0 + ((Character) obj).charValue());
                } else if (clazz == byte.class) {
                    return new JSonObject(((Byte) obj).longValue());
                } else if (clazz == short.class) {
                    return new JSonObject(((Short) obj).longValue());
                } else if (clazz == int.class) {
                    return new JSonObject(((Integer) obj).longValue());
                } else if (clazz == long.class) {
                    return new JSonObject(((Long) obj).longValue());
                } else if (clazz == float.class) {
                    return new JSonObject(((Float) obj).doubleValue());
                } else if (clazz == double.class) { return new JSonObject(((Double) obj).doubleValue()); }
            } else if (clazz.isEnum()) {
                return new JSonObject(obj + "");
            } else if (obj instanceof Boolean) {
                return new JSonObject(((Boolean) obj).booleanValue());
            } else if (obj instanceof Character) {
                return new JSonObject(0 + ((Character) obj).charValue());
            } else if (obj instanceof Byte) {
                return new JSonObject(((Byte) obj).longValue());
            } else if (obj instanceof Short) {
                return new JSonObject(((Short) obj).longValue());
            } else if (obj instanceof Integer) {
                return new JSonObject(((Integer) obj).longValue());
            } else if (obj instanceof Long) {
                return new JSonObject(((Long) obj).longValue());
            } else if (obj instanceof Float) {
                return new JSonObject(((Float) obj).doubleValue());
            } else if (obj instanceof Double) {
                return new JSonObject(((Double) obj).doubleValue());

            } else if (obj instanceof String) {
                return new JSonObject((String) obj);
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

    /**
     * @param value
     * @param type
     * @return
     */
    private Object jsonToObject(final JSonNode json, final Type type) {
        final ClassCache cc;
        try {
            if (json instanceof JSonObject) {
                switch (((JSonObject) json).getType()) {
                case BOOLEAN:
                case DOUBLE:
                case LONG:
                case STRING:
                    return ((JSonObject) json).getValue();

                case NULL:
                    return null;

                }
            }

            if (type instanceof Class) {
                final Class<?> clazz = (Class<?>) type;
                if (List.class.isAssignableFrom(clazz)) {
                    System.err.println("TYPE?!");
                } else if (clazz.isArray()) {
                    final JSonArray obj = (JSonArray) json;
                    final Object arr = Array.newInstance(clazz.getComponentType(), obj.size());
                    for (int i = 0; i < obj.size(); i++) {
                        Object v = this.jsonToObject(obj.get(i), clazz.getComponentType());
                        if (clazz.getComponentType().isPrimitive()) {
                            if (clazz.getComponentType() == boolean.class) {
                                v = ((Boolean) v).booleanValue();
                            } else if (clazz.getComponentType() == char.class) {
                                v = (char) ((Long) v).byteValue();
                            } else if (clazz.getComponentType() == byte.class) {
                                v = ((Long) v).byteValue();
                            } else if (clazz.getComponentType() == short.class) {
                                v = ((Long) v).shortValue();
                            } else if (clazz.getComponentType() == int.class) {
                                v = ((Long) v).intValue();
                            } else if (clazz.getComponentType() == long.class) {
                                v = ((Long) v).longValue();
                            } else if (clazz.getComponentType() == float.class) {
                                v = ((Double) v).floatValue();
                            } else if (clazz.getComponentType() == double.class) {
                                //
                                v = ((Double) v).doubleValue();

                            }
                        }
                        Array.set(arr, i, v);

                    }
                    return arr;
                } else {

                    final JSonObject obj = (JSonObject) json;
                    cc = ClassCache.getClassCache(clazz);

                    @SuppressWarnings("unchecked")
                    final Object inst = cc.getInstance();
                    JSonNode value;
                    for (final Setter s : cc.getSetter()) {

                        value = obj.get(s.getKey());
                        if (value == null) {
                            continue;
                        }

                        s.setValue(inst, this.jsonToObject(value, s.getType()));

                    }

                    return inst;
                }
            } else if (type instanceof ParameterizedTypeImpl) {
                final ParameterizedTypeImpl pType = (ParameterizedTypeImpl) type;
                if (List.class.isAssignableFrom(pType.getRawType())) {
                    @SuppressWarnings("unchecked")
                    final List<Object> inst = (List<Object>) pType.getRawType().newInstance();
                    final JSonArray obj = (JSonArray) json;
                    for (final JSonNode n : obj) {
                        inst.add(this.jsonToObject(n, pType.getActualTypeArguments()[0]));
                    }
                    return inst;
                } else if (Map.class.isAssignableFrom(pType.getRawType())) {
                    @SuppressWarnings("unchecked")
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param <T>
     * @param json
     * @param typeRef
     */
    public <T> T jsonToObject(final JSonNode json, final TypeRef<T> type) {
        final Class<?> clazz = (Class<?>) ((ParameterizedTypeImpl) type.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];

        return (T) this.jsonToObject(json, clazz);
    }
}
