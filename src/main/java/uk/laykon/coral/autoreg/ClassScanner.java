package uk.laykon.coral.autoreg;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class ClassScanner {

    private ClassScanner() {
    }

    static Set<Class<?>> findClasses(String packageName, ClassLoader classLoader) {
        Set<Class<?>> classes = new HashSet<>();
        String packagePath = packageName.replace('.', '/');

        try {
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();

                if ("file".equals(protocol)) {
                    scanDirectory(url, packageName, classLoader, classes);
                } else if ("jar".equals(protocol)) {
                    scanJar(url, packagePath, classLoader, classes);
                }
            }
        } catch (IOException ignored) {
        }

        return classes;
    }

    private static void scanDirectory(URL url, String packageName, ClassLoader classLoader, Set<Class<?>> classes) {
        try {
            Path root = Path.of(url.toURI());
            if (!Files.exists(root)) {
                return;
            }

            try (var stream = Files.walk(root)) {
                stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".class"))
                    .forEach(path -> {
                        String relative = root.relativize(path).toString().replace('\\', '/');
                        String className = packageName + "."
                            + relative.substring(0, relative.length() - ".class".length()).replace('/', '.');
                        loadClass(className, classLoader, classes);
                    });
            }
        } catch (URISyntaxException | IOException ignored) {
        }
    }

    private static void scanJar(URL url, String packagePath, ClassLoader classLoader, Set<Class<?>> classes) {
        try {
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            try (JarFile jarFile = connection.getJarFile()) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!name.startsWith(packagePath) || !name.endsWith(".class") || entry.isDirectory()) {
                        continue;
                    }

                    String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                    loadClass(className, classLoader, classes);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void loadClass(String className, ClassLoader classLoader, Set<Class<?>> classes) {
        try {
            classes.add(Class.forName(className, false, classLoader));
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
        }
    }
}
