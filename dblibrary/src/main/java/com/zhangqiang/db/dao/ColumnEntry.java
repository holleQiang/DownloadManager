package com.zhangqiang.db.dao;

public class ColumnEntry {

    private String name;
    private String type;
    private boolean primaryKey;
    private boolean autoIncrement;
    private boolean index;
    private boolean unique;

    public String getName() {
        return name;
    }

    public ColumnEntry setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ColumnEntry setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public ColumnEntry setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public ColumnEntry setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    public boolean isIndex() {
        return index;
    }

    public ColumnEntry setIndex(boolean index) {
        this.index = index;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public ColumnEntry setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }
}
