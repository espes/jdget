package org.appwork.remotecall.server;

import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.HashMap;

import org.appwork.remotecall.RemoteCallInterface;
import org.appwork.remotecall.ResponseAlreadySentException;
import org.appwork.remotecall.Utils;
import org.appwork.remotecall.client.MethodHandler;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.utils.logging.Log;

public class RemoteCallServer {

    private final HashMap<String, RemoteCallServiceWrapper> servicesMap;

    public RemoteCallServer() {

        this.servicesMap = new HashMap<String, RemoteCallServiceWrapper>();
    }

    public <T extends RemoteCallInterface> void addHandler(final Class<T> class1, final T serviceImpl) throws ParsingException {

        if (this.servicesMap.containsKey(class1.getSimpleName())) { throw new IllegalArgumentException("Service " + class1 + " already exists"); }
        this.servicesMap.put(class1.getSimpleName(), new RemoteCallServiceWrapper(class1, serviceImpl));
    }

    protected String handleRequest(final Requestor requestor, final String clazz, final String method, final String[] parameters) throws ServerInvokationException {
        try {
            final RemoteCallServiceWrapper service = this.servicesMap.get(new String(clazz));
            if (service == null) { //
                throw new ServerInvokationException(this.handleRequestError(requestor, new BadRequestException("Service not defined: " + clazz)), requestor);

            }
            // find method

            final MethodHandler m = service.getHandler(method);
            if (m == null) { throw new ServerInvokationException(this.handleRequestError(requestor, new BadRequestException("Routine not defined: " + method)), requestor); }

            final TypeRef<Object>[] types = m.getTypeRefs();
            if (types.length != parameters.length) { throw new ServerInvokationException(this.handleRequestError(requestor, new BadRequestException("parameters did not match " + method)), requestor); }
            final Object[] params = new Object[types.length];
            try {
                for (int i = 0; i < types.length; i++) {
//parameters should already be urldecoded here
                    
                    if(types[i].getType()==String.class){
                        //fix if there is no " around strings
                         if(!parameters[i].startsWith("\""))parameters[i]="\""+parameters[i]; 
                         if(!parameters[i].endsWith("\""))parameters[i]+="\"";
                     }
                    params[i] = Utils.convert(JSonStorage.restoreFromString(parameters[i], types[i], null), types[i].getType());
                }

            } catch (final Exception e) {
                throw new ServerInvokationException(this.handleRequestError(requestor, new BadRequestException("Parameter deserialize error for " + method)), requestor);
            }

            Object answer;

            answer = service.call(m.getMethod(), params);
            return JSonStorage.serializeToJson(answer);

        } catch (final InvocationTargetException e1) {
            final Throwable cause = e1.getCause();
            if (cause != null) {
                if (cause instanceof ResponseAlreadySentException) { throw (ResponseAlreadySentException) cause; }
                throw new ServerInvokationException(this.handleRequestError(requestor, cause), requestor);
            }
            throw new ServerInvokationException(this.handleRequestError(requestor, new RuntimeException(e1)), requestor);
        } catch (ServerInvokationException e) {
            throw e;
        } catch (final Throwable e) {
            throw new ServerInvokationException(this.handleRequestError(requestor, e), requestor);
        }

    }

    protected String handleRequestError(final Requestor requestor, final Throwable e) {
        // TODO byte[]-generated method stub
        Log.exception(e);
        final StringBuilder sb = new StringBuilder();

        try {
            sb.append(JSonStorage.serializeToJson(new ExceptionWrapper(e)));
        } catch (final Throwable e1) {

            // TODO Auto-generated catch block
            // TODO: URLENCODE here
            e1.printStackTrace();
            sb.append("{\"name\":\"java.lang.Exception\",\"exception\":{\"cause\":null,\"message\":\"Serialize Problem: ");
            sb.append(e1.getMessage());
            sb.append(e1.getLocalizedMessage());
            sb.append("\",\"stackTrace\":[]}}");

        }

        return sb.toString();
    }
}
