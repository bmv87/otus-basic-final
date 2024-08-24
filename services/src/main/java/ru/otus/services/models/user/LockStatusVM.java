package ru.otus.services.models.user;

public class LockStatusVM {
    private boolean locked;

    public LockStatusVM(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
