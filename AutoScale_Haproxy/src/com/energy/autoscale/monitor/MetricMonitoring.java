package com.energy.autoscale.monitor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.energy.autoscale.scale.ServerScale;

public class MetricMonitoring {

	public int parseCSV(String metric, String url) {
		Document doc = null;
		int ret = 0;
		try {

			doc = Jsoup.connect(url + ";csv").get();
			String body = doc.body().text();
			
			System.out.println("**************************************");
			// System.out.println(body);

			int start = body.indexOf("haproxy_http,BACKEND");
			int end = body.indexOf("stats,FRONTEND");
			String backendServer = body.substring(start, end);
			//System.out.println("************" + backendServer);

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

			System.out.println("Queue Length: " + queueLength);
			System.out.println("Request Rate: " + requestRate);
			System.out.println("Response Time: " + resposeTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void monitorQueueLength(String url, int minThreshold, int maxThreshold, int refreshTime) {

		System.out.println("#################Metric: Queue length, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold);
		while (true) {
			int val = parseCSV("ql", url);

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

			try {
				Thread.currentThread().sleep(refreshTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void monitorRequestRate(String url, int minThreshold, int maxThreshold, int refreshTime) {

		System.out.println("#################Metric: Request Rate, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold);
		while (true) {
			int val = parseCSV("rr", url);

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

			try {
				Thread.currentThread().sleep(refreshTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void monitorResponseTime(String url, int minThreshold, int maxThreshold, int refreshTime) {

		System.out.println("#################Metric: Response Time, minThreshold: " + minThreshold + ", maxThreshold: "
				+ maxThreshold);
		while (true) {
			int val = parseCSV("rt", url);

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

			try {
				Thread.currentThread().sleep(refreshTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
