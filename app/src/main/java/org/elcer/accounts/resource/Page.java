package org.elcer.accounts.resource;

public enum Page {
    SELF("This page"),
    NEXT("Next page"),
    LAST("Last page"),
    PREV("Previous page");

    private final String title;

    Page(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
