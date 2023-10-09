package com.zhangqiang.downloadmanager.plugin.http.range;

public class RangePart {

    private String unit;
    private long start;
    private long end;
    private long total;

    public String getUnit() {
        return unit;
    }

    public RangePart setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public long getStart() {
        return start;
    }

    public RangePart setStart(long start) {
        this.start = start;
        return this;
    }

    public long getEnd() {
        return end;
    }

    public RangePart setEnd(long end) {
        this.end = end;
        return this;
    }

    public long getTotal() {
        return total;
    }

    public RangePart setTotal(long total) {
        this.total = total;
        return this;
    }

    @Override
    public String toString() {
        return "RangePart{" +
                "unit='" + unit + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", total=" + total +
                '}';
    }
}
