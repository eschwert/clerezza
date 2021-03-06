/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.rdf.virtuoso.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the connection to the Virtuoso DBMS
 * 
 * @author enridaga
 */
public class ConnectionTest {

	static final Logger log = LoggerFactory.getLogger(ConnectionTest.class);

	private static Connection connection = null;

	@BeforeClass
	public static void before() throws ClassNotFoundException, SQLException {
		org.junit.Assume.assumeTrue(!TestUtils.SKIP);
		connection = TestUtils.getConnection();
	}
	
	public void testIsValid() throws SQLException {
		assertTrue(connection.isValid(10));
	}

	@Test
	public void testIsClosed() throws SQLException {
		assertFalse(connection.isClosed());
	}

	@Test
	public void testConnection() {
		log.info("testConnection()");
		try {

			Statement st = connection.createStatement();
			log.debug("Populate graph <mytest>");
			String[] queries = {
					"sparql clear graph <mytest>",
					"sparql insert into graph <mytest> { <xxx> <P01> \"test1\"@en }",
					"sparql insert into graph <mytest> { <xxx> <P01> \"test2\"@it }",
					"sparql PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> insert into graph <mytest> { <xxx> <P01> \"test3\"^^xsd:string }",
					"sparql insert into graph <mytest> { <xxx> <P01> \"test4\" }",
					"sparql insert into graph <mytest> { <xxx> <P01> \"test5\" . <xxx> <P02> _:b1"
							+ " . _:b1 <P03> <yyy> " + " . _:b3 <P05> <zzz> "
							+ " . _:b3 <P05> <ppp> " +
							// This is to consider that we can force it
							" .  <nodeID://b10005> <P05> <ooo> " + " }",
					"sparql insert into graph <mytest> { <nodeID://b10005> <property> <nodeID://b10007>}",
					"sparql insert into graph <mytest> { <enridaga> <property> \"Literal value\"^^<http://datatype#type>}",
					"sparql insert into graph <mytest> { <nodeID://b10005> <property> <nodeID://b10007>}" };
			for (String q : queries) {
				log.debug("Querying: {}", q);
				st.execute(q);
			}

			String query = "sparql SELECT * from <mytest> WHERE {?s ?p ?o}";
			log.debug("Querying: {}", query);
			ResultSet rs = st.executeQuery(query);
			TestUtils.stamp(rs);
		} catch (SQLException e) {
			log.error("SQL ERROR: ", e);
			assertTrue(false);
		} catch (Exception e) {
			log.error("SQL ERROR: ", e);
			assertTrue(false);
		}
	}

	@Test
	public void test() throws ClassNotFoundException, SQLException {
		DatabaseMetaData dm = connection.getMetaData();
		log.debug("Username is {}", dm.getUserName());
//		Properties p = connection.getClientInfo();
//		if (p == null) {
//			log.warn("Client info is null...");
//		} else
//			for (Entry<Object, Object> e : p.entrySet()) {
//				log.info("Client info property: {} => {}", e.getKey(),
//						e.getValue());
//			}
		String SQL = "SELECT DISTINCT id_to_iri(G) FROM DB.DBA.RDF_QUAD quad ";
		Connection cn = TestUtils.getConnection();
		long startAt = System.currentTimeMillis();
		// get the list of quad using SQL
		log.debug("Executing SQL: {}", SQL);
		cn.createStatement().executeQuery(SQL);
		long endAt = System.currentTimeMillis();
		log.debug("Using SQL: {}ms", endAt - startAt);
		SQL = "SPARQL SELECT DISTINCT ?G WHERE {GRAPH ?G {?S ?P ?O} }";
		startAt = System.currentTimeMillis();
		// get the list of quad using SQL+SPARQL
		log.debug("Executing SQL: {}", SQL);
		cn.createStatement().executeQuery(SQL);
		endAt = System.currentTimeMillis();
		log.debug("Using SQL+SPARQL: {}ms", endAt - startAt);
	}
}
