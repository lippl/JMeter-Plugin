/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.staiger.jmeter.protocol.http.config.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;

import it.staiger.jmeter.protocol.http.sampler.DynamicMultiPartHttp;
import it.staiger.jmeter.protocol.http.util.VariableFileArg;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;
/*
 * Note: this class is currently only suitable for use with HTTSamplerBase.
 * If it is required for other classes, then the appropriate configure() and modifyTestElement()
 * method code needs to be written.
 */
/**
 * A GUI panel allowing the user to enter file information for http upload.
 * Used by MultipartUrlConfigGui for use in HTTP Samplers.
 */
public class VariableFilePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 240L;

    /** The title label for this component. */
    private JLabel tableLabel;

    /** The table containing the list of files. */
    private transient JTable table;

    /** The model for the files table. */
    private transient ObjectTableModel tableModel; // only contains VariableFileArg elements

    /** A button for adding new files to the table. */
    private JButton add;

    /** A button for removing files from the table. */
    private JButton delete;

    /** Command for adding a row to the table. */
    private static final String ADD = "add"; // $NON-NLS-1$

    /** Command for removing a row from the table. */
    private static final String DELETE = "delete"; // $NON-NLS-1$

    private static final String CONTENT = "send_content_label:";//"send_content_label"; // $NON-NLS-1$
    
    private static final String FILENAME = "send_file_name_label:"; //"send_file_name_label"; // $NON-NLS-1$

    /** The parameter name column title of file table. */
    private static final String PARAMNAME = "send_file_param_name_label"; //$NON-NLS-1$

    /** The mime type column title of file table. */
    private static final String MIMETYPE = "send_file_mime_label"; //$NON-NLS-1$

    public VariableFilePanel() {
        this(""); // required for unit tests
    }

    /**
     * Create a new filePanelfilePanel as an embedded component, using the
     * specified title.
     *
     * @param label
     *  the title for the component.
     */
    public VariableFilePanel(String label) {
        tableLabel = new JLabel(label);
        init();
    }

    /**
     * Initialize the table model used for the http files table.
     */
    private void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] {
                CONTENT, FILENAME, PARAMNAME, MIMETYPE},
            VariableFileArg.class,
            new Functor[] {
	            new Functor("getContent"), //$NON-NLS-1$
	            new Functor("getName"), //$NON-NLS-1$
                new Functor("getParamName"), //$NON-NLS-1$
                new Functor("getMimeType")}, //$NON-NLS-1$
            new Functor[] {
	            new Functor("setContent"), //$NON-NLS-1$
	            new Functor("setName"), //$NON-NLS-1$
                new Functor("setParamName"), //$NON-NLS-1$
                new Functor("setMimeType")}, //$NON-NLS-1$
            new Class[] {String.class, String.class, String.class, String.class});
    }

    public static boolean testFunctors(){
    	VariableFilePanel instance = new VariableFilePanel(""); //$NON-NLS-1$
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null,instance.getClass());
    }

    /**
     * Resize the table columns to appropriate widths.
     *
     * @param table
     *  the table to resize columns for
     */
    private void sizeColumns(JTable table) {
        GuiUtils.fixSize(table.getColumn(FILENAME), table);
        GuiUtils.fixSize(table.getColumn(PARAMNAME), table);
        GuiUtils.fixSize(table.getColumn(MIMETYPE), table);
    }

    /**
     * Save the GUI data in the DynamicMultiPartHttp element.
     *
     * @param testElement {@link TestElement} to modify
     */
    public void modifyTestElement(TestElement testElement) {
        GuiUtils.stopTableEditing(table);
        if (testElement instanceof DynamicMultiPartHttp) {
            DynamicMultiPartHttp base = (DynamicMultiPartHttp) testElement;
            int rows = tableModel.getRowCount();
            @SuppressWarnings("unchecked") // we only put VariableFileArgs in it
            Iterator<VariableFileArg> modelData = (Iterator<VariableFileArg>) tableModel.iterator();
            VariableFileArg[] files = new VariableFileArg[rows];
            int row=0;
            while (modelData.hasNext()) {
                VariableFileArg file = modelData.next();
                files[row++]=file;
            }
            base.setVariableFiles(files);
        }
    }

    /**
     * A newly created component can be initialized with the contents of a
     * DynamicMultiPartHttp object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param testElement the DynamicMultiPartHttp to be used to configure the GUI
     */
    public void configure(TestElement testElement) {
        if (testElement instanceof DynamicMultiPartHttp) {
            DynamicMultiPartHttp base = (DynamicMultiPartHttp) testElement;
            tableModel.clearData();
            for(VariableFileArg file : base.getVariableFiles()){
                tableModel.addRow(file);
            }
            checkDeleteAndBrowseStatus();
        }
    }


    /**
     * Enable or disable the delete button depending on whether or not there is
     * a row to be deleted.
     */
    private void checkDeleteAndBrowseStatus() {
        // Disable DELETE and BROWSE buttons if there are no rows in
        // the table to delete.
        if (tableModel.getRowCount() == 0) {
            delete.setEnabled(false);
        } else {
            delete.setEnabled(true);
        }
    }

    /**
     * Clear all rows from the table.
     */
    public void clear() {
        GuiUtils.stopTableEditing(table);
        tableModel.clearData();
    }

    /**
     * Invoked when an action occurs. This implementation supports the add and
     * delete buttons.
     *
     * @param e
     *  the event that has occurred
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(ADD)) {
            addFile("", ""); //$NON-NLS-1$
        }
        runCommandOnSelectedFile(action);
    }

    /**
     * runs specified command on currently selected file.
     *
     * @param command specifies which process will be done on selected
     * file. it's coming from action command currently catched by
     * action listener.
     *
     * @see runCommandOnRow
     */
    private void runCommandOnSelectedFile(String command) {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row
        if (table.isEditing()) {
            TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
            cellEditor.cancelCellEditing();
        }
        int rowSelected = table.getSelectedRow();
        if (rowSelected >= 0) {
            runCommandOnRow(command, rowSelected);
            tableModel.fireTableDataChanged();
            // Disable DELETE and BROWSE if there are no rows in the table to delete.
            checkDeleteAndBrowseStatus();
            // Table still contains one or more rows, so highlight (select)
            // the appropriate one.
            if (tableModel.getRowCount() != 0) {
                int rowToSelect = rowSelected;
                if (rowSelected >= tableModel.getRowCount()) {
                    rowToSelect = rowSelected - 1;
                }
                table.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }
    }

    /**
     * runs specified command on currently selected table row.
     *
     * @param command specifies which process will be done on selected
     * file. it's coming from action command currently catched by
     * action listener.
     *
     * @param rowSelected index of selected row.
     */
    private void runCommandOnRow(String command, int rowSelected) {
        if (DELETE.equals(command)) {
            tableModel.removeRow(rowSelected);
        }
    }

    /**
     * Add a new file row to the table.
     */
    private void addFile(String content, String name) {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        GuiUtils.stopTableEditing(table);

        tableModel.addRow(new VariableFileArg(content, name));

        // Enable DELETE (which may already be enabled, but it won't hurt)
        delete.setEnabled(true);

        // Highlight (select) the appropriate row.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
    }


    /**
     * Stop any editing that is currently being done on the table. This will
     * save any changes that have already been made.
     */
    protected void stopTableEditing() {
        GuiUtils.stopTableEditing(table);
    }
    
    /**
     * Create the main GUI panel which contains the file table.
     *
     * @return the main GUI panel
     */
    private Component makeMainPanel() {
        initializeTableModel();
        table = new JTable(tableModel);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return makeScrollPane(table);
    }

    /**
     * Create a panel containing the title label for the table.
     *
     * @return a panel containing the title label
     */
    private Component makeLabelPanel() {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        labelPanel.add(tableLabel);
        return labelPanel;
    }

    /**
     * Create a panel containing the add and delete buttons.
     *
     * @return a GUI panel containing the buttons
     */
    private JPanel makeButtonPanel() {
        add = new JButton(JMeterUtils.getResString("add")); // $NON-NLS-1$
        add.setActionCommand(ADD);
        add.setEnabled(true);

        delete = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
        delete.setActionCommand(DELETE);

        checkDeleteAndBrowseStatus();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add.addActionListener(this);
        delete.addActionListener(this);
        buttonPanel.add(add);
        buttonPanel.add(delete);
        return buttonPanel;
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {
        JPanel p = this;

        p.setLayout(new BorderLayout());

        p.add(makeLabelPanel(), BorderLayout.NORTH);
        p.add(makeMainPanel(), BorderLayout.CENTER);
        // Force a minimum table height of 70 pixels
        p.add(Box.createVerticalStrut(70), BorderLayout.WEST);
        p.add(makeButtonPanel(), BorderLayout.SOUTH);

        table.revalidate();
        sizeColumns(table);
    }

    private JScrollPane makeScrollPane(Component comp) {
        JScrollPane pane = new JScrollPane(comp);
        pane.setPreferredSize(pane.getMinimumSize());
        return pane;
    }
}
