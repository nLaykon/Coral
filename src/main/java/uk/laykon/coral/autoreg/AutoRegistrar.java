package uk.laykon.coral.autoreg;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class AutoRegistrar {

    private AutoRegistrar() {
    }

    public static void registerAll(JavaPlugin plugin, String basePackage) {
        Set<Class<?>> classes = ClassScanner.findClasses(basePackage, plugin.getClass().getClassLoader());
        Map<Class<?>, Object> instances = new HashMap<>();
        Map<String, List<MethodBinding>> commandHandlers = new HashMap<>();
        Map<String, List<MethodBinding>> tabHandlers = new HashMap<>();

        for (Class<?> clazz : classes) {
            if (!isInstantiable(clazz)) {
                continue;
            }

            Method[] methods = clazz.getDeclaredMethods();
            if (methods.length == 0) {
                continue;
            }

            Object instance = null;
            for (Method method : methods) {
                AutoCommand autoCommand = method.getAnnotation(AutoCommand.class);
                AutoTabComplete autoTabComplete = method.getAnnotation(AutoTabComplete.class);
                AutoEvent autoEvent = method.getAnnotation(AutoEvent.class);
                AutoRunnable autoRunnable = method.getAnnotation(AutoRunnable.class);
                if (autoCommand == null && autoTabComplete == null && autoEvent == null && autoRunnable == null) {
                    continue;
                }

                if (instance == null) {
                    instance = instances.computeIfAbsent(clazz, key -> instantiate(plugin, key));
                    if (instance == null) {
                        break;
                    }
                }

                method.setAccessible(true);

                if (autoCommand != null) {
                    registerCommandMethod(plugin, instance, method, autoCommand, commandHandlers);
                }
                if (autoTabComplete != null) {
                    registerTabMethod(plugin, instance, method, autoTabComplete, tabHandlers);
                }
                if (autoEvent != null) {
                    registerEventMethod(plugin, instance, method, autoEvent);
                }
                if (autoRunnable != null) {
                    registerRunnableMethod(plugin, instance, method, autoRunnable);
                }
            }
        }

        registerDynamicCommands(plugin, commandHandlers, tabHandlers);
    }

    private static boolean isInstantiable(Class<?> clazz) {
        return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }

    private static void registerCommandMethod(JavaPlugin plugin, Object instance, Method method, AutoCommand annotation, Map<String, List<MethodBinding>> commandHandlers) {
        if (annotation.value().length == 0) {
            plugin.getLogger().warning("Skipping command method with no names: " + describeMethod(method));
            return;
        }

        for (String commandName : annotation.value()) {
            String key = normalize(commandName);
            if (key.isBlank()) {
                continue;
            }
            commandHandlers.computeIfAbsent(key, ignored -> new ArrayList<>()).add(new MethodBinding(instance, method, annotation));
        }
    }

    private static void registerTabMethod(JavaPlugin plugin, Object instance, Method method, AutoTabComplete annotation, Map<String, List<MethodBinding>> tabHandlers) {
        if (annotation.value().length == 0) {
            plugin.getLogger().warning("Skipping tab method with no command names: " + describeMethod(method));
            return;
        }

        for (String commandName : annotation.value()) {
            String key = normalize(commandName);
            if (key.isBlank()) {
                continue;
            }
            tabHandlers.computeIfAbsent(key, ignored -> new ArrayList<>()).add(new MethodBinding(instance, method, null));
        }
    }

    private static void registerEventMethod(JavaPlugin plugin, Object instance, Method method, AutoEvent annotation) {
        Class<? extends Event> eventClass = resolveEventClass(method);
        if (eventClass == null) {
            plugin.getLogger().warning("Skipping @AutoEvent method (missing Event parameter): " + describeMethod(method));
            return;
        }

        Listener listener = new Listener() {
        };
        EventExecutor executor = (ignored, event) -> invokeEventMethod(plugin, instance, method, event);
        plugin.getServer().getPluginManager().registerEvent(
            eventClass,
            listener,
            annotation.priority(),
            executor,
            plugin,
            annotation.ignoreCancelled()
        );
        plugin.getLogger().info("Registered event method: " + describeMethod(method));
    }

    private static void invokeEventMethod(JavaPlugin plugin, Object instance, Method method, Event event) throws EventException {
        try {
            Object[] args = buildArgs(plugin, method, null, null, null, null, event);
            method.invoke(instance, args);
        } catch (Exception ex) {
            throw new EventException(ex);
        }
    }

    private static void registerRunnableMethod(JavaPlugin plugin, Object instance, Method method, AutoRunnable annotation) {
        Runnable runner = () -> {
            try {
                Object[] args = buildArgs(plugin, method, null, null, null, null, null);
                method.invoke(instance, args);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to run @AutoRunnable method " + describeMethod(method) + ": " + ex.getMessage());
            }
        };

        long delay = Math.max(0L, annotation.delayTicks());
        long period = annotation.periodTicks();

        if (period <= 0L) {
            if (annotation.async()) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runner, delay);
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, runner, delay);
            }
        } else if (annotation.async()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runner, delay, period);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, runner, delay, period);
        }
        plugin.getLogger().info("Registered runnable method: " + describeMethod(method));
    }

    private static void registerDynamicCommands(JavaPlugin plugin, Map<String, List<MethodBinding>> commandHandlers, Map<String, List<MethodBinding>> tabHandlers) {
        if (commandHandlers.isEmpty()) {
            return;
        }

        CommandMap commandMap = getCommandMap();
        if (commandMap == null) {
            plugin.getLogger().severe("CommandMap not found. Commands were not auto-registered.");
            return;
        }

        for (Map.Entry<String, List<MethodBinding>> entry : commandHandlers.entrySet()) {
            String commandName = entry.getKey();
            List<MethodBinding> handlers = entry.getValue();
            MethodBinding primary = handlers.getFirst();
            AutoCommand commandMeta = Objects.requireNonNull(primary.commandMeta());

            PluginCommand pluginCommand = newPluginCommand(commandName, plugin);
            if (pluginCommand == null) {
                plugin.getLogger().warning("Could not create plugin command: " + commandName);
                continue;
            }

            pluginCommand.setExecutor((sender, command, label, args) -> executeCommand(plugin, handlers, sender, command, label, args));
            pluginCommand.setTabCompleter((sender, command, alias, args) -> completeCommand(plugin, tabHandlers.getOrDefault(commandName, List.of()), sender, command, alias, args));
            if (!commandMeta.description().isBlank()) {
                pluginCommand.setDescription(commandMeta.description());
            }
            if (!commandMeta.usage().isBlank()) {
                pluginCommand.setUsage(commandMeta.usage());
            }
            if (!commandMeta.permission().isBlank()) {
                pluginCommand.setPermission(commandMeta.permission());
            }
            if (!commandMeta.permissionMessage().isBlank()) {
                pluginCommand.setPermissionMessage(commandMeta.permissionMessage());
            }
            if (commandMeta.aliases().length > 0) {
                pluginCommand.setAliases(Arrays.stream(commandMeta.aliases()).map(AutoRegistrar::normalize).filter(alias -> !alias.isBlank()).toList());
            }

            commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), pluginCommand);
            syncInMemoryPluginDescription(plugin, commandName, commandMeta);
            plugin.getLogger().info("Registered command: " + commandName);
        }
    }

    private static boolean executeCommand(JavaPlugin plugin, List<MethodBinding> handlers, CommandSender sender, Command command, String label, String[] args) {
        boolean handled = false;
        for (MethodBinding binding : handlers) {
            try {
                Object[] methodArgs = buildArgs(plugin, binding.method(), sender, command, label, args, null);
                Object result = binding.method().invoke(binding.instance(), methodArgs);
                if (result instanceof Boolean boolResult) {
                    handled = handled || boolResult;
                } else {
                    handled = true;
                }
            } catch (Exception ex) {
                plugin.getLogger().warning("Command method failed: " + describeMethod(binding.method()) + " - " + formatException(ex));
            }
        }
        return handled;
    }

    private static List<String> completeCommand(JavaPlugin plugin, List<MethodBinding> handlers, CommandSender sender, Command command, String alias, String[] args) {
        if (handlers.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        for (MethodBinding binding : handlers) {
            try {
                Object[] methodArgs = buildArgs(plugin, binding.method(), sender, command, alias, args, null);
                Object result = binding.method().invoke(binding.instance(), methodArgs);
                if (result == null) {
                    continue;
                }

                if (result instanceof Iterable<?> iterable) {
                    for (Object value : iterable) {
                        if (value != null) {
                            completions.add(String.valueOf(value));
                        }
                    }
                    continue;
                }

                if (result instanceof String[] array) {
                    completions.addAll(Arrays.asList(array));
                    continue;
                }

                if (result instanceof String single) {
                    completions.add(single);
                }
            } catch (Exception ex) {
                plugin.getLogger().warning("Tab method failed: " + describeMethod(binding.method()) + " - " + formatException(ex));
            }
        }
        return completions;
    }

    private static Class<? extends Event> resolveEventClass(Method method) {
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (Event.class.isAssignableFrom(parameterType)) {
                @SuppressWarnings("unchecked")
                Class<? extends Event> eventClass = (Class<? extends Event>) parameterType;
                return eventClass;
            }
        }
        return null;
    }

    private static Object[] buildArgs(JavaPlugin plugin, Method method, CommandSender sender, Command command, String labelOrAlias, String[] args, Event event) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] resolved = new Object[parameterTypes.length];

        CommandContext commandContext = null;
        TabContext tabContext = null;

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];

            if (type == JavaPlugin.class || type == Plugin.class) {
                resolved[i] = plugin;
                continue;
            }
            if (type == CommandSender.class) {
                resolved[i] = sender;
                continue;
            }
            if (type == Command.class) {
                resolved[i] = command;
                continue;
            }
            if (type == String.class) {
                resolved[i] = labelOrAlias;
                continue;
            }
            if (type == String[].class) {
                resolved[i] = args == null ? new String[0] : args;
                continue;
            }
            if (type == CommandContext.class) {
                if (commandContext == null) {
                    commandContext = new CommandContext(plugin, sender, command, labelOrAlias, args == null ? new String[0] : args);
                }
                resolved[i] = commandContext;
                continue;
            }
            if (type == TabContext.class) {
                if (tabContext == null) {
                    tabContext = new TabContext(plugin, sender, command, labelOrAlias, args == null ? new String[0] : args);
                }
                resolved[i] = tabContext;
                continue;
            }
            if (event != null && type.isAssignableFrom(event.getClass())) {
                resolved[i] = event;
                continue;
            }

            throw new IllegalArgumentException("Unsupported parameter type in " + describeMethod(method) + ": " + type.getName());
        }

        return resolved;
    }

    private static PluginCommand newPluginCommand(String name, Plugin plugin) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static CommandMap getCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            return (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static void syncInMemoryPluginDescription(JavaPlugin plugin, String commandName, AutoCommand autoCommand) {
        try {
            Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();
            Map<String, Object> data = new LinkedHashMap<>();
            if (!autoCommand.description().isBlank()) {
                data.put("description", autoCommand.description());
            }
            if (!autoCommand.usage().isBlank()) {
                data.put("usage", autoCommand.usage());
            }
            if (!autoCommand.permission().isBlank()) {
                data.put("permission", autoCommand.permission());
            }
            if (autoCommand.aliases().length > 0) {
                data.put("aliases", Arrays.asList(autoCommand.aliases()));
            }
            commands.put(commandName, data);
        } catch (Exception ignored) {
        }
    }

    private static String describeMethod(Method method) {
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String formatException(Exception ex) {
        Throwable root = ex;
        if (ex instanceof InvocationTargetException invocation && invocation.getCause() != null) {
            root = invocation.getCause();
        }
        String message = root.getMessage();
        if (message == null || message.isBlank()) {
            return root.getClass().getSimpleName();
        }
        return root.getClass().getSimpleName() + ": " + message;
    }

    private static Object instantiate(JavaPlugin plugin, Class<?> clazz) {
        try {
            Constructor<?> pluginConstructor = clazz.getDeclaredConstructor(JavaPlugin.class);
            pluginConstructor.setAccessible(true);
            return pluginConstructor.newInstance(plugin);
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Constructor<?> emptyConstructor = clazz.getDeclaredConstructor();
            emptyConstructor.setAccessible(true);
            return emptyConstructor.newInstance();
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("Failed to instantiate " + clazz.getName() + ": " + ex.getMessage());
            return null;
        }
    }

    private record MethodBinding(Object instance, Method method, AutoCommand commandMeta) {
    }
}
