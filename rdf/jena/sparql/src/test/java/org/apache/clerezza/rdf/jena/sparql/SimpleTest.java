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
package org.apache.clerezza.rdf.jena.sparql;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Literal;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.core.sparql.ParseException;
import org.apache.clerezza.rdf.core.sparql.QueryParser;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.ontologies.DC;

/**
 * 
 * @author rbn
 */
public class SimpleTest {

	@Test
	public void simpleStringQuery() throws ParseException {
		SimpleGraph data = new SimpleGraph();
		final String titleValue = "SPARQL Tutorial";
		data.add(new TripleImpl(new IRI("http://example.org/book/book1"),
				DC.title, new PlainLiteralImpl(titleValue)));
		String query = "SELECT ?title WHERE" + "{"
				+ "    <http://example.org/book/book1> <"
				+ DC.title.getUnicodeString() + "> ?title ." + "}";
		ResultSet resultSet = (ResultSet) TcManager.getInstance()
				.executeSparqlQuery(query, data);
		Assert.assertEquals(titleValue, ((Literal) resultSet.next()
				.get("title")).getLexicalForm());

		List<String> columnNames = resultSet.getResultVars();
		Assert.assertEquals(columnNames.size(), 1);
		Assert.assertEquals(columnNames.get(0), "title");
	}

	@Test
	public void simpleSelectQuery() throws ParseException {
		SimpleGraph data = new SimpleGraph();
		final String titleValue = "SPARQL Tutorial";
		data.add(new TripleImpl(new IRI("http://example.org/book/book1"),
				DC.title, new PlainLiteralImpl(titleValue)));
		String query = "SELECT ?title WHERE" + "{"
				+ "    <http://example.org/book/book1> <"
				+ DC.title.getUnicodeString() + "> ?title ." + "}";
		ResultSet resultSet = (ResultSet) TcManager.getInstance()
				.executeSparqlQuery(QueryParser.getInstance().parse(query),
						data);
		Assert.assertEquals(titleValue, ((Literal) resultSet.next()
				.get("title")).getLexicalForm());
	}

	@Test
	public void simpleAskQuery() throws ParseException {
		SimpleGraph data = new SimpleGraph();
		final String titleValue = "SPARQL Tutorial";
		data.add(new TripleImpl(new IRI("http://example.org/book/book1"),
				DC.title, new PlainLiteralImpl(titleValue)));
		String query = "ASK WHERE" + "{"
				+ "    <http://example.org/book/book1> <"
				+ DC.title.getUnicodeString() + "> ?title ." + "}";
		Assert.assertEquals(
				Boolean.TRUE,
				TcManager.getInstance().executeSparqlQuery(
						QueryParser.getInstance().parse(query), data));
	}

	/* Uncommented due to problem with resolution of CLEREZZA-761
          
         @Test
	public void simpleDescribe() throws ParseException {
		SimpleGraph data = new SimpleGraph();
		final String titleValue = "SPARQL Tutorial";
		data.add(new TripleImpl(new IRI("http://example.org/book/book1"),
				DC.title, new PlainLiteralImpl(titleValue)));
		String query = "DESCRIBE " + "    <http://example.org/book/book1>";
		Assert.assertEquals(
				1,
				((ImmutableGraph) TcManager.getInstance().executeSparqlQuery(
						QueryParser.getInstance().parse(query), data)).size());
	}*/

	@Test
	public void simpleConstruct() throws ParseException {
		SimpleGraph data = new SimpleGraph();
		final String titleValue = "SPARQL Tutorial";
		data.add(new TripleImpl(new IRI("http://example.org/book/book1"),
				DC.title, new PlainLiteralImpl(titleValue)));
		String query = "PREFIX foaf:   <http://xmlns.com/foaf/0.1/> "
				+ "CONSTRUCT { <http://foo/bar> foaf:name ?title } WHERE" + "{"
				+ "    <http://example.org/book/book1> <"
				+ DC.title.getUnicodeString() + "> ?title ." + "}";
		Assert.assertEquals(
				1,
				((ImmutableGraph) TcManager.getInstance().executeSparqlQuery(
						QueryParser.getInstance().parse(query), data)).size());
	}
    
    /* Currently fails because of Preparser not yet supporting this type of queries
    @Test
	public void simpleInsert() throws ParseException {
		SimpleGraph data = new SimpleGraph();
		final String titleValue = "SPARQL Tutorial";
		data.add(new TripleImpl(new IRI("http://example.org/book/book1"),
				DC.title, new PlainLiteralImpl(titleValue)));
        
		String query = "PREFIX foaf:   <http://xmlns.com/foaf/0.1/> "
				+ "INSERT DATA { <http://foo/bar> foaf:name 'Fred'}";
        TcManager.getInstance().executeSparqlQuery(query, data);
		Assert.assertEquals("Not having the right number of triples after insert",2,data.size());
	}*/
    
    @Test
	public void simpleDrop() throws ParseException {
		SimpleGraph data = new SimpleGraph();
		final String titleValue = "SPARQL Tutorial";
		data.add(new TripleImpl(new IRI("http://example.org/book/book1"),
				DC.title, new PlainLiteralImpl(titleValue)));
        
		String query = "DROP ALL";
        TcManager.getInstance().executeSparqlQuery(query, data);
		Assert.assertEquals("GRAPH not empty after DROP ALL",0,data.size());
	}
}
