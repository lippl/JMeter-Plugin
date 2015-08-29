package it.staiger.jmeter.protocol.http.sampler;
//package org.apache.jmeter.protocol.http.sampler;

import it.staiger.jmeter.protocol.http.config.DynamicFiles;
import it.staiger.jmeter.protocol.http.sampler.HTTPHC4DynFiles;
import it.staiger.jmeter.protocol.http.util.VariableFileArgs;
import it.staiger.jmeter.protocol.http.util.VariableFileArg;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class DynamicMultiPartHttp extends HTTPSamplerBase implements Interruptible {
	private static final long serialVersionUID = 240L;
	
    @SuppressWarnings("unused")
	private static final Logger log = LoggingManager.getLoggerForClass();
    
    public static final String EP1 = "DynMP.EP1";
    public static final String EP2 = "DynMP.EP2";
    public static final String FULL_REQUEST = "DynMP.FULL_REQUEST";
    public static final String ATTACHMENT_NUMBERS = "DynMP.ATTACHMENT_NUMBERS";
    public static final String RECORDTYPE = "DynMP.RECORDTYPE";
    public static final String THRESHOLD = "DynMP.THRESHOLD";
    public static final String LOG_FILES = "DynMP.LOG_FILES";
    public static final String BLOCK_MERGE = "DynMP.BLOCK_MERGE";
    public static final String OWN_ARGUMENTS = "DynMP.ARGUMENTS";
    public static final String ARGUMENT_THRESHOLD = "DynMP.ARGUMENT_THRESHOLD";
    public static final String STATIC_THRESHOLD = "DynMP.STATIC_THRESHOLD";
    public static final String VARIABLE_THRESHOLD = "DynMP.VARIABLE_THRESHOLD";
    public static final String DYNAMIC_THRESHOLD = "DynMP.DYNAMIC_THRESHOLD";
    public static final String VARIABLE_FILE_ARGS = "DynMP.VARIABLE_FILE_ARGS";
    public static final String DYNAMIC_FILE_ARGS = "DynMP.DYNAMIC_FILE_ARGS";
    
    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<String>(
            Arrays.asList(new String[]{
                    "it.staiger.jmeter.protocol.http.config.gui.DynamicFilePanel"}));

    private final transient HTTPHC4DynFiles hc;
    
    public DynamicMultiPartHttp(){
        hc = new HTTPHC4DynFiles(this);
    }

    @Override
    public boolean interrupt() {
        return hc.interrupt();
    }
    
    /**
     * Sets up the correct endpoint depending on the threshold.
     */
    @Override
    public SampleResult sample(Entry e) {
    	
    	URL endpoint = null;
    	
    	setMethod(HTTPConstants.POST);
    	setDoMultipartPost(true);

    	//set correct Endpoint
        try{
	    	if(getRecordType()<getRecordType())
	    		endpoint = new URL(getEndpoint1());
	    	else{
	    		endpoint = new URL(getEndpoint2());
	    	}
        }catch(MalformedURLException ex){
        	ex.getMessage();
        }

    	//give to HTTPClient for execution
    	return sample(endpoint, HTTPConstants.POST, false, 0);
    }

    /**
     * Executes POSt in class {@link HTTPHC4DynFiles}.
     */
    @Override
    protected HTTPSampleResult sample(URL u, String method,
            boolean areFollowingRedirect, int depth) {
        return hc.sample(u, method, areFollowingRedirect, depth);
    }
    

    /*
     * Method to set files list to be uploaded.
     *
     * @param value
     *   STATICFileArgs object that stores file list to be uploaded.
     */
    private void setVariableFileArgs(VariableFileArgs value) {
        if (value.getVariableFileArgCount() > 0){
            setProperty(new TestElementProperty(VARIABLE_FILE_ARGS, value));
        } else {
            removeProperty(VARIABLE_FILE_ARGS); // no point saving an empty list
        }
    }

    /*
     * Method to get files list to be uploaded.
     */
    private VariableFileArgs getVariableFileArgs() {
        return (VariableFileArgs) getProperty(VARIABLE_FILE_ARGS).getObjectValue();
    }

    /**
     * Get the collection of files as a list.
     * The list is built up from the filename/filefield/mimetype properties,
     * plus any additional entries saved in the FILE_ARGS property.
     *
     * If there are no valid file entries, then an empty list is returned.
     *
     * @return an array of file arguments (never null)
     */
    public VariableFileArg[] getVariableFiles() {
        final VariableFileArgs fileArgs = getVariableFileArgs();
        return fileArgs == null ? new VariableFileArg[] {} : fileArgs.asArray();
    }

    public int getVariableFileCount(){
        return getVariableFiles().length;
    }
    /**
     * Saves the list of files.
     * The first file is saved in the Filename/field/mimetype properties.
     * Any additional files are saved in the FILE_ARGS array.
     *
     * @param files list of files to save
     */
    public void setVariableFiles(VariableFileArg[] files) {
        VariableFileArgs fileArgs = new VariableFileArgs();
        // Weed out the empty files
        if (files.length > 0) {
            for(int i=0; i < files.length; i++){
                VariableFileArg file = files[i];
                if (file.isNotEmpty()){
                    fileArgs.addVariableFileArg(file);
                }
            }
        }
        setVariableFileArgs(fileArgs);
    }   

    /*
     * Method to set files list to be uploaded.
     *
     * @param value
     *   HTTPFileArgs object that stores file list to be uploaded.
     */
    private void setDynamicFiles(HTTPFileArgs value) {
        if (value.getHTTPFileArgCount() > 0){
            setProperty(new TestElementProperty(DYNAMIC_FILE_ARGS, value));
        } else {
            removeProperty(DYNAMIC_FILE_ARGS); // no point saving an empty list
        }
    }

    /*
     * Method to get files list to be uploaded.
     */
    private HTTPFileArgs getDynamicFileArgs() {
        return (HTTPFileArgs) getProperty(DYNAMIC_FILE_ARGS).getObjectValue();
    }

    /**
     * Get the collection of files as a list.
     * The list is built up from the filename/filefield/mimetype properties,
     * plus any additional entries saved in the FILE_ARGS property.
     *
     * If there are no valid file entries, then an empty list is returned.
     *
     * @return an array of file arguments (never null)
     */
    public HTTPFileArg[] getDynamicFiles() {
        final HTTPFileArgs fileArgs = getDynamicFileArgs();
        return fileArgs == null ? new HTTPFileArg[] {} : fileArgs.asArray();
    }

    public int getDynamicFileCount(){
        return getDynamicFiles().length;
    }
    /**
     * Saves the list of files.
     * The first file is saved in the Filename/field/mimetype properties.
     * Any additional files are saved in the FILE_ARGS array.
     *
     * @param files list of files to save
     */
    public void setDynamicFiles(HTTPFileArg[] files) {
        HTTPFileArgs fileArgs = new HTTPFileArgs();
        // Weed out the empty files
        if (files.length > 0) {
            for(int i=0; i < files.length; i++){
                HTTPFileArg file = files[i];
                if (file.isNotEmpty()){
                    fileArgs.addHTTPFileArg(file);
                }
            }
        }
        setDynamicFiles(fileArgs);
    }

    @Override
    public void addTestElement(TestElement el) {
        if (el instanceof DynamicFiles && !getBlockMerge()) {
        	setDynamicFiles(((HTTPFileArgs) el).asArray());
        } else {
            super.addTestElement(el);
        }
    }

    /**
     * Setters
     */

    public void setEndpoint1(String text) {
        setProperty(EP1, text);
    }

    public void setEndpoint2(String text) {
        setProperty(EP2, text);
    }

    public void setAttachmentNumbers(String text) {
        setProperty(ATTACHMENT_NUMBERS, text);
    }

    public void setRecordType(String text) {
        setProperty(RECORDTYPE, text);
    }

    public void setThreshold(String text) {
        setProperty(THRESHOLD, text);
    }
    
    public void setLogFiles(boolean selected) {
        setProperty(LOG_FILES, selected);
    }
    
    public void setBlockMerge(boolean selected) {
        setProperty(BLOCK_MERGE, selected);
    }

    public void setOwnArguments(Arguments value) {
        setProperty(new TestElementProperty(OWN_ARGUMENTS, value));
    }
    
    public void setArgumentThreshold(boolean selected) {
        setProperty(ARGUMENT_THRESHOLD, selected);
    }
    
    public void setStaticThreshold(boolean selected) {
        setProperty(STATIC_THRESHOLD, selected);
    }
    
    public void setVariableThreshold(boolean selected) {
        setProperty(VARIABLE_THRESHOLD, selected);
    }
    
    public void setDynamicThreshold(boolean selected) {
        setProperty(DYNAMIC_THRESHOLD, selected);
    }

    /**
     * Getters
     */
    public String getEndpoint1() {
        return getPropertyAsString(EP1);
    }

    public String getEndpoint2() {
        return getPropertyAsString(EP2);
    }

    public String getConnectTimeoutAsString() {
        return getPropertyAsString(CONNECT_TIMEOUT);
    }

    public String getResponseTimeoutAsString() {
        return getPropertyAsString(RESPONSE_TIMEOUT);
    }

    public int getRecordType() {
        return getPropertyAsInt(RECORDTYPE);
    }

    public String getRecordTypeAsString() {
        return getPropertyAsString(RECORDTYPE);
    }

    public int getThreshold() {
        return getPropertyAsInt(THRESHOLD);
    }

    public String getThresholdAsString() {
        return getPropertyAsString(THRESHOLD);
    }

    public String getAttachmentNumbers() {
        return getPropertyAsString(ATTACHMENT_NUMBERS);
    }

    public boolean getLogFiles() {
        return getPropertyAsBoolean(LOG_FILES);
    }

    public boolean getBlockMerge() {
        return getPropertyAsBoolean(BLOCK_MERGE);
    }

    public Arguments getOwnArguments() {
        return (Arguments) getProperty(OWN_ARGUMENTS).getObjectValue();
    }

    public boolean getArgumentThreshold() {
        return getPropertyAsBoolean(ARGUMENT_THRESHOLD);
    }

    public boolean getStaticThreshold() {
        return getPropertyAsBoolean(STATIC_THRESHOLD);
    }

    public boolean getVariableThreshold() {
        return getPropertyAsBoolean(VARIABLE_THRESHOLD);
    }

    public boolean getDynamicThreshold() {
        return getPropertyAsBoolean(DYNAMIC_THRESHOLD);
    }


    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return (APPLIABLE_CONFIG_CLASSES.contains(guiClass) || super.applies(configElement));
    }
    
}