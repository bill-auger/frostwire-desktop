/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;
import org.h2.message.DbException;
import org.h2.message.TraceSystem;
import org.h2.store.fs.FileUtils;

/**
 * Sorted properties file.
 * This implementation requires that store() internally calls keys().
 */
public class SortedProperties extends Properties {

    private static final long serialVersionUID = 1L;

    public synchronized Enumeration<Object> keys() {
        Vector<String> v = new Vector<String>();
        for (Object o : keySet()) {
            v.add(o.toString());
        }
        Collections.sort(v);
        return new Vector<Object>(v).elements();
    }

    /**
     * Get a boolean property value from a properties object.
     *
     * @param prop the properties object
     * @param key the key
     * @param def the default value
     * @return the value if set, or the default value if not
     */
    public static boolean getBooleanProperty(Properties prop, String key, boolean def) {
        String value = prop.getProperty(key, "" + def);
        try {
            return Boolean.valueOf(value).booleanValue();
        } catch (Exception e) {
            TraceSystem.traceThrowable(e);
            return def;
        }
    }

    /**
     * Get an int property value from a properties object.
     *
     * @param prop the properties object
     * @param key the key
     * @param def the default value
     * @return the value if set, or the default value if not
     */
    public static int getIntProperty(Properties prop, String key, int def) {
        String value = prop.getProperty(key, "" + def);
        try {
            return Integer.decode(value);
        } catch (Exception e) {
            TraceSystem.traceThrowable(e);
            return def;
        }
    }

    /**
     * Load a properties object from a file.
     *
     * @param fileName the name of the properties file
     * @return the properties object
     */
    public static synchronized SortedProperties loadProperties(String fileName) throws IOException {
        SortedProperties prop = new SortedProperties();
        if (FileUtils.exists(fileName)) {
            InputStream in = null;
            try {
                in = FileUtils.newInputStream(fileName);
                prop.load(in);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        return prop;
    }

    /**
     * Store a properties file. The header and the date is not written.
     *
     * @param fileName the target file name
     */
    public synchronized void store(String fileName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        store(out, null);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        InputStreamReader reader = new InputStreamReader(in, "ISO8859-1");
        LineNumberReader r = new LineNumberReader(reader);
        Writer w;
        try {
            w = new OutputStreamWriter(FileUtils.newOutputStream(fileName, false));
        } catch (Exception e) {
            throw DbException.convertToIOException(e);
        }
        PrintWriter writer = new PrintWriter(new BufferedWriter(w));
        while (true) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (!line.startsWith("#")) {
                writer.print(line + "\n");
            }
        }
        writer.close();
    }

    /**
     * Convert the map to a list of line in the form key=value.
     *
     * @return the lines
     */
    public synchronized String toLines() {
        StringBuilder buff = new StringBuilder();
        for (Entry<Object, Object> e : new TreeMap<Object, Object>(this).entrySet()) {
            buff.append(e.getKey()).append('=').append(e.getValue()).append('\n');
        }
        return buff.toString();
    }

    /**
     * Convert a String to a map.
     *
     * @param s the string
     * @return the map
     */
    public static SortedProperties fromLines(String s) {
        SortedProperties p = new SortedProperties();
        for (String line : StringUtils.arraySplit(s, '\n', true)) {
            int idx = line.indexOf('=');
            if (idx > 0) {
                p.put(line.substring(0, idx), line.substring(idx + 1));
            }
        }
        return p;
    }

}
