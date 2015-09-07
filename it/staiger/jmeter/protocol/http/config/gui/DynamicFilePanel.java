/*
 * @@@LICENSE
 *
 */

package it.staiger.jmeter.protocol.http.config.gui;

import it.staiger.jmeter.protocol.http.config.DynamicFiles;
import it.staiger.jmeter.protocol.http.sampler.DynamicHttpPostSampler;
import it.staiger.jmeter.util.gui.StaigerUtils;

import java.awt.BorderLayout;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.GuiPackage;
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
    private JTextField folder;
    private JCheckBox setArgs;
    private JCheckBox SHA256;
	private JLabel example;
    private JLabeledChoice saveMethod;
    
    /**
     * Create a new ArgumentsPanel as a standalone component.
     */
    public DynamicFilePanel() {
        this(title, true, true);
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
     * Save the GUI data in the DynamicHttpPostSampler element,
     * if it is embedded and DynamicFiles element if it is standalone.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     * @param testElement TestElement to modify
     */
    public void modifyTestElement(TestElement testElement) {
    	GuiUtils.stopTableEditing(table);
        @SuppressWarnings("unchecked") // we only put HTTPFileArgs in it
        Iterator<HTTPFileArg> modelData = (Iterator<HTTPFileArg>) tableModel.iterator();
        
        if (testElement instanceof DynamicHttpPostSampler) {
        	int rows = tableModel.getRowCount();
            DynamicHttpPostSampler base = (DynamicHttpPostSampler) testElement;
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
            base.setRelativePath(folder.getText());
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
        	log.warn("Using Panel for invalid TestElemet");// $NON-NLS-1$
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
     * DynamicHttpPostSampler object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param testElement the DynamicHttpPostSampler to be used to configure the GUI
     */
    public void configure(TestElement testElement) {
        if (testElement instanceof DynamicHttpPostSampler) {
            DynamicHttpPostSampler base = (DynamicHttpPostSampler) testElement;
            tableModel.clearData();
            for(HTTPFileArg file : base.getDynamicFiles()){
                tableModel.addRow(file);
            }
            checkDeleteAndBrowseStatus();
        }
        else if (testElement instanceof DynamicFiles) {
        	super.configure(testElement);
        	DynamicFiles base = (DynamicFiles) testElement;

	        propertyPrefix.setText(base.getPropertyPrefix());
	        attachmentsCT.setText(base.getAttachmentsCT());
	        folder.setText(base.getRelativePath());
	        setArgs.setSelected(base.getArgs());
	        SHA256.setSelected(base.getSHA256());
	        example.setText(propertyPrefix.getText() + "X(_Path || _ParamName || _MimeType || _SHA256)");// $NON-NLS-1$
	        saveMethod.setText(base.getSaveMethod());
	        tableModel.clearData();
            for(HTTPFileArg file : base.asArray()){
                tableModel.addRow(file);
            }
            checkDeleteAndBrowseStatus();
        }
        else
        	log.warn("Using Panel for invalid TestElemet");// $NON-NLS-1$
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
	        attachmentsCT.setText("image/jpeg");// $NON-NLS-1$
	        folder.setText("");// $NON-NLS-1$
	        SHA256.setSelected(true);
	        setArgs.setSelected(true);
	        saveMethod.setText(DynamicFiles.SAVE_METHOD_VAR);
	        example.setText(propertyPrefix.getText() + "X");// $NON-NLS-1$
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
     * Add a new file row to the table.
     */
    private void addFile(HTTPFileArg file) {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        GuiUtils.stopTableEditing(table);

        tableModel.addRow(file);

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
        String path = folder.getText();
        if(path.isEmpty())
        	path = FileDialoger.getLastJFCDirectory();
        JFileChooser chooser = new JFileChooser(new File(path));

        chooser.setDialogTitle("select folder");// $NON-NLS-1$
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(GuiPackage.getInstance().getMainFrame()) == JFileChooser.APPROVE_OPTION) {
                path = chooser.getSelectedFile().getAbsolutePath();
                FileDialoger.setLastJFCDirectory(path);
        }
        return path;
    }

    /**
     * Fills the table with the file parameters from the import settings panel
     */
    private void importFiles() {
    	String relPath = folder.getText();
    	String replace = relPath + File.separator;
	    if(relPath.isEmpty()){
	    	relPath = FileDialoger.getLastJFCDirectory();
	    	replace = "";
	    }
	    
        JFileChooser chooser = new JFileChooser(new File(relPath));
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("select files");// $NON-NLS-1$

        if (chooser.showOpenDialog(GuiPackage.getInstance().getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            for(File file : files){
            	String path=file.getAbsolutePath().replace(replace, "");
            	String name = file.getName();
            	int last = name.lastIndexOf(".");
            	if(last!=-1)
            		name = name.substring(0, last);
            			
            	HTTPFileArg hFile = new HTTPFileArg(path,name,
										attachmentsCT.getText());

            	addFile(hFile);			
            }
            FileDialoger.setLastJFCDirectory(files[0].getAbsolutePath());
        }
		
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
     * Create a panel containing the config components settings.
     *
     * @return a panel all settings
     */
    protected JPanel makeSettingsPanel(){

        JPanel settingsPanel = new VerticalPanel();       
 
        propertyPrefix = new JTextField(10);
        settingsPanel.add(StaigerUtils.getInputPanel("Prefix:", propertyPrefix));// $NON-NLS-1$
        settingsPanel.add(getExamplePanel());
        settingsPanel.add(getOptionsPanel()); 
        
        return settingsPanel;
    }

    
    /**
     * Create a panel containing the save method selection and an example of the var/prop format.
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
        						+ "Variable - set for every Thread user in this Thread Context");// $NON-NLS-1$
        
        panel.add(StaigerUtils.getInputPanel("prop/var enumeration:", example));// $NON-NLS-1$
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

        setArgs = new JCheckBox("Export File Parameters:");// $NON-NLS-1$
        setArgs.setFont(null);
        SHA256 = new JCheckBox("Export SHA256 Hash:");// $NON-NLS-1$
        SHA256.setFont(null);
        
        optionsPanel.add(setArgs);
        optionsPanel.add(SHA256);
        
        return optionsPanel;
    }
    
    
    /**
     * Create the main GUI panel which contains the file table and control buttons.
     *
     * @return the main GUI panel
     */
    protected JPanel makeFilePanel() {
    	JPanel mainPanel = new JPanel(new BorderLayout(0, 5));

    	if(standalone){
    		mainPanel.add(makeSettingsPanel(),BorderLayout.NORTH);
    	}else{
    		mainPanel.add(StaigerUtils.makeLabelPanel(tableLabel),BorderLayout.NORTH);
    	}
    		
        initializeTableModel();
        table = new JTable(tableModel);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        mainPanel.add(StaigerUtils.makeScrollPanel(table),BorderLayout.CENTER);
        mainPanel.add(Box.createVerticalStrut(70), BorderLayout.WEST);
        mainPanel.add(makeButtonPanel(),BorderLayout.SOUTH);
        
        return mainPanel;
    }

    /**
     * Create a panel containing the import options.
     *
     * @return a panel containing the import options.
     */
    protected JPanel makeImportPanel(){
    	
        JPanel importPanel = new JPanel(new BorderLayout());
        
        importPanel.setBorder(BorderFactory.createTitledBorder("Import Files:"));// $NON-NLS-1$

        importBtn = new JButton("Add multiple"); // $NON-NLS-1$
        importBtn.setActionCommand(IMPORT);
        importBtn.addActionListener(this);

        importPanel.add(getImportInfo(), BorderLayout.NORTH);
        importPanel.add(importBtn, BorderLayout.SOUTH);
        
        return importPanel;
    }
    /**
     * Create a panel containing the source files Mime-Type and relative path.
     *
     * @return a panel containing the source files parameters.
     */
    private JPanel getImportInfo(){
	    JPanel panel = new HorizontalPanel();
	    panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Source:")); // $NON-NLS-1$

	    attachmentsCT = new JTextField(10);
        folder = new JTextField(10);
	    folder.setToolTipText("leave Empty for relative path to ${base.dir}");// $NON-NLS-1$
        browseFolder = new JButton("Browse"); // $NON-NLS-1$
        browseFolder.setActionCommand(BROWSE_IMPORT_PATH);
        browseFolder.addActionListener(this);
	    
        panel.add(StaigerUtils.getInputPanel("MIME Type:", attachmentsCT));// $NON-NLS-1$
	    panel.add(StaigerUtils.getInputPanel("set Path Relative to:", folder, browseFolder));// $NON-NLS-1$
	    
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

        p.add(makeFilePanel(), BorderLayout.CENTER);
        // Force a minimum table height of 100 pixels
        p.add(Box.createVerticalStrut(100), BorderLayout.WEST);


        if (standalone) {
        	p.add(makeImportPanel(), BorderLayout.SOUTH);
            add(p, BorderLayout.CENTER);
        }
        
        table.revalidate();
        sizeColumns(table);

    }
	
	@Override
	public String getStaticLabel() {
		return title;
	}
    @Override
    public String getLabelResource() {
    	return this.getClass().getSimpleName();
    }
}
