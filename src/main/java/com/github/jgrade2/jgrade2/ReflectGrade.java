package com.github.jgrade2.jgrade2;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Helper class used to consolidate all {@link java.lang.reflect} functionality
 * in grabbing annotated methods and making sure they have the proper
 * signature. Much of this code is borrowed from Peter Froehlich's Jaybee
 * which is open source at
 * <a href="https://github.com/phf/jb">https://github.com/phf/jb</a>.
 */
final class ReflectGrade {

    /**
     * Private constructor to prevent instantiation.
     */
    private ReflectGrade() { }

    /**
     * Load a class from the current directory. The loader is not closed as the resource is
     * still necessary for the rest of jGrade2 to uitilize.
     * <p>
     * Method is borrowed from <a href="https://github.com/phf/jb">https://github.com/phf/jb</a>.
     *
     * @param className The name of the class to load.
     * @return The class object.
     * @throws ClassNotFoundException If the class cannot be found.
     * @throws MalformedURLException If the URL is malformed.
     *
     */
    static Class<?> load(String className) throws ClassNotFoundException, MalformedURLException {
        URL url = FileSystems.getDefault().getPath("").toUri().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[]{url});
        return loader.loadClass(className);
    }

    /**
     * A helper class to hold the three types of grade methods.
     */
    private static class GradeMethods {
        private SortedMap<String, Method> beforeGradeMethods;
        private SortedMap<String, Method> doneGradeMethods;
        private SortedMap<String, Method> gradeMethods;

        /**
         * Create a new GradeMethods object.
         */
        GradeMethods() {
            beforeGradeMethods = new TreeMap<>();
            doneGradeMethods = new TreeMap<>();
            gradeMethods = new TreeMap<>();
        }

        /**
         * Convert the maps to a list of methods.
         * @return The list of methods.
         */
        List<Method> toMethodList() {
            List<Method> l = new ArrayList<>();
            l.addAll(beforeGradeMethods.values());
            l.addAll(gradeMethods.values());
            l.addAll(doneGradeMethods.values());
            return l;
        }
    }

    /**
     * Check if the method has a grade annotation. Should only have 1.
     * @param m The method to check.
     * @return True if the method has a grade annotation, false otherwise.
     */
    private static boolean hasGradeAnnotation(Method m) {
        int num = (m.isAnnotationPresent(BeforeGrading.class) ? 1 : 0)
                + (m.isAnnotationPresent(Grade.class) ? 1 : 0)
                + (m.isAnnotationPresent(AfterGrading.class) ? 1 : 0);

        if (num > 1) {
            System.err.printf("method %s has too many grade annotations, should only have 1", m.getName());
        }

        return num == 1;
    }

    /**
     * Add the method to the appropriate map if it is valid.
     * @param gradeMethods The GradeMethods object to add to.
     * @param m The method to add.
     */
    private static void addIfValid(GradeMethods gradeMethods, Method m) {
        if (!hasGradeAnnotation(m) || !isValidSignature(m)) {
            return;
        }

        if (m.isAnnotationPresent(BeforeGrading.class)) {
            gradeMethods.beforeGradeMethods.put(m.getName(), m);
        } else if (m.isAnnotationPresent(Grade.class)) {
            gradeMethods.gradeMethods.put(m.getName(), m);
        } else if (m.isAnnotationPresent(AfterGrading.class)) {
            gradeMethods.doneGradeMethods.put(m.getName(), m);
        }
    }

    /**
     * Check if the method has the proper signature.
     * @param m The method to check.
     * @return True if the method has the proper signature, false otherwise.
     */
    private static boolean isValidSignature(Method m) {
        if (m.getParameterCount() != 1) {
            System.err.printf("method %s should have exactly 1 parameter\n", m.getName());
        } else if (!m.getParameterTypes()[0].equals(Grader.class)) {
            System.err.printf("method %s parameter should be of type Grader\n", m.getName());
        } else if (m.getReturnType() != Void.TYPE) {
            System.err.printf("method %s must return void, not %s\n", m.getName(), m.getReturnType());
        } else if (Modifier.isStatic(m.getModifiers())) {
            System.err.printf("method %s must not be static\n", m.getName());
        } else if (!Modifier.isPublic(m.getModifiers())) {
            System.err.printf("method %s must be declared public\n", m.getName());
        } else {
            return true;
        }

        return false;
    }

    /**
     * Get the list of grade methods from the class.
     * @param c The class to get the grade methods from.
     * @return The list of grade methods.
     */
    static List<Method> graderMethods(Class<?> c) {
        GradeMethods gradeMethods = new GradeMethods();
        for (Method m: c.getMethods()) {
            addIfValid(gradeMethods, m);
        }
        return gradeMethods.toMethodList();
    }
}
