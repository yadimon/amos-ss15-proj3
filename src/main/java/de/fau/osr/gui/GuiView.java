
package de.fau.osr.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.*;

import de.fau.osr.bl.RequirementsTraceabilityMatrix;
import de.fau.osr.bl.RequirementsTraceabilityMatrixByImpact;
import de.fau.osr.core.vcs.base.CommitFile;
import de.fau.osr.gui.GuiViewElementHandler.ButtonState;
import de.fau.osr.gui.util.SpiceTraceabilityProgressBar;

/*
 * View part of the MVC. This Class is responsible for the setting up the UI and interacting with the 
 * Elements. Whenever the texts or functionality of UI-Elements are changed, this class must be called.
 */
public class GuiView{
	public static class HighlightedLine{
		String line;
		boolean highlighted;
		public HighlightedLine(String line, boolean hightlighted){
			this.line = line;
			this.highlighted = hightlighted;
		}
	}

	
	private class HighlightedLine_Renderer implements ListCellRenderer<HighlightedLine>{
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		
		@Override
		public Component getListCellRendererComponent(
				JList<? extends HighlightedLine> list, HighlightedLine value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if(value.highlighted){
				renderer.setForeground(Color.RED);
			}
			renderer.setText(value.line);
			
			return renderer;
		}
	}


	/**
	 * Displays each CommitFile by file name, cell highlighting and impact percentage
	 */
	private class CommitFile_ImpactRenderer implements ListCellRenderer<CommitFile> {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		
		@Override
		public Component getListCellRendererComponent(
				JList<? extends CommitFile> list, CommitFile value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			switch(value.commitState){
				case MODIFIED:
					renderer.setBackground(Color.YELLOW);
					break;
				case ADDED:
					renderer.setBackground(Color.GREEN);
					break;
				case DELETED:
					renderer.setBackground(Color.RED);
					break;
				default:
					renderer.setBackground(Color.WHITE);
					break;
			}

			renderer.setText(String.format("%s - %f", value.newPath, value.impact));

			return renderer;
		}
	}

	/**
	 * Displays each CommitFile by just file name.
	 */
	private class CommitFile_SimpleFilenameRenderer implements ListCellRenderer<CommitFile> {
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent(
				JList<? extends CommitFile> list, CommitFile value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			renderer.setText(value.newPath.getName());

			return renderer;
		}
	}

	//The UI-Elements themselves are handled by the Element Handler. 
	private GuiViewElementHandler elementHandler;
	
	GuiView(GuiController guiController) {
		elementHandler = new GuiViewElementHandler(guiController);
	}
	
