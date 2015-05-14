package de.fau.osr.gui;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.regex.PatternSyntaxException;

import javax.swing.JList;

import org.eclipse.jgit.api.errors.GitAPIException;

import de.fau.osr.core.db.CSVFileDataSource;
import de.fau.osr.core.db.DataSource;
import de.fau.osr.core.vcs.base.CommitFile;
import de.fau.osr.core.vcs.impl.GitVcsClient;
import de.fau.osr.core.vcs.interfaces.VcsClient;
import de.fau.osr.gui.GuiView.HighlightedLine;
import de.fau.osr.gui.GuiViewElementHandler.ButtonState;
import de.fau.osr.util.AppProperties;


/*
 * Using a MVC-Pattern for the GUI.
 * The Controller class fetches the data from the Modell and passes it to the View.
 * Additional it eventually sets Events to the GUI-Elements, so that an action can call
 * the appropriate controller method, within which the data is fetched again ...
 */
public class GuiController {
	//Whether the user gets another chance to input correct data
	enum RetryStatus{Retry, Exit, Cancel}
	private static final int MAX_RETRIES = 3;
	private RetryStatus Status;
	
	GuiView guiView;
	GuiModel guiModel;

	JList<String> requirements_JList;
	JList<String> commitMessages_JList;
	JList<String> commitFileName_JList;
	JList<HighlightedLine> code_JList;

	// sorting algorithm for commitFileName_JList
	Comparator<CommitFile> commitFileSorting;
	

