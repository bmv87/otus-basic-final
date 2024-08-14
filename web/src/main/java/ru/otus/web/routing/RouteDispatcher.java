package ru.otus.web.routing;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.handlers.*;
import ru.otus.web.http.Constants;
import ru.otus.web.http.HttpContext;
import ru.otus.web.http.HttpMethod;
import ru.otus.web.security.Autentificated;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RouteDispatcher {

    private final Map<Route, HttpContextHandler> routes = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(RouteDispatcher.class);

    public RouteDispatcher() {
        Reflections reflections = new Reflections("ru.otus.web.controllers");
        Set<Class<?>> set = reflections.getTypesAnnotatedWith(Controller.class);
        for (Class<?> c : set) {
            for (Method m : c.getMethods()) {
                var routeAnnotations = m.getAnnotation(RoutePath.class);
                if (routeAnnotations == null) {
                    continue;
                }

                var route = new Route(routeAnnotations.method(), routeAnnotations.path());
                HttpContextHandler handlersChain = new RouteHandler(route, m);

                var authAnnotations = m.getAnnotation(Autentificated.class);
                if (authAnnotations != null) {
                    handlersChain = new TokenHandler(handlersChain);
                }
                handlersChain = new ExceptionHandler(handlersChain);
                routes.put(route, handlersChain);
                logger.debug(route.toString());
            }
        }
        Map<Route, HttpContextHandler> optionsRoutes = new HashMap<>();
        var groupedRoutes = routes.keySet().stream()
                .collect(Collectors.groupingBy(Route::getPathString));
        for (var g : groupedRoutes.entrySet()) {
            var route = new Route(HttpMethod.OPTIONS, g.getKey());
            logger.debug(route.toString());
            Map<String, String> headers = new HashMap<>();

            var allowed = HttpMethod.OPTIONS + "," + g.getValue().stream()
                    .map(v -> v.getMethod().name())
                    .collect(Collectors.joining(","));
            logger.debug("Allowed methods: {}", allowed);

            headers.put(Constants.Headers.ACCESS_CONTROL_ALLOW_METHODS, allowed);
            optionsRoutes.put(route, new ExceptionHandler(new OptionsRequestHandler(headers)));
        }
        routes.putAll(optionsRoutes);
    }

    public void execute(HttpContext context) throws IOException {
        if (context.getRequest().getRawRequest() == null ||
                context.getRequest().getRawRequest().isBlank()) {
            return;
        }
        var routePath = context.getRequest().getPath();
        var method = context.getRequest().getMethod();

        var key = routes.keySet().stream()
                .sorted()
                .filter(route -> route.getMethod() == method && route.isSameRoutePath(routePath))
                .findFirst()
                .orElse(null);
        if (key == null) {
            new ExceptionHandler(new NotFoundHandler()).execute(context);
        } else {
            routes.get(key).execute(context);
        }
    }
}
