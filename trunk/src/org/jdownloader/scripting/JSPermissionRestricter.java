package org.jdownloader.scripting;

import java.util.concurrent.ConcurrentHashMap;

import org.appwork.utils.logging.Log;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.tools.shell.Global;

/**
 * from http://codeutopia.net/blog/2009/01/02/sandboxing-rhino-in-java/
 * 
 * ============================================================================ =========
 * 
 * Sandboxing Rhino in Java January 2, 2009 – 12:07 am Tags: Java, JavaScript, Security I’ve been working on a Java app which needed Rhino
 * for scripting. The app would need to run untrusted JavaScript code from 3rd parties, so I had to find a way to block access to all Java
 * methods, except the ones I wanted. This would not be a problem if there was an easy way to disable LiveConnect - the feature of Rhino
 * which provides java access to scripts - but there is no such thing.
 * 
 * However, after a lot of digging around, I finally found a way to do this without too much hacking. In fact, it can be done by just
 * extending a few of the Rhino classes, and using the setters provided to override some of the default ones.
 * 
 * 
 * ClassShutter
 * 
 * Let’s first look at the ClassShutter, which can be used to restrict access to Java packages and classes.
 * 
 * //cx is the Context instance you're using to run scripts cx.setClassShutter(new ClassShutter() { public boolean visibleToScripts(String
 * className) { if(className.startsWith("adapter")) return true;
 * 
 * return false; } }); The above will effectively disable access to all Java classes onwards from the point where the shutter was set.
 * However, if you run any scripts before setting the shutter, classes accessed there can still be used! You can use this to your advantage,
 * for example to provide specific classes in the scripts under different names or such.
 * 
 * You probably noticed the comparison to “adapter” in the shutter. This is for when you implement interfaces or extend Java classes. Rhino
 * will create new classes based on those interfaces/classes, and they will be called adapterN, where N is a number. If you block access to
 * classes starting with adapter, you can’t implement or extend, and my use-case required that.
 * 
 * However, there is a limitation in the ClassShutter…
 * 
 * Reflection
 * 
 * As you may know, you can use someInstance.getClass().forName(”some.package.Class”).newInstance() to get a new instance of
 * some.package.Class.
 * 
 * This will not get blocked by the ClassShutter! We need to disable access to getClass() to block this.
 * 
 * While the ClassShutter is relatively well documented, doing this required more research. A post in the Rhino mailing list finally pushed
 * me to the right direction: Overriding certain NativeJavaObject methods and creating a custom
 * sun.org.mozilla.javascript.internal.ContextFactory and WrapFactory for that.
 * 
 * Here is an extended NativeJavaObject, which blocks access to getClass. You could use this approach to block access to other methods too:
 * 
 * public static class SandboxNativeJavaObject extends NativeJavaObject { public SandboxNativeJavaObject(Scriptable scope, Object
 * javaObject, Class staticType) { super(scope, javaObject, staticType); }
 * 
 * @Override public Object get(String name, Scriptable start) { if (name.equals("getClass")) { return NOT_FOUND; }
 * 
 *           return super.get(name, start); } } To make the above class work, you need two more classes:
 * 
 *           A WrapFactory which returns our SandboxNativeJavaObject’s
 * 
 *           public static class SandboxWrapFactory extends WrapFactory {
 * @Override public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType) { return new
 *           SandboxNativeJavaObject(scope, javaObject, staticType); } } And a sun.org.mozilla.javascript.internal.ContextFactory, which
 *           returns Context’s which use SandboxWrapFactory:
 * 
 *           public class SandboxContextFactory extends sun.org.mozilla.javascript.internal.ContextFactory {
 * @Override protected Context makeContext() { Context cx = super.makeContext(); cx.setWrapFactory(new SandboxWrapFactory()); return cx; } }
 *           Finally, to make all this work, we need to tell Rhino the global sun.org.mozilla.javascript.internal.ContextFactory:
 * 
 *           sun.org.mozilla.javascript.internal.ContextFactory.initGlobal(new SandboxContextFactory()); With this, we are done. Now, when
 *           you use sun.org.mozilla.javascript.internal.ContextFactory.getGlobal().enterContext(), you will get sandboxing contexts. But
 *           why did we need to set it globally? This is because it would appear that certain things, such as the adapter classes, use the
 *           global context factory to get some context for themselves, and without setting the global factory, they would get unlimited
 *           access.
 * 
 *           In closing
 * 
 *           I hope this is useful for someone. It took me a long time to figure it all out, so here it is now, all documented in one place.
 *           =)
 * 
 *           The mailing list post where I found the direction for blocking getClass can be found here. Thanks to Charles Lowell.
 * 
 *           There is also the SecurityController, which may be useful in further securing the class.
 * 
 *           And as a final warning, while this approach works for me, and I haven’t yet found any way to get past the sandboxing and into
 *           Java-land… but there may be a way, and if you find one, do let me know.
 * 
 * 
 * 
 *           ================================================================== == =================
 * @author thomas
 * 
 */
public class JSPermissionRestricter {
    static public class SandboxContextFactory extends ContextFactory {
        @Override
        protected Context makeContext() {
            Context cx = super.makeContext();
            cx.setWrapFactory(new SandboxWrapFactory());
            cx.setClassShutter(new ClassShutter() {
                public boolean visibleToScripts(String className) {
                    Thread cur = Thread.currentThread();
                    if (TRUSTED_THREAD.containsKey(cur)) {
                        Log.L.severe("Trusted Thread Loads: " + className);
                        return true;

                    }
                    if (className.startsWith("adapter")) {
                        return true;

                    } else if (className.equals("net.sourceforge.htmlunit.corejs.javascript.EcmaError")) {
                        Log.L.severe("Javascript error occured");
                        return true;
                    } else {

                        throw new RuntimeException("Security Violation " + className);
                    }

                }
            });

            return cx;
        }
    }

