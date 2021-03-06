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
package de.fau.osr.app;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.fau.osr.core.db.CSVFileDataSource;
import de.fau.osr.core.db.DataSource;
import de.fau.osr.core.vcs.base.VcsEnvironment;
import de.fau.osr.core.vcs.interfaces.VcsClient;
import de.fau.osr.util.AppProperties;
import de.fau.osr.util.parser.CommitMessageParser;
import de.fau.osr.util.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * @author Taleh Didover
 *
 * This is an application to fulfill the acceptance criterion for Req-13
 */
public class PostTraceabilityApp {

    private static String storageFileName = "req--commit.csv";

    private static class CliOptions {
        @Parameter(names = "-repo", required = true)
        String repoURL;
    }

    private static String readReqID(BufferedReader br) throws IOException {
        Integer reqID = null;
        boolean retry = true;

        while (retry) {

            try {
                System.out.print("Enter 1 requirement id: Req-");
                reqID = Integer.valueOf(br.readLine());
                retry = false;
            } catch (NumberFormatException err) {
                System.out.println("Input is invalid! Just enter an integer.");
            }
        }

        return String.valueOf(Math.abs(reqID));
    }

    private static String readCommitID(BufferedReader br) throws IOException {
        String commitID = null;
        boolean retry = true;

        while (retry) {
                System.out.print("Enter 1 commit id: ");
                commitID = br.readLine().trim().toLowerCase();
                if (!commitID.isEmpty())
                    retry = false;
        }

        return commitID;
    }

    public static void main(String[] args) {
        CliOptions cli = new CliOptions();
        new JCommander(cli, args);
        VcsClient client = VcsClient.connect(VcsEnvironment.GIT, cli.repoURL);
        Parser parser = new CommitMessageParser(Pattern.compile(AppProperties.GetValue("RequirementPattern")));

        Path repoFilePath = Paths.get(cli.repoURL);


        Path storageFilePath = repoFilePath.getParent().resolve(storageFileName);
        DataSource storage = null;

        try {
            storage = new CSVFileDataSource(storageFilePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.format("Will store post traceability datas at '%s'%n", storageFilePath.toString());
        System.out.format("CTRL-C to exit!%n%n");


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean isRunning = true;

        while (isRunning) {
            String reqID = null;
            String commitID = null;

            try {
                reqID = readReqID(br);
            } catch (IOException ioe) {
                System.out.println("ERROR: An I/O error occured when reading Requirement ID.");
                System.exit(1);
            }

            try {
                commitID = readCommitID(br);
            } catch (IOException ioe) {
                System.out.println("ERROR: An I/O error occured during reading commit id.");
                System.exit(1);
            }

            try {
                assert storage != null;
                storage.addReqCommitRelation(reqID, commitID);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
