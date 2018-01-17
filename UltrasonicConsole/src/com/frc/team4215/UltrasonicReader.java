package com.frc.team4215;

import java.io.InputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class UltrasonicReader implements Runnable, SerialPortEventListener {

	public static int MIN_DISTANCE = 300;
	public static int MAX_DISTANCE = 5000;

	private static int InstanceCounter = 0;
	private static int BufferByteSize = 6;

	// length of the data you hope to get
	private InputStream inputStream;
	private SerialPort serialPort;
	private String serialPortId;
	private Thread readThread;
	private String name;

	private int distance = UltrasonicReader.MIN_DISTANCE;

	public static UltrasonicReader Create(String portName) {

		String normalizedPortName = portName.toLowerCase();

		// Create iterator for port identifiers list
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		// Loop while next element
		while (portList.hasMoreElements()) {
			// Get next comm port identifier
			CommPortIdentifier commPort = (CommPortIdentifier) portList.nextElement();
			// Is it a serial port ?
			if (commPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				// does name match passed in name
				if (normalizedPortName.equals(commPort.getName().toLowerCase())) {
					try {
						// Create reader which is used to pull data from
						return new UltrasonicReader(commPort, portName);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}
		}

		return null;
	}

	private UltrasonicReader(CommPortIdentifier commPort, String name) throws Exception {
		this.serialPortId = "UltrasonicReader_" + InstanceCounter++;
		this.name = name;

		SerialPort serialPort = (SerialPort) commPort.open(this.serialPortId, 2000);

		// configure
		serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		// defines port
		this.serialPort = serialPort;
		// defines inputStream from buffer
		this.inputStream = this.serialPort.getInputStream();
		// listens for data
		this.serialPort.addEventListener(this);
		// set listener thread
		this.readThread = new Thread(this);
		// sets the port to constantly read data
		this.serialPort.notifyOnDataAvailable(true);
	}

	public void Listen() {
		this.serialPort.notifyOnDataAvailable(true);
		// System.out.println("Listening?");
	}

	public void Stop() {
		this.serialPort.notifyOnDataAvailable(false);
		// System.out.println("Stop Listening?");
		// this.readThread.interrupt();
	}

	public void run() {
		try {
			// this.readThread.sleep(20000);
			// Thread.sleep(20000);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		// Redundant cases for different data types
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
			// If buffer is empty stop method
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		// Reads data available into buffer
		case SerialPortEvent.DATA_AVAILABLE:
			// sets index into buffer to zero
			int index = 0;
			// num of bytes read - not total
			int read = 0;
			// list of bytes which has bounds of six
			byte[] buffer = new byte[BufferByteSize];

			try {
				while (index < BufferByteSize
						&& ((read = inputStream.read(buffer, index, BufferByteSize - index)) != -1)) {
					// System.out.printf("read: %d, capacity: %d\n", read, capacity);
					index += read;
				}

				// Searches for '82' which starts each datastream
				for (int i = 0; i < index; i++) {
					if (buffer[i] == 82 && index - i - 1 >= 4) {
						this.distance = (1000 * (buffer[i + 1] - 48)) + (100 * (buffer[i + 2] - 48))
								+ (10 * (buffer[i + 3] - 48)) + (buffer[i + 4] - 48);
						break;
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			}
			break;
		}
	}

	public int getDistance() {
		return this.distance;
	}

	public String getSerialPortId() {
		return this.serialPortId;
	}

	public String getName() {
		return this.name;
	}
}
