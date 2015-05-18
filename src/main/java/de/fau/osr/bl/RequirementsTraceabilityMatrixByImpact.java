package de.fau.osr.bl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fau.osr.core.vcs.base.CommitFile;
/**
 * @author Gayathery Sathya
 * @desc This class represents a traceability matrix for requirements based on impact
 */
public class RequirementsTraceabilityMatrixByImpact {

	List<String> requirements;
	List<String> files;
	Tracker tracker;
	Map<RequirementFilePair,RequirementFileImpactValue> requirementTraceabilityByImpactMatrix = new HashMap<RequirementFilePair, RequirementFileImpactValue>();
	public RequirementsTraceabilityMatrixByImpact(Tracker tracker){
		this.tracker = tracker;
		requirements = new ArrayList<String>();
		files = new ArrayList<String>();
	}
	public void Process(){
		try {
			requirements.addAll(tracker.getAllRequirements());
			files.addAll(tracker.getAllFilesAsString());
			for(String requirement : requirements){
				Collection<CommitFile> requirementCommitFiles = tracker.getCommitFilesForRequirementID(requirement);
				for(CommitFile requirementCommitFile : requirementCommitFiles){
					requirementTraceabilityByImpactMatrix.put(new RequirementFilePair(requirement,requirementCommitFile.newPath.getPath()), new RequirementFileImpactValue((float)requirementCommitFile.impact));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void ProcessAlternate(){
		try {
			requirements.addAll(tracker.getAllRequirements());
			files.addAll(tracker.getAllFilesAsString());
			for(String requirement: requirements){
				System.out.println(requirement);
				for(String file : files){
					if(!tracker.getAllRequirementsForFile(file).contains(requirement)){
						System.out.println("C:" + file);
						continue;
					}
					System.out.println("P:" + file);
					requirementTraceabilityByImpactMatrix.put(new RequirementFilePair(requirement,file), new RequirementFileImpactValue(tracker.getImpactPercentageForFileAndRequirement(file, requirement)));
					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public List<String> getRequirements(){
		return requirements;
	}
	
	public List<String> getFiles(){
		return files;
	}
	public RequirementFileImpactValue getImpactValue(RequirementFilePair reqFilePair){
		return requirementTraceabilityByImpactMatrix.get(reqFilePair);
	}
	
	
}