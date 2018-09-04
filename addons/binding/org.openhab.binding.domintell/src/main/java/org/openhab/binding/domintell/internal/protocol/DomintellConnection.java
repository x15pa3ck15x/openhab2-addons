/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol;

import org.openhab.binding.domintell.internal.config.BridgeConfig;
import org.openhab.binding.domintell.internal.protocol.exception.ModuleException;
import org.openhab.binding.domintell.internal.protocol.model.AbstractBaseModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

/**
 * {@link DomintellConnection} class implements the communication protocol provided by the Domintell DETH02 ethernet
 * module.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellConnection {
    /**
     * Class logger.
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellConnection.class);

    /**
     * Module cache
     */
    private final HashMap<String, AbstractBaseModule> moduleCache = new HashMap<>();

    /**
     * Outgoing message queue
     */
    private LinkedBlockingQueue<String> sendQueue = new LinkedBlockingQueue<>();

    /**
     * Domintell system date
     */
    private Date domintellSysdate;


    /**
     * Bridge network configuration
     */
    private BridgeConfig config;

    /**
     * Login state listener
     */
    private final StateListener stateListener;

    /**
     * Configuration event listener
     */
    private final ConfigurationEventListener configEventListener;

    /**
     * Connection state
     */
    private StateListener.State currentState;

    /**
     * Connection thread
     */
    private ConnectionMonitoringThread connectionThread;

    /**
     * Constructor.
     *
     * @param config Configuration.
     * @param configEventListener Configuration event listener.
     */
    public DomintellConnection(BridgeConfig config, StateListener stateListener, ConfigurationEventListener configEventListener) {
        this.config = config;
        this.stateListener = stateListener;
        this.configEventListener = configEventListener;
    }

    private void updateState(StateListener.State state, String msg) {
        this.currentState = state;
        stateListener.stateChanged(state, msg);
    }

    public void startGateway() {
        if (config.isValid()) {
            connectionThread = new ConnectionMonitoringThread();
            connectionThread.start();
        } else {
            updateState(StateListener.State.ERROR, "Invalid parameters.");
        }
    }

    public void stopGateway() {
        connectionThread.interrupt();
        connectionThread = null;
        logger.info("Stopping Domintell connection.");
    }

    public void sendCommand(String command) {
        try {
            if (isOnline()) {
                sendQueue.put(command);
            } else {
                logger.debug("Not sending command as the connection is not online: {}", command);
            }
        } catch (InterruptedException e) {
            logger.debug("Domintell command not sent: {}", command);
        }
    }

    public Date getDomintellSysdate() {
        return domintellSysdate;
    }

    /**
     * Retrieves Domintell module from the cache. The module is created if missing.
     *
     * @param address Domintell address
     * @return Requested module
     * @throws ModuleException in case the address is invalid.
     */
    public AbstractBaseModule getDomintellModule(ModuleType moduleType, ModuleAddress address)
            throws ModuleException {
        Class<? extends AbstractBaseModule> moduleClass = moduleType.getClazz();
        String key = moduleType.toString() + address;
        AbstractBaseModule module = moduleCache.get(key);
        if (module == null) {
            //missing the device - creating
            try {
                Constructor<? extends AbstractBaseModule> constructor = moduleClass.getConstructor(DomintellConnection.class, ModuleType.class, ModuleAddress.class);
                module = constructor.newInstance(this, moduleType, address);
                configEventListener.handleNewModule(module);
            } catch (Exception e) {
                throw new ModuleException("Unable to instantiate module", address, e);
            }
        }
        if (!(moduleClass.isInstance(module))) {
            throw new ModuleException("Invalid module type found at given address", address, null);
        }
        moduleCache.put(key, module);
        return module;
    }

    /**
     * Connection status check.
     *
     * @return True if online.
     */
    public boolean isOnline() {
        return currentState == StateListener.State.ONLINE;
    }

    /**
     * Send APPINFO message to discover all the modules in the installation.
     */
    public void discover() {
        sendCommand(MessageReceiverThread.MSG_APPINFO);
    }

    public void setConfig(BridgeConfig config) {
        this.config = config;
    }

    /**
     * Connection monitoring thread
     */
    private class ConnectionMonitoringThread extends Thread {
        /**
         * Timeout on the reader thread i.e. we must receive a packet within that delay since the last read. There
         * should be a clock update every minute from the Domintell master, so using 61s for this should be safe.
         */
        static final int SOCKET_READ_TIMEOUT = 61; //s

        /**
         * Read thread checking interval
         */
        private static final int CONNECTION_CHECK_TIMEOUT = 60; //s

        /**
         * Time between retries
         */
        private static final int RECONNECT_TIMEOUT = 5; //s

        /**
         * Thread for receiving messages from Domintell system (message producer for readQueue).
         */
        private MessageReceiverThread readerThread;

        /**
         * Thread for sending messages to Domintell system (message consumer for sendQueue).
         */
        private MessageSenderThread writerThread;

        /**
         * Socket
         */
        private DatagramSocket socket;

        /**
         * Start the socket and the two read/write threads.
         */
        private void startBackgroundThreads() {
            try {
                updateState(StateListener.State.INITIALIZING, null);
                socket = new DatagramSocket();
                socket.setSoTimeout(SOCKET_READ_TIMEOUT * 1000);

                logger.trace("Connecting to Domintell system: {}", config.toString());
                socket.connect(config.getInternetAddress(), config.getPort());
                logger.trace("Socket connected");

                readerThread = new MessageReceiverThread(socket);
                readerThread.start();

                writerThread = new MessageSenderThread(socket);
                writerThread.start();

                writerThread.login();
                updateState(StateListener.State.STARTING_SESSION, null);
            } catch (SocketException | UnknownHostException e) {
                logger.debug("Configuration error", e);
            }
        }

        /**
         * Stop reader/writer threads and close the socket.
         */
        private void stopBackgroundThreads() {
            updateState(StateListener.State.STOPPING, null);

            //stop the writer
            if (writerThread != null && writerThread.isAlive()) {
                writerThread.logout();
                writerThread.interrupt();
            }
            writerThread = null;

            //stop the reader
            if (readerThread != null && readerThread.isAlive()) {
                readerThread.interrupt();
            }
            readerThread = null;

            //close the socket
            if (socket != null) {
                if (socket.isConnected()) {
                    socket.disconnect();
                }
                if (!socket.isClosed()) {
                    socket.close();
                }
                socket = null;
            }

            updateState(StateListener.State.OFFLINE, null);
        }

        @Override
        public void run() {
            if (socket == null) {
                try {
                    while (!isInterrupted()) {
                        //start the communication threads and try to log in
                        startBackgroundThreads();

                        //wait for the read thread to die in case connection was dropped
                        while (readerThread != null) {
                            readerThread.join(CONNECTION_CHECK_TIMEOUT * 1000);
                            if (!readerThread.isAlive()) {
                                if (currentState == StateListener.State.ERROR) {
                                    logger.debug("Connection error. Clean and try to reconnect");
                                } else {
                                    logger.info("Receiver thread is dead. Clean and try to reconnect");
                                }
                                break;
                            } else {
                                //read thread is active - ping the other side
                                writerThread.hello();
                            }
                        }

                        if (currentState != StateListener.State.FATAL) {
                            //read thread is dead - reconnect after timeout
                            logger.debug("Waiting for {}s to reconnect", RECONNECT_TIMEOUT);
                            Thread.sleep(RECONNECT_TIMEOUT);
                            stopBackgroundThreads();
                        } else {
                            //connection problem
                            logger.warn("Unable to connect to Domintell system. Please check the binding " +
                                    "configuration: {}", config.toString());
                            interrupt();
                        }
                    }
                } catch (InterruptedException e) {
                    logger.trace("Connection thread has been interrupted");
                } finally {
                    stopBackgroundThreads();
                }
                logger.debug("Connection thread stopped");
            }
        }
    }

    /**
     * Message reader thread.
     */
    private class MessageSenderThread extends Thread {
        /**
         * Sender logger
         */
        final Logger loggerSender = LoggerFactory.getLogger(logger.getName() + ".sender");

        //static Domintell commands
        private static final String CMD_HELLO = "HELLO";
        private static final String CMD_PING = "PING";
        private static final String CMD_LOGIN = "LOGIN";
        private static final String CMD_LOGOUT = "LOGOUT";

        /**
         * Delay between messages
         */
        private static final int MESSAGE_DELAY = 25;

        /**
         * Connected socket
         */
        private DatagramSocket socket;


        MessageSenderThread(DatagramSocket socket) {
            super();
            this.socket = socket;
        }

        @Override
        public void run() {
            logger.debug("Sender thread started");
            while (true) {
                try {
                    String cmd = sendQueue.take();
                    if (currentState == StateListener.State.ONLINE || CMD_LOGIN.equals(cmd)) {
                        //blocking read of the send queue
                        send(cmd);
                        // Domintell specifies 25ms delay between messages
                        Thread.sleep(MESSAGE_DELAY);
                    } else {
                        logger.debug("Skipping command as the connection is not online: {}", cmd);
                    }
                } catch (InterruptedException e) {
                    logger.trace("Sender thread interrupted");
                    break;
                }
            }
            logger.trace("Sender thread stopped");
        }

        void login() {
            send(CMD_LOGIN);
            ping();
        }

        private void logout() {
            send(CMD_LOGOUT);
        }

        void hello() {
            send(CMD_HELLO);
        }

        void ping() {
            send(CMD_PING);
        }

        private void send(String cmd) {
            loggerSender.trace("Sending message: >{}<", cmd);
            byte[] buf = cmd.getBytes();
            try {
                DatagramPacket p = new DatagramPacket(buf, buf.length, config.getInternetAddress(), config.getPort());
                socket.send(p);
            } catch (IOException e) {
                loggerSender.trace("Could not send message: >{}<", cmd);
            }
        }
    }

    private class MessageReceiverThread extends Thread {
        /**
         * Communication message encoding
         */
        private static final String CHARSET = "ISO-8859-1";

        /**
         * Receiver logger
         */
        final Logger loggerReceiver = LoggerFactory.getLogger(logger.getName() + ".receiver");

        //static response messages
        private static final String MSG_SESSION_OPENED = "INFO:Session opened";
        private static final String MSG_AUTH_FAILED = "INFO:Auth failed";
        private static final String MSG_ACCESS_DENIED = "INFO:Access denied";
        private static final String MSG_SESSION_TIMEOUT = "INFO:Session timeout";
        private static final String MSG_WORLD = "INFO:World";
        private static final String MSG_APPINFO = "APPINFO";
        private static final String MSG_SESSION_CLOSED = "INFO:Session closed";

        /**
         * SOcker reader buffer size
         */
        private static final int READ_BUFFER_SIZE = 256;

        /**
         * Connected socket
         */
        private DatagramSocket socket;

        /**
         * A pattern to match the date/time packet sent by the DETH02
         */
        private Pattern dateTimePattern;

        /**
         * Date formatter for parsing Domintell system date
         */
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");

        MessageReceiverThread(DatagramSocket socket) {
            super();
            this.socket = socket;
            this.dateTimePattern = Pattern.compile("\\d{1,2}:\\d{1,2} \\d{1,2}/\\d{1,2}/\\d{1,2}");
        }

        @Override
        public void run() {
            logger.debug("Receiver thread started");
            while (!isInterrupted()) {
                String msg = null;
                try {
                    byte[] buffer = new byte[READ_BUFFER_SIZE];
                    DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                    socket.receive(p);
                    msg = new String(p.getData(), 0, p.getLength(), CHARSET);

                    // Get rid of line ending
                    msg = msg.trim();

                    loggerReceiver.trace("Receiver thread got packet >{}<", msg);

                    if (msg.startsWith(MSG_SESSION_OPENED)) {
                        updateState(StateListener.State.ONLINE, null);
                    } else if (msg.startsWith(MSG_AUTH_FAILED)) {
                        updateState(StateListener.State.FATAL, "Authentication failed");
                        break;
                    } else if (msg.startsWith(MSG_ACCESS_DENIED)) {
                        updateState(StateListener.State.FATAL, "Access denied");
                        break;
                    } else if (msg.startsWith(MSG_SESSION_TIMEOUT) || msg.startsWith(MSG_SESSION_CLOSED)) {
                        //closing the reader
                        break;
                    } else if (msg.startsWith(MSG_WORLD)) {
                        updateState(StateListener.State.ONLINE, null);
                        loggerReceiver.trace("Domintell system replied to HELLO");
                    } else if (dateTimePattern.matcher(msg).matches()) {
                        loggerReceiver.trace("Domintell system reported date/time: {}", msg);
                        domintellSysdate = dateFormat.parse(msg);
                        updateState(StateListener.State.ONLINE, null);
                    } else if (msg.contains(MSG_APPINFO)) {
                        loggerReceiver.trace("APPINFO message: {}", msg);
                    } else {
                        processModuleStatusMessage(msg);
                    }
                } catch (SocketTimeoutException se) {
                    if (currentState == StateListener.State.ONLINE) {
                        updateState(StateListener.State.STALE, null);
                        loggerReceiver.trace("No messages received in the last {} seconds.", ConnectionMonitoringThread.SOCKET_READ_TIMEOUT);
                    } else {
                        loggerReceiver.trace("Still no messages received. Stopping the reader thread.");
                        updateState(StateListener.State.ERROR, "Cannot connect to to Domintell system.");
                        break;
                    }
                } catch (IOException e) {
                    logger.debug("Error receiving packet from Domintell system", e);
                    updateState(StateListener.State.ERROR, "I/O Exception");
                    break;
                } catch (ParseException e) {
                    logger.debug("Unable to parse Domintell system date: {}", msg);
                }
            }
            logger.debug("Receiver thread stopped");
        }

        private void processModuleStatusMessage(String packetText) {
            // First 3 chars in module type
            // Next 6 is address
            // Then 1 char is "type of data"

            logger.trace("Module status update: {}", packetText);
            String mtStr = packetText.substring(0, 3);
            try {
                ModuleType moduleType = ModuleType.valueOf(mtStr);
                String address = packetText.substring(3, 9);

                Class<? extends AbstractBaseModule> moduleClass = moduleType.getClazz();
                if (moduleClass != null) {
                    ModuleAddress moduleAddress;
                    moduleAddress = new ModuleAddress(address);
                    AbstractBaseModule module = getDomintellModule(moduleType, moduleAddress);
                    if (module != null) {
                        module.processUpdate(packetText.substring(9).trim());
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Unknown module type: {}", mtStr);
            } catch (ModuleException e) {
                logger.debug("Impossible to get module", e.getAddress());
            }
        }
    }
}
