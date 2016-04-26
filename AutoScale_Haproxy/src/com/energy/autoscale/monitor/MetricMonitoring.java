package com.energy.autoscale.monitor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.energy.autoscale.scale.ServerScale;

public class MetricMonitoring {

	public int parseCSV(String metric, String url) throws Exception {
		Document doc = null;
		int ret = 0;
		try {

			doc = Jsoup.connect(url + ";csv").get();
			String body = doc.body().text();

			System.out.println("*****************Performance monitoring**************************");
			//System.out.println(body);

			int start = body.indexOf("haproxy_http,BACKEND");
			int end = body.indexOf("stats,FRONTEND");
			String backendServer = body.substring(start, end);
			// System.out.println("************" + backendServer);

			String[] arr = backendServer.split(",");
			int queueLength = Integer.parseInt(arr[2]);
			int requestRate = Integer.parseInt(arr[33]);
			int resposeTime = Integer.parseInt(arr[arr.length - 3]);

			if (metric.equals("ql"))
				ret = queueLength;
			else if (metric.equals("rr"))
				ret = requestRate;
			else if (metric.equals("rt"))
				ret = resposeTime;
			else
				ret = -1;

		} catch (Exception e) {
			// e.printStackTrace();
			throw new Exception();
		}
		return ret;
	}

	public void monitorQueueLength(String url, int minThreshold, int maxThreshold, int refreshTime) {

		System.out.println("#########Metric: Queue length, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold+"##########");
		int timeElapsed=0;
		while (true) {

			try {
				int val = parseCSV("ql", url);
				System.out.println("Queue Length: " + val);
				
				
				if (val == -1) {
					System.out.println("no metric data found");
					break;
				}

				ServerScale scale = new ServerScale();
				if (val > maxThreshold)
					scale.addServer();
				else if (val < minThreshold)
					scale.removeServer();
				else
					System.out.println("No additional server is required!");

				System.out.println("Time elapsed since start: "+timeElapsed+" seconds");
				Thread.currentThread().sleep(refreshTime);
				timeElapsed=timeElapsed+refreshTime/1000;
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}
		}
	}

	public void monitorRequestRate(String url, int minThreshold, int maxThreshold, int refreshTime) {

		System.out.println("###########Metric: Request Rate, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold+"##########");
		int timeElapsed=0;
		while (true) {
			try {
				int val = parseCSV("rr", url);
				System.out.println("Request Rate: " + val);
				
				if (val == -1) {
					System.out.println("no metric data found");
					break;
				}

				ServerScale scale = new ServerScale();
				if (val > maxThreshold)
					scale.addServer();
				else if (val < minThreshold)
					scale.removeServer();
				else
					System.out.println("No additional server is required!");

				System.out.println("Time elapsed since start: "+timeElapsed+" seconds");
				Thread.currentThread().sleep(refreshTime);
				timeElapsed=timeElapsed+refreshTime/1000;
			} catch (Exception e) {
				//e.printStackTrace();
				continue;
			}
		}
	}

	public void monitorResponseTime(String url, int minThreshold, int maxThreshold, int refreshTime) {

		System.out.println("#########Metric: Response Time, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold+"##########");
		int prevRT=Integer.MIN_VALUE;
		int timeElapsed=0;
		while (true) {

			try {
				int val = parseCSV("rt", url);
				System.out.println("Response Time: " + val);
				
				if (val == -1) {
					System.out.println("no metric data found");
					break;
				}

				ServerScale scale = new ServerScale();
				if (val > maxThreshold)
					scale.addServer();
				else if (val < minThreshold){
					if(prevRT>=val)
						scale.removeServer();
				}
				else
					System.out.println("No additional server is required!");
				prevRT=val;

				System.out.println("Time elapsed since start: "+timeElapsed+" seconds");
				Thread.currentThread().sleep(refreshTime);
				timeElapsed=timeElapsed+refreshTime/1000;
			} catch (Exception e) {
				//e.printStackTrace();
				continue;
			}
		}
	}

}
