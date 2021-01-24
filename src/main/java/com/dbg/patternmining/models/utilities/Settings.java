/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid

This file is part of Grami.

Grami is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Grami is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dbg.patternmining.models.utilities;

public class Settings 
{

	public static boolean isApproximate=false;   //EXACT
	public static double approxEpsilon = 0.0001;
	public static double approxConstant = 100000;
	
	public static boolean isAutomorphismOn= true;
	
	public static boolean isDecomposeOn= true;

	public static boolean CACHING = true;
	
	public static boolean DISTINCTLABELS = true;
	
	public static boolean LimitedTime = true;

	public static boolean PRINT = false;
	
	//datasets folder
//	public static String datasetsFolder = "/Users/zengjian/iParkWSFiles/projects/dataSets/";
	public static String datasetsFolder = "./dataset/";
	//server folder
//	public static String datasetsFolder = "/home/zengjian/dataSets/";
	
	//given frequency, if not given then its value is -1
	public static int frequency = -1;
	
	//core-based pattern: 3 edges
	public static int firstEdge = 1;
	public static int secondEdge = 2;
	public static int thirdEdge = 3;

	
	//the filename
//	public static String fileName = "WCGoals.lg";
//	public static String fileName = "summaries.lg";
//	public static String fileName = "YouTube.lg";
//	public static String fileName = "Amazon.lg";
	
//	public static String fileName = "Mico.lg";
//	public static String fileName = "Citeseer.lg";
//	public static String fileName = "Yago3.lg";
//	public static String fileName = "OscarWinners.lg";
//	public static String fileName = "JoinTableGraphCD.lg";
	public static String fileName = "phylogeny.lg";

	//core-pattern file
//	public static String coreFileName = "CiteseerCore.lg";
//	public static String coreFileName = "YagoLinkQ2.lg";
//	public static String coreFileName = "MicoLinkQ3.lg";
//	public static String coreFileName = "AmazonLinkQ2.lg";
//	public static String coreFileName = "summaryCore.lg";
//	public static String coreFileName = "WCGoalsQ21.lg";
//	public static String coreFileName = "OscarQ23.lg";
//	public static String coreFileName = "OscarTest.lg";
//	public static String coreFileName = "JoinTableGraphCDCore.lg";
	public static String coreFileName = "phyQ22.lg";
//	public static String coreFileName = "JoinTableGraphDCore.lg";
	
	public static boolean print = false;

	//top-k results
	public static int k =5;
	
	//the maximum number of the same label appears in the resulted pattern
	public static int numLabelAppears = -1;
}
