package ru.larev.payload;

/**
 * @author Larev Pavel
 * @author http://telegram.me/larev
 */
public class JobCreated {
    private long id;

    public JobCreated(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
