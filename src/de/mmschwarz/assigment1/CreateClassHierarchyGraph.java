package de.mmschwarz.assigment1;

import java.io.File;
import java.io.IOException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import grail.interfaces.DirectedEdgeInterface;
import grail.interfaces.DirectedGraphInterface;
import grail.interfaces.DirectedNodeInterface;
import grail.properties.GraphProperties;
import grail.setbased.SetBasedDirectedGraph;
import grail.util.GraphLoadSave;
import me.tomassetti.support.DirExplorer;
import model.CommonMetaModel20;

public class CreateClassHierarchyGraph {
	public static void main(String[] args) {
		final String sourceDirectory = "C:\\users\\michael\\workspace\\frontend-demo\\src";
		final String outputFileName = "graph.gml";
		new GraphLoadSave().saveGML(constructClassHierachyGraph(new File(sourceDirectory)), outputFileName, true);
	}

	public static DirectedGraphInterface constructClassHierachyGraph(File projectDir) {
		final DirectedGraphInterface dgi = new SetBasedDirectedGraph();
		new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
			System.out.println(path);
			for(int i = 0; i < path.length(); i++){
				System.out.print("*");
			}
			System.out.print("\n");
			try {
				new VoidVisitorAdapter<Object>() {
					
					@Override
					public void visit(ClassOrInterfaceDeclaration n, Object arg) {
						DirectedNodeInterface node = dgi.createNode(n.getName());
						dgi.addNode(node);
						node.setProperty((GraphProperties.LABEL), n.getName());
						node.setProperty((GraphProperties.TYPE), CommonMetaModel20.ClassType.getType());
						System.out.println("Class: " + n.getName());
						if (n.getChildrenNodes() != null) {
							for (final ClassOrInterfaceType c : n.getExtends()) {
								node = dgi.createNode(c.getName());
								dgi.addNode(node);
								node.setProperty((GraphProperties.LABEL), c.getName());
								node.setProperty((GraphProperties.TYPE), CommonMetaModel20.ExtendsRef.getType());

								if (!dgi.containsEdge((DirectedNodeInterface) dgi.getNode(c.getName()),
										(DirectedNodeInterface) dgi.getNode(n.getName()))) {
									DirectedEdgeInterface edge = dgi.createEdge(null,
											(DirectedNodeInterface) dgi.getNode(c.getName()),
											(DirectedNodeInterface) dgi.getNode(n.getName()));
									dgi.addEdge(edge);
								}

								System.out.println("superClass: " + c.getName());
							}
							for (final ClassOrInterfaceType c : n.getImplements()) {
								node = dgi.createNode(c.getName());
								dgi.addNode(node);
								node.setProperty((GraphProperties.LABEL), c.getName());
								node.setProperty((GraphProperties.TYPE), CommonMetaModel20.ImplementsRef.getType());

								if (!dgi.containsEdge((DirectedNodeInterface) dgi.getNode(c.getName()),
										(DirectedNodeInterface) dgi.getNode(n.getName()))) {
									DirectedEdgeInterface edge = dgi.createEdge(null,
											(DirectedNodeInterface) dgi.getNode(c.getName()),
											(DirectedNodeInterface) dgi.getNode(n.getName()));
									dgi.addEdge(edge);
								}
								System.out.println("Interface: " + c.getName());
							}
						}
						super.visit(n, arg);
					}
				}.visit(JavaParser.parse(file), null);
				System.out.println();
			} catch (ParseException |

					IOException e) {
				new RuntimeException(e);
			}
		}).explore(projectDir);
		return dgi;
	}
}
