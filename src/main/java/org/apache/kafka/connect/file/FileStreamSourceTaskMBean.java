package org.apache.kafka.connect.file;

public interface FileStreamSourceTaskMBean {
    public String getFilename();
    public long getStreamOffset();
}
