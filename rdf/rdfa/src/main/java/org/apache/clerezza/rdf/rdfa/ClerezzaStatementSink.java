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
package org.apache.clerezza.rdf.rdfa;

import java.util.HashMap;
import java.util.Map;

import net.rootdev.javardfa.StatementSink;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.Language;
import org.apache.clerezza.commons.rdf.Literal;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.TypedLiteralImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Henry Story <henry.story@bblfish.net>
 */
public class ClerezzaStatementSink implements StatementSink {

    private static Logger log = LoggerFactory.getLogger(ClerezzaStatementSink.class);
    private Map<String, BlankNode> bnodeLookup;
    Graph mgraph;

    public ClerezzaStatementSink(Graph mGraph) {
        this.mgraph = mGraph;
    }

    @Override
    public void start() {
        bnodeLookup = new HashMap<String, BlankNode>();
    }

    @Override
    public void end() {
        bnodeLookup = null;
    }

    @Override
    public void addObject(String subject, String predicate, String object) {
        mgraph.add(new TripleImpl(transform(subject), new IRI(predicate), transform(object)));
    }

    private BlankNodeOrIRI transform(String nonLiteral) {
        BlankNodeOrIRI s;
        RDFTerm o;
        if (nonLiteral.startsWith("_:")) {
            s = bnodeLookup.get(nonLiteral);
            if (s == null) {
                s = new BlankNode();
                bnodeLookup.put(nonLiteral, (BlankNode) s);
            }
        } else {
            s = new IRI(nonLiteral);
        }
        return s;
    }

    @Override
    public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
        Literal obj;
        if (datatype == null) {
            if (lang == null) {
                obj = new PlainLiteralImpl(lex);
            } else {
                obj = new PlainLiteralImpl(lex, new Language(lang));
            }
        } else {
            obj = new TypedLiteralImpl(lex, new IRI(datatype));
        }
        mgraph.add(new TripleImpl(transform(subject), new IRI(predicate), obj));
    }

    @Override
    public void addPrefix(String prefix, String uri) {
//       try {
//         handler.handleNamespace(prefix, uri);
//      } catch (RDFHandlerException rDFHandlerException) {
//         java.util.logging.Logger.getLogger(ClerezzaStatementSink.class.getName()).log(Level.WARNING, null, rDFHandlerException);
//      }
    }

    @Override
    public void setBase(String base) {
    }
}
