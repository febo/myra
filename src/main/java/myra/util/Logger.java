/*
 * Logger.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2015 Fernando Esteban Barril Otero
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package myra.util;

import java.io.PrintStream;

/**
 * The <code>Logger</code> class provides an utility method to log messages.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Logger {
    /**
     * The logger destination.
     */
    private static PrintStream destination = System.out;

    /**
     * Sets the logger destination stream.
     * 
     * @param destination
     *            the destination stream to set.
     */
    public static void setDestination(PrintStream destination) {
        Logger.destination = destination;
    }

    /**
     * Logs a message.
     * 
     * @param message
     *            the message format string.
     * @param args
     *            the message arguments.
     */
    public static void log(String message, Object... args) {
        destination.print(String.format(message, args));
    }

    /**
     * Flushes the log streams.
     */
    public static void flush() {
        destination.flush();
    }

    /**
     * Closes the log streams.
     */
    public static void close() {
        destination.close();
    }
}