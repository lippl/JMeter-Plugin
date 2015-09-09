/*
 * @@@LICENSE
 *
 */

package it.staiger.jmeter.protocol.http.config;

import it.staiger.jmeter.services.FileContentServer;

import java.io.Serializable;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A set of HTTPFileArg objects.
 *
 */
public class DynamicFiles extends HTTPFileArgs implements Serializable, LoopIterationListener {

    private static final long serialVersionUID = 240L;
    
    @SuppressWarnings("unused")
	private static final Logger log = LoggingManager.getLoggerForClass();
    
    public static final String PROPERTY_PREFIX = "DyanmicFiles.PROPERTY_PREFIX";
    public static final String ATTACHMENTS_CT = "DyanmicFiles.ATTACHMENTS_CT";
    public static final String ATTACHMENTS_PATH = "DyanmicFiles.ATTACHMENTS_PATH";
    public static final String SET_SHA256 = "DyanmicFiles.SET_SHA256";
    public static final String SET_ARGS = "DyanmicFiles.SET_ARGS";
    public static final String SAVE_METHOD = "DyanmicFiles.SAVE_METHOD";
    public static final String SAVE_METHOD_PROP = "Property";
    public static final String SAVE_METHOD_VAR = "Variable";
    
    boolean firstIteration=true;

    /**
     * Create a new HTTPFileArgs object with no files.
     */
    public DynamicFiles() {
        super();
    }

    /**
     * saves the files, depending on the selection, into
     * variables in the config elements thread context or
     * in global properties
     * only done once at first run of Thread, as Files should not change in-between!
     */
	@Override
	public void iterationStart(LoopIterationEvent iterEvent) {

		int i = 1;
		
        PropertyIterator iter = getHTTPFileArgsCollection().iterator();
        while (getArgs() && firstIteration==true && iter.hasNext()) {
            HTTPFileArg fileArg = (HTTPFileArg) iter.next().getObjectValue();
            String name = getPropertyPrefix() + Integer.toString(i++);
            
            /*
             * switch case not possible because compared cases are not constant strings
             */
            switch(getSaveMethod()){
            case SAVE_METHOD_PROP:
                	JMeterUtils.setProperty(name + "_Path", fileArg.getPath());
                	JMeterUtils.setProperty(name + "_ParamName", fileArg.getParamName());
                	JMeterUtils.setProperty(name + "_MimeType", fileArg.getMimeType());
	                
	    			if(getSHA256()){
	    				JMeterUtils.setProperty(name + "_SHA256", FileContentServer.getServer().getSHA256(fileArg.getPath()));
	    			}
	    			break;
            case SAVE_METHOD_VAR:
	                final JMeterContext context = getThreadContext();
	                JMeterVariables threadVars = context.getVariables();
	                
                	threadVars.put(name + "_Path", fileArg.getPath());
                	threadVars.put(name + "_ParamName", fileArg.getParamName());
                	threadVars.put(name + "_MimeType", fileArg.getMimeType());
	                
	    			if(getSHA256()){
	    				threadVars.put(name + "_SHA256", FileContentServer.getServer().getSHA256(fileArg.getPath()));
	    			}
	    			break;
            default:
	    			log.error("Invalid save method: " + getSaveMethod());
	    			break;
            }// end of if
        }// end of while loop
		
        firstIteration=false;
	}// end of method
	
	/*
	 * Are implemented by FileContentServer for now
	byte[] getFile(String path){
		try {
			return IOUtils.toByteArray(new FileInputStream(FileServer.getFileServer().getResolvedFile(path)));
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}
	
	private String getSHA256(String path){
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return new String(Hex.encodeHex(md.digest(getFile(path))));
		} catch (NoSuchAlgorithmException e2) {
			e2.printStackTrace();
			return null;
		}
	}
	*/
	
    /*
     * Setters
     */

	/**
	 * Utility function to ensure, that the property name does not contain blanks.
	 * If it does, they will be replaced by underscores.
	 * @param text Prefix which is to be set and checked for blanks first.
	 */
    public void setPropertyPrefix(String text) {
    	if(text.contains(" ")){
    		log.warn("Property prefix must not contain blanks! underscore (_) used instead.");
    		text = text.replaceAll("\\s+", "_");
    	}
        setProperty(PROPERTY_PREFIX, text);
    }

    public void setAttachmentsCT(String text) {
        setProperty(ATTACHMENTS_CT, text);
    }

    public void setSaveMethod(String text) {
        setProperty(SAVE_METHOD, text);
    }
    
    public void setSHA256(boolean text) {
        setProperty(SET_SHA256, text);
    }

    public void setArgs(boolean text) {
        setProperty(SET_ARGS, text);
    }
    
    public void setRelativePath(String text) {
        setProperty(ATTACHMENTS_PATH, text);
    }
    
    
    /*
     * Getters
     */

    public String getPropertyPrefix() {
        return getPropertyAsString(PROPERTY_PREFIX);
    }

    public String getAttachmentsCT() {
        return getPropertyAsString(ATTACHMENTS_CT);
    }

    public String getRelativePath() {
        return getPropertyAsString(ATTACHMENTS_PATH);
    }

    public String getSaveMethod() {
        return getPropertyAsString(SAVE_METHOD);
    }
    
    public boolean getSHA256() {
        return getPropertyAsBoolean(SET_SHA256);
    }

    public boolean getArgs() {
    	return getPropertyAsBoolean(SET_ARGS);
    }
}