	/*
	 * Called to start the initially starts the program. Setting up GUI and displaying the initial data:
	 * All Requirements from external tool/DB(jira...)
	 */
	public GuiController(){	
		Status = RetryStatus.Retry;

		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				
				guiView = new GuiView();
				
				for(int i = 0; true; i++){
					File repoFile = null;
					try {
						repoFile = guiView.Repo_OpeningDialog();
					} catch (IOException e1) {
						System.exit(0);
					}
					String reqPatternString = guiView.Pattern_OpeningDialog(AppProperties.GetValue("RequirementPattern"));
					try {
						guiModel = reInitModel(null, null, repoFile, reqPatternString);
						break;
					} catch (PatternSyntaxException | IOException e) {
						if(i >= MAX_RETRIES){
							Status = RetryStatus.Exit;
						}
						guiView.showErrorDialog(e.getMessage());
						handleError();
					}
				}
				
				guiView.showView();
				initializeButtonActions();
				try {
					requirementsFromDB();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Comparator<CommitFile> getCommitFileSorting() {
		return commitFileSorting;
	}

	public void setCommitFileSorting(Comparator<CommitFile> commitFileSorting) {
		this.commitFileSorting = commitFileSorting;
		// TODO we need a refresh method
		System.out.format("Commit file sorting selected: %s%n", commitFileSorting);
	}

	/*
	 * Every Event should cause a method in this controller to run. Therefore,
	 * whereever a action is defined, the Controller must be available. This 
	 * function passes the controller-element to the ElementHandler
	 */
	protected void initializeButtonActions() {
		guiView.initializeButtonActions(this);
	}

	
	
	/*
	 * Navigation: ->Requirements
	 * Clear: All
	 * Setting: Requirements
	 * Using: getAllRequirements
	 */
	void requirementsFromDB() throws IOException {
		guiView.clearAll();
		
		String[] requirements = guiModel.getAllRequirements();
		requirements_JList = new JList<String>(requirements);
		guiView.showRequirements(requirements_JList);
		
		guiView.addMouseListener(requirements_JList, new MouseEvent(this, Action.CommitsAndFilesFromRequirement));
	}
	
	/*
	 * Navigation: ->Requirements->Commit
	 * Clear: Files/Code/ImpactPercentage
	 * Setting: Commits
	 * Using: getCommitsFromRequirementID
	 */
	void commitsFromRequirement(String requirement) throws IOException {
		guiView.clearFiles();
		guiView.clearCode();
		guiView.clearImpactPercentage();
		
		commitMessages_JList = new JList<String>(guiModel.getCommitsFromRequirementID(requirement));
		guiView.showCommits(commitMessages_JList);
		
		guiView.addMouseListener(commitMessages_JList, new MouseEvent(this, Action.FilesFromCommit));
	}

	/*
	 * Navigation: ->Requirements->File
	 * Clear: Code/ImpactPercentage
	 * Setting: Files
	 * Using: getFilesFromRequirement
	 */
	void filesFromRequirement(String requirementID) throws IOException {
		guiView.clearCode();
		guiView.clearImpactPercentage();

		commitFileName_JList = new JList<String>(guiModel.getFilesFromRequirement(requirementID, commitFileSorting));
		guiView.showFiles(commitFileName_JList);
		
		guiView.addMouseListener(commitFileName_JList, new MouseEvent(this, Action.CommitsAndCodeFromRequirementAndFile));
		
	}
	
	/*
	 * Navigation: ->Requirements->File->Commit
	 * Clear: 
	 * Setting: Commits
	 * Using: commitsFromRequirementAndFile
	 */
	void commitsFromRequirementAndFile(String requirementID, int fileIndex) {
		try {
			commitMessages_JList = new JList<String>(guiModel.commitsFromRequirementAndFile(requirementID, fileIndex));
		} catch (IOException e) {
			guiView.showErrorDialog("Internal storing Error");
			return;
		}
		guiView.showCommits(commitMessages_JList);
	}
	
	


	
	/*
	 * Navigation: ->Files->Code
	 * Clear: ImpactPercentage
	 * Setting: Code
	 * Using: getChangeDataFromFileIndex
	 */
	void codeFromFile(int filesIndex, String requirementID) {
		guiView.clearImpactPercentage();
		
		try {
			code_JList = new JList<HighlightedLine>(guiModel.getBlame(filesIndex, requirementID));
		}catch(FileNotFoundException e){
			guiView.showInformationDialog("Can only be displayed if file is up-to-date!");
			return;
		} catch (IOException | GitAPIException e) {
			guiView.showErrorDialog("Internal storing Error" + e);
			return;
		}
		
		guiView.showCode(code_JList);
		
		guiView.addMouseListener(code_JList, new MouseEvent(this, Action.RequirmentsFromCode));
	}
	
	/**
	 * Navigation: ->File->Code->Requirements
	 * Clear: Commits
	 * Setting: Requirements
	 * Using: getRequirementsForBlame
	 * @param filesIndex
	 * @param codeIndex
	 */
	void requirementsFromCode(int filesIndex, int codeIndex){
		guiView.clearCommits();
		
		try{
			requirements_JList = new JList<String>(guiModel.getRequirementsForBlame(codeIndex, filesIndex));
		} catch (IOException | GitAPIException e) {
			guiView.showErrorDialog("Internal storing Error" + e);
			return;
		}
		
		guiView.showRequirements(requirements_JList);
	}

	/*
	 * Navigation: ->Files
	 * Clear: All
	 * Setting: Files
	 * Using: getAllFiles
	 */
	void filesFromDB() {
		guiView.clearAll();
		
		commitFileName_JList = new JList<String>(guiModel.getAllFiles(getCommitFileSorting()));
		guiView.showFiles(commitFileName_JList);
		
		guiView.addMouseListener(commitFileName_JList, new MouseEvent(this, Action.RequirementsAndCommitsFromFile));
	}

	/*
	 * Navigation: ->File->Requirement
	 * Clear: 
	 * Setting: Requirement
	 * Using: getRequirementsFromFile
	 */
	void requirementsFromFile(String filePath) throws IOException {
		requirements_JList = new JList<String>(guiModel.getRequirementsFromFile(filePath));
		guiView.showRequirements(requirements_JList);
		
		guiView.addMouseListener(requirements_JList, new MouseEvent(this, Action.CommitsFromRequirementAndFile));
	}
	
	/*
	 * Navigation: ->File->Commit
	 * Clear: 
	 * Setting: Commits
	 * Using: getCommitsFromFile
	 */
	void commitsFromFile(String filePath){
		commitMessages_JList = new JList<String>(guiModel.getCommitsFromFile(filePath));
		guiView.showCommits(commitMessages_JList);
		
		guiView.addMouseListener(commitMessages_JList, new MouseEvent(this, Action.RequirementsFromFileAndCommit));
	}

	/*
	 * Navigation: ->File->Requirement->Commit
	 * Clear: 
	 * Setting: Commits
	 * Using: commitsFromRequirementAndFile
	 */
	void commitsFromRequirementAndFile(String requirementID, String filePath) throws IOException {
		commitMessages_JList = new JList<String>(guiModel.commitsFromRequirementAndFile(requirementID, filePath));
		guiView.showCommits(commitMessages_JList);
	}

	/*
	 * Navigation: ->Files->Commit->Requirement
	 * Clear: 
	 * Setting: Requirement
	 * Using: getRequirementsFromFileAndCommit
	 */
	void requirementsFromFileAndCommit(int commitIndex, String filePath) {
		try {
			requirements_JList = new JList<String>(guiModel.getRequirementsFromFileAndCommit(commitIndex, filePath));
		} catch (FileNotFoundException e) {
			guiView.showErrorDialog("Internal storing Error");
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		guiView.showRequirements(requirements_JList);
		
	}

	
	
	
	/*
	 * Navigation: ->Commits
	 * Clear: All
	 * Setting: Commits
	 * Using: getCommits
	 */
	void commitsFromDB() {
		guiView.clearAll();
		
		commitMessages_JList = new JList<String>(guiModel.getCommitsFromDB());
		guiView.showCommits(commitMessages_JList);
		
		guiView.addMouseListener(commitMessages_JList, new MouseEvent(this, Action.RequirementsAndFilesFromCommit));
	}
	
	/*
	 * Navigation: ->Commit->Files
	 * Clear: Code/ImpactPercentage
	 * Setting: Files
	 * Using: getFilesFromCommit
	 */
	void filesFromCommit(int commitIndex) {
		guiView.clearCode();
		guiView.clearImpactPercentage();

		try {
			commitFileName_JList = new JList<String>(guiModel.getFilesFromCommit(commitIndex, commitFileSorting));
		} catch (FileNotFoundException e) {
			guiView.showErrorDialog("Internal storing Error");
			return;
		}
		guiView.showFiles(commitFileName_JList);
		
		guiView.addMouseListener(commitFileName_JList, new MouseEvent(this, Action.CodeFromFile));
	}

	/*
	 * Navigation: ->Commits->Requirements
	 * Clear: 
	 * Setting: Requirements
	 * Using: getRequirementsFromCommit
	 */
	void requirementsFromCommit(int commitIndex) {
		try {
			requirements_JList = new JList<String>(guiModel.getRequirementsFromCommit(commitIndex));
		} catch (FileNotFoundException e) {
			guiView.showErrorDialog("Internal storing Error");
			return;
		}
		guiView.showRequirements(requirements_JList);
	}
	
	/*
	 * For button AddLinkage
	 */
	void requirementsAndCommitsFromDB() {
		guiView.clearAll();
		
		String[] requirements;
		try {
			requirements = guiModel.getAllRequirements();
		} catch (IOException e) {
			guiView.showErrorDialog("Internal storing Error");
			return;
		}
		requirements_JList = new JList<String>(requirements);
		guiView.showRequirements(requirements_JList);
		guiView.addMouseListener(requirements_JList, new MouseEvent(this, Action.RequirementToLinkage));
		
		commitMessages_JList = new JList<String>(guiModel.getCommitsFromDB());
		guiView.showCommits(commitMessages_JList);
		guiView.addMouseListener(commitMessages_JList, new MouseEvent(this, Action.CommitToLinkage));
		
		guiView.switchLinkage_Button(ButtonState.Activate);
	}

	void RequirementToLinkage(String requirementID) {
		guiView.showLinkageRequirement(requirementID);
	}

	void CommitToLinkage(String commit) {
		guiView.showLinkageCommit(commit);
	}

	
	/*
	 * For now only terminated the application if the user retried some input to often.
	 * Later on should handle all actions that have to be completed before exit.
	 */
	void handleError(){
		if(Status == RetryStatus.Exit){
			System.exit(1);
		}
	}
	
	/*
	 *For reconfiguring the repository to a new path while the application is running
	 *Once this method is successful, the application refers to the new repository 
	 */
	
	void reConfigureRepository() throws IOException {
		GuiModel guiModelTrial = guiModel;
		for(int i = 0; i<=MAX_RETRIES; i++){
			if(i == MAX_RETRIES){
				Status = RetryStatus.Cancel;
				guiView.showErrorDialog("Maximum retries exceeded");
				return;
			}
			File repoFile = null;
			try {
				repoFile = guiView.Repo_OpeningDialog();
			} catch (IOException e1) {
				
			}
			if(repoFile == null){
				Status = RetryStatus.Cancel;
				return;
			}
			try {
				guiModelTrial = reInitModel(null, null, repoFile, guiModel.getCurrentRequirementPatternString());
				guiView.showInformationDialog("Repository Path modified to " + repoFile.getPath());
				break;
			} catch (IOException | RuntimeException e) {
				
				guiView.showErrorDialog(e.getMessage());
				handleError();
			}
		}
		guiModel = guiModelTrial;
		requirementsFromDB();
	}
	/*
	 * For reconfiguring the requirement pattern to a new pattern while the application is running
	 * Once this method is successful, the application refers to the new requirement pattern 
	 */
	void reConfigureRequirementPattern() throws IOException {
		GuiModel guiModelTrial = guiModel;
		for(int i = 0; true; i++){
			if(i == MAX_RETRIES){
				Status = RetryStatus.Cancel;
				guiView.showErrorDialog("Maximum retries exceeded");
				return;
			}
			String reqPatternString = guiView.Pattern_OpeningDialog(guiModel.getCurrentRequirementPatternString());
			if(reqPatternString == null){
				Status = RetryStatus.Cancel;
				return;
			}
			try {
				guiModelTrial = reInitModel(null, null, new File(guiModel.getCurrentRepositoryPath()), reqPatternString);
				guiView.showInformationDialog("Requirement Pattern modified to " + reqPatternString);
				break;
			} catch (RuntimeException | IOException e) {				
				guiView.showErrorDialog(e.getMessage());
				handleError();
			}
		}
		guiModel = guiModelTrial;
		requirementsFromDB();
		
	}
	
	/*
	 * method to divert configuration calls
	 */
	void reConfigure() throws IOException {
		switch(guiView.Configure_OptionDialog())
		{
		// these values have to be replaced by some enums
		case 0:
			reConfigureRepository();
			break;
		case 1:
			reConfigureRequirementPattern();
			break;
		}
			
		
	}

    /**
     * initialize model again. If any arg is null, the default value will be used
     * @param vcs vcs client
     * @param ds data source
     * @param repoFile path to git repo
     * @param reqPatternString pattern to parse req id from commit messages
     * @return model to use
     */
    private GUITrackerToModelAdapter reInitModel(VcsClient vcs, DataSource ds, File repoFile, String reqPatternString) throws IOException {

        if (repoFile == null){
            repoFile = new File(AppProperties.GetValue("DefaultRepoPath"));
        }

        if (ds == null){
            ds = new CSVFileDataSource(new File(repoFile.getParentFile(), AppProperties.GetValue("DefaultPathToCSVFile")));
        }

        if (vcs == null){
            vcs = new GitVcsClient(repoFile.toString());
        }

        if (reqPatternString == null){
            reqPatternString = AppProperties.GetValue("RequirementPattern");
        }

        return new GUITrackerToModelAdapter(vcs, ds, repoFile, reqPatternString);
    }
    
    void addLinkage(String requirementID, int commitIndex) {
		try {
			guiModel.addRequirementCommitLinkage(requirementID, commitIndex);
			guiView.showInformationDialog("Successfully Added!");
		} catch (FileNotFoundException e) {
			guiView.showErrorDialog("Internal storing Error");
			return;
		}finally{
			guiView.clearAll();
		}
	}
}
