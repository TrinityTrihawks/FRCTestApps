package com.frc.team4215;

public class UltrasonicConsole {

	public static void main(String[] args) {

		// UltrasonicReader reader = UltrasonicReader.Create("/dev/ttyUSB0");
		UltrasonicReader reader = UltrasonicReader.Create("COM3");

		if (reader == null) {
			System.out.println("No reader found!");
			return;
		}

		while (true) {
			try {
				System.out.println("dist: " + reader.getDistance());
				Thread.sleep(250);
			} catch (InterruptedException e) {
				System.out.println("Exception: " + e.getMessage());
			}
		}
	}
}
