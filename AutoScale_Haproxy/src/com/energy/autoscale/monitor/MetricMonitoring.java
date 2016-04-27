package com.energy.autoscale.monitor;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.energy.autoscale.scale.ServerScale;

public class MetricMonitoring {

	public int[] parseCSV(String metric, String url) throws Exception {
		Document doc = null;
		int[] ret = new int[2];
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

			numServers=StringUtils.countMatches(body, "haproxy_http")-1;

			if (metric.equals("ql"))
				ret[0] = queueLength;
			else if (metric.equals("rr"))
				ret[0] = requestRate;
			else if (metric.equals("rt"))
				ret[0] = resposeTime;
			else
				ret[0] = -1;

			ret[1]=numServers;
		} catch (Exception e) {
			// e.printStackTrace();
			throw new Exception();
		}
		return ret;
	}

	public void monitorQueueLength(String url, int minThreshold, int maxThreshold, int refreshTime) {

		System.out.println("#########Metric: Queue length, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold + "##########");
		int timeElapsed = 0;
		int numServers=0;
		while (true) {

			try {
				int[] ret = parseCSV("ql", url);
				int val=ret[0];
				numServers=ret[1];
				System.out.println("Queue Length: " + val);

				if (val == -1) {
					System.out.println("no metric data found");
					break;
				}
				
				boolean serverAdded=false;
				boolean serverRemoved=false;
				
				ServerScale scale = new ServerScale();
				if (val > maxThreshold)
					serverAdded=scale.addServer();
				else if (val < minThreshold)
					serverRemoved=scale.removeServer();
				else
					System.out.println("No additional server is required!");
				
				if(serverAdded)
					numServers++;
				if(serverRemoved)
					numServers--;

				System.out.println("Active # of servers: "+numServers);
				System.out.println("Time elapsed since start: " + timeElapsed + " seconds");
				Thread.currentThread().sleep(refreshTime);
				timeElapsed = timeElapsed + refreshTime / 1000;
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}
	}

	public void monitorRequestRate(String url, int minThreshold, int maxThreshold, int refreshTime) {

		System.out.println("###########Metric: Request Rate, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold + "##########");
		int timeElapsed = 0;
		int numServers=0;
		while (true) {
			try {
				int[] ret = parseCSV("rr", url);
				int val=ret[0];
				numServers=ret[1];
				System.out.println("Request Rate: " + val);

				if (val == -1) {
					System.out.println("no metric data found");
					break;
				}
				
				boolean serverAdded=false;
				boolean serverRemoved=false;
				
				ServerScale scale = new ServerScale();
				if (val > maxThreshold)
					serverAdded=scale.addServer();
				else if (val < minThreshold)
					serverRemoved=scale.removeServer();
				else
					System.out.println("No additional server is required!");
				
				if(serverAdded)
					numServers++;
				if(serverRemoved)
					numServers--;

				System.out.println("Active # of servers: "+numServers);
				System.out.println("Time elapsed since start: " + timeElapsed + " seconds");
				Thread.currentThread().sleep(refreshTime);
				timeElapsed = timeElapsed + refreshTime / 1000;
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}
	}

	public void monitorResponseTime(String url, int minThreshold, int maxThreshold, int refreshTime) {

		System.out.println("#########Metric: Response Time, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold + "##########");
		int prevRT = Integer.MIN_VALUE;
		int timeElapsed = 0;
		int numServers=0;
		boolean isRTSet=false;
		
		while (true) {

			try {
				int[] ret = parseCSV("rt", url);
				int val=ret[0];
				numServers=ret[1];
				
				System.out.println("Response Time: " + val);

				if (val == -1) {
					System.out.println("no metric data found");
					break;
				}

				boolean serverAdded=false;
				boolean serverRemoved=false;
				
				ServerScale scale = new ServerScale();
				if (val > maxThreshold){
					serverAdded=scale.addServer();
					isRTSet=true;
				}
				else if (val < minThreshold) {
					if (prevRT >= val){
						serverRemoved=scale.removeServer();
						prevRT=Integer.MIN_VALUE;
						isRTSet=true;
					}
				} else
					System.out.println("No additional server is required!");
				
				if(!isRTSet)
					prevRT = val;
				else
					isRTSet=false;

				if(serverAdded)
					numServers++;
				if(serverRemoved)
					numServers--;
					
				System.out.println("Active # of servers: "+numServers);
				System.out.println("Time elapsed since start: " + timeElapsed + " seconds");
				Thread.currentThread().sleep(refreshTime);
				timeElapsed = timeElapsed + refreshTime / 1000;
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}
	}

}
