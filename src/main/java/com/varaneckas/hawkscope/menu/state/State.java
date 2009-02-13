package com.varaneckas.hawkscope.menu.state;


/**
 * State of {@link MainMenu} 
 *
 * @author Tomas Varaneckas
 * @version $Id$
 */
public abstract class State {

    /**
     * Action to MouseEvent
     * 
     * @param event mouse event
     */
    public abstract void act(final StateEvent event);
    
    /**
     * State initialization
     */
    public abstract void init();

}