package com.limegroup.gnutella.gui.options;

import java.awt.event.ActionEvent;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.actions.AbstractAction;
import com.limegroup.gnutella.gui.actions.LimeAction;


public class ConfigureOptionsAction extends AbstractAction {

    private static final long serialVersionUID = -4910940664898276702L;

    /**
      * Resource key to go to in the options window
      */
    private String paneTitle;


    /**
      * Creates a generic defered Action to open an Options pane
      */
    public ConfigureOptionsAction(String pane) {
        paneTitle = pane;
    }

    /**
      * Creates a specialized defered Action to open an Options pane
      * specifying name, tooltip, and icon properties for IconButton creation
      */
    public ConfigureOptionsAction(String pane, String name, String tooltip, String iconName) {
        this(pane);
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(LimeAction.ICON_NAME, iconName);
    }

    /**
      * Launches LimeWire's options with the given options pane selected.
      */
    public void actionPerformed(ActionEvent e) {
        GUIMediator.instance().setOptionsVisible(true, paneTitle);
    }
}
