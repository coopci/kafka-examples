package org.apache.kafka.connect.rollinglog;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RollingLogManager {



    /**
     * Find the next file by semantic order.
     * If lastFile == null or lastFile can not be found, return the first file name by semantic order.
     * If lastFile exists, return the `smallest` file name `greater than` lastFile by semantic order.
     *
     * @return null if no file matching the criterion exists.
     **/
    String findNext(String dirpath, String pattern, String lastFile) {

        File dir = new File(dirpath);
        String[] files = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        });


        String ret = this.findNext(Arrays.asList(files), lastFile);
        return ret;
    }


    String findNext(List<String> filenames, String lastFile) {

        if (filenames == null || filenames.size() == 0) {
            return null;
        }
        FilenameComparator comparator = new FilenameComparator();
        Collections.sort(filenames, comparator);

        if (lastFile == null || lastFile.length()==0) {
            return filenames.get(0);
        }

        for (String filename : filenames) {
            int res = comparator.compare(filename, lastFile);
            if (res > 0) { // Found one
                return filename;
            }
        }
        // All file name in filenames are `smaller` than lastFile.

        return null;
    }
}
