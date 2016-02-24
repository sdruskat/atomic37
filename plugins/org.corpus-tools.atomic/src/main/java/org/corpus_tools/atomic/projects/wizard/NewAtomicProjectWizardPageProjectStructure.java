/*******************************************************************************
 * Copyright 2016 Friedrich-Schiller-Universität Jena
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Stephan Druskat - initial API and implementation
 *******************************************************************************/
package org.corpus_tools.atomic.projects.wizard;

import java.util.HashSet;
import java.util.Set;

import org.corpus_tools.atomic.internal.projects.DefaultProjectData;
import org.corpus_tools.atomic.projects.Corpus;
import org.corpus_tools.atomic.projects.Document;
import org.corpus_tools.atomic.projects.ProjectNode;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.core.databinding.beans.BeanProperties;

/**
 * A wizard page for the user to construct the structure of a project.
 * <p>
 * 
 * @author Stephan Druskat <stephan.druskat@uni-jena.de>
 */
public class NewAtomicProjectWizardPageProjectStructure extends WizardPage {
	private DataBindingContext m_bindingContext;
	private Text corpusNameText;
	private Text addSubCorpusNameText;
	private Text addDocumentNameText;
	private Text documentNameText;
	private Text sourceTextText;
	private DefaultProjectData model;
	private Text projectNameText;
	private Corpus selectedCorpus;
	private Document selectedDocument;
	private Set<Control> corpusConstrols = new HashSet<>(), documentControls = new HashSet<>();
	private TreeViewer projectTreeViewer;

	/**
	 * Default constructor calling the constructor {@link #NewAtomicProjectWizardPageProjectStructure(String)} with the default page name.
	 */
	public NewAtomicProjectWizardPageProjectStructure() {
		super("Create the project structure");
		// setModel(new DefaultProjectData());
		setModel(dELETEMECreateModel());
		setTitle("Create the project structure");
		setDescription("Create the structure of the new project by adding corpora, subcorpora, and documents.");
		/*
		 * FIXME TODO: Add context-sensitive help to Atomic, the the "?" button will show in the wizard. Add the following description to a help "window" of sorts: Every corpus must have a name and can contain n (sub-) corpora and n
		 * documents. Every document must have a name and must contain one source text. Must include Eclipse Help plugin for this.
		 */
	}

	/**
	 * TODO: Description
	 *
	 * @return
	 */
	private DefaultProjectData dELETEMECreateModel() {
		DefaultProjectData data = new DefaultProjectData();
		data.setName("project");
		Corpus root = new Corpus();
		root.setName("root");
		Corpus c1 = new Corpus();
		c1.setName("c1");
		Document c1d1 = new Document();
		c1d1.setName("c1d1");
		c1d1.setSourceText("tc1d1");
		c1.addChild(c1d1);
		Document c1d2 = new Document();
		c1d2.setName("c1d2");
		c1d2.setSourceText("tc1d2");
		c1.addChild(c1d2);
		Corpus c2 = new Corpus();
		c2.setName("c2");
		Document c2d1 = new Document();
		c2d1.setName("c2d1");
		c2d1.setSourceText("tc2d1");
		c2.addChild(c2d1);
		Document c2d2 = new Document();
		c2d2.setName("c2d2");
		c2d2.setSourceText("tc2d2");
		c2.addChild(c2d2);
		root.addChild(c1);
		root.addChild(c2);
		data.addCorpus(root);
		return data;
	}

