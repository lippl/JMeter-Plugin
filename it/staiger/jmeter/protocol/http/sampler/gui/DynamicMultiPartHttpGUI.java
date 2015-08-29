package it.staiger.jmeter.protocol.http.sampler.gui;
//package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
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
import it.staiger.jmeter.protocol.http.sampler.DynamicMultiPartHttp;



public class DynamicMultiPartHttpGUI extends AbstractSamplerGui{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String title = "HTTP Dynamic Post";//"http_dynamic_post";

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

    
    public DynamicMultiPartHttpGUI() {
        init();
        initFields();
    }

    @Override
    public void configure(TestElement element) {
        super.configureTestElement(element);
        if (element instanceof DynamicMultiPartHttp) {
        	DynamicMultiPartHttp dynamicSampler = (DynamicMultiPartHttp) element;
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
        DynamicMultiPartHttp sampler = new DynamicMultiPartHttp();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @param sampler
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        super.configureTestElement(sampler);

        if (sampler instanceof DynamicMultiPartHttp) {
            Arguments args = (Arguments) argsPanel.createTestElement();
            HTTPArgument.convertArgumentsToHTTP(args);
            DynamicMultiPartHttp dynamicSampler = (DynamicMultiPartHttp) sampler;
            dynamicSampler.setEndpoint1(ep1.getText());
            dynamicSampler.setEndpoint2(ep2.getText());
            dynamicSampler.setConnectTimeout(connTimeout.getText());
            dynamicSampler.setResponseTimeout(respTimeout.getText());
            dynamicSampler.setAttachmentNumbers(attachmentNumbers.getText());
            dynamicSampler.setRecordType(recordtype.getText());
            dynamicSampler.setThreshold(threshold.getText());
            dynamicSampler.setUseKeepAlive(keepAlive.isSelected());
            dynamicSampler.setBlockMerge(blockMerge.isSelected());

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
        ep1.setText("http://${host}:${port}${path_statistic}");
        ep2.setText("http://${host}:${port}${path_enforcement}");
        connTimeout.setText("10000");
        respTimeout.setText("10000");
        recordtype.setText("${recordtype}");
        threshold.setText("2");
        keepAlive.setSelected(true);
        logFiles.setSelected(false);
        blockMerge.setSelected(false);
        staticThreshold.setSelected(false);
        variableThreshold.setSelected(false);
        dynamicThreshold.setSelected(true);
        attachmentNumbers.setText("${images}");
    }
    
    /**
     * Create a panel containing the title label for the table.
     *
     * @return a panel containing the title label
     */
    private JPanel makeLabelPanel(String label) {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        labelPanel.add(new JLabel(label));
        return labelPanel;
    }

    /**
     * Create a panel containing the webserver endpoint) and timeouts (connect+request).
     *
     * @return the panel
     */
    protected final JPanel getWebServerTimeoutPanel() {
        // WEB SERVER PANEL
        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server")));

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
	    final JPanel connPanel = getconnTimeoutPanel();
	    final JPanel reqPanel = getrespTimeoutPanel();
	    timeOut.add(connPanel);
	    timeOut.add(reqPanel);
	    return timeOut;
    }

    /**
     * Create a panel containing the timeout (connect).
     *
     * @return the panel
     */
    private JPanel getconnTimeoutPanel() {
        connTimeout = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_connect")); // $NON-NLS-1$
        label.setLabelFor(connTimeout);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(connTimeout, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create a panel containing the timeout (request).
     *
     * @return the panel
     */
    private JPanel getrespTimeoutPanel() {
        respTimeout = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_response")); // $NON-NLS-1$
        label.setLabelFor(respTimeout);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(respTimeout, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create a panel containing the endpoint.
     *
     * @return the panel
     */
    private JPanel getEndpointsPanel(){
	    JPanel Endpoints = new VerticalPanel();
	    Endpoints.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Endpoints:")); // $NON-NLS-1$


	    final JPanel connPanel = getEP1Panel();
	    final JPanel reqPanel = getEP2Panel();
        keepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive"));
	    Endpoints.add(connPanel);
	    Endpoints.add(reqPanel);
	    Endpoints.add(keepAlive);
	    return Endpoints;
    }

    /**
     * Create a panel containing the first endpoint to be used when under threshold.
     *
     * @return the panel
     */
    private JPanel getEP1Panel() {
        ep1 = new JTextField(10);

        JLabel label = new JLabel("below threshold:"); // $NON-NLS-1$
        label.setLabelFor(ep1);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(ep1, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create a panel containing the first endpoint to be used when achieving the threshold.
     *
     * @return the panel
     */
    private JPanel getEP2Panel() {
        ep2 = new JTextField(10);

        JLabel label = new JLabel("achieved threshold:"); // $NON-NLS-1$
        label.setLabelFor(ep2);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(ep2, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create a panel containing the selection on where the thresholds should be applied on.
     *
     * @return the panel
     */
    private JPanel getThresholdPanel(){

    	argumentThreshold = new JCheckBox("Arguments");
    	staticThreshold = new JCheckBox("static Files");
    	variableThreshold = new JCheckBox("variable Files");
    	dynamicThreshold = new JCheckBox("dynamic Files");


    	JPanel boxes = new VerticalPanel();

    	boxes.add(argumentThreshold);
    	boxes.add(staticThreshold);
    	boxes.add(variableThreshold);
    	boxes.add(dynamicThreshold);
    	
    	boxes.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Apply threshold for:"));
    	
		return boxes;
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

        logFiles = new JCheckBox("log file contents");
        blockMerge = new JCheckBox("Block external dynamic files");

    	checkBoxes.add(logFiles);
    	checkBoxes.add(blockMerge);
    	
		return checkBoxes;
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
        addToPanel(fields, labelConstraints, 0, i, new JLabel("recordtype: "));
        addToPanel(fields, editConstraints, 1, i++, recordtype = new JTextField(20));
        addToPanel(fields, labelConstraints, 0, i, new JLabel("threshold value: "));
        addToPanel(fields, editConstraints, 1, i++, threshold = new JTextField(20));
        addToPanel(fields, labelConstraints, 0, i, new JLabel("dynamic Files (comma seperated): "));
        addToPanel(fields, editConstraints, 1, i++,attachmentNumbers = new JTextField(20));
        addToPanel(fields, labelConstraints, 1, i,getCheckBoxes());  
    	
		return fields;
    }

    /**
     * Create a panel containing the dynamic Post settings.
     *
     * @return the panel
     */
    private JPanel getDynamicSettings(){
    	
    	JPanel dynamicSettings = new JPanel(new BorderLayout());
    	dynamicSettings.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Dynamic settings:"));

    	dynamicSettings.add(getFields(), BorderLayout.CENTER); 
    	dynamicSettings.add(getThresholdPanel(), BorderLayout.EAST);
    	
		return dynamicSettings;
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
     * Create a panel containing all diffrent variations of input files and parameters.
     *
     * @return the panel
     */
    protected JTabbedPane getFilePanel() {
        tabbedPane = new JTabbedPane();
        
        argsPanel = new HTTPArgumentsPanel();
        staticFiles = new HTTPFileArgsPanel("Static Files");
        variableFiles = new VariableFilePanel("Files from variables");
        dynamicFiles = new DynamicFilePanel("Dynamic Files (will be overwritten by \"HTTP Dynamic Files\" config element)", true, false);
        tabbedPane.add(JMeterUtils.getResString("post_as_parameters"), argsPanel);// $NON-NLS-1$
        tabbedPane.add("Static Files", staticFiles);// $NON-NLS-1$
        tabbedPane.add("Variable Files", variableFiles);// $NON-NLS-1$
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

    /**
     * Utility function to add a Component to a @GridbagLayout
     * @param panel the paneld, the component is to be added.
     * @param constraints The constraints which are to be used.
     * @param col column of the GridbagLayout.
     * @param row row of the GridbagLayout.
     * @param component component which is to be added to the panel.
     */
    private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent component) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(component, constraints);
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