package com.energy.autoscale.monitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.ws.Response;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.energy.autoscale.scale.ServerScale;

public class MetricMonitoring {
	
	BufferedWriter objBufferedWriter = null;
	public void init(){
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
		Date date = new Date();
		//System.out.println(dateFormat.format(date));

		try {
			String sFilePath = "./log/";
			String sFileName=dateFormat.format(date)+".txt";
			
			File objOutFile = new File(sFilePath,sFileName);
			FileWriter objFileWriter = new FileWriter(objOutFile );
			objBufferedWriter = new BufferedWriter(objFileWriter);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int[] parseCSV(String metric, String url) throws Exception {
		Document doc = null;
		int[] ret = new int[4];
		int numServers = 0;

		try {

			doc = Jsoup.connect(url + ";csv").get();
			String body = doc.body().text();

			System.out.println();
			System.out.println("*****************Performance monitoring**************************");
			// System.out.println(body);

			int start = body.indexOf("haproxy_http,BACKEND");
			int end = body.indexOf("stats,FRONTEND");
			String backendServer = body.substring(start, end);
			// System.out.println("************" + backendServer);

			String[] arr = backendServer.split(",");
			int queueLength = Integer.parseInt(arr[2]);
			int requestRate = Integer.parseInt(arr[33]);
			int resposeTime = Integer.parseInt(arr[arr.length - 3]);
			
			System.out.println("Request Rate: " + requestRate);
			System.out.println("Queue Length: " + queueLength);
			System.out.println("Response Time: " + resposeTime);

			numServers = StringUtils.countMatches(body, "haproxy_http") - 1;

			ret[0]=requestRate;
			ret[1]=queueLength;
			ret[2]=resposeTime;
			ret[3]=numServers;
			
		} catch (Exception e) {
			// e.printStackTrace();
			throw new Exception();
		}
		return ret;
	}

	
	public void monitorQueueLength(String url, int minThreshold, int maxThreshold, int refreshTime) throws IOException {

		init();
		System.out.println("#########Metric: Queue length, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold + "##########");
		int timeElapsed = 0;
		int numServers = 0;
		
		objBufferedWriter.write("#########Metric: Queue length, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold + "##########\n");
		objBufferedWriter.write("#Request Rate\tQueuelength\tResponse Time\tTime Elapsed\tNum Servers\n");
		while (true) {

			try {
				int[] ret = parseCSV("ql", url);
				int val = ret[1];
				numServers = ret[3];
				//System.out.println("Queue Length: " + val);

				if (val == -1) {
					System.out.println("no metric data found");
					break;
				}

				boolean serverAdded = false;
				boolean serverRemoved = false;

				ServerScale scale = new ServerScale();
				if (val > maxThreshold)
					serverAdded = scale.addServer();
				else if (val < minThreshold)
					serverRemoved = scale.removeServer();
				else
					System.out.println("No additional server is required!");

				if (serverAdded)
					numServers++;
				if (serverRemoved)
					numServers--;

				System.out.println("Active # of servers: " + numServers);
				System.out.println("Time elapsed since start: " + timeElapsed + " seconds");
				objBufferedWriter.write(ret[0]+"\t"+ret[1]+"\t"+ret[2]+"\t"+timeElapsed+"\t"+numServers+"\n");
				objBufferedWriter.flush();
				
				Thread.currentThread().sleep(refreshTime);
				timeElapsed = timeElapsed + refreshTime / 1000;
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}
		objBufferedWriter.close();
	}

	public void monitorRequestRate(String url, int minThreshold, int maxThreshold, int refreshTime) throws IOException {

		init();
		System.out.println("###########Metric: Request Rate, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold + "##########");
		int timeElapsed = 0;
		int numServers = 0;
		
		objBufferedWriter.write("#########Metric: Request Rate, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold + "##########\n");
		objBufferedWriter.write("#Request Rate\tQueuelength\tResponse Time\tTime Elapsed\tNum Servers\n");
		while (true) {
			try {
				int[] ret = parseCSV("rr", url);
				int val = ret[0];
				numServers = ret[3];
				//System.out.println("Request Rate: " + val);

				if (val == -1) {
					System.out.println("no metric data found");
					break;
				}

				boolean serverAdded = false;
				boolean serverRemoved = false;

				ServerScale scale = new ServerScale();
				if (val > maxThreshold)
					serverAdded = scale.addServer();
				else if (val < minThreshold)
					serverRemoved = scale.removeServer();
				else
					System.out.println("No additional server is required!");

				if (serverAdded)
					numServers++;
				if (serverRemoved)
					numServers--;

				System.out.println("Active # of servers: " + numServers);
				System.out.println("Time elapsed since start: " + timeElapsed + " seconds");
				objBufferedWriter.write(ret[0]+"\t"+ret[1]+"\t"+ret[2]+"\t"+timeElapsed+"\t"+numServers+"\n");
				objBufferedWriter.flush();
				
				Thread.currentThread().sleep(refreshTime);
				timeElapsed = timeElapsed + refreshTime / 1000;
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}
		objBufferedWriter.close();
	}

	public void monitorResponseTime(String url, int minThreshold, int maxThreshold, int refreshTime) throws IOException {

		init();
		System.out.println("#########Metric: Response Time, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold + "##########");
		int prevRT = Integer.MIN_VALUE;
		int timeElapsed = 0;
		int numServers = 0;
		boolean isRTSet = false;

		objBufferedWriter.write("#########Metric: Response Time, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold + "##########\n");
		objBufferedWriter.write("#Request Rate\tQueuelength\tResponse Time\tTime Elapsed\tNum Servers\n");
		while (true) {

			try {
				int[] ret = parseCSV("rt", url);
				int val = ret[2];
				numServers = ret[3];
				//System.out.println("Response Time: " + val);

				if (val == -1) {
					System.out.println("no metric data found");
					break;
				}

				boolean serverAdded = false;
				boolean serverRemoved = false;

				ServerScale scale = new ServerScale();
				if (val > maxThreshold) {
					serverAdded = scale.addServer();
					isRTSet = true;
					prevRT = Integer.MIN_VALUE;
				} else if (val < minThreshold) {
					if (prevRT >= val) {
						serverRemoved = scale.removeServer();
						prevRT = Integer.MIN_VALUE;
						isRTSet = true;
					}
				} else
					System.out.println("No additional server is required!");

				if (!isRTSet)
					prevRT = val;
				else
					isRTSet = false;

				if (serverAdded)
					numServers++;
				if (serverRemoved)
					numServers--;

				System.out.println("Active # of servers: " + numServers);
				System.out.println("Time elapsed since start: " + timeElapsed + " seconds");
				objBufferedWriter.write(ret[0]+"\t"+ret[1]+"\t"+ret[2]+"\t"+timeElapsed+"\t"+numServers+"\n");
				objBufferedWriter.flush();
				
				Thread.currentThread().sleep(refreshTime);
				timeElapsed = timeElapsed + refreshTime / 1000;
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}
		objBufferedWriter.close();
	}

}
