package de.uni_jena.iaa.linktype.atomic.atomical.console;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.corpus_tools.atomic.api.editors.DocumentGraphEditor;
import org.corpus_tools.atomic.api.editors.SaltGraphUpdatable;
import org.corpus_tools.atomic.console.ConsoleCommandBaseListener;
import org.corpus_tools.atomic.console.ConsoleCommandParser;
import org.corpus_tools.atomic.console.ConsoleCommandParser.Anno_argsContext;
import org.corpus_tools.atomic.console.ConsoleCommandParser.AnnotateCommandContext;
import org.corpus_tools.atomic.console.ConsoleCommandParser.HelpCommandContext;
import org.corpus_tools.atomic.console.ConsoleCommandParser.NewSpanNodeCommandContext;
import org.corpus_tools.atomic.console.ConsoleCommandParser.NewStructureNodeCommandContext;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.eclipse.emf.common.util.URI;

class CommandExecutor extends ConsoleCommandBaseListener {

	/**
	 * 
	 */
	private final AtomicalConsole atomicalConsole;

	/**
	 * @param atomicalConsole
	 */
	CommandExecutor(AtomicalConsole atomicalConsole) {
		this.atomicalConsole = atomicalConsole;
	}

	private DocumentGraphEditor editor;
	private SDocumentGraph graph;
	
	private void out(String msg) {
		try {
			this.atomicalConsole.out.write(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean checkValidEditor() {
		if (editor != null && graph != null) {					
			return true;
		} else {
			out("No active editor. Command will be ignored.\n");
		}

		return false;
	}
	
	private void updateEditor() {
		if(graph != null && editor instanceof SaltGraphUpdatable) {
			((SaltGraphUpdatable) editor).updateSDocumentGraph(graph);
		}
	}
	
	private SAnnotation createAnno(Anno_argsContext args, SNode node) {
		if(node != null && args != null) {
			String ns = args.anno.ns == null ? null : args.anno.ns.getText();
			String name = args.anno.name.getText();
			String value = args.value.getText();
			
			return node.createAnnotation(ns, name, value);
		}
		return null;
	}
	
	private SNode getNodeByID(String id) {
		URI nodeURI = graph.getPath().appendFragment(id);
		return graph.getNode(nodeURI.toString());
	}
	
	@Override
	public void enterCommanChain(ConsoleCommandParser.CommanChainContext ctx) {
		editor = atomicalConsole.getEditor();
		graph = atomicalConsole.getGraph();

	}

	@Override
	public void enterHelpCommand(HelpCommandContext ctx) {
		atomicalConsole.displayHelp();
	}

	@Override
	public void enterClearCommand(ConsoleCommandParser.ClearCommandContext ctx) {
		atomicalConsole.clearConsole();
	}
	

	@Override
	public void enterAnnotateCommand(AnnotateCommandContext ctx) {
		if(checkValidEditor()) {
			List<SNode> elementsToAnnotate = new LinkedList<>();
			
			for(Token element : ctx.elements) {
				SNode newNode = getNodeByID(element.getText());
				if(newNode != null) {
					elementsToAnnotate.add(newNode);
				}
			}
			
			// get the annotation information
			String ns = ctx.anno.ns == null ? null : ctx.anno.ns.getText();
			String name = ctx.anno.name.getText();

			if(ns == null && name != null) {
				// find first matching namespace of existing annotation
				ns = "atomic";
				eachElement:
				for(SNode n : elementsToAnnotate) {
					for(SAnnotation anno : n.getAnnotations()) {
						if(name.equals(anno.getName())) {
							ns = anno.getNamespace();
							break eachElement;
						}
					}
				}
			}
			String value = ctx.value == null ? null : ctx.value.getText();
			
			
			// add the annotation to each node in the list
			for(SNode n : elementsToAnnotate) {
				SAnnotation existing = n.getAnnotation(ns, name);
				if(existing == null && value != null) {
					n.createAnnotation(ns, name, value);
				} else {
					if(value == null) {
						// delete the annotation
						n.removeLabel(ns, name);
					} else {
						// change annotation value
						existing.setValue(value);
					}
				}
			}
			updateEditor();
		}
	}
	
	@Override
	public void enterNewStructureNodeCommand(NewStructureNodeCommandContext ctx) {
		if(checkValidEditor()) {
			List<SStructuredNode> children = new LinkedList<>();
			
			for(Token e : ctx.elements) {
				SNode c = getNodeByID(e.getText());
				if(c != null && c instanceof SStructuredNode) {
					children.add((SStructuredNode) c);
				}
			}
			
			SStructure newNode = graph.createStructure(children);
			createAnno(ctx.anno_args(), newNode);
			updateEditor();
		}
	}
	
	@Override
	public void enterNewSpanNodeCommand(NewSpanNodeCommandContext ctx) {
		if(checkValidEditor()) {
			List<SToken> children = new LinkedList<>();
			
			for(Token e : ctx.elements) {
				SNode c = getNodeByID(e.getText());
				if(c != null && c instanceof SToken) {
					children.add((SToken) c);
				}
			}
			
			SSpan newNode = graph.createSpan(children);
			createAnno(ctx.anno_args(), newNode);
			updateEditor();
		}
	}

}