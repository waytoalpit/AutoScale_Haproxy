package com.energy.autoscale.scale;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ServerScale {

	public void addServer() {

		File file = null;
		FileWriter writer = null;
		BufferedReader reader = null;
		try {

			String sCurrentLine;
			String newContent = "";
			file = new File("/etc/haproxy/haproxy.cfg");
			reader = new BufferedReader(new FileReader(file));
			int count = 0;
			boolean restart = false;

			while ((sCurrentLine = reader.readLine()) != null) {

				if (sCurrentLine.contains("#server app-server")) {

					if (count == 0) {
						count = 1;
						System.out.println("Adding a new server...");
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
				restart = false;
			}
			else
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
	}

	public void removeServer() {

		File file = null;
		FileWriter writer = null;
		BufferedReader reader = null;
		try {

			String sCurrentLine;
			String newContent = "";
			int count = 1;
			boolean flag = true;
			file = new File("/etc/haproxy/haproxy.cfg");
			reader = new BufferedReader(new FileReader(file));
			boolean restart = false;

			while ((sCurrentLine = reader.readLine()) != null) {

				if (sCurrentLine.contains("server app-server") && !sCurrentLine.contains("#server app-server")
						&& flag == true) {
					if (count == 1) {
						count = 0;
						newContent = newContent + sCurrentLine + "\r\n";
					} else {
						System.out.println("Removing an existing server...");
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
				restart = false;
			}
			else
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
	}

	public void restartHaproxy() {

		ProcessBuilder pb = new ProcessBuilder("bash", "-c", "haproxy -D -f /etc/haproxy/haproxy.cfg -p /var/run/haproxy.pid -sf $(cat /var/run/haproxy.pid)");
		pb.redirectErrorStream(true);
		
		try {
			Process shell = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
