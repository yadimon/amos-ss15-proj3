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
package de.fau.osr.gui.View.Presenter;

import de.fau.osr.gui.Controller.Visitor;
import de.fau.osr.gui.Model.DataElements.DataElement;
import de.fau.osr.gui.Model.DataElements.PathDE;
import de.fau.osr.gui.Model.DataElements.ImpactDE;

import javax.swing.*;

import java.awt.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Presenter for DataElement PathDE
 * @author: Taleh Didover
 */
public class Presenter_PathImpact extends Presenter{
    private ArrayList<PathDE> filePaths;
    private ImpactDE impact;

    public ArrayList<PathDE> getPathDE() {
        return filePaths;
    }
    
    public ImpactDE getImpact(){
        return impact;
    }

    public void setPathDE(ArrayList<PathDE> commitFiles) {
        this.filePaths = commitFiles;
    }
    
    public void setImpact(ImpactDE impact){
        this.impact = impact;
    }

    public Presenter_PathImpact(ArrayList<PathDE> filePaths, ImpactDE impact) {
        this.filePaths = filePaths;
        this.impact = impact;
    }

    public String getText(){
        String pathDE = filePaths.get(0).FilePath.getFileName().toString();
        return String.format("%s - %.1f", pathDE, impact.Impact);
    }

    public boolean isAvailable(){
        PathDE pathDE = filePaths.get(0);
        return Files.exists(pathDE.FilePath);
    }

    public Color getColor(){
        return Color.WHITE;
    }

    @Override
    public JLabel present(JLabel defaultLabel) {
        defaultLabel.setBackground(getColor());
        defaultLabel.setText(getText());

        if(!isAvailable()){
            defaultLabel.setForeground(UIManager
                    .getColor("Label.disabledForeground"));
            defaultLabel.setBorder(null);
        }
        return defaultLabel;
    }

    @Override
    public Collection<? extends DataElement> visit(Visitor visitor){
        return visitor.toDataElement(this);
    }
}
