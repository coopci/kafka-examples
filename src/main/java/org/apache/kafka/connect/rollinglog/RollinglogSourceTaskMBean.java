package org.apache.kafka.connect.rollinglog;

public interface RollinglogSourceTaskMBean {
    public String getFilename();
    public long getStreamOffset();
}
