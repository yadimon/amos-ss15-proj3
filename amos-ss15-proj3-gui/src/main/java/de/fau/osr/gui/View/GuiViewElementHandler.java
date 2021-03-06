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
package de.fau.osr.gui.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitLayout.Split;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Divider;

import de.fau.osr.gui.Controller.GuiController;
import de.fau.osr.gui.View.ElementHandler.Code_ElementHandler;
import de.fau.osr.gui.View.ElementHandler.Commit_ElementHandler;
import de.fau.osr.gui.View.ElementHandler.Configuration_ElementHandler;
import de.fau.osr.gui.View.ElementHandler.ElementHandler;
import de.fau.osr.gui.View.ElementHandler.Impact_ElementHandler;
import de.fau.osr.gui.View.ElementHandler.Linkage_ElementHandler;
import de.fau.osr.gui.View.ElementHandler.MenuHandler;
import de.fau.osr.gui.View.ElementHandler.PathDE_ElementHandler;
import de.fau.osr.gui.View.ElementHandler.Requirement_Detail_ElementHandler;
import de.fau.osr.gui.View.ElementHandler.Requirement_ElementHandler;
import de.fau.osr.gui.util.filtering.FilterByExactString;

public class GuiViewElementHandler extends JFrame {
    

    private static final long serialVersionUID = 1L;
    private GuiController guiController;

    private MenuHandler Menu_Handler = new MenuHandler();
    private Requirement_ElementHandler Requirement_Handler = new Requirement_ElementHandler();
    private Commit_ElementHandler Commit_Handler = new Commit_ElementHandler();
    private PathDE_ElementHandler PathDE_Handler = new PathDE_ElementHandler();
    private Impact_ElementHandler Impact_Handler = new Impact_ElementHandler();
    private Code_ElementHandler Code_Handler = new Code_ElementHandler(Impact_Handler.getScrollPane());
    
    
    private Requirement_ElementHandler Requirement_HandlerRequirementTab = new Requirement_ElementHandler();
    private Requirement_Detail_ElementHandler Requirement_Detail_Handler = new Requirement_Detail_ElementHandler();
    
    private Requirement_ElementHandler Requirement_Handler_ManagementTab = new Requirement_ElementHandler();
    private Commit_ElementHandler Commit_Handler_ManagementTab = new Commit_ElementHandler();
    private Linkage_ElementHandler Linkage_Handler = new Linkage_ElementHandler();
    private Configuration_ElementHandler configuration_ElementHandler = new Configuration_ElementHandler();
    private TraceabilityMatrixByImpactViewHandlerPanel traceabilityMatrixByImpactViewHandlerPanel = new TraceabilityMatrixByImpactViewHandlerPanel();
    private int currentTab = 0, previousTab = 0;
    
    private JPanel mainNavigationPanel;
    private JPanel requirementModificationPanel;
    private JPanel LinkageManagmentPanel;
    private JPanel homePanel;
    private JPanel traceabilityMatrixPanel;
    
    private JTabbedPane tabpane;

    public GuiViewElementHandler(GuiController guiController) {
        this.guiController = guiController;
        
        initializeButtonActions();
        
        setTitle("ReqTracker");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setBackground(Color.WHITE);
        setJMenuBar(Menu_Handler.getMenuBar());
        setIcon();
        createTabs();
        positionMainPanelElements();
        positionRequirementPanelElements();
        positionRequirementManagementPanelElements();
        positionTraceabilityMatrixPanelElements();
        positionHomePanelElements();
        
        
        pack();
        setVisible(true);
    }
    
    public void doInitialization(){
        initializeButtonActions();
        requirementPanelAction();
        requirementManagementAction();
    }
  
    public Requirement_ElementHandler getRequirement_ElementHandler() {
        return Requirement_Handler;
    }
    
    public Commit_ElementHandler getCommit_ElementHandler() {
        return Commit_Handler;
    }
    
    public Impact_ElementHandler getImpact_ElementHandler() {
        return Impact_Handler;
    }
    
