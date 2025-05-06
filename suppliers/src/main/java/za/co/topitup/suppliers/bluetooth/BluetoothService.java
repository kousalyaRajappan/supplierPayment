
package za.co.topitup.suppliers.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothService";
    private static final boolean DEBUG = true;

    // Name for the SDP record when creating server socket
    private static final String NAME = "ZJPrinter";
    //UUID must be this
    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private final InputStream mmInStream= null;;
    private final OutputStream mmOutStream= null;;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static BluetoothSocket bluetoothSocket ;
    public static String ErrorMessage = "No_Error_Message";
    public static int statusByteR;

    /**
     * Constructor. Prepares a new BTPrinter session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }


    /**
     * Set the current state of the connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (DEBUG) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Main_Activity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (DEBUG) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (DEBUG) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */


    @SuppressLint("MissingPermission")
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "✔ CONNECTED: " + device.getName() + " - Initializing ConnectedThread...");

        if (socket == null) {
            Log.e(TAG, "❌ ERROR: BluetoothSocket is NULL!");
            return;
        }

        // Cancel any running threads
        if (mConnectThread != null) { mConnectThread.cancel(); mConnectThread = null; }
        if (mConnectedThread != null) {
            Log.w(TAG, "⚠ WARNING: Existing ConnectedThread found, canceling...");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        bluetoothSocket = socket;

        // Initialize and start new thread
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        Log.d(TAG, "✔ ConnectedThread started successfully.");
        setState(STATE_CONNECTED);
    }
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (DEBUG) Log.d(TAG, "stop");
        setState(STATE_NONE);
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    public boolean checkPrinterStatusBeforePrinting() {
        if (bluetoothSocket == null) {
            Log.e(TAG, "❌ Printer is not connected.");
            return false;
        }

        try {
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            InputStream inputStream = bluetoothSocket.getInputStream();

            // Send printer status command
            byte[] statusCommand = new byte[]{0x10, 0x04, 0x04};
            outputStream.write(statusCommand);
            outputStream.flush();
            Log.d(TAG, "✅ Status command sent");

            // Wait for response
            long startTime = System.currentTimeMillis();
            while (inputStream.available() == 0) {
                if (System.currentTimeMillis() - startTime > 2000) {
                    Log.e(TAG, "⚠ No response from printer (Timeout)");
                    if(statusByteR == 18){
                        return true;
                    }else if (statusByteR == 114) {
                        Log.w(TAG, "⚠ Printer out of paper (statusByteR == 114)");
                        return false;
                    }else {
                        return false;

                    }
                }
                Thread.sleep(50);
            }

            // Read printer status
            byte[] buffer = new byte[4];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead > 0) {
                Log.d(TAG, "📄 Printer Status Bytes: " + Arrays.toString(buffer));

                boolean hasPaper = interpretStatus(buffer);
                if (hasPaper) {
                    return true;
                }
            }
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "❌ Error communicating with printer", e);
        }

        // 🔴 Printer might have closed the connection. Reconnect before printing.
        return reconnectToPrinter();
    }
    public boolean reconnectToPrinter() {
        if (bluetoothSocket != null) {
            try {
                Log.d(TAG, "🔄 Closing existing Bluetooth socket...");
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException e) {
                Log.e(TAG, "❌ Failed to close Bluetooth socket", e);
            }
        }

        if (mAdapter == null || !mAdapter.isEnabled()) {
            Log.e(TAG, "❌ Bluetooth is disabled.");
            return false;
        }

        // Get the last connected device
        BluetoothDevice lastDevice = null;
        if (mConnectedThread != null && bluetoothSocket != null) {
            lastDevice = bluetoothSocket.getRemoteDevice();
        }

        if (lastDevice == null) {
            Log.e(TAG, "❌ No previously connected device found.");
            return false;
        }

        Log.d(TAG, "🔄 Reconnecting to printer: " + lastDevice.getName());

        // Attempt to reconnect
        connect(lastDevice);

        try {
            Thread.sleep(2000); // Give some time for reconnection
        } catch (InterruptedException e) {
            Log.e(TAG, "⚠ Reconnection delay interrupted", e);
        }

        if (mState == STATE_CONNECTED) {
            Log.d(TAG, "✅ Reconnection successful.");
            return true;
        } else {
            Log.e(TAG, "❌ Reconnection failed.");
            return false;
        }
    }



    private  boolean interpretStatus(byte[] status) {
        if (status.length < 1) {
            Log.e(TAG, "❌ Invalid printer status response");
            return false;
        }

        int statusByte = status[0];

        Log.d(TAG, "📝 Raw Printer Status Byte.........: " + statusByte);

        // Check bit 5 (0x20) for "out of paper" status
        if (statusByte == 18) {
            Log.w(TAG, "⚠ Printer is ready!");
            return true;
        }
        if ((statusByte & 0x20) != 0) {
            Log.w(TAG, "⚠ Printer is out of paper!");
            return false;
        }

        if (statusByte == 0x72) {  // 114 in decimal
            Log.w(TAG, "⚠ Printer is out of paper!");
            // Instead of closing, keep the connection alive and notify the UI
            mHandler.postDelayed(() -> requestPrinterStatus(), 5000);
            return false;
        }
        if (statusByte == 114) {  // Out of paper
            Log.w(TAG, "⚠ Printer is out of paper!");
            // DO NOT close the socket or reset Bluetooth service
            return false;
        }
        // Some printers use bit 3 (0x08) to indicate paper issues
        if ((statusByte & 0x08) != 0) {
            Log.w(TAG, "⚠ Paper is low or missing!");
            return false;
        }

        Log.d(TAG, "✅ Printer is ready with paper.");
        return true;
    }
    private void requestPrinterStatus() {
        Log.d(TAG, "🔄 Requesting printer status...");
        write(new byte[]{0x10, 0x04, 0x04}); // Resend the status command
    }
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Main_Activity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Main_Activity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }



    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        private boolean running = true;

        public AcceptThread() {
            try {
                mmServerSocket = mAdapter.listenUsingRfcommWithServiceRecord("MyBluetoothApp", MY_UUID);
                Log.d(TAG, "Server socket created successfully");
            } catch (IOException e) {
                Log.e(TAG, "Server socket listen() failed", e);
                mmServerSocket = null;
            }
        }

        @Override
        public void run() {
            Log.d(TAG, "AcceptThread started... Waiting for connection");

            if (mmServerSocket == null) {
                Log.e(TAG, "Server socket is null, exiting AcceptThread.");
                return;
            }

            BluetoothSocket socket = null;

            while (running && mState != STATE_CONNECTED) {
                try {
                    Log.d(TAG, "Listening for connections...");

                    // Use timeout to prevent indefinite blocking
                    socket = mmServerSocket.accept();  // Blocking call

                    if (socket != null) {
                        synchronized (BluetoothService.this) {
                            Log.d(TAG, "Connection accepted from " + socket.getRemoteDevice().getName());

                            if (mState == STATE_LISTEN || mState == STATE_CONNECTING) {
                                Log.d(TAG, "Starting ConnectedThread...");
                                connected(socket, socket.getRemoteDevice());
                            } else {
                                Log.w(TAG, "Unwanted connection, closing socket.");
                                socket.close();
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread failed or timeout", e);
                    if (running) {
                        Log.d(TAG, "Restarting AcceptThread...");
                        BluetoothService.this.start();
                    }
                    break;
                }
            }

            Log.d(TAG, "AcceptThread exiting...");
        }

        public void cancel() {
            running = false;
            Log.d(TAG, "Canceling AcceptThread...");
            try {
                if (mmServerSocket != null) {
                    mmServerSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "close() of server socket failed", e);
            }
        }
    }



    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private volatile boolean running = true; // Flag to control thread

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "Creating ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error getting streams", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            Log.i(TAG, "ConnectedThread started");
            byte[] buffer = new byte[256];
            int bytes;

            while (running) {
                try {
                    if (mmSocket == null || !mmSocket.isConnected()) {
                        Log.w(TAG, "⚠ Bluetooth socket is not connected. Exiting thread...");
                        break;
                    }

                    int availableBytes = 0;
                    try {
                        availableBytes = mmInStream.available();
                    } catch (IOException e) {
                        Log.e(TAG, "❌ InputStream is closed or not available", e);
                        connectionLost();
                        restartServiceIfNeeded();
                        break;
                    }

                    if (availableBytes  > 0) {
                        bytes = mmInStream.read(buffer);
                        if (bytes > 0) {
                            byte[] readData = Arrays.copyOf(buffer, bytes);
                            statusByteR = readData[0] & 0xFF;

                            Log.d(TAG, "📝 Raw Printer Status Byte.......22222222....: " + statusByteR);

                            if (statusByteR == 114) {  // 114 means out of paper
                                Log.w(TAG, "⚠ Printer is out of paper!");
                                continue;  // DO NOT close the socket
                            }
                            if (statusByteR == 18) {  // 114 means out of paper
                                Log.w(TAG, "⚠ Printer have paper!");
                                continue;  // DO NOT close the socket
                            }
                            Log.d(TAG, "Received: " + new String(readData, StandardCharsets.UTF_8));
                            mHandler.obtainMessage(Main_Activity.MESSAGE_READ, bytes, -1, readData).sendToTarget();
                        }
                    } else {
                        Thread.sleep(10); // Reduce CPU usage while waiting
                    }
                } catch (IOException e) {
                    Log.e(TAG, "InputStream disconnected", e);
                    connectionLost();
                    restartServiceIfNeeded();
                    break;
                } catch (InterruptedException e) {
                    Log.e(TAG, "Thread interrupted", e);
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                Log.d(TAG, "Printer Response: " );

                mmOutStream.write(buffer);
                mmOutStream.flush();

                // Wait for printer response (if needed)
//                Thread.sleep(30);
                if (mmInStream.available() > 0) {
                    byte[] response = new byte[256];
                    int responseBytes = mmInStream.read(response);
                    Log.d(TAG, "Printer Response: " + new String(response, 0, responseBytes, StandardCharsets.UTF_8));
                }

                mHandler.obtainMessage(Main_Activity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch ( Exception e) {
                Log.e(TAG, "Error during write", e);
            }
        }

        public void cancel() {
            running = false; // Stop the loop
            try {
                mmSocket.close();
                Log.d(TAG, "Socket closed");
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }

        private void connectionLost() {
            Log.e(TAG, "Connection lost");
            mHandler.obtainMessage(Main_Activity.MESSAGE_TOAST, -1, -1, "Device disconnected").sendToTarget();
        }

        private void restartServiceIfNeeded() {
            if (mState != STATE_NONE) {
                Log.i(TAG, "Restarting Bluetooth service");
                BluetoothService.this.start();
            }
        }
    }



}