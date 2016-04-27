package com.energy.autoscale.scale;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ServerScale {

	public boolean addServer() {

		File file = null;
		FileWriter writer = null;
		BufferedReader reader = null;
		boolean restart = false;

		try {

			String sCurrentLine;
			String newContent = "";
			file = new File("/etc/haproxy/haproxy.cfg");
			reader = new BufferedReader(new FileReader(file));
			int count = 0;

			while ((sCurrentLine = reader.readLine()) != null) {

				if (sCurrentLine.contains("#server app-server")) {

					if (count == 0) {
						count = 1;
						System.out.println("A new server has been added!");
						String modifiedLine = sCurrentLine.replace("#server app-server", "server app-server");
						newContent = newContent + modifiedLine + "\r\n";
						restart = true;
						continue;
					}
				}

				newContent = newContent + sCurrentLine + "\r\n";
			}
			reader.close();

			if (restart == true) {
				writer = new FileWriter("/etc/haproxy/haproxy.cfg");
				writer.write(newContent);
				writer.close();

				ServerScale scale = new ServerScale();
				scale.restartHaproxy();
			} else
				System.out.println("Need to scale up but no more additional server present!");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return restart;
	}

	public boolean removeServer() {

		File file = null;
		FileWriter writer = null;
		BufferedReader reader = null;
		boolean restart = false;

		try {

			String sCurrentLine;
			String newContent = "";
			int count = 1;
			boolean flag = true;
			file = new File("/etc/haproxy/haproxy.cfg");
			reader = new BufferedReader(new FileReader(file));

			while ((sCurrentLine = reader.readLine()) != null) {

				if (sCurrentLine.contains("server app-server") && !sCurrentLine.contains("#server app-server")
						&& flag == true) {
					if (count == 1) {
						count = 0;
						newContent = newContent + sCurrentLine + "\r\n";
					} else {
						System.out.println("An existing server has been removed!");
						String modifiedLine = sCurrentLine.replace("server app-server", "#server app-server");
						newContent = newContent + modifiedLine + "\r\n";
						flag = false;
						restart = true;
					}
					continue;
				}
				newContent = newContent + sCurrentLine + "\r\n";
			}
			reader.close();

			if (restart == true) {
				writer = new FileWriter("/etc/haproxy/haproxy.cfg");
				writer.write(newContent);
				writer.close();

				ServerScale scale = new ServerScale();
				scale.restartHaproxy();
			} else
				System.out.println("Need to scale down but no more additional server present!");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return restart;
	}

	public void restartHaproxy() {

		try {
			ProcessBuilder pb = new ProcessBuilder("/home/restart.sh");
			Process p = pb.start(); // Start the process.
			p.waitFor(); // Wait for the process to finish.
			System.out.println("Script executed successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
