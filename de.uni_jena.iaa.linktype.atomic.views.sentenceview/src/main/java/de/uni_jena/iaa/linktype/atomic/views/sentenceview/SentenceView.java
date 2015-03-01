/**
 * 
 */
package de.uni_jena.iaa.linktype.atomic.views.sentenceview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.uni_jena.iaa.linktype.atomic.core.corpus.GraphService;
import de.uni_jena.iaa.linktype.atomic.core.model.ModelRegistry;

/**
 * @author Stephan Druskat
 * 
 */
public class SentenceView extends ViewPart implements ISelectionProvider, IPartListener2 {

	private ListenerList listeners = new ListenerList();

	private CheckboxTableViewer sentenceTableViewer;

	private SDocumentGraph graph;

	private ArrayList<SSpan> linkedSentences = new ArrayList<SSpan>();
	private HashMap<SSpan, SSpan> linkedSentencesForSentence = new HashMap<SSpan, SSpan>();
	private HashSet<SSpan> linkSourceSentences = new HashSet<SSpan>();

	@Override
	public void createPartControl(Composite parent) {
		graph = getInput();
		getSite().setSelectionProvider(this);
		final IWorkbenchWindow workbenchWindow = getSite().getWorkbenchWindow();
		workbenchWindow.getPartService().addPartListener(this);
		parent.setLayout(new GridLayout(1, true));

		addSelectionButtons(parent);

		setSentenceTableViewer(CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL));
		getSentenceTableViewer().getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		getSentenceTableViewer().setContentProvider(new SentenceContentProvider());
		getSentenceTableViewer().setLabelProvider(new SentenceLabelProvider(this));
		getSentenceTableViewer().setInput(getGraph());

		TableColumn column = new TableColumn(getSentenceTableViewer().getTable(), SWT.FILL);
		column.setText("Sentences");
		column.pack();

