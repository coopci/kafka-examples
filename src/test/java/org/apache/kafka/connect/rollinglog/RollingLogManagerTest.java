package org.apache.kafka.connect.rollinglog;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RollingLogManagerTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void testFindNext(){


        File temproot= folder.getRoot();
        System.out.println("temproot: " + temproot.getAbsolutePath());


        RollingLogManager mgr = new RollingLogManager();

        LinkedList<String> filenames = new LinkedList<String>();

        filenames.add("tracing-log-134.1.log");
        filenames.add("tracing-log-1234.log");
        String lastFile = "tracing-log-234.log";

        // normal case
        String found = mgr.findNext(filenames, lastFile);
        assertEquals("tracing-log-1234.log", found);


        // lastFile not exists
        String found1 = mgr.findNext(filenames, null);
        assertEquals("tracing-log-134.1.log", found1);

        String found2 = mgr.findNext(filenames, "");
        assertEquals("tracing-log-134.1.log", found2);

        String found3 = mgr.findNext(filenames, "ad");
        assertEquals("tracing-log-134.1.log", found3);


        // lastFile is greatest
        String found4 = mgr.findNext(filenames, "tracing-log-1234.log");
        assertNull(found4);
    }

    @Test
    public void testFindNext2(){


        File temproot= folder.getRoot();
        System.out.println("temproot: " + temproot.getAbsolutePath());

    }
}
