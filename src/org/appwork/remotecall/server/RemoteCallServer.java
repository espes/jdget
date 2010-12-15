package org.appwork.remotecall.server;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;

import org.appwork.remotecall.RemoteCallService;
import org.appwork.remotecall.Utils;
import org.appwork.storage.JSonStorage;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class RemoteCallServer {

    private final HashMap<String, RemoteCallServiceWrapper> servicesMap;

    public RemoteCallServer() {

        servicesMap = new HashMap<String, RemoteCallServiceWrapper>();
    }

    public void addHandler(final Class<?> class1, final RemoteCallService serviceImpl) {

        if (servicesMap.containsKey(class1.getSimpleName())) { throw new IllegalArgumentException("Service " + class1 + " already exists"); }
        servicesMap.put(class1.getSimpleName(), RemoteCallServiceWrapper.create(serviceImpl));
    }

    /**
     * @param string
     * @param class1
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private Object convert(final String string, final Class<?> class1) throws JsonParseException, JsonMappingException, IOException {
        final Object obj = JSonStorage.restoreFromString(string, class1);
        return Utils.convert(obj, class1);

    }

    protected String handleRequest(final String clazz, final String method, final String parameter) {

        final RemoteCallServiceWrapper service = servicesMap.get(new String(clazz));
        if (service == null) { throw new IllegalArgumentException("No service " + clazz + " registered"); }
        // find method
        final String[] parameters = parameter.split(Utils.PARAMETER_DELIMINATOR + "");
        final Method m = service.getMethod(method);
        final Class<?>[] types = m.getParameterTypes();
        final Object[] params = new Object[types.length];
        try {
            for (int i = 0; i < types.length; i++) {
                params[i] = convert(URLDecoder.decode(parameters[i], "UTF-8"), types[i]);
            }

            return Utils.serialiseSingleObject(service.call(m, params));

        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Could not serialize answer");
        }

    }

    protected String handleRequestError(final Throwable e, final String clazz, final String method, final String parameter) {
        // TODO byte[]-generated method stub
        return null;
    }

}