    public static class SandboxWrapFactory extends WrapFactory {

        @SuppressWarnings("rawtypes")
        @Override
        public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType) {
            if (javaObject instanceof EcmaError) {
                Log.exception((EcmaError) javaObject);
            }
            return new SandboxNativeJavaObject(scope, javaObject, staticType);
        }

    }

    public static class SandboxNativeJavaObject extends NativeJavaObject {

        private static final long serialVersionUID = -2783084485265910840L;

        public SandboxNativeJavaObject(Scriptable scope, Object javaObject, Class<?> staticType) {

            super(scope, javaObject, staticType);
        }

        @Override
        public Object get(String name, Scriptable start) {

            if (name.equals("getClass")) {
                Log.L.severe("JS Security Exception");
                return NOT_FOUND;
            }

            return super.get(name, start);

        }
    }

    private static ConcurrentHashMap<Thread, Boolean> TRUSTED_THREAD = new ConcurrentHashMap<Thread, Boolean>();

    public static Object evaluateTrustedString(Context cx, Global scope, String source, String sourceName, int lineno, Object securityDomain) {
        try {
            TRUSTED_THREAD.put(Thread.currentThread(), true);

            return cx.evaluateString(scope, source, sourceName, lineno, securityDomain);
        } finally {
            TRUSTED_THREAD.remove(Thread.currentThread());
        }
    }

    public static void init() {
        // try {
        //
        // sun.org.mozilla.javascript.internal.ContextFactory.initGlobal(new sun.org.mozilla.javascript.internal.ContextFactory() {
        //
        // @Override
        // protected sun.org.mozilla.javascript.internal.Context makeContext() {
        // throw new
        // WTFException("No Allowed. Use org.mozilla.javascript.* or net.sourceforge.htmlunit.corejs.javascript.* (JD2 only!) instead");
        // }
        //
        // @Override
        // protected boolean hasFeature(sun.org.mozilla.javascript.internal.Context context, int i) {
        // throw new
        // WTFException("No Allowed. Use org.mozilla.javascript.* or net.sourceforge.htmlunit.corejs.javascript.* (JD2 only!) instead");
        // }
        //
        // @Override
        // protected GeneratedClassLoader createClassLoader(ClassLoader classloader) {
        // throw new
        // WTFException("No Allowed. Use org.mozilla.javascript.* or net.sourceforge.htmlunit.corejs.javascript.* (JD2 only!) instead");
        // }
        //
        // @Override
        // protected Object doTopCall(Callable callable, sun.org.mozilla.javascript.internal.Context context,
        // sun.org.mozilla.javascript.internal.Scriptable scriptable, sun.org.mozilla.javascript.internal.Scriptable scriptable1, Object[]
        // aobj) {
        // throw new
        // WTFException("No Allowed. Use org.mozilla.javascript.* or net.sourceforge.htmlunit.corejs.javascript.* (JD2 only!) instead");
        // }
        //
        // @Override
        // protected void observeInstructionCount(sun.org.mozilla.javascript.internal.Context context, int i) {
        // throw new
        // WTFException("No Allowed. Use org.mozilla.javascript.* or net.sourceforge.htmlunit.corejs.javascript.* (JD2 only!) instead");
        // }
        //
        // @Override
        // protected void onContextCreated(sun.org.mozilla.javascript.internal.Context context) {
        // throw new
        // WTFException("No Allowed. Use org.mozilla.javascript.* or net.sourceforge.htmlunit.corejs.javascript.* (JD2 only!) instead");
        // }
        //
        // @Override
        // protected void onContextReleased(sun.org.mozilla.javascript.internal.Context context) {
        // throw new
        // WTFException("No Allowed. Use org.mozilla.javascript.* or net.sourceforge.htmlunit.corejs.javascript.* (JD2 only!) instead");
        // }
        // });
        // // let's do a test
        // final ScriptEngineManager manager = new ScriptEngineManager();
        // final ScriptEngine engine = manager.getEngineByName("javascript");
        //
        // if (engine != null) {
        // throw new WTFException("Failed to disable internal Rhino");
        // }
        // } catch (ExceptionInInitializerError e) {
        // // desired exception.
        // } catch (Throwable e) {
        // // sun.org.mozilla.javascript.internal.* may be unavailable
        // e.printStackTrace();
        // }

        try {
            ContextFactory.initGlobal(new SandboxContextFactory());
            // let's do a test
            try {
                Context cx = null;
                try {
                    cx = ContextFactory.getGlobal().enterContext();

                    cx.evaluateString(cx.initStandardObjects(), "java.lang.System.out.println('TEST')", "<cmd>", 1, null);

                } finally {
                    Context.exit();
                }

                throw new SecurityException("Could not install the sun.org.mozilla.javascript.internal Sandbox!");
            } catch (NullPointerException e) {
                throw e;
            } catch (SecurityException e) {
                throw e;
            } catch (RuntimeException e) {
                if (!"Security Violation org".equals(e.getMessage())) {
                    throw e;
                } else {
                    // test successfull. Security Sandbox successfully initialized
                }

            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
