package org.apache.kafka.connect.file;

public interface FileStreamSourceTaskMonitorMBean {
    public String getFilename();
    public long getStreamOffset();
}
