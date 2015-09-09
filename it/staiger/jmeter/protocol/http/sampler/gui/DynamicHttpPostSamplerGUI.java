/*
 * @@@LICENSE
 *
 */

package it.staiger.jmeter.protocol.http.sampler.gui;
//package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.gui.HTTPFileArgsPanel;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import it.staiger.jmeter.protocol.http.config.gui.DynamicFilePanel;
import it.staiger.jmeter.protocol.http.config.gui.VariableFilePanel;
import it.staiger.jmeter.protocol.http.sampler.DynamicHttpPostSampler;
import it.staiger.jmeter.util.gui.StaigerUtils;



public class DynamicHttpPostSamplerGUI extends AbstractSamplerGui{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String title = "HTTP Dynamic Post";// $NON-NLS-1$

	@SuppressWarnings("unused")
	private static final Logger log = LoggingManager.getLoggerForClass();
	
	boolean useArgPanel;

    // Tabbed pane that contains parameters and raw body
    private JTabbedPane tabbedPane;
    
    private JTextField ep1;
    private JTextField ep2;
    private JTextField connTimeout;
    private JTextField respTimeout;
    private JTextField attachmentNumbers;
    private JTextField recordtype;
    private JTextField threshold;
    private JCheckBox keepAlive;
    private JCheckBox logFiles;
    private JCheckBox argumentThreshold;
    private JCheckBox dynamicThreshold;
    private JCheckBox staticThreshold;
    private JCheckBox variableThreshold;
    private JCheckBox blockMerge;
    private HTTPArgumentsPanel argsPanel;
    private HTTPFileArgsPanel staticFiles;
    private DynamicFilePanel dynamicFiles;
    private VariableFilePanel variableFiles;

    
    public DynamicHttpPostSamplerGUI() {
        init();
        initFields();
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof DynamicHttpPostSampler) {
        	DynamicHttpPostSampler dynamicSampler = (DynamicHttpPostSampler) element;
        	ep1.setText(dynamicSampler.getEndpoint1());
	        ep2.setText(dynamicSampler.getEndpoint2());
	        respTimeout.setText(dynamicSampler.getResponseTimeoutAsString());
	        connTimeout.setText(dynamicSampler.getConnectTimeoutAsString());
	        attachmentNumbers.setText(dynamicSampler.getAttachmentNumbers());
	        recordtype.setText(dynamicSampler.getRecordTypeAsString());
	        threshold.setText(dynamicSampler.getThresholdAsString());
	        keepAlive.setSelected(dynamicSampler.getUseKeepAlive());
	        logFiles.setSelected(dynamicSampler.getLogFiles());
	        blockMerge.setSelected(dynamicSampler.getBlockMerge());

	        argumentThreshold.setSelected(dynamicSampler.getArgumentThreshold());
	        staticThreshold.setSelected(dynamicSampler.getStaticThreshold());
	        variableThreshold.setSelected(dynamicSampler.getVariableThreshold());
	        dynamicThreshold.setSelected(dynamicSampler.getDynamicThreshold());

	        argsPanel.configure(dynamicSampler.getOwnArguments());
            staticFiles.configure(dynamicSampler);
            dynamicFiles.configure(dynamicSampler);
            variableFiles.configure(dynamicSampler);
        }
    }

    @Override
    public TestElement createTestElement() {
        DynamicHttpPostSampler sampler = new DynamicHttpPostSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @param sampler TestElement which is to be modified
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        super.configureTestElement(sampler);

        if (sampler instanceof DynamicHttpPostSampler) {
            Arguments args = (Arguments) argsPanel.createTestElement();
            HTTPArgument.convertArgumentsToHTTP(args);
            DynamicHttpPostSampler dynamicSampler = (DynamicHttpPostSampler) sampler;
            dynamicSampler.setEndpoint1(ep1.getText());
            dynamicSampler.setEndpoint2(ep2.getText());
            dynamicSampler.setConnectTimeout(connTimeout.getText());
            dynamicSampler.setResponseTimeout(respTimeout.getText());
            dynamicSampler.setAttachmentNumbers(attachmentNumbers.getText());
            dynamicSampler.setRecordType(recordtype.getText());
            dynamicSampler.setThreshold(threshold.getText());
            dynamicSampler.setUseKeepAlive(keepAlive.isSelected());
            dynamicSampler.setBlockMerge(blockMerge.isSelected());
            dynamicSampler.setLogFiles(logFiles.isSelected());

            dynamicSampler.setArgumentThreshold(argumentThreshold.isSelected());
            dynamicSampler.setStaticThreshold(staticThreshold.isSelected());
            dynamicSampler.setVariableThreshold(variableThreshold.isSelected());
            dynamicSampler.setDynamicThreshold(dynamicThreshold.isSelected());

            dynamicSampler.setOwnArguments(args);
            staticFiles.modifyTestElement(dynamicSampler);
            dynamicFiles.modifyTestElement(dynamicSampler);
            variableFiles.modifyTestElement(dynamicSampler);
        }
    }
    

    /**
     * clears all fields and boxes and initializes them.
     * Tables will be cleared as well.
     * Is called when resetting the GUI.
     */
    @Override
    public void clearGui() {
        super.clearGui();
        initFields();
        argsPanel.clear();
        variableFiles.clear();
        staticFiles.clear();
        dynamicFiles.clear();
    }

    /**
     * Initializes all fields and boxes. Is called by {@link #init()} on Panel creation and clearing.
     */
    private void initFields() {
        ep1.setText("");// $NON-NLS-1$
        ep2.setText("");// $NON-NLS-1$
        connTimeout.setText("");// $NON-NLS-1$
        respTimeout.setText("");// $NON-NLS-1$
        recordtype.setText("");// $NON-NLS-1$
        threshold.setText("");// $NON-NLS-1$
        keepAlive.setSelected(false);
        logFiles.setSelected(false);
        blockMerge.setSelected(false);
        staticThreshold.setSelected(false);
        variableThreshold.setSelected(false);
        dynamicThreshold.setSelected(false);
        attachmentNumbers.setText("");// $NON-NLS-1$
    }


    /**
     * Create a panel containing the Posts settings.
     *
     * @return the panel
     */
    private JPanel getSettingsPanel(){
        JPanel settingsPanel = new VerticalPanel(0, VerticalPanel.TOP_ALIGNMENT);
        settingsPanel.add(getWebServerTimeoutPanel());
        settingsPanel.add(getDynamicSettings());
        
        return settingsPanel;
    }

    /**
     * Create a panel containing the webserver endpoint) and timeouts (connect+request).
     *
     * @return the panel
     */
    protected JPanel getWebServerTimeoutPanel() {
        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server")));// $NON-NLS-1$

        webServerPanel.add(getEndpointsPanel(), BorderLayout.CENTER);
        webServerPanel.add(getTimeoutPanel(), BorderLayout.EAST);

        return webServerPanel;
    }

    /**
     * Create a panel containing the timeouts (connect+request).
     *
     * @return the panel
     */
    private JPanel getTimeoutPanel(){
	    JPanel timeOut = new VerticalPanel();
	    timeOut.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
	            JMeterUtils.getResString("web_server_timeout_title"))); // $NON-NLS-1$

	    connTimeout = new JTextField(10);
	    respTimeout = new JTextField(10);

	    final JPanel connPanel =  StaigerUtils.getInputPanel(JMeterUtils.getResString("web_server_timeout_connect") + "   ", connTimeout);
	    final JPanel reqPanel =  StaigerUtils.getInputPanel(JMeterUtils.getResString("web_server_timeout_response"), respTimeout);
	    timeOut.add(connPanel);
	    timeOut.add(reqPanel);
	    return timeOut;
    }

    /**
     * Create a panel containing the endpoint.
     *
     * @return the panel
     */
    private JPanel getEndpointsPanel(){
	    JPanel Endpoints = new VerticalPanel();
	    Endpoints.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Endpoints:")); // $NON-NLS-1$

        ep1 = new JTextField(10);
        ep2 = new JTextField(10);

	    final JPanel firstPanel = StaigerUtils.getInputPanel("below threshold:", ep1);
	    final JPanel secondPanel = StaigerUtils.getInputPanel("achieved threshold:", ep2);
	    Endpoints.add(firstPanel);
	    Endpoints.add(secondPanel);
	    return Endpoints;
    }

    /**
     * Create a panel containing the dynamic Post settings.
     *
     * @return the panel
     */
    protected JPanel getDynamicSettings(){
    	
    	JPanel dynamicSettings = new JPanel(new BorderLayout());
    	dynamicSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Dynamic settings:"));// $NON-NLS-1$

    	dynamicSettings.add(getFields(), BorderLayout.CENTER); 
    	dynamicSettings.add(getThresholdPanel(), BorderLayout.EAST);
    	
		return dynamicSettings;
    }

    /**
     * Create a panel containing the options concerning the dynamiic handling of the threshold.
     *
     * @return the panel
     */
    private JPanel getFields(){
    	
    	JPanel fields = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        labelConstraints.insets = new java.awt.Insets(6, 0, 0, 0);

        GridBagConstraints editConstraints = new GridBagConstraints();
        editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        editConstraints.weightx = 10.0;
        editConstraints.fill = GridBagConstraints.HORIZONTAL;
        editConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        
        int i = 0;
        StaigerUtils.addToPanel(fields, labelConstraints, 0, i, new JLabel("recordtype: "));// $NON-NLS-1$
        StaigerUtils.addToPanel(fields, editConstraints, 1, i++, recordtype = new JTextField(20));
        StaigerUtils.addToPanel(fields, labelConstraints, 0, i, new JLabel("threshold value: "));// $NON-NLS-1$
        StaigerUtils.addToPanel(fields, editConstraints, 1, i++, threshold = new JTextField(20));
        StaigerUtils.addToPanel(fields, labelConstraints, 0, i, new JLabel("dynamic Files (comma seperated): "));// $NON-NLS-1$
        StaigerUtils.addToPanel(fields, editConstraints, 1, i++,attachmentNumbers = new JTextField(20));
        StaigerUtils.addToPanel(fields, labelConstraints, 1, i,getCheckBoxes());  
    	
		return fields;
    }

    /**
     * Create a panel containing further options to log the files content
     * and whether to block overwriting the dynamic files from external config elements.
     *
     * @return the panel
     */
    private JPanel getCheckBoxes(){
    	
    	JPanel checkBoxes = new HorizontalPanel();
    	
    	//checkBoxes.setBorder(BorderFactory.createEtchedBorder());

        keepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive"));// $NON-NLS-1$
        keepAlive.setFont(null);
        logFiles = new JCheckBox("log file contents");// $NON-NLS-1$
        logFiles.setFont(null);
        blockMerge = new JCheckBox("Block external dynamic files");// $NON-NLS-1$
        blockMerge.setFont(null);

        checkBoxes.add(keepAlive);
    	checkBoxes.add(logFiles);
    	checkBoxes.add(blockMerge);
    	
		return checkBoxes;
    }

    /**
     * Create a panel containing the selection on where the thresholds should be applied on.
     *
     * @return the panel
     */
    private JPanel getThresholdPanel(){

    	argumentThreshold = new JCheckBox("Parameters");// $NON-NLS-1$
    	argumentThreshold.setFont(null);
    	variableThreshold = new JCheckBox("variable Files");// $NON-NLS-1$
    	variableThreshold.setFont(null);
    	staticThreshold = new JCheckBox("static Files");// $NON-NLS-1$
    	staticThreshold.setFont(null);
    	dynamicThreshold = new JCheckBox("dynamic Files");// $NON-NLS-1$
    	dynamicThreshold.setFont(null);

    	JPanel boxes = new VerticalPanel();
    	
    	boxes.add(argumentThreshold);
    	boxes.add(variableThreshold);
    	boxes.add(staticThreshold);
    	boxes.add(dynamicThreshold);
    	
    	boxes.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Apply threshold for:"));// $NON-NLS-1$
    	
		return boxes;
    }

    /**
     * Create a panel containing all diffrent variations of input files and parameters.
     *
     * @return the panel
     */
    protected JTabbedPane getFilePanel() {
        tabbedPane = new JTabbedPane();
        
        argsPanel = new HTTPArgumentsPanel();
        variableFiles = new VariableFilePanel("Files from variables");// $NON-NLS-1$
        staticFiles = new HTTPFileArgsPanel("Static Files");// $NON-NLS-1$
        dynamicFiles = new DynamicFilePanel("Dynamic Files (will be overwritten by \"HTTP Dynamic Files\" config element)", true, false);// $NON-NLS-1$
        tabbedPane.add(JMeterUtils.getResString("post_as_parameters"), argsPanel);// $NON-NLS-1$
        tabbedPane.add("Variable Files", variableFiles);// $NON-NLS-1$
        tabbedPane.add("Static Files", staticFiles);// $NON-NLS-1$
        tabbedPane.add("Dynamic Files", dynamicFiles);// $NON-NLS-1$
        return tabbedPane;
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        JPanel container = new JPanel(new BorderLayout());
        
        container.add(getSettingsPanel(), BorderLayout.NORTH);
        container.add(getFilePanel(), BorderLayout.CENTER);

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
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