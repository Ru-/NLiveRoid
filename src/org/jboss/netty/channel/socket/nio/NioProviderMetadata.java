/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.channel.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.netty.util.internal.SystemPropertyUtil;

import android.util.Log;

/**
 * Provides information which is specific to a NIO service provider
 * implementation.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2162 $, $Date: 2010-02-18 11:23:51 +0900 (Thu, 18 Feb 2010) $
 *
 */
class NioProviderMetadata {
    private static final String CONSTRAINT_LEVEL_PROPERTY =
        "org.jboss.netty.channel.socket.nio.constraintLevel";

    private static final String OLD_CONSTRAINT_LEVEL_PROPERTY =
        "java.nio.channels.spi.constraintLevel";

    /**
     * 0 - no need to wake up to get / set interestOps (most cases)
     * 1 - no need to wake up to get interestOps, but need to wake up to set.
     * 2 - need to wake up to get / set interestOps    (old providers)
     */
    static final int CONSTRAINT_LEVEL;

    static {
        int constraintLevel = -1;

        // Use the system property if possible.
        constraintLevel = SystemPropertyUtil.get(CONSTRAINT_LEVEL_PROPERTY, -1);
        if (constraintLevel < 0 || constraintLevel > 2) {
            // Try the old property.
            constraintLevel = SystemPropertyUtil.get(OLD_CONSTRAINT_LEVEL_PROPERTY, -1);
            if (constraintLevel < 0 || constraintLevel > 2) {
                constraintLevel = -1;
            } else {
            	Log.d("NioProviderMetadata",
                        "System property '" +
                        OLD_CONSTRAINT_LEVEL_PROPERTY +
                        "' has been deprecated.  Use '" +
                        CONSTRAINT_LEVEL_PROPERTY + "' instead.");
            }
        }

        if (constraintLevel >= 0) {
        	Log.d("NioProviderMetadata",
                    "Setting the NIO constraint level to: " + constraintLevel);
        }

        if (constraintLevel < 0) {
            constraintLevel = detectConstraintLevelFromSystemProperties();

            if (constraintLevel < 0) {
            	//Androidじゃない(エミュレータでなる?)
                constraintLevel = 2;
                Log.d("NioProviderMetadata",
                        "Couldn't determine the NIO constraint level from " +
                        "the system properties; using the safest level (2)");
            } else if (constraintLevel != 0) {
            	//Androidじゃない
            	Log.d("NioProviderMetadata",
                        "Using the autodetected NIO constraint level: " +
                        constraintLevel +
                        " (Use better NIO provider for better performance)");
            } else {
            	//Android(0)
            	Log.d("NioProviderMetadata",
                        "Using the autodetected NIO constraint level(0 = Android): " +
                        constraintLevel);
            }
        }

        CONSTRAINT_LEVEL = constraintLevel;

        if (CONSTRAINT_LEVEL < 0 || CONSTRAINT_LEVEL > 2) {
            try {
				throw new Exception(
				        "NioProviderMetadata Unexpected NIO constraint level: " +
				        CONSTRAINT_LEVEL + ", please report this error.");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

    private static int detectConstraintLevelFromSystemProperties() {
        String version = SystemPropertyUtil.get("java.specification.version");
        String vminfo = SystemPropertyUtil.get("java.vm.info", "");
        String os = SystemPropertyUtil.get("os.name");
        String vendor = SystemPropertyUtil.get("java.vm.vendor");
        String provider;
        try {
            provider = SelectorProvider.provider().getClass().getName();
        } catch (Exception e) {
            // Perhaps security exception.
            provider = null;
        }

        if (version == null || os == null || vendor == null || provider == null) {
            return -1;
        }

        os = os.toLowerCase();
        vendor = vendor.toLowerCase();
//
//        Log.d("0VM",version);
//        Log.d("1VM",vminfo);//何故か出力しない
//        Log.d("2VM",os);
//        Log.d("3VM",vendor);
//        Log.d("4VM",provider);

        // dalvik VM
        if (vendor.indexOf("android") >= 0) {//Androidを追加(他消した)
            // Linux
            if (os.indexOf("linux") >= 0) {
                if (provider.equals("org.apache.harmony.nio.internal.SelectorProviderImpl")) {
                    return 0;
                }

            // android
            }
        }

        // Others (untested)
        return -1;
    }

    private static final class ConstraintLevelAutodetector {

        ConstraintLevelAutodetector() {
            super();
        }

        int autodetect() {
            final int constraintLevel;
            ExecutorService executor = Executors.newCachedThreadPool();
            boolean success;
            long startTime;
            int interestOps;

            ServerSocketChannel ch = null;
            SelectorLoop loop = null;

            try {
                // Open a channel.
                ch = ServerSocketChannel.open();

                // Configure the channel
                try {
                    ch.socket().bind(new InetSocketAddress(0));
                    ch.configureBlocking(false);
                } catch (Throwable e) {
                	Log.d("NioProviderMetadata","Failed to configure a temporary socket.", e);
                    return -1;
                }

                // Prepare the selector loop.
                try {
                    loop = new SelectorLoop();
                } catch (Throwable e) {
                	Log.d("NioProviderMetadata","Failed to open a temporary selector.", e);
                    return -1;
                }

                // Register the channel
                try {
                    ch.register(loop.selector, 0);
                } catch (Throwable e) {
                	Log.d("NioProviderMetadata","Failed to register a temporary selector.", e);
                    return -1;
                }

                SelectionKey key = ch.keyFor(loop.selector);

                // Start the selector loop.
                executor.execute(loop);

                // Level 0
                success = true;
                for (int i = 0; i < 10; i ++) {

                    // Increase the probability of calling interestOps
                    // while select() is running.
                    do {
                        while (!loop.selecting) {
                            Thread.yield();
                        }

                        // Wait a little bit more.
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    } while (!loop.selecting);

                    startTime = System.nanoTime();
                    key.interestOps(key.interestOps() | SelectionKey.OP_ACCEPT);
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_ACCEPT);

                    if (System.nanoTime() - startTime >= 500000000L) {
                        success = false;
                        break;
                    }
                }

                if (success) {
                    constraintLevel = 0;
                } else {
                    // Level 1
                    success = true;
                    for (int i = 0; i < 10; i ++) {

                        // Increase the probability of calling interestOps
                        // while select() is running.
                        do {
                            while (!loop.selecting) {
                                Thread.yield();
                            }

                            // Wait a little bit more.
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                // Ignore
                            }
                        } while (!loop.selecting);

                        startTime = System.nanoTime();
                        interestOps = key.interestOps();
                        synchronized (loop) {
                            loop.selector.wakeup();
                            key.interestOps(interestOps | SelectionKey.OP_ACCEPT);
                            key.interestOps(interestOps & ~SelectionKey.OP_ACCEPT);
                        }

                        if (System.nanoTime() - startTime >= 500000000L) {
                            success = false;
                            break;
                        }
                    }
                    if (success) {
                        constraintLevel = 1;
                    } else {
                        constraintLevel = 2;
                    }
                }
            } catch (Throwable e) {
                return -1;
            } finally {
                if (ch != null) {
                    try {
                        ch.close();
                    } catch (Throwable e) {
                    	Log.d("NioProviderMetadata","Failed to close a temporary socket.", e);
                    }
                }

                if (loop != null) {
                    loop.done = true;
                    try {
                        executor.shutdownNow();
                    } catch (NullPointerException ex) {
                        // Some JDK throws NPE here, but shouldn't.
                    }

                    try {
                        for (;;) {
                            loop.selector.wakeup();
                            try {
                                if (executor.awaitTermination(1, TimeUnit.SECONDS)) {
                                    break;
                                }
                            } catch (InterruptedException e) {
                                // Ignore
                            }
                        }
                    } catch (Throwable e) {
                        // Perhaps security exception.
                    }

                    try {
                        loop.selector.close();
                    } catch (Throwable e) {
                    	Log.d("NioProviderMetadata","Failed to close a temporary selector.", e);
                    }
                }
            }

            return constraintLevel;
        }
    }

    private static final class SelectorLoop implements Runnable {
        final Selector selector;
        volatile boolean done;
        volatile boolean selecting; // Just an approximation

        SelectorLoop() throws IOException {
            selector = Selector.open();
        }

        public void run() {
            while (!done) {
                synchronized (this) {
                    // Guard
                }
                try {
                    selecting = true;
                    try {
                        selector.select(1000);
                    } finally {
                        selecting = false;
                    }

                    Set<SelectionKey> keys = selector.selectedKeys();
                    for (SelectionKey k: keys) {
                        k.interestOps(0);
                    }
                    keys.clear();
                } catch (IOException e) {
                	Log.d("NioProviderMetadata","Failed to wait for a temporary selector.", e);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        for (Entry<Object, Object> e: System.getProperties().entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
        System.out.println();
        System.out.println("Hard-coded Constraint Level: " + CONSTRAINT_LEVEL);
        System.out.println(
                "Auto-detected Constraint Level: " +
                new ConstraintLevelAutodetector().autodetect());
    }

    private NioProviderMetadata() {
        // Unused
    }
}
