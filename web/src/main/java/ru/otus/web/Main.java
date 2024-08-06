package ru.otus.web;

public class Main {
    public static void main(String[] args) throws Exception {
        try (var server = new HttpServer(8189)) {
            server.start();
        }
    }
}