	/*
	 * @copydoc @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		// Calculate and set good size and position for dialog
		Monitor[] monitors = getShell().getDisplay().getMonitors();
		Monitor activeMonitor = null;

		Rectangle r = getShell().getBounds();
		for (int i = 0; i < monitors.length; i++) {
			if (monitors[i].getBounds().intersects(r)) {
				activeMonitor = monitors[i];
			}
		}
		Rectangle bounds = activeMonitor.getClientArea();
		int boundsWidth = bounds.width;
		int boundsHeight = bounds.height;
		Point size = getShell().computeSize((int) (boundsWidth * (80.0f / 100.0f)), (int) (boundsHeight * (80.0f / 100.0f)));

		int x = bounds.x + ((bounds.width - size.x) / 2);
		getShell().setSize(size);
		getShell().setLocation(x, 0);

		// Create controls
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		// Project name
		Group projectGroup = new Group(container, SWT.NONE);
		projectGroup.setText("Project");
		projectGroup.setLayout(new GridLayout(2, false));
		projectGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label lblName_2 = new Label(projectGroup, SWT.NONE);
		lblName_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName_2.setText("Name:");

		projectNameText = new Text(projectGroup, SWT.BORDER);
		projectNameText.setText("New project");
		projectNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// Project contents
		SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
		sashForm.setLocation(0, 0);
		GridData gridDataSashForm = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gridDataSashForm.horizontalAlignment = SWT.FILL;
		gridDataSashForm.verticalAlignment = SWT.FILL;
		sashForm.setLayoutData(gridDataSashForm);

		Composite leftComposite = new Composite(sashForm, SWT.NONE);
		leftComposite.setLayout(new GridLayout(2, false));

		Button btnNewCorpus = new Button(leftComposite, SWT.NONE);
		btnNewCorpus.setText("New corpus");

		Button btnRemoveElement = new Button(leftComposite, SWT.NONE);
		btnRemoveElement.setText("Remove element");
		btnRemoveElement.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// System.err.println(getModel());
			}
		});

		projectTreeViewer = new TreeViewer(leftComposite, SWT.SINGLE);
		new Label(leftComposite, SWT.NONE);
		projectTreeViewer.setContentProvider(new ProjectTreeContentProvider());
		projectTreeViewer.setInput(getModel());
		projectTreeViewer.setLabelProvider(new ProjectTreeLabelProvider());
		projectTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		projectTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof TreeSelection) {
					TreeSelection selection = (TreeSelection) event.getSelection();
					if (selection.getFirstElement() instanceof ProjectNode) {
						ProjectNode node = (ProjectNode) selection.getFirstElement();
						if (node instanceof Corpus) {
							setSelectedCorpus((Corpus) node);
							setSelectedDocument(null);
							for (Control field : getDocumentControls()) {
								field.setEnabled(false);
							}
							for (Control field : getCorpusControls()) {
								field.setEnabled(true);
							}
							corpusNameText.setText(node.getName());
						}
						else if (node instanceof Document) {
							setSelectedDocument((Document) node);
							setSelectedCorpus(null);
							for (Control field : getCorpusControls()) {
								field.setEnabled(false);
							}
							for (Control field : getDocumentControls()) {
								field.setEnabled(true);
							}
							documentNameText.setText(node.getName());
							if (((Document) node).getSourceText() != null) {
								sourceTextText.setText(((Document) node).getSourceText());
							}
							
						}
					}
				}
			}
		});

		Composite rightComposite = new Composite(sashForm, SWT.NONE);
		rightComposite.setLayout(new GridLayout(1, false));

		Group grpCorpus = new Group(rightComposite, SWT.NONE);
		grpCorpus.setText("Corpus");
		grpCorpus.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		grpCorpus.setLayout(new GridLayout(3, false));

		Label lblName = new Label(grpCorpus, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name:");

		corpusNameText = new Text(grpCorpus, SWT.BORDER);
		corpusNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		getCorpusControls().add(corpusNameText);

		Button saveCorpusNameBtn = new Button(grpCorpus, SWT.NONE);
		saveCorpusNameBtn.setText("Save");
		getCorpusControls().add(saveCorpusNameBtn);
		saveCorpusNameBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				getSelectedCorpus().setName(corpusNameText.getText());
				projectTreeViewer.refresh();
			}
		});

		Label lblAddSubcorpus = new Label(grpCorpus, SWT.NONE);
		lblAddSubcorpus.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAddSubcorpus.setText("Add subcorpus:");

		addSubCorpusNameText = new Text(grpCorpus, SWT.BORDER);
		addSubCorpusNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		getCorpusControls().add(addSubCorpusNameText);

		Button addSubCorpusBtn = new Button(grpCorpus, SWT.NONE);
		addSubCorpusBtn.setText("Add");
		getCorpusControls().add(addSubCorpusBtn);
		addSubCorpusBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Corpus newSubCorpus = new Corpus();
				newSubCorpus.setName(addSubCorpusNameText.getText());
				getSelectedCorpus().addChild(newSubCorpus);
				projectTreeViewer.refresh();
				addSubCorpusNameText.setText("");
			}
		});

		Label lblAddDocument = new Label(grpCorpus, SWT.NONE);
		lblAddDocument.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAddDocument.setText("Add document:");

		addDocumentNameText = new Text(grpCorpus, SWT.BORDER);
		addDocumentNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		getCorpusControls().add(addDocumentNameText);

		Button addDocumentBtn = new Button(grpCorpus, SWT.NONE);
		addDocumentBtn.setText("Add");
		getCorpusControls().add(addDocumentBtn);
		addDocumentBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Document newDocument = new Document();
				newDocument.setName(addDocumentNameText.getText());
				getSelectedCorpus().addChild(newDocument);
				projectTreeViewer.refresh();
				addDocumentNameText.setText("");
			}
		});

		Group grpDocument = new Group(rightComposite, SWT.NONE);
		grpDocument.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpDocument.setLayout(new GridLayout(3, false));
		grpDocument.setText("Document");

		Label lblName_1 = new Label(grpDocument, SWT.NONE);
		lblName_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName_1.setText("Name:");

		documentNameText = new Text(grpDocument, SWT.BORDER);
		documentNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		getDocumentControls().add(documentNameText);

		Button saveDocumentNameBtn = new Button(grpDocument, SWT.NONE);
		saveDocumentNameBtn.setText("Save");
		getDocumentControls().add(saveDocumentNameBtn);
		saveDocumentNameBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				getSelectedDocument().setName(documentNameText.getText());
				projectTreeViewer.refresh();
			}
		});

		Label lblSourceText = new Label(grpDocument, SWT.NONE);
		lblSourceText.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		lblSourceText.setText("Source text:");

		sourceTextText = new Text(grpDocument, SWT.BORDER | SWT.MULTI);
		sourceTextText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		getDocumentControls().add(sourceTextText);
		new Label(grpDocument, SWT.NONE);

		Button browseSourceTextBtn = new Button(grpDocument, SWT.NONE);
		browseSourceTextBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		browseSourceTextBtn.setText("Browse");
		getDocumentControls().add(browseSourceTextBtn);

		Button saveSourceTextBtn = new Button(grpDocument, SWT.NONE);
		saveSourceTextBtn.setText("Save");
		getDocumentControls().add(saveSourceTextBtn);
		saveSourceTextBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				getSelectedDocument().setSourceText(sourceTextText.getText());
			}
		});

		sashForm.setWeights(new int[] { 1, 1 });
		m_bindingContext = initDataBindings();
	}

	@Override
	public void performHelp() {
		Shell shell = new Shell(getShell());
		shell.setText("My Custom Help !!");
		shell.setLayout(new GridLayout());
		shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Browser browser = new Browser(shell, SWT.NONE);
		browser.setUrl("http://stackoverflow.com/questions/7322489/cant-put-content-behind-swt-wizard-help-button");
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		shell.open();
	}

	/**
	 * @return the model
	 */
	public DefaultProjectData getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(DefaultProjectData model) {
		this.model = model;
	}

