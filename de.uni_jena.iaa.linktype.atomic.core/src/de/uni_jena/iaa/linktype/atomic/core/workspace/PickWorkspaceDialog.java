/*******************************************************************************
 * Copyright 2013 Friedrich Schiller University Jena
 * stephan.druskat@uni-jena.de
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package de.uni_jena.iaa.linktype.atomic.core.workspace;


/**
 * @author Stephan Druskat
 * @deprecated by de.uni_jena.iaa.linktype.atomic.workspace.PickWorkspaceDialog in Workspace bundle
 */
public class PickWorkspaceDialog extends de.uni_jena.iaa.linktype.atomic.workspace.PickWorkspaceDialog {

	    /**
	     * Creates a new workspace dialog with a specific image as title-area image.
	     * 
	     * @param switchWorkspace true if we're using this dialog as a switch workspace dialog
	     * @param wizardImage Image to show
	     */
	    public PickWorkspaceDialog(boolean switchWorkspace/*, Image wizardImage*/) {
	        super(switchWorkspace);
	    }


}
