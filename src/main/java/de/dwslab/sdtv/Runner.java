package de.dwslab.sdtv;

import java.io.IOException;

/**
 * The main class that runs SDType&Validate
 * @author Heiko
 */
public class Runner {
	public static void main(String[] args) {
		LoadFiles loadFiles = new LoadFiles();
		ComputeBaseStatistics computeBaseStatistics = new ComputeBaseStatistics();
		MaterializeSDTypes materializeSDTypes = new MaterializeSDTypes();
		MaterializeSDValidate materializeSDValidate = new MaterializeSDValidate();
		try {
			loadFiles.loadProperties(args[0]); 									//./enwiki-20151002-mappingbased-objects-uncleaned.ttl
			loadFiles.createPropertyIndices();
			loadFiles.loadTypes(args[1]); 										//./enwiki-20151002-instance-types-transitive.ttl
			loadFiles.createTypeIndices();
			loadFiles.loadDisambiguations(args[2]);								//./enwiki-20151002-disambiguations-unredirected.ttl
			loadFiles.createDisambiguationIndices();
			computeBaseStatistics.computeGlobalTypeDistribution();
			computeBaseStatistics.computePerPredicateDistribution();
			materializeSDTypes.computeSDTypes();
			materializeSDTypes.writeTypeFile(args[3], 0.4f);					//./sdtypes.ttl
			materializeSDValidate.computeSDValidateScores();
			materializeSDValidate.writeWrongStatementsFile(args[4], 0.15f);		//./sdinvalid.ttl
			if(1<0)
				throw new IOException();
		} catch (IOException e) {
			System.out.println("Error loading input files");
			e.printStackTrace();
		}
	}
}
