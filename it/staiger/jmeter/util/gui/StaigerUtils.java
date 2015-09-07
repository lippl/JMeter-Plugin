package it.staiger.jmeter.util.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public abstract class StaigerUtils {

    
    /**
     * Utility function to create a Panel, which contains the given TextField and a label
     * @param comp the TextField which is to be embedded.
     * @param labelName String for the Label of the text field.
     * @return the JPanel with the labeled field.
     */
    public static JPanel getInputPanel(String labelName, JComponent comp) {
        return getInputPanel(labelName, comp, null);
    }
    /**
     * Utility function to create a Panel, which contains the given TextField and a label
     * @param labelName String for the Label of the text field.
     * @param comp1 the Component which is to be embedded in the middle.
     * @param comp2 Component to be set at the right side of the panel - May be used for Buttons etc.
     * @return the JPanel with the labeled field.
     */
    public static JPanel getInputPanel(String labelName, JComponent comp1, JComponent comp2) {
        JLabel label = new JLabel(labelName); // $NON-NLS-1$
        label.setLabelFor(comp1);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(comp1, BorderLayout.CENTER);
        if(comp2 != null)
        	panel.add(comp2, BorderLayout.EAST);

        return panel;
    }

    /**
     * Utility function to add a Component to a @GridbagLayout
     * @param panel the panel, the component is to be added.
     * @param constraints The constraints which are to be used.
     * @param col column of the GridbagLayout.
     * @param row row of the GridbagLayout.
     * @param comp component which is to be added to the panel.
     */
    public static void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent comp) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(comp, constraints);
    }


    /**
     * Utility function to add a Component into a new scrollabel panel
     * @param comp component which is to be added to the panel. 
     * @return a panel containing the title label
     */
    public static JScrollPane makeScrollPanel(Component comp) {
        JScrollPane pane = new JScrollPane(comp);
        pane.setPreferredSize(pane.getMinimumSize());
        return pane;
    }

    
    /**
     * Create a panel containing the title label.
     *
     * @param label the label which is to be centered in the returned Panel 
     * @return a panel containing the title label
     */
    public static Component makeLabelPanel(JLabel label) {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        labelPanel.add(label);
        return labelPanel;
    }
}
