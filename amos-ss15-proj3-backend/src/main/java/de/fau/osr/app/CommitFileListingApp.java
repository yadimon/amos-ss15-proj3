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
import de.fau.osr.core.vcs.base.CommitFile;
import de.fau.osr.core.vcs.base.VcsEnvironment;
import de.fau.osr.core.vcs.interfaces.VcsClient;

import java.util.Iterator;

/**
 * @author tobias
 * This is an application to fulfill the acceptance criterion for Req-5
 */
public class CommitFileListingApp {
    private static class CliOptions {
        @Parameter(names = "-repo", required = true)
        String repoURL;
        @Parameter(names = "-commit", required = true)
        String commitId;
    }
    public static void main(String[] args) {
        CliOptions cli = new CliOptions();
        new JCommander(cli, args);
        final VcsClient client = VcsClient.connect(VcsEnvironment.GIT, cli.repoURL);
        final String commitId = cli.commitId;
        for(CommitFile file : new Iterable<CommitFile>() {

            @Override
            public Iterator<CommitFile> iterator() {
                return client.getCommitFiles(commitId).get().iterator();
            }}) {
            System.out.println(file.oldPath + " " + file.commitState + " " + file.newPath);
        }
    }
}
