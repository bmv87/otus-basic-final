package ru.otus.repository.models;

import java.util.ArrayList;
import java.util.List;

public class PaginatedResult <T> {

    long totalCount;

    List<T> items = new ArrayList<T>();

    public PaginatedResult() {
    }

    public PaginatedResult(long totalCount, List<T> items) {
        this.totalCount = totalCount;
        this.items = items;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}