    public Code_ElementHandler getCode_ElementHandler() {
        return Code_Handler;
    }
    
    public Linkage_ElementHandler getLinkage_ElementHandler() {
        return Linkage_Handler;
    }
    
    public Requirement_ElementHandler getRequirement_ElementHandlerRequirementTab() {
        return Requirement_HandlerRequirementTab;
    }
    
    public Requirement_Detail_ElementHandler getRequirement_Detail_ElementHandler() {
        return Requirement_Detail_Handler;
    }
    
    public Requirement_ElementHandler getRequirement_Handler_ManagementTab() {
        return Requirement_Handler_ManagementTab;
    }
    
    public Commit_ElementHandler getCommit_Handler_ManagementTab() {
        return Commit_Handler_ManagementTab;
    }
    
    public PathDE_ElementHandler getPathDE_ElementHandler() {
        return PathDE_Handler;
    }
    
    public Collection<ElementHandler> getElementHandlers(){
        ArrayList<ElementHandler> elementHandlers = new ArrayList<ElementHandler>();
        elementHandlers.add(Requirement_Handler);
        elementHandlers.add(Commit_Handler);
        elementHandlers.add(PathDE_Handler);
        elementHandlers.add(Impact_Handler);
        elementHandlers.add(Code_Handler);
        elementHandlers.add(Linkage_Handler);
        return elementHandlers;
    }
    private void setIcon(){
        java.util.List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(GuiViewElementHandler.class.getResource("/icons/ReqTrackerLogo.png")).getImage());
        icons.add(new ImageIcon(GuiViewElementHandler.class.getResource("/icons/ReqTrackerLogoFull.png")).getImage());
        setIconImages(icons);
               
    }
    private void createTabs(){
       
        mainNavigationPanel = new JPanel();
        mainNavigationPanel.setLayout(new BorderLayout());
        requirementModificationPanel = new JPanel();
        requirementModificationPanel.setLayout(new BorderLayout());
        LinkageManagmentPanel = new JPanel();
        LinkageManagmentPanel.setLayout(new BorderLayout());
        homePanel = new JPanel();
        homePanel.setLayout(new BorderLayout());
        traceabilityMatrixPanel = new JPanel();
        traceabilityMatrixPanel.setLayout(new BorderLayout());
        
        tabpane = new JTabbedPane
        (JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
        
        tabpane.addTab("Home", homePanel);
        tabpane.addTab("Navigation", mainNavigationPanel);
        tabpane.addTab("Requirements", requirementModificationPanel);
        tabpane.addTab("Linkage Management", LinkageManagmentPanel);
        tabpane.addTab("Traceability Matrix", traceabilityMatrixPanel);
        this.add(tabpane);
    }
    
    /**
     * This method creates a pane split into resizeable columns, containing its
     * arguments. The original column widths are weighted according to the
     * getWeight functions of each element handler.
     * @param elemHandlers
     * @return a freshly created split pane containing the elements
     */
    private JXMultiSplitPane createMultiColums(ElementHandler... elemHandlers) {
    	/*
    	 * allocate one column for each real column and each divider
    	 * -> elemHandlers.length columns
    	 * -> elemHandlers.length - 1 dividers
    	 */
    	MultiSplitLayout.Node[] columns = new MultiSplitLayout.Node[elemHandlers.length*2 - 1];
    	/*
    	 * The sum of the weights in a MultiSplitLayout has to be <= 1.0,
    	 * divide each arbitrary weight by the sum of all weights to get there.
    	 */
    	double totalWeight = 0;
    	for(ElementHandler elemHandler : elemHandlers)
    		totalWeight += elemHandler.getWeight();
        for(int i=0; i<elemHandlers.length; ++i) {
        	MultiSplitLayout.Leaf leaf = new MultiSplitLayout.Leaf(Integer.toString(i));
        	columns[2*i] = leaf;
        	leaf.setWeight(elemHandlers[i].getWeight() / totalWeight);
        }
        for(int i=0; i<columns.length; ++i)
        	if(columns[i] == null)
        		columns[i] = new MultiSplitLayout.Divider();
        MultiSplitLayout.Node layoutRoot = new MultiSplitLayout.RowSplit(columns);
        MultiSplitLayout layout = new MultiSplitLayout(layoutRoot);
        layout.setLayoutByWeight(true);
        JXMultiSplitPane pane = new JXMultiSplitPane(layout);
        for(int i=0; i<elemHandlers.length; ++i)
        	pane.add(Integer.toString(i), elemHandlers[i].toComponent());
        return pane;
    }

    private void positionMainPanelElements() {
        
        /* *layoutStructure* explained:
            2D-Array: 1st dimension ==> Columns, 2nd dimension ==> Rows

            {
                {Component01, Component02, ... }, # Column
                {Component11, Component12, ... }, # Column
            }

            ComponentXY: X-->Column-No, Y-->Row-No
        */
        // Based on this layout sturcture GUI will be created by using MultiSplinPane
        /*
        Component[][] layoutStructure = {
                {RequirementID_textField, RequirementID_button, RequirementID_label, RequirementSearch_textField, RequirementID_scrollPane},
//              {Commit_textField, Commit_button, Commit_label, Commit_scrollPane},
                {Commit_textField,  Commit_label, Commit_scrollPane},
//              {Linkage_button, Files_button, FilesSort_combobox, Files_label, Files_scrollPane},
                {Linkage_button, Files_button, Files_label, Files_scrollPane},
                {Requirements2Lines_label, Requirements2Lines_scrollPane},
                {Code_label, Code_scrollPane}

        };

        MultiSplitPane pane = new MultiSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        for ( Component[] i: layoutStructure) {
            MultiSplitPane column = new MultiSplitPane(JSplitPane.VERTICAL_SPLIT, false);
            for (Component j: i)
                column.addComponent(j);
            pane.addComponent(column);
        }
        */

        ElementHandler[] elemHandlers = {
                Requirement_Handler, Commit_Handler, PathDE_Handler, Impact_Handler, Code_Handler,
        };
        JXMultiSplitPane pane = createMultiColums(elemHandlers);

        //setLayout(new BorderLayout());
        mainNavigationPanel.add(pane, BorderLayout.CENTER);

        /*

        GroupLayout layout = new GroupLayout(getContentPane());
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
										//here follow the columns of the UI
										.addGroup(Requirement_Handler.toHorizontalGroup(layout))
										.addGroup(Commit_Handler.toHorizontalGroup(layout))
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
														.addComponent(Linkage_Handler.getButton())
														.addGroup(CommitFile_Handler.toHorizontalGroup(layout))
										)
										.addGroup(Impact_Handler.toHorizontalGroup(layout))
										.addGroup(Code_Handler.toHorizontalGroup(layout))
						)
        );

        layout.setVerticalGroup(
            layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
								//two rows, one to create linkage, the other one for the rest
								.addGroup(Linkage_Handler.toHorizontalGroup(layout))
								.addGroup(layout.createParallelGroup()
												.addGroup(Requirement_Handler.toVerticalGroup(layout))
												.addGroup(Commit_Handler.toVerticalGroup(layout))
												.addGroup(CommitFile_Handler.toVerticalGroup(layout))
												.addGroup(Impact_Handler.toVerticalGroup(layout))
												.addGroup(Code_Handler.toVerticalGroup(layout))
								)
				)
        );
        
        Requirement_Handler.linkSize(layout);

        setLayout(layout);
        */
    }
    
    private void positionRequirementPanelElements() {
    	Requirement_HandlerRequirementTab = new Requirement_ElementHandler();
        Requirement_Detail_Handler = new Requirement_Detail_ElementHandler();
        JXMultiSplitPane pane = createMultiColums(Requirement_HandlerRequirementTab, Requirement_Detail_Handler);
       
        requirementModificationPanel.add(pane, BorderLayout.CENTER);
    }
    
    private void requirementPanelAction(){
        guiController.setRequirementIDFiltering(new FilterByExactString());
        
        Requirement_HandlerRequirementTab.setButtonAction(()->{
            guiController.requirementsFromDBForRequirementTab();
        });
    }
    
    private void positionRequirementManagementPanelElements() {
    	JXMultiSplitPane pane = createMultiColums(Requirement_Handler_ManagementTab, Commit_Handler_ManagementTab, Linkage_Handler);
   
        LinkageManagmentPanel.add(pane, BorderLayout.CENTER);
    }
    
    private void requirementManagementAction(){
        Requirement_Handler_ManagementTab.setButtonAction(()->{
            guiController.requirementsFromDBForManagementTab();
        });
        
        Commit_Handler_ManagementTab.setButtonAction(()->{
            guiController.commitsFromDBForManagementTab();
        });
    }
   
    private void positionHomePanelElements() {
    	configuration_ElementHandler.setController(guiController,tabpane);
        tabpane.setEnabledAt(1, false);
        tabpane.setEnabledAt(2, false);
        tabpane.setEnabledAt(3, false);
        tabpane.setEnabledAt(4, false);
        setOnClickAction();
        homePanel.add(configuration_ElementHandler, BorderLayout.CENTER);
    }
    
    private void positionTraceabilityMatrixPanelElements() {
    	traceabilityMatrixByImpactViewHandlerPanel.setInternalGenerationVisibility(true);
    	traceabilityMatrixByImpactViewHandlerPanel.setExportEnable(false);
    	traceabilityMatrixPanel.add(traceabilityMatrixByImpactViewHandlerPanel, BorderLayout.CENTER);
    }
    
    public void setOnClickAction(){
        tabpane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                previousTab = currentTab;
                currentTab = tabpane.getSelectedIndex();
                if(tabpane.getTitleAt(currentTab).equals("Navigation") &&
                        tabpane.getTitleAt(previousTab).equals("Linkage Management")){
                    if(Linkage_Handler.isDataLayerChanged()){
                        guiController.refresh();
                        Linkage_Handler.setDataLayerChanged(false);
                    }
                }
                
                
            }
        });
    }
    
   
	void initializeButtonActions() {
        PathDE_Handler.setButtonAction(()->guiController.filesFromDB());

        Requirement_Handler.setButtonAction(()->{
            guiController.requirementsFromDB();
        });

//        Commit_Handler.setButtonAction(()->guiController.commitsFromDB());

        Linkage_Handler.setButtonAction(()->guiController.requirementsAndCommitsFromDB());

        Menu_Handler.setConfigureAction(() -> {
            try {
                guiController.reConfigure();
            } catch (IOException e) {
                e.printStackTrace();
            }

            
        });
        
        traceabilityMatrixByImpactViewHandlerPanel.setImpactAction(() -> {
          
                guiController.showTraceabilityMatrixByImpactInTabbedView(traceabilityMatrixByImpactViewHandlerPanel);
                PopupManager popUpManager = new PopupManager();
                popUpManager.showInformationDialog("Traceability matrix Generation is complete");
            
        });

        Menu_Handler.setImpactAction(() -> {
            guiController.getTraceabilityMatrixByImpact();
        });
        
        /*DONOTREMOVE
        Menu_Handler.setByOtherDataAction(()->{
            guiController.getTraceabilityMatrix();
        });
        */
        
        guiController.setRequirementIDFiltering(new FilterByExactString());
        
        Requirement_Handler.setSearchTextFieldAction((RequirementSearch_textField)->{
            guiController.setRequirementIDFiltering(new FilterByExactString(RequirementSearch_textField.getText()));
        });
        
        for(Requirement_ElementHandler column :
        	new Requirement_ElementHandler[] {Requirement_Handler, Requirement_HandlerRequirementTab, Requirement_Handler_ManagementTab}) {
        	 column.setRefreshAction(new AbstractAction("Refresh Linkage") {
     			@Override
     			public void actionPerformed(ActionEvent arg0) {
     				guiController.refresh();
     			}
             });
        }
       
    }
    
}
