package com.energy.autoscale.parser;

import java.io.IOException;

import com.energy.autoscale.monitor.MetricMonitoring;

public class Inputparser {

	public static void main(String[] args) throws IOException {

		if (args.length != 5)
			System.out.println("Invalid set of arguments! \n Please enter:  "
					+ "\n 1. Haproxy Statistics web Url \n 2. metric type \n 3. threshold to scale down \n "
					+ "4. threshold to scale up \n 5. Refresh interval");

		MetricMonitoring metricMonitoring = new MetricMonitoring();
		int minThreshold = Integer.parseInt(args[2]);
		int maxThreshold = Integer.parseInt(args[3]);
		int refreshTime = Integer.parseInt(args[4]) * 1000;

		if (args[1].equalsIgnoreCase("ql"))
			metricMonitoring.monitorQueueLength(args[0], minThreshold, maxThreshold, refreshTime);
		else if (args[1].equalsIgnoreCase("rr"))
			metricMonitoring.monitorRequestRate(args[0], minThreshold, maxThreshold, refreshTime);
		else if (args[1].equalsIgnoreCase("rt"))
			metricMonitoring.monitorResponseTime(args[0], minThreshold, maxThreshold, refreshTime);
		else
			System.out.println("Wrong metric selected!");

	}

}