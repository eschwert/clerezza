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

package org.apache.clerezza.rdf.web.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Literal;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.web.ontologies.BACKUP;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service to restore the triple collection
 *
 * @author reto
 */
@Component
@Service(Restorer.class)
public class Restorer {

    private final static Logger log = LoggerFactory.getLogger(Restorer.class);

    @Reference
    Parser parser;

    /**
     * Restores triple-collections from a backup to a specified TcProvider
     *
     * @param backupData the bytes of a backup zip
     * @param target the TcProvider into which to restore the data
     */
    public void restore(InputStream backupData, TcProvider target) throws IOException {
        ZipInputStream compressedTcs = new ZipInputStream(backupData);

        Map<String, Graph> extractedTc = new HashMap<String, Graph>();
        String folder = "";
        ZipEntry entry;
        ImmutableGraph metaGraph = null;
        while ((entry = compressedTcs.getNextEntry()) != null) {
            String entryName = entry.getName();
            if (entry.isDirectory()) {
                folder = entryName;
            } else {
                File tempFile = File.createTempFile("graph", "data");
                OutputStream fos = new FileOutputStream(tempFile);
                
                int count;
                byte buffer[] = new byte[2048];
                while ((count = compressedTcs.read(buffer, 0, 2048)) != -1) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                InputStream serializedGraph = new FileInputStream(tempFile);
                /*
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int count;
                byte buffer[] = new byte[2048];
                while ((count = compressedTcs.read(buffer, 0, 2048)) != -1) {
                    baos.write(buffer, 0, count);
                }
                baos.close();
                ByteArrayInputStream serializedGraph = new ByteArrayInputStream(
                        baos.toByteArray());
                */
                if (entryName.equals("triplecollections.nt")) {
                    metaGraph = parser.parse(serializedGraph,
                            SupportedFormat.N_TRIPLE, null);
                } else {
                    ImmutableGraph deserializedGraph = parser.parse(serializedGraph,
                            SupportedFormat.N_TRIPLE, null);
                    extractedTc.put(entryName, deserializedGraph);
                }
                serializedGraph.close();
            }
        }
        if (metaGraph == null) {
            throw new RuntimeException("No metadata graph found in backup.");
        }
        compressedTcs.close();
        {
            final Iterator<Triple> mGraphIterator = metaGraph.filter(null, RDF.type, BACKUP.Graph);
            while (mGraphIterator.hasNext()) {
                GraphNode graphGN = new GraphNode(mGraphIterator.next().getSubject(), metaGraph);
                String fileName = graphGN.getLiterals(BACKUP.file).next().getLexicalForm();
                Graph extracted = extractedTc.get(fileName);
                
                Graph mGraph;
                boolean created = false;
                try {
                    mGraph = target.getMGraph((IRI)graphGN.getNode());
                    try {
                        mGraph.clear();
                    } catch (UnsupportedOperationException ex) {
                        log.warn("could not restore "+graphGN.getNode()+" as the exsting triple "
                                + "collection could not be cleared");
                        continue;
                    }
                } catch (NoSuchEntityException ex) {
                    mGraph = target.createGraph((IRI)graphGN.getNode());
                    created = true;
                }
                try {
                    mGraph.addAll(extracted);
                } catch (Exception ex) {
                    String actionDone = created ? "created" : "cleared";
                    log.error("after the mgraph "+graphGN.getNode()+" could successfully be "+actionDone
                            + ", an exception occured adding the data", ex);
                }
            }
        }
        {
            final Iterator<Triple> graphIterator = metaGraph.filter(null, RDF.type, BACKUP.Graph);
            while (graphIterator.hasNext()) {
                GraphNode graphGN = new GraphNode(graphIterator.next().getSubject(), metaGraph);
                String fileName = graphGN.getLiterals(BACKUP.file).next().getLexicalForm();
                Graph extracted = extractedTc.get(fileName);
                try {
                    target.deleteGraph((IRI)graphGN.getNode());
                } catch (UnsupportedOperationException ex) {
                    log.warn("could not restore "+graphGN.getNode()+" as the exsting triple "
                            + "collection could not be deleted");
                    continue;
                } catch (NoSuchEntityException ex) {
                    log.debug("could not remove "+graphGN.getNode()+", no such entity");
                }
                target.createImmutableGraph((IRI)graphGN.getNode(), extracted);
            }
        }
        for (Map.Entry<String, Graph> pathTcPair : extractedTc.entrySet()) {
            Literal fileNameLit = LiteralFactory.getInstance().createTypedLiteral(
                        pathTcPair.getKey());
            Iterator<Triple> graphResIterator = metaGraph.filter(null, BACKUP.file, fileNameLit);
        }
    }
}
