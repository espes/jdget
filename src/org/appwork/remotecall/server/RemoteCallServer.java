package org.appwork.remotecall.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;

import org.appwork.remotecall.Utils;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.Exceptions;
import org.appwork.utils.logging.Log;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class RemoteCallServer {

    private final HashMap<String, RemoteCallServiceWrapper> servicesMap;

    public RemoteCallServer() {

        this.servicesMap = new HashMap<String, RemoteCallServiceWrapper>();
    }

    public <T> void addHandler(final Class<T> class1, final T serviceImpl) {

        if (this.servicesMap.containsKey(class1.getSimpleName())) { throw new IllegalArgumentException("Service " + class1 + " already exists"); }
        this.servicesMap.put(class1.getSimpleName(), RemoteCallServiceWrapper.create(serviceImpl));
    }

    /**
     * breaks the stacktrace. the invoke process is not important
     * 
     * @param e
     * @param callerid
     */
    private void cleanUpStackTrace(Throwable e, final String callerid) {
        // if e is no runtimeexception, we certainly expect and handle this
        // exception type anyway. no reason for transmitting stacktraces
        if (e instanceof RuntimeException && !(e instanceof BadRequestException)) {
            final StackTraceElement[] stack = e.getStackTrace();
            StackTraceElement[] newStack = new StackTraceElement[] {};
            for (final StackTraceElement el : stack) {
                if (el.getClassName().startsWith(java.lang.reflect.Method.class.getName())) {
                    final StackTraceElement[] n = new StackTraceElement[newStack.length - 2];
                    System.arraycopy(newStack, 0, n, 0, newStack.length - 2);
                    n[n.length - 1] = new StackTraceElement("RemotecallServer from ", callerid, null, -1);
                    e.setStackTrace(n);
                    break;

                } else {

                    final StackTraceElement[] n = new StackTraceElement[newStack.length + 1];
                    System.arraycopy(newStack, 0, n, 0, newStack.length);
                    n[n.length - 1] = el;
                    newStack = n;
                }
            }
            e = e.getCause();
        }
        while (e != null) {
            // do not send cause stacktraces
            e.setStackTrace(e.getStackTrace().length > 0 ? new StackTraceElement[] { e.getStackTrace()[0] } : new StackTraceElement[] {});
            e = e.getCause();
        }
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

    protected String handleRequest(final String remoteID, final String clazz, final String method, final String[] parameters) throws ServerInvokationException {
        try {
            final RemoteCallServiceWrapper service = this.servicesMap.get(new String(clazz));
            if (service == null) { //
                throw new ServerInvokationException(this.handleRequestError(remoteID, new BadRequestException("Service not defined: " + clazz)), remoteID);

            }
            // find method

            final Method m = service.getMethod(method);
            if (m == null) { throw new ServerInvokationException(this.handleRequestError(remoteID, new BadRequestException("Routine not defined: " + method)), remoteID); }
            final Class<?>[] types = m.getParameterTypes();
            if (types.length != parameters.length) { throw new ServerInvokationException(this.handleRequestError(remoteID, new BadRequestException("parameters did not match " + method)), remoteID); }
            final Object[] params = new Object[types.length];
            try {
                for (int i = 0; i < types.length; i++) {
                    params[i] = this.convert(URLDecoder.decode(parameters[i], "UTF-8"), types[i]);
                }

            } catch (final Exception e) {
                throw new ServerInvokationException(this.handleRequestError(remoteID, new BadRequestException("Parameter deserialize error for " + method)), remoteID);
            }

            Object answer;
            answer = service.call(m, params);
            return JSonStorage.serializeToJson(answer);

        } catch (final InvocationTargetException e1) {
            // TODO Auto-generated catch block
            final Throwable cause = e1.getCause();
            if (cause != null) { throw new ServerInvokationException(this.handleRequestError(remoteID, cause), remoteID); }
            throw new ServerInvokationException(this.handleRequestError(remoteID, new RuntimeException(e1)), remoteID);
        }catch(ServerInvokationException e){
            throw e;
        } catch (final Throwable e) {
            // TODO Auto-generated catch block

            throw new ServerInvokationException(this.handleRequestError(remoteID, e), remoteID);
        }

    }

    protected String handleRequestError(final String callerid, final Throwable e) {
        // TODO byte[]-generated method stub
        Log.exception(e);
        final StringBuilder sb = new StringBuilder();

        this.cleanUpStackTrace(e, callerid);
        try {
            sb.append(JSonStorage.serializeToJson(new ExceptionWrapper(e)));
        } catch (final Throwable e1) {
            try {
                sb.append(JSonStorage.serializeToJson(new ExceptionWrapper(new UnserialisableException(Exceptions.getStackTrace(e)))));
            } catch (final Throwable e2) {
                // TODO Auto-generated catch block
                // TODO: URLENCODE here
                e2.printStackTrace();
                sb.append("{\"name\":\"java.lang.Exception\",\"exception\":{\"cause\":null,\"message\":\"Serialize Problem: ");
                sb.append(e1.getMessage());
                sb.append(e1.getLocalizedMessage());
                sb.append("\",\"stackTrace\":[]}}");
            }

            // e1.printStackTrace();
            // seems we could not serialize the original Exception. create a
            // dummy

        }

        return sb.toString();
    }
}
