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

import it.staiger.jmeter.protocol.http.config.DynamicFiles;
import it.staiger.jmeter.protocol.http.sampler.DynamicMultiPartHttp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.log.Logger;

/**
 * A GUI panel allowing the user to enter file information for http upload.
 * Used by MultipartUrlConfigGui for use in HTTP Samplers.
 */
public class DynamicFilePanel extends AbstractConfigGui implements ActionListener {

    private static final long serialVersionUID = 240L;
    
    private static final String title = "HTTP Dynamic Files"; // "http_dynamic_files";

    @SuppressWarnings("unused")
	private static final Logger log = LoggingManager.getLoggerForClass();

    /** The table containing the list of files. */
    private transient JTable table;

    /** The Title label of the file table. */
    private JLabel tableLabel;

    /** The model for the files table. */
    private transient ObjectTableModel tableModel; // only contains HTTPFileArg elements

    /** A button for adding new files to the table. */
    private JButton add;

    /** A button for browsing file system to set path of selected row in table. */
    private JButton browse;

    /** A button for browsing file system to set folder for import. */
    private JButton browseFolder;

    /** A button for removing files from the table. */
    private JButton delete;

    /** Button to move a argument up*/
    private JButton up;

    /** Button to move a argument down*/
    private JButton down;

    /** Button to move a argument down*/
    private JButton importBtn;

    /** Button to implement up and down buttons*/
    private final boolean enableUpDown;
    
    /**
     * Boolean indicating whether this component is a standalone component or it
     * is intended to be used as a subpanel for another component.
     */
    private final boolean standalone;

    /** Command for adding a row to the table. */
    private static final String ADD = "add"; // $NON-NLS-1$

    /** Command for browsing filesystem to set path of selected row in table. */
    private static final String BROWSE = "browse"; // $NON-NLS-1$

    /** Command for browsing filesystem to set path of selected row in table. */
    private static final String BROWSE_IMPORT_PATH = "browse_import_path"; // $NON-NLS-1$

    /** Command for removing a row from the table. */
    private static final String DELETE = "delete"; // $NON-NLS-1$

    /** Command for moving a row up in the table. */
    private static final String UP = "up"; // $NON-NLS-1$

    /** Command for moving a row down in the table. */
    private static final String DOWN = "down"; // $NON-NLS-1$

    /** Command for moving a row down in the table. */
    private static final String IMPORT = "import"; // $NON-NLS-1$

    /** The file path column title of file table. */
    private static final String FILEPATH = "send_file_filename_label"; // $NON-NLS-1$

    /** The parameter name column title of file table. */
    private static final String PARAMNAME = "send_file_param_name_label"; //$NON-NLS-1$

    /** The mime type column title of file table. */
    private static final String MIMETYPE = "send_file_mime_label"; //$NON-NLS-1$


    private JTextField propertyPrefix;
    private JTextField attachmentsCT;
    private JTextField attachmentsFirst;
    private JTextField attachmentsLast;
    private JTextField attachmentsFileNamePre;
    private JTextField attachmentsFileNameSuf;
    private JTextField attachmentsNamePre;
    private JTextField attachmentsNameSuf;
    private JTextField folder;
    private JCheckBox setArgs;
    private JCheckBox SHA256;
	private JLabel example;
    private JLabeledChoice saveMethod;
    
    /**
     * Create a new ArgumentsPanel as a standalone component.
     */
    public DynamicFilePanel() {
        this(title, true, true);// $NON-NLS-1$
    }
    /**
     * Create a new DynamicFilePanel as an embedded component, using the
     * specified title.
     *
     * @param label
     *  the title for the component.
     */
    public DynamicFilePanel(String label) {
    	this(label, true, false);
    }

    /**
     * Create a new DynamicFilePanel as a standalone component, using the
     * specified title.
     *
     * @param label
     *  the title for the component.
     * @param enableUpDown
     *  enables the buttons to order the files.
     * @param standalone
     *  the indicator to make a standalone or embedded component
     */
    public DynamicFilePanel(String label, boolean enableUpDown, boolean standalone) {
        this.tableLabel = new JLabel(label);
        this.enableUpDown = enableUpDown;
        this.standalone = standalone;
        init();
        initFields();
    }