		getSentenceTableViewer().addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				getLinkSourceSentences().clear();
				getLinkedSentencesForSentence().clear();
				getLinkedSentences().clear();
				for (Object checkedElement : getSentenceTableViewer().getCheckedElements()) {
					HashSet<SSpan> linkedSentencesForCurrentElement = GraphService.getLinkedSentences((SSpan) checkedElement);
					if (!linkedSentencesForCurrentElement.isEmpty()) {
						getLinkSourceSentences().add((SSpan) checkedElement);
					}
					for (SSpan span : linkedSentencesForCurrentElement) {
						getLinkedSentencesForSentence().put(span, (SSpan) checkedElement);
					}
					getLinkedSentences().addAll(linkedSentencesForCurrentElement);
				}
				getSentenceTableViewer().refresh();
				notifySelectionListeners();
			}
		});

		getSentenceTableViewer().getTable().setHeaderVisible(true);
		getSentenceTableViewer().getTable().setLinesVisible(true);
	}

	/**
	 * @return
	 */
	private SDocumentGraph getInput() {
		IEditorInput editorInput = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
		if (editorInput instanceof FileEditorInput) {
			IFile file = ((FileEditorInput) editorInput).getFile();
			return ModelRegistry.getModel(file);
		}
		return null;
	}

	/**
	 * 
	 */
	private void notifySelectionListeners() {
		for (int i = 0; i < listeners.getListeners().length; i++) {
			((ISelectionChangedListener) listeners.getListeners()[i]).selectionChanged(new SelectionChangedEvent(SentenceView.this, new StructuredSelection(getSentenceTableViewer().getCheckedElements())));
		}
	}

	/**
	 * @param parent
	 */
	private void addSelectionButtons(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2, false));
		buttonComposite.setData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		Button selectButton = createButton(buttonComposite, "&Select all", GridData.HORIZONTAL_ALIGN_FILL);
		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (getGraph().getSTokens().size() > 500) {
					if (MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Show whole graph?", "WARNING: Rendering all sentences at once is an expensive operation, which may take long, and in some cases result in an application crash.\nDo you want to proceed?")) {
						getSentenceTableViewer().setAllChecked(true);
						getSentenceTableViewer().refresh();
						notifySelectionListeners();
					}
				}
				else {
					getSentenceTableViewer().setAllChecked(true);
					getSentenceTableViewer().refresh();
					notifySelectionListeners();
				}
			}
		};
		selectButton.addSelectionListener(listener);

		Button deselectButton = createButton(buttonComposite, "&Deselect all", GridData.HORIZONTAL_ALIGN_FILL);
		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getSentenceTableViewer().setAllChecked(false);
				getLinkedSentences().clear();
				getLinkedSentencesForSentence().clear();
				getLinkSourceSentences().clear();
				notifySelectionListeners();
				getSentenceTableViewer().refresh();

			}
		};
		deselectButton.addSelectionListener(listener);
	}

	/**
	 * @param buttonComposite
	 * @param string
	 * @param horizontalAlignFill
	 * @return
	 */
	private Button createButton(Composite parent, String label, int style) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		GridData data = new GridData(style);
		button.setLayoutData(data);
		return button;
	}

	@Override
	public void setFocus() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		return new StructuredSelection(getSentenceTableViewer().getCheckedElements());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener
	 * (org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse
	 * .jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		Object[] list = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((ISelectionChangedListener) list[i]).selectionChanged(new SelectionChangedEvent(this, new StructuredSelection(getSentenceTableViewer().getCheckedElements())));
		}
	}

	/**
	 * @return the graph
	 */
	public SDocumentGraph getGraph() {
		return graph;
	}

	/**
	 * @param graph
	 *            the graph to set
	 */
	public void setGraph(SDocumentGraph graph) {
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.
	 * IWorkbenchPartReference)
	 */
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof EditorPart) {
			EditorPart editor = (EditorPart) partRef.getPart(false);
			if (editor.getEditorInput() instanceof FileEditorInput) {
				FileEditorInput input = (FileEditorInput) editor.getEditorInput();
				if (input.getFile().getName().endsWith(SaltFactory.FILE_ENDING_SALT) && !input.getFile().getName().equals(SaltFactory.FILE_SALT_PROJECT)) {
					if (getSentenceTableViewer() != null && !getSentenceTableViewer().getControl().isDisposed()) {
						getSentenceTableViewer().setInput(getInput());
						getSentenceTableViewer().refresh();
					}
				}
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	/**
	 * @return the linkedSentences
	 */
	public ArrayList<SSpan> getLinkedSentences() {
		return linkedSentences;
	}

	/**
	 * @param linkedSentences
	 *            the linkedSentences to set
	 */
	public void setLinkedSentences(ArrayList<SSpan> linkedSentences) {
		this.linkedSentences = linkedSentences;
	}

	/**
	 * @return the linkedSentencesForSentence
	 */
	public HashMap<SSpan, SSpan> getLinkedSentencesForSentence() {
		return linkedSentencesForSentence;
	}

	/**
	 * @param linkedSentencesForSentence
	 *            the linkedSentencesForSentence to set
	 */
	public void setLinkedSentencesForSentence(HashMap<SSpan, SSpan> linkedSentencesForSentence) {
		this.linkedSentencesForSentence = linkedSentencesForSentence;
	}

	/**
	 * @return the sentenceTableViewer
	 */
	public CheckboxTableViewer getSentenceTableViewer() {
		return sentenceTableViewer;
	}

	/**
	 * @param sentenceTableViewer
	 *            the sentenceTableViewer to set
	 */
	public void setSentenceTableViewer(CheckboxTableViewer sentenceTableViewer) {
		this.sentenceTableViewer = sentenceTableViewer;
	}

	/**
	 * @return the linkSourceSentences
	 */
	public HashSet<SSpan> getLinkSourceSentences() {
		return linkSourceSentences;
	}

	/**
	 * @param linkSourceSentences the linkSourceSentences to set
	 */
	public void setLinkSourceSentences(HashSet<SSpan> linkSourceSentences) {
		this.linkSourceSentences = linkSourceSentences;
	}

}
