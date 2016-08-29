package de.dwslab.sdtv;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Initializes the database
 * @author Heiko
 *
 */
public class LoadFiles {
	private int chunkSize = 100000;
	
	/**
	 * Runtime: ~10 minutes
	 * @param filename
	 * @throws IOException
	 */
	public void loadProperties(String filename) throws IOException, CompressorException {
		System.out.println("Property assertions: load started");
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error preparing insert");
			e.printStackTrace();
		}

		Util.removeTableIfExisting("dbpedia_properties");
		try {
			String createStatement = "CREATE TABLE dbpedia_properties (subject VARCHAR(1000) NOT NULL, predicate VARCHAR(1000) NOT NULL, object VARCHAR(1000) NOT NULL)";
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating table");
			e.printStackTrace();
		}
		
		long count = 0;
		long errors = 0;
		BufferedReader BR = getBufferedReaderForCompressedFile(filename);
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error preparing insert");
			e.printStackTrace();
		}
		String line = BR.readLine();
		while(line != null) {
			boolean insert = false;
			String sqlInsert = "INSERT INTO dbpedia_properties VALUES(";
			if(line.startsWith("#"))
				continue;
			line=line.replace("'","''");
			StringTokenizer stk = new StringTokenizer(line,"> ",false);
			String subject = stk.nextToken();
			subject = subject.replace("<","");
			sqlInsert += "'" + subject + "',";

			String predicate = stk.nextToken();
			predicate = predicate.replace("<","");
			sqlInsert += "'" + predicate + "',";
			
			String object = stk.nextToken();
			if(object.startsWith("<")) {
				if(object.startsWith("<http://dbpedia.org/resource/")) {
					object = object.replace(" .","");
					object = object.replace("<","");
					sqlInsert +="'" + object + "')";
					insert = true;
				}
			}
			if(insert) {
				try {
					stmt.addBatch(sqlInsert);
				} catch (SQLException e) {
					System.out.println("Error: could not add to batch");
					e.printStackTrace();
				}
				count++;
				if(count%chunkSize==0) {
					try {
						int[] results = stmt.executeBatch();
						for(int i : results)
							if(i==0)
								errors++;
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		try {
			int[] results = stmt.executeBatch();
			for(int i : results)
				if(i==0)
					errors++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BR.close();
		System.out.println("Property assertions: load finished. Loaded " + count + " statements, encountered " + errors + " error(s).");
		Util.checkTable("dbpedia_properties");
	}
	

	/**
	 * Runtime: ~20 minutes
	 */
	public void createPropertyIndices() {
		System.out.println("Creating indices on property table");
		Util.createIndex("dbpedia_properties", "subject");
		Util.createIndex("dbpedia_properties", "predicate");
		Util.createIndex("dbpedia_properties", "object");
	}
	
	/**
	 * Runtime ~10 minutes
	 * @param filename
	 * @throws IOException
	 */
	public void loadTypes(String filename) throws IOException, CompressorException {
		System.out.println("Type assertions: load started");
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error preparing insert");
			e.printStackTrace();
		}
			
		Util.removeTableIfExisting("dbpedia_types");
		try {
			String createStatement = "CREATE TABLE dbpedia_types (resource VARCHAR(1000) NOT NULL, type VARCHAR(1000) NOT NULL)";
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating table");
			e.printStackTrace();
		}
		
		long count = 0;
		long errors = 0;
		BufferedReader BR = getBufferedReaderForCompressedFile(filename);
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error preparing insert");
			e.printStackTrace();
		}
		String line = BR.readLine();
		while(line != null) {
			boolean insert = false;
			String sqlInsert = "INSERT INTO dbpedia_types VALUES(";
			if(line.startsWith("#"))
				continue;
			line=line.replace("'","''");
			StringTokenizer stk = new StringTokenizer(line,"> ",false);
			String subject = stk.nextToken();
			subject = subject.replace("<","");
			sqlInsert += "'" + subject + "',";

			// the predicate is only rdf.type, so ignore
			String predicate = stk.nextToken();
			
			String object = stk.nextToken();
			object = object.replace(" .","");
			object = object.replace("<","");
			sqlInsert +="'" + object + "')";
			try {
				stmt.addBatch(sqlInsert);
			} catch (SQLException e) {
				System.out.println("Error: could not add to batch");
				e.printStackTrace();
			}
			count++;
			if(count%chunkSize==0) {
				try {
					int[] results = stmt.executeBatch();
					for(int i : results)
						if(i==0)
							errors++;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			int[] results = stmt.executeBatch();
			for(int i : results)
				if(i==0)
					errors++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BR.close();
		System.out.println("Type assertions: load finished. Loaded " + count + " statements, encountered " + errors + " error(s).");
		Util.checkTable("dbpedia_types");
	}

	/**
	 * Runtime ~30 minutes
	 */
	public void createTypeIndices() {
		System.out.println("Creating indices on type table");
		Util.createIndex("dbpedia_types", "resource");
		Util.createIndex("dbpedia_types", "type");
	}
	
	public void loadDisambiguations(String fileName) throws IOException, CompressorException {
		System.out.println("Disambiguations: load started");
		Connection conn = ConnectionManager.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error preparing insert");
			e.printStackTrace();
		}
			
		Util.removeTableIfExisting("dbpedia_disambiguations");
		try {
			String createStatement = "CREATE TABLE dbpedia_disambiguations (resource VARCHAR(1000) NOT NULL)";
			stmt.execute(createStatement);
		} catch (SQLException e) {
			System.out.println("Error creating table");
			e.printStackTrace();
		}
		
		Collection<String> resources = new HashSet<String>();
		long count = 0;
		long errors = 0;
		BufferedReader BR = getBufferedReaderForCompressedFile(fileName);
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error preparing insert");
			e.printStackTrace();
		}
		String line = BR.readLine();
		while(line != null) {
			boolean insert = false;
			if(line.startsWith("#"))
				continue;
			line=line.replace("'","''");
			StringTokenizer stk = new StringTokenizer(line,"> ",false);
			String subject = stk.nextToken();
			subject = subject.replace("<","");
			if(!resources.contains(subject)) {
				String sqlInsert = "INSERT INTO dbpedia_disambiguations VALUES('" + subject + "')";

				try {
					stmt.addBatch(sqlInsert);
				} catch (SQLException e) {
					System.out.println("Error: could not add to batch");
					e.printStackTrace();
				}
				count++;
				if(count%chunkSize==0) {
					try {
						int[] results = stmt.executeBatch();
						for(int i : results)
							if(i==0)
								errors++;
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			resources.add(subject);
		}
		try {
			int[] results = stmt.executeBatch();
			for(int i : results)
				if(i==0)
					errors++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BR.close();
		System.out.println("Disambiguations: load finished. Loaded " + count + " resources, encountered " + errors + " error(s).");
		Util.checkTable("dbpedia_disambiguations");
	
	}
	
	public void doLoad(String typeFileName, String propertyFileName, String disambiguationFileName) throws IOException, CompressorException {
		loadTypes(typeFileName);
		createTypeIndices();
		loadProperties(propertyFileName);
		createPropertyIndices();
		loadDisambiguations(disambiguationFileName);
		createDisambiguationIndices();
	}

	public void createDisambiguationIndices() {
		Util.createIndex("dbpedia_disambiguations", "resource");
	}

	public static BufferedReader getBufferedReaderForCompressedFile(String fileIn) throws FileNotFoundException, CompressorException {
		FileInputStream fin = new FileInputStream(fileIn);
		BufferedInputStream bis = new BufferedInputStream(fin);
		CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
		BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
		return br2;
	}
}