	/*
	 * Setting up the initial Dialog to choose the Repository.
	 * @return File to the chosen directory. Keep in mind, that the file is not checked yet.
	 * @throws IOException whenever the user cancels the Dialog.
	 * (This can not be solved)
	 */
	File Repo_OpeningDialog() throws IOException{
		JFileChooser chooser = new JFileChooser("..");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileHidingEnabled(false);

		int returnValue = chooser.showDialog(null,"Auswahl des Repository");
		
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}else {
			throw new IOException("Fehler bei er Repository Auswahl");
		}
	}
	
	/*
	 * Opening the initial Dialog to choose a Pattern for searching for requirements in commit messages
	 * or external sources.
	 * @return String containing the directly the user input. Not yet checked whether it's a proper pattern
	 */
	String Pattern_OpeningDialog(String currentPattern) {
		String msg = "Gebe \"Requirement-Pattern\" als RegExp ein.\n" +
				"Mehrere \"Requirement-Pattern\" müssen gemäß der RegExp-Syntax\n" +
				"mit | (Pipe) getrennt eingegeben werden.";
		return JOptionPane.showInputDialog(msg, currentPattern);
	}
	
	/*
	 * Method to open a option dialog for configuration where user can select which 
	 * configuration needs to be modified
	 * @return integer containing the option selected for configuration (TODO int to be converted to 
	 * a proper enum)
	 */
	int Configure_OptionDialog(){
		Object[] options = { "Change Repository", "Change Requirement Pattern"};
		return JOptionPane.showOptionDialog(null, "Choose to Configure", "SpiceTraceability Configuration", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
	}

	/*
	 * Creating the UI. Frame + Elements
	 */
	void showView() {
		elementHandler.setVisible(true);
	}

	/*
	 * Showing an Dialog to the User. Marked as Error dialog.
	 * @parameter message to be presented to the user
	 */
	void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(null, message, "Fehler", JOptionPane.ERROR_MESSAGE);
	}
	
	/*
	 * Showing a dialog to the user. Marked as Information dialog
	 * @parameter message to be presented to the user
	 */
	void showInformationDialog(String message) {
		JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	/*
	 * Clearing all scrollpanes. Containing the Code_ScrollPane. And clearing all Textfields.
	 * Deactivating Linkage_Button
	 * Color is set to the initial white.
	 */
	void clearAll(){
		clearRequirements();
		
		clearCommits();
		
		clearFiles();
		
		clearCode();

		clearImpactPercentage();
		
		elementHandler.getCommit_textField().setText("");
		elementHandler.getRequirementID_textField().setText("");
		
		switchLinkage_Button(ButtonState.Deactivate);
	}

	void clearImpactPercentage() {
		JPanel panelimpact = new JPanel(new GridLayout());
		panelimpact.setBackground(Color.WHITE);
		elementHandler.getImpactPercentage_scrollPane().setViewportView(panelimpact);
	}

	void clearFiles() {
		JPanel panelfiles = new JPanel(new GridLayout());
		panelfiles.setBackground(Color.WHITE);
		elementHandler.getFiles_scrollPane().setViewportView(panelfiles);
//  	elementHandler.getReqIdsByLines_scrollPane().setViewportView(panelfiles);
	}

	
	void clearRequirements() {
		JPanel panelrequirement = new JPanel(new GridLayout());
		panelrequirement.setBackground(Color.WHITE);
		elementHandler.getRequirementID_scrollPane().setViewportView(panelrequirement);
	}

	void clearCommits() {
		JPanel panelcommit = new JPanel(new GridLayout());
		panelcommit.setBackground(Color.WHITE);
		elementHandler.getCommit_scrollPane().setViewportView(panelcommit);
	}

	void clearCode() {
		JPanel panel = new JPanel(new GridLayout());
		panel.setBackground(Color.WHITE);
		elementHandler.getCode_scrollPane().setViewportView(panel);

		panel = new JPanel(new GridLayout());
//		panel.setBackground(UIManager.getColor("Panel.background"));
		panel.setBackground(UIManager.getColor(Color.WHITE));
		elementHandler.getRequirements2Lines_scrollPane().setViewportView(panel);
	}

	/*
	 * Showing all Elements of the JList parameter in the RequirementsID_Scrollpane
	 * @parameter requirements_JList containing the Elements to be displayed
	 */
	void showRequirements(JList<String> requirements_JList) {
		JPanel panel = new JPanel(new GridLayout());
		
		panel.add(requirements_JList);
		elementHandler.getRequirementID_scrollPane().setViewportView(panel);
		
	}

	/*
	 * Showing all Elements of the JList parameter in the Commit_Scrollpane
	 * @parameter commitMessage_JList containing the Elements to be displayed
	 */
	void showCommits(JList<String> commitMessages_JList) {
		JPanel panel = new JPanel(new GridLayout());
		
		panel.add(commitMessages_JList);
		elementHandler.getCommit_scrollPane().setViewportView(panel);
	}

	/*
	 * Showing all Elements of the JList parameter in the Files_Scrollpane
	 * WITH rendering.
	 * @parameter commitFile_JList containing the Elements to be displayed
	 */
	void showFiles(JList<CommitFile> commitFileName_JList) {
		showFilesByGivenRenderer(commitFileName_JList, new CommitFile_ImpactRenderer());
	}

	/*
	 * Showing all Elements of the JList parameter in the Files_Scrollpane
	 * WITHOUT rendering.
	 * @parameter commitFile_JList containing the Elements to be displayed
	 */
	void showFilesWithoutRendering(JList<CommitFile> commitFileName_JList) {
		showFilesByGivenRenderer(commitFileName_JList, new CommitFile_SimpleFilenameRenderer());
	}

	/**
	 * Will be called by public Methods *showFiles()* and *showFilesNoRendering*
	 * @author Taleh Didover
	 */
	private void showFilesByGivenRenderer(JList<CommitFile> commitFileName_JList, ListCellRenderer<CommitFile> renderer) {
		JPanel panel = new JPanel(new GridLayout());
        commitFileName_JList.setCellRenderer(renderer);
		panel.add(commitFileName_JList);
		elementHandler.getFiles_scrollPane().setViewportView(panel);
	}

	/*
	 * Showing the parameter String in the Code_Scrollpane.
	 * Until now. There is no further processing of the data. This will probably change
	 * with Req-11
	 * @parameter changeData String to be presented
	 */
	void showCode(JList<HighlightedLine> code_JList) {
		JPanel panel = new JPanel(new GridLayout());

		HighlightedLine_Renderer renderer = new HighlightedLine_Renderer();
		code_JList.setCellRenderer(renderer);

		panel.add(code_JList);
		elementHandler.getCode_scrollPane().setViewportView(panel);
	}

	/*
	 * Showing the parameter String in the Code_Scrollpane.
	 * Until now. There is no further processing of the data. This will probably change
	 * with Req-11
	 * @parameter changeData String to be presented
	 */
	void showRequirementIdsByLines(JList<String> requirements2Lines_JList) {
		JPanel panel = new JPanel(new GridLayout());
		panel.add(requirements2Lines_JList);
		elementHandler.getRequirements2Lines_scrollPane().setViewportView(panel);
	}

	/*
	 * Adding a Mouselistener to the passed component 
	 */
	void addMouseListener(JComponent component, MouseListener actListener){
		component.addMouseListener(actListener);
	}

	void showLinkageRequirement(String requirementID) {
		elementHandler.getRequirementID_textField().setText(requirementID);
	}
	
	void showLinkageCommit(String commit) {
		elementHandler.getCommit_textField().setText(commit);
	}

	void switchLinkage_Button(ButtonState buttonState){
		elementHandler.switchLinkageButton(buttonState);
	}
	
	/**
	 * shows the traceability matrix window with filled in data
	 * @param requirementsTraceabilityMatrix
	 */
	void showTraceabilityMatrix(RequirementsTraceabilityMatrix requirementsTraceabilityMatrix){
		try {
			TraceabilityMatrixViewHandler trMatrix = new TraceabilityMatrixViewHandler();
			trMatrix.setRequirementsTraceabilityMatrix(requirementsTraceabilityMatrix);
			trMatrix.initTable();
			trMatrix.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			trMatrix.setVisible(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * shows the traceability matrix (by impact) window with filled in data
	 * @param requirementsTraceabilityMatrixByImpact the matrix which contains the info for traceability
	 */
	void showTraceabilityMatrixByImpact(RequirementsTraceabilityMatrixByImpact requirementsTraceabilityMatrixByImpact){

			TraceabilityMatrixByImpactViewHandler trMatrixByImpact = new TraceabilityMatrixByImpactViewHandler();
			trMatrixByImpact.setRequirementsTraceabilityMatrix(requirementsTraceabilityMatrixByImpact);
			trMatrixByImpact.initTable();
			trMatrixByImpact.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			trMatrixByImpact.setVisible(true);

	}
	/*
	 * method to show progress bar for the processing of traceability matrix by impact values
	 */
	void showTraceabilityMatrixByImpactProgressBar(){
		
		final SpiceTraceabilityProgressBar progressBar = new SpiceTraceabilityProgressBar();
		progressBar.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		progressBar.setProgressBarContent("Generating Traceability Matrix (by Impact)");
		progressBar.setVisible(true);
		
		class showTraceabilityMatrixByImpactProgressBarThread implements Runnable{

			@Override
			public void run() {
				Boolean completion = false;
				while(RequirementsTraceabilityMatrixByImpact.processProgress<=100){
					progressBar.setProgressBarValue(RequirementsTraceabilityMatrixByImpact.processProgress);
					try {
						Thread.sleep(100);
					
						if(completion)
							break;
						if(RequirementsTraceabilityMatrixByImpact.processProgress == 100){
							completion = true;							
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
				}
				progressBar.dispatchEvent(new WindowEvent(progressBar, WindowEvent.WINDOW_CLOSING));
				
			}
			
		}
		Thread tr = new Thread(new showTraceabilityMatrixByImpactProgressBarThread());
        tr.start();
	}
}

