/*
 * This file is part of ReqTracker.
 *
 * Copyright (C) 2015 Taleh Didover, Florian Gerdes, Dmitry Gorelenkov,
 *     Rajab Hassan Kaoneka, Katsiaryna Krauchanka, Tobias Polzer,
 *     Gayathery Sathya, Lukas Tajak
 *
 * ReqTracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReqTracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with ReqTracker.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fau.osr;

import de.fau.osr.core.vcs.base.Commit;
import de.fau.osr.core.vcs.base.CommitFile;
import de.fau.osr.core.vcs.base.CommitState;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This Class handles all Testdata. If u want to get any Path related to a Test Repository or any other Test file,
 * you might want to use this class. Beside providing access to the TestData via Paths, the functions contained in @category Repository Data
 * can be used to get immediate access to the TestData itself.
 * @author Florian Gerdes
 * @see <a href="URL#https://github.com/uv48uson/amos-ss15-proj3/wiki/3.-Testing">Test data</a>
 */
public class PublicTestData {

    /**
     * @category Path Management
     */
    private static final String CSV_FILE_PATH = "/TestRepositoryData.csv";
    private static final String LOCAL_TEST_REPOSITORY_URI = "/TestRepository/git";

    public static String getGitTestRepo() {
        URL url = PublicTestData.class.getResource(LOCAL_TEST_REPOSITORY_URI);
        //System.err.println(url);
        File file;
        try {
          file = new File(url.toURI());
        } catch(URISyntaxException e) {
          file = new File(url.getPath());
        }

        return file.getAbsolutePath();
    }

    public static String getTestRepoCsv() {
        return CSV_FILE_PATH;
    }

    public static String getSampleReqID(){
        return "1";
    }

    public static String getSampleFilePathFromTestRepository(){
        return "TestFile4";
    }

    /**
     * @category Repository Data
     */
    private List<Commit> commits  = new ArrayList<Commit>();

    public PublicTestData(){
        commits = new ArrayList<Commit>();
        parseCsvFile();
    }

    public List<Commit> getCommits(){
        return commits;
    }



    /**
     * @return All Commits containing in the CSVFile, which at least have one requirements linked in the message
     */
    public List<Commit> getCommitsWithReqIds() {
        List<Commit> commitsWithReqIds = new ArrayList<Commit>();
        for(Commit commit: commits){
            if(commit.getRequirements().size() > 0){
                commitsWithReqIds.add(commit);
            }
        }
        return commitsWithReqIds;
    }


    /**
     * @category Internal
     */
    private void parseCsvFile(){
        BufferedReader br = null;
        String csvSplitStringsBy = ";";
        String csvSplitEntriesBy = ",";

        try {
            InputStream inputStream = PublicTestData.class.getResourceAsStream(CSV_FILE_PATH);
            br = new BufferedReader(new InputStreamReader(inputStream));

            String line = "";
            while ((line = br.readLine()) != null) {
                String[] commitString = line.split(csvSplitStringsBy);

                Set<String> requirements = new HashSet<>();
                String [] requirementStrings = commitString[2].split(csvSplitEntriesBy);
                for(String str: requirementStrings){
                    if(!str.equals("")){
                        requirements.add(str);
                    }
                }


                List<CommitFile> files = new ArrayList<CommitFile>();
                if(commitString.length >= 4){
                    String [] filesStrings = commitString[3].split(csvSplitEntriesBy);
                    for(int i = 0; i<filesStrings.length; i = i+3){
                        files.add(new CommitFile(new File(getGitTestRepo()).getParentFile(), new File(filesStrings[i]), new File(filesStrings[i+2]), CommitState.valueOf(filesStrings[i+1]),null,""));
                    }
                }

                Commit commit = new Commit(commitString[0], commitString[1], requirements, ()->files.stream());
                commits.add(commit);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