    /**
     * Initialize the table model used for the http files table.
     */
    private void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] {
                FILEPATH, PARAMNAME, MIMETYPE},
            HTTPFileArg.class,
            new Functor[] {
                new Functor("getPath"), //$NON-NLS-1$
                new Functor("getParamName"), //$NON-NLS-1$
                new Functor("getMimeType")}, //$NON-NLS-1$
            new Functor[] {
                new Functor("setPath"), //$NON-NLS-1$
                new Functor("setParamName"), //$NON-NLS-1$
                new Functor("setMimeType")}, //$NON-NLS-1$
            new Class[] {String.class, String.class, String.class});
    }

    /**
     * Resize the table columns to appropriate widths.
     *
     * @param table
     *  the table to resize columns for
     */
    private void sizeColumns(JTable table) {
        GuiUtils.fixSize(table.getColumn(PARAMNAME), table);
        GuiUtils.fixSize(table.getColumn(MIMETYPE), table);
    }

    /**
     * Save the GUI data in the DynamicMultiPartHttp element,
     * if it is embedded and DynamicFiles element if it is standalone.
     *
     * @param testElement {@link TestElement} to modify
     */
    public void modifyTestElement(TestElement testElement) {
    	GuiUtils.stopTableEditing(table);
        @SuppressWarnings("unchecked") // we only put HTTPFileArgs in it
        Iterator<HTTPFileArg> modelData = (Iterator<HTTPFileArg>) tableModel.iterator();
        
        if (testElement instanceof DynamicMultiPartHttp) {
        	int rows = tableModel.getRowCount();
            DynamicMultiPartHttp base = (DynamicMultiPartHttp) testElement;
            HTTPFileArg[] files = new HTTPFileArg[rows];
            int row=0;
            while (modelData.hasNext()) {
                HTTPFileArg file = modelData.next();
                if(StringUtils.isEmpty(file.getPath())) {
                    continue;
                }
                files[row++]=file;
            }
            base.setDynamicFiles(files);
        }
        else if (testElement instanceof DynamicFiles) {
        	DynamicFiles base = (DynamicFiles) testElement;
        	
            base.clear();

            base.setPropertyPrefix(propertyPrefix.getText());
            base.setAttachmentsCT(attachmentsCT.getText());
            base.setAttachmentsNamePre(attachmentsNamePre.getText());
            base.setAttachmentsNameSuf(attachmentsNameSuf.getText());
            base.setAttachmentsFileNamePre(attachmentsFileNamePre.getText());
            base.setAttachmentsFileNameSuf(attachmentsFileNameSuf.getText());
            base.setArgs(setArgs.isSelected());
            base.setSHA256(SHA256.isSelected());
            base.setSaveMethod(saveMethod.getText());
            
            List<HTTPFileArg> files = new ArrayList<HTTPFileArg>();
            while (modelData.hasNext()) {
                HTTPFileArg file = modelData.next();
                if(StringUtils.isEmpty(file.getPath())) {
                    continue;
                }
                files.add(file);
            }
            base.setHTTPFileArgs(files);
            
            super.configureTestElement(testElement);
        }
        else
        	log.warn("Using Panel for invalid TestElemet");
    }
    
    /**
     * Creates a new DynamicFiles testelemet if a new standalone Panel is added to the TestPlan
     */
	@Override
	public TestElement createTestElement() {

		DynamicFiles args = new DynamicFiles();
		
        modifyTestElement(args);
        return args;
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
            for(HTTPFileArg file : base.getDynamicFiles()){
                tableModel.addRow(file);
            }
            checkDeleteAndBrowseStatus();
        }
        else if (testElement instanceof DynamicFiles) {
        	configureTestElement(testElement);
        	DynamicFiles base = (DynamicFiles) testElement;

	        propertyPrefix.setText(base.getPropertyPrefix());
	        attachmentsCT.setText(base.getAttachmentsCT());
	        attachmentsCT.setText(base.getAttachmentsCT());
	        attachmentsNamePre.setText(base.getAttachmentsNamePre());
	        attachmentsNameSuf.setText(base.getAttachmentsNameSuf());
	        attachmentsFileNamePre.setText(base.getAttachmentsFileNamePre());
	        attachmentsFileNameSuf.setText(base.getAttachmentsFileNameSuf());
	        setArgs.setSelected(base.getArgs());
	        SHA256.setSelected(base.getSHA256());
	        example.setText(propertyPrefix.getText() + "X(_Path || _ParamName || _MimeType || _SHA256)");
	        saveMethod.setText(base.getSaveMethod());
	        tableModel.clearData();
            for(HTTPFileArg file : base.asArray()){
                tableModel.addRow(file);
            }
            checkDeleteAndBrowseStatus();
        }
        else
        	log.warn("Using Panel for invalid TestElemet");
    }


    /**
     * Enable or disable the delete button depending on whether or not there is
     * a row to be deleted.
     */
    private void checkDeleteAndBrowseStatus() {
        // Disable DELETE and BROWSE buttons if there are no rows in
        // the table to delete.
        if (tableModel.getRowCount() == 0) {
            browse.setEnabled(false);
            delete.setEnabled(false);
        } else {
            browse.setEnabled(true);
            delete.setEnabled(true);
        }
        if(enableUpDown && tableModel.getRowCount()>1) {
            up.setEnabled(true);
            down.setEnabled(true);
        }
    }


    /**
     * Clear all rows from the table.
     */
    public void clear() {
        GuiUtils.stopTableEditing(table);
        tableModel.clearData();
    }

    @Override
    public void clearGui() {
        super.clearGui();
        clear();
        initFields();
    }
    /**
     * Initializes all fields and boxes. Is called by {@link #init()} on Panel creation and clearing.
     */
    private void initFields(){
    	if(standalone){
	        propertyPrefix.setText("image_");
	        attachmentsFirst.setText("1");
	        attachmentsLast.setText("10");
	        attachmentsCT.setText("image/jpeg");
	        attachmentsNamePre.setText("image");
	        attachmentsNameSuf.setText("");
	        attachmentsFileNamePre.setText("image");
	        attachmentsFileNameSuf.setText(".jpg");
	        SHA256.setSelected(true);
	        setArgs.setSelected(true);
	        saveMethod.setText(DynamicFiles.SAVE_METHOD_VAR);
	        example.setText(propertyPrefix.getText() + "X");
    	}
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
            addFile(""); //$NON-NLS-1$
        } else if (action.equals(UP)) {
	        moveUp();
	    } else if (action.equals(DOWN)) {
	        moveDown();
	    } else if (action.equals(IMPORT)) {
	        importFiles();
        } else if (action.equals(BROWSE_IMPORT_PATH)) {
            String path = browseAndGetFolderPath();
            if(!path.isEmpty())
            	folder.setText(path);
	    } else
	    	runCommandOnSelectedFile(action);
    }

    /**
     * Filles the table with the file parameters from the import settings panel
     */
    private void importFiles() {

        tableModel.clearData();
        String folderPath = "";
        if(!folder.getText().isEmpty())
        	folderPath = folder.getText() + File.separator;
        
        for(int i = Integer.parseInt(attachmentsFirst.getText()); i <= Integer.parseInt(attachmentsLast.getText()); i++){
        	HTTPFileArg file = new HTTPFileArg(folderPath + attachmentsFileNamePre.getText() + Integer.toString(i) + attachmentsFileNameSuf.getText(),
												attachmentsNamePre.getText() +  Integer.toString(i) + attachmentsNameSuf.getText(),
        										attachmentsCT.getText());
            tableModel.addRow(file);
        }
        checkDeleteAndBrowseStatus();
		
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
        } else if (BROWSE.equals(command)) {
            String path = browseAndGetFilePath();
            if(!path.isEmpty())
            	tableModel.setValueAt(path, rowSelected, 0);
        }
    }

    /**
     * Add a new file row to the table.
     */
    private void addFile(String path) {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        GuiUtils.stopTableEditing(table);

        tableModel.addRow(new HTTPFileArg(path));

        // Enable DELETE (which may already be enabled, but it won't hurt)
        delete.setEnabled(true);
        browse.setEnabled(true);

        // Highlight (select) the appropriate row.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
    }

    /**
     * opens a dialog box to choose a file and returns selected file's
     * path.
     *
     * @return a new File object
     */
    private String browseAndGetFilePath() {
        String path = ""; //$NON-NLS-1$
        JFileChooser chooser = FileDialoger.promptToOpenFile();
        if (chooser != null) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                path = file.getPath();
            }
        }
        return path;
    }
    /**
     * opens a dialog box to choose a file and returns selected file's
     * folder.
     *
     * @return a new File object of selected folder
     */
    private String browseAndGetFolderPath() {
        String path = ""; //$NON-NLS-1$
        JFileChooser chooser = FileDialoger.promptToOpenFile();
        if (chooser != null) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                path = file.getParent();
            }
        }
        return path;
    }

    /**
     * Cancel cell editing if it is being edited
     */
    private void cancelEditing() {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row
        if (table.isEditing()) {
            TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
            cellEditor.cancelCellEditing();
        }
    }
    
    /**
     * Move a row down
     */
    private void moveDown() {
        cancelEditing();

        int[] rowsSelected = table.getSelectedRows();
        if (rowsSelected.length > 0 && rowsSelected[rowsSelected.length - 1] < table.getRowCount() - 1) {
            table.clearSelection();
            for (int i = rowsSelected.length - 1; i >= 0; i--) {
                int rowSelected = rowsSelected[i];
                tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected + 1);
            }
            for (int rowSelected : rowsSelected) {
                table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
            }
        }
    }

    /**
     *  Move a row down
     */
    private void moveUp() {
        cancelEditing();

        int[] rowsSelected = table.getSelectedRows();
        if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
            table.clearSelection();
            for (int rowSelected : rowsSelected) {
                tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected - 1);
            }
            for (int rowSelected : rowsSelected) {
                table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
            }
        }
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
    private JPanel makeMainPanel() {
    	JPanel mainPanel = new JPanel(new BorderLayout(0, 5));

    	if(standalone){
    		mainPanel.add(makeSettingsPanel(),BorderLayout.NORTH);
    	}else{
    		mainPanel.add(makeLabelPanel(),BorderLayout.NORTH);
    	}
    		
        initializeTableModel();
        table = new JTable(tableModel);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        mainPanel.add(makeScrollPane(table),BorderLayout.CENTER);
        mainPanel.add(Box.createVerticalStrut(70), BorderLayout.WEST);
        mainPanel.add(makeButtonPanel(),BorderLayout.SOUTH);
        
        return mainPanel;
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
     * Create a panel containing the config components settings.
     *
     * @return a panel all settings
     */
    private JPanel makeSettingsPanel(){

        JPanel settingsPanel = new VerticalPanel();       
 
        propertyPrefix = new JTextField(10);
        settingsPanel.add(getInputPanel(propertyPrefix, "Prefix:"));
        settingsPanel.add(getExamplePanel());
        settingsPanel.add(getOptionsPanel()); 
        
        return settingsPanel;
    }

    
    /**
     * Create a panel containing the save method selection and an example of the var/prop syntax.
     *
     * @return a panel containing further settings
     */
    private JPanel getExamplePanel(){

        JPanel panel = new HorizontalPanel();  

        example = new JLabel();
        example.setFont(null);
        saveMethod = new JLabeledChoice("Save Method:",
        				new String[]{DynamicFiles.SAVE_METHOD_PROP, DynamicFiles.SAVE_METHOD_VAR});
        saveMethod.setToolTipText("Property - makes file available above Thread Groups\t"
        						+ "Variable - set for every Thread user in this Thread Context");
        
        panel.add(getInputPanel(example, "prop/var enumeration:"));
        panel.add(saveMethod);        
        
        return panel;
    }
    
    /**
     * Create a panel containing the export options.
     *
     * @return a panel containing the export options.
     */
    private JPanel getOptionsPanel(){

        JPanel optionsPanel = new HorizontalPanel();  

        setArgs = new JCheckBox("Export File Parameters:");
        SHA256 = new JCheckBox("Export SHA256 Hash:");
        
        optionsPanel.add(setArgs);
        optionsPanel.add(SHA256);
        
        return optionsPanel;
    }

    /**
     * Create a panel containing the import options.
     *
     * @return a panel containing the import options.
     */
    private JPanel makeImportPanel(){
    	
        JPanel importPanel = new JPanel(new BorderLayout());
        
        importPanel.setBorder(BorderFactory.createTitledBorder("Import Files:"));

        importBtn = new JButton("import"); // $NON-NLS-1$
        importBtn.setActionCommand(IMPORT);
        importBtn.addActionListener(this);

        importPanel.add(getImportFields(), BorderLayout.NORTH);
        importPanel.add(importBtn, BorderLayout.SOUTH);
        
        importPanel.setToolTipText("leave Empty for relative path to ${base.dir}");
        return importPanel;
    }
    /**
     * Create a panel containing all import options.
     *
     * @return a panel containing all import options.
     */
    private JPanel getImportFields(){
    	
        JPanel importFields = new HorizontalPanel();

        importFields.add(getSourceFiles());
        importFields.add(getParameterInfos());
        
        return importFields;
    }
    /**
     * Create a panel containing the source files parameters.
     *
     * @return a panel containing the source files parameters.
     */
    private JPanel getSourceFiles(){
	    JPanel panel = new VerticalPanel();
	    panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Source:")); // $NON-NLS-1$

	    attachmentsFileNamePre = new JTextField(10);
	    attachmentsFileNameSuf = new JTextField(10);
	    attachmentsFirst = new JTextField(10);
	    attachmentsLast = new JTextField(10);
	    panel.add(getFolder());
	    panel.add(getInputPanel(attachmentsFileNamePre, "Filename Prefix:"));
	    panel.add(getInputPanel(attachmentsFileNameSuf, "Filename Suffix:"));
	    panel.add(getInputPanel(attachmentsFirst, "First file number:"));
	    panel.add(getInputPanel(attachmentsLast, "Last file number:"));
	    return panel;
    }
    /**
     * Create a panel containing the files parameters.
     *
     * @return a panel containing the files parameters.
     */
    private JPanel getParameterInfos(){
	    JPanel panel = new VerticalPanel();
	    panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Parameters:")); // $NON-NLS-1$

	    attachmentsNamePre = new JTextField(10);
	    attachmentsNameSuf = new JTextField(10);
	    attachmentsCT = new JTextField(10);
	    panel.add(getInputPanel(attachmentsNamePre, "Filename Prefix:"));
	    panel.add(getInputPanel(attachmentsNameSuf, "Filename Suffix:"));
	    panel.add(getInputPanel(attachmentsCT, "MIME Type:"));
	    return panel;
    }
    /**
     * Create a panel containing the folder field and browse button.
     *
     * @return a panel containing the folder field and browse button.
     */
    private JPanel getFolder() {
        folder = new JTextField(10);

        JLabel label = new JLabel("Folder:"); // $NON-NLS-1$
        label.setLabelFor(folder);
        browseFolder = new JButton("Browse"); // $NON-NLS-1$
        browseFolder.setActionCommand(BROWSE_IMPORT_PATH);
        browseFolder.addActionListener(this);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(folder, BorderLayout.CENTER);
        panel.add(browseFolder, BorderLayout.EAST);

        return panel;
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

        browse = new JButton(JMeterUtils.getResString("browse")); // $NON-NLS-1$
        browse.setActionCommand(BROWSE);

        delete = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
        delete.setActionCommand(DELETE);
        
        if(enableUpDown) {
            up = new JButton(JMeterUtils.getResString("up")); // $NON-NLS-1$
            up.setActionCommand(UP);
    
            down = new JButton(JMeterUtils.getResString("down")); // $NON-NLS-1$
            down.setActionCommand(DOWN);
        }
	        
        checkDeleteAndBrowseStatus();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add.addActionListener(this);
        browse.addActionListener(this);
        delete.addActionListener(this);
        buttonPanel.add(add);
        buttonPanel.add(browse);
        buttonPanel.add(delete);
        if(enableUpDown) {
            up.addActionListener(this);
            down.addActionListener(this);
            buttonPanel.add(up);
            buttonPanel.add(down);
        }
        return buttonPanel;
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {
        JPanel p = this;

        if (standalone) {
            setLayout(new BorderLayout(0, 5));
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
            p = new JPanel();
        }

        p.setLayout(new BorderLayout());

        //p.add(makeLabelPanel(), BorderLayout.NORTH);
        p.add(makeMainPanel(), BorderLayout.CENTER);
        // Force a minimum table height of 100 pixels
        p.add(Box.createVerticalStrut(100), BorderLayout.WEST);


        if (standalone) {
        	p.add(makeImportPanel(), BorderLayout.SOUTH);
            add(p, BorderLayout.CENTER);
        }
        
        table.revalidate();
        sizeColumns(table);

    }
    
    /**
     * Utility function to create a Panel, which contains the given @TextField and a label
     * @param field the @TextField which is to be embedded.
     * @param labelName @String for the Label of the text field.
     * @return the @JPanel with the labeld field.
     */
    protected JPanel getInputPanel(JComponent field, String labelName) {
        JLabel label = new JLabel(labelName); // $NON-NLS-1$
        label.setLabelFor(field);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

	@Override
	public String getLabelResource() {
		return title;
	}
	
	@Override
	public String getStaticLabel() {
		return title;
	}
}