	/**
	 * @return the selectedCorpus
	 */
	public Corpus getSelectedCorpus() {
		return selectedCorpus;
	}

	/**
	 * @param selectedCorpus the selectedCorpus to set
	 */
	public void setSelectedCorpus(Corpus selectedCorpus) {
		this.selectedCorpus = selectedCorpus;
	}

	/**
	 * @return the selectedDocument
	 */
	public Document getSelectedDocument() {
		return selectedDocument;
	}

	/**
	 * @param selectedDocument the selectedDocument to set
	 */
	public void setSelectedDocument(Document selectedDocument) {
		this.selectedDocument = selectedDocument;
	}

	/**
	 * @return the corpusConstrols
	 */
	private Set<Control> getCorpusControls() {
		return corpusConstrols;
	}

	/**
	 * @return the documentControls
	 */
	private Set<Control> getDocumentControls() {
		return documentControls;
	}

	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue observeTextTxtNewProjectObserveWidget = WidgetProperties.text(SWT.Modify).observe(projectNameText);
		IObservableValue nameModelObserveValue = BeanProperties.value("name").observe(model);
		bindingContext.bindValue(observeTextTxtNewProjectObserveWidget, nameModelObserveValue, null, null);
		//
		IObservableValue observeTextCorpusNameTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(corpusNameText);
		IObservableValue nameSelectedCorpusObserveValue = BeanProperties.value("name").observe(selectedCorpus);
		bindingContext.bindValue(observeTextCorpusNameTextObserveWidget, nameSelectedCorpusObserveValue, null, null);
		//
		IObservableValue observeTextDocumentNameTextObserveWidget = WidgetProperties.text(SWT.Modify).observe(documentNameText);
		IObservableValue nameSelectedDocumentObserveValue = BeanProperties.value("name").observe(selectedDocument);
		bindingContext.bindValue(observeTextDocumentNameTextObserveWidget, nameSelectedDocumentObserveValue, null, null);
		//
		return bindingContext;
	}
}
