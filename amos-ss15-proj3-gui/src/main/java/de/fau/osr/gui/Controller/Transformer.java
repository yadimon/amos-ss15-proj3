package de.fau.osr.gui.Controller;

import de.fau.osr.gui.Model.DataElements.DataElement;
import de.fau.osr.gui.Model.DataElements.Requirement;
import de.fau.osr.gui.View.ElementHandler.ElementHandler;
import de.fau.osr.gui.View.Presenter.Presenter;

import java.util.Collection;
import java.util.function.Supplier;

public class Transformer {
    private static Visitor visitor = new Visitor_Swing();
    
    public static void setVisitor(Visitor visitortmp){
        visitor = visitortmp;
    }
    
    public static Presenter[] transformDataElementsToPresenters(Collection<? extends DataElement> dataElements){
        Presenter[] result = new Presenter[dataElements.size()];
        int i = 0;
        for(DataElement dataElement: dataElements){
            result[i] = dataElement.visit(visitor);
            i++;
        }
        
        return result;
    }
    
    public static void process(ElementHandler elementHandler, Runnable buttonAction, Supplier<Collection<? extends DataElement>> fetching){
        Collection<? extends DataElement> dataElements = fetching.get();
        Presenter[] presenter = transformDataElementsToPresenters(dataElements);
        elementHandler.setScrollPane_Content(presenter, buttonAction);
    }
    
    public static Collection<Requirement> castToRequirement(Collection<DataElement> dataElement){
        return (Collection)dataElement;
    }
}
