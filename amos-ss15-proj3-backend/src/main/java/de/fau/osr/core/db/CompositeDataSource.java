package de.fau.osr.core.db;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This <tt>DataSource</tt> uses multiple <tt>DataSource</tt>'s
 * Created by Dmitry Gorelenkov on 13.05.2015.
 */
public class CompositeDataSource extends DataSource {

    private DataSource dataSource;
    private ArrayList<DataSource> dataSources;

    /**
     * <tt>DataSource</tt>, that uses many <tt>DataSource</tt>'s for query data, but only one for update
     * @param mainSource source to use modifying methods like addSource()
     * @param otherSources other source, to query information
     */
    public CompositeDataSource(DataSource mainSource, DataSource ...otherSources ) {
        dataSources = new ArrayList<>();
        dataSource = mainSource;
        addSource(mainSource);
        addSources(otherSources);
    }

    public void addSource(DataSource ds){
        dataSources.add(ds);
    }

    public void addSources(DataSource ...manySources){
        dataSources.addAll(Arrays.asList(manySources));
    }


    @Override
    protected void doAddReqCommitRelation(String reqId, String commitId) throws IOException, OperationNotSupportedException {
        dataSource.addReqCommitRelation(reqId, commitId);
    }

    @Override
    protected void doRemoveReqCommitRelation(String reqId, String commitId) throws IOException, OperationNotSupportedException {
        dataSource.removeReqCommitRelation(reqId, commitId);
    }

    @Override
    protected SetMultimap<String, String> doGetAllReqCommitRelations() throws IOException {
        SetMultimap<String, String> result = HashMultimap.create();
        for (DataSource ds : dataSources){
            result.putAll(ds.getAllReqCommitRelations());
        }

        return result;
    }
}