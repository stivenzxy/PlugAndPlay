package com.plugAndPlay.Entities;

public class Audio {
    private Long id;
    private final String name;
    private final String format;

    public Audio(Long id, String name, String format) {
        this.id = id;
        this.name = name;
        this.format = format;
    }

    public Audio(String name, String path, String format) {
        this.name = name;
        this.format = format;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getFormat() { return format; }

    @Override
    public String toString() {
        return "Audio{id=" + id + ", name='" + name + "', format='" + format + "'}";
    }
}
