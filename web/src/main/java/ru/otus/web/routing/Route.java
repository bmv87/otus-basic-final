package ru.otus.web.routing;

import ru.otus.web.http.HttpMethod;

import java.util.Arrays;
import java.util.Objects;

public class Route implements Comparable<Route> {

    private final HttpMethod method;
    private final String pathString;
    private final String[] paths;

    public Route(HttpMethod method, String pathString) {
        if (pathString.contains("?") || pathString.contains("/r/n") || pathString.contains(" ")) {
            throw new IllegalArgumentException("pathString parameter contains invalid symbols");
        }
        if (pathString.startsWith("/")) {
            throw new IllegalArgumentException("pathString parameter can't start with symbols '/'");
        }
        this.method = method;
        this.pathString = pathString;
        this.paths = parsePaths(pathString);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String[] getPaths() {
        return paths;
    }

    public String getPathString() {
        return pathString;
    }

    private String[] parsePaths(String pathString) {
        return pathString.split("/");
    }

    public int getIndexOfPathVariable(String name) {

        for (int i = 0; i < paths.length; i++) {
            if (paths[i].equalsIgnoreCase("{" + name + "}")) {
                return i;
            }
        }
        return -1;
    }

    private String parsePathString(String pathString) {

        var pathEndIndex = pathString.indexOf("?");
        if (pathEndIndex == -1) {
            pathEndIndex = pathString.indexOf(" ");
        }
        if (pathEndIndex == -1) {
            return pathString;
        }
        return pathString.substring(0, pathEndIndex);
    }

    @Override
    public int compareTo(Route o) {
        return this.pathString.compareTo(o.pathString);
    }

    public boolean isSameRoute(HttpMethod currentMethod, String fullPathString) {

        if (currentMethod != method) {
            return false;
        }
        var currentPath = parsePathString(fullPathString);
        if (currentPath.equalsIgnoreCase(pathString)) {
            return true;
        }
        var currentPaths = parsePaths(parsePathString(fullPathString));
        if (currentPaths.length != paths.length) {
            return false;
        }
        if (paths.length == 1 && isPathVariable(paths[0])) {
            return true;
        }

        for (int i = 0; i < paths.length; i++) {
            var path = paths[i];
            if (!isPathVariable(path) && !path.equalsIgnoreCase(currentPaths[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isPathVariable(String path) {
        return path.startsWith("{") && path.endsWith("}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return method == route.method && Objects.equals(pathString, route.pathString) && Objects.deepEquals(paths, route.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, pathString, Arrays.hashCode(paths));
    }

    @Override
    public String toString() {
        return "Route: " +
                method + " " + pathString;
    }
}
