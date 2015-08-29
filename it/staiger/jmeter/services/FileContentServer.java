package it.staiger.jmeter.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.jmeter.services.FileServer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Only to be used with Files from {@link org.apache.jmeter.services.FileServer} class!
 */
public class FileContentServer {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final Map<File, byte[]> files = new HashMap<>();

    private static final FileContentServer server = new FileContentServer();

    // Cannot be instantiated
    private FileContentServer() {
    }

    /**
     * @return the singleton instance of the server.
     */
    public static FileContentServer getServer() {
        return server;
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename - relative (to base) or absolute file name (must not be null)
     */
    public void reserveFile(String path) {
        reserveFile(FileServer.getFileServer().getResolvedFile(path));
    }

    /**
     * Creates an association between a filename and a File inputOutputObject,
     * and stores it for later use - unless it is already stored.
     *
     * @param filename - relative (to base) or absolute file name (must not be null)
     * @param charsetName - the character set encoding to use for the file (may be null)
     */
    private void reserveFile(File file) {
    	if(files.get(file)==null){
    		files.put(file, getFile(file));
    		log.debug("added " + file.getPath());
    	}else{
    		log.debug(file.getPath() + " already reserved");
    		
    	}
    }
    

	private byte[] getFile(File file){
		try {
			return IOUtils.toByteArray(new FileInputStream(file));
		} catch (IOException e1) {
			log.error("Could not read file " + file.getPath());
			e1.printStackTrace();
			return null;
		}
	}

    /**
     * Get {@link File} instance for provided file path,
     * resolve file location relative to base dir or script dir when needed
     * @param path original path to file, maybe relative
     * @return {@link File} instance 
     */
    public byte[] getResolvedFile(String file) {
        return getResolvedFile(FileServer.getFileServer().getResolvedFile(file));
    }
    /**
     * Get byte[] for provided file path,
     * @param path original path to file, maybe relative
     * @return {@link File} instance 
     */
    private byte[] getResolvedFile(File file) {
        reserveFile(file);
        return files.get(file);
    }

    /**
     * Get byte[] for provided file path,
     * @param path original path to file, maybe relative
     * @return {@link File} instance 
     */
    public String getSHA256(String path) {
        return getSHA256(FileServer.getFileServer().getResolvedFile(path));
    }
    
    private String getSHA256(File file) {
        reserveFile(file);
        return getSHA256(files.get(file));
    }
    
	private String getSHA256(byte[] content){
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return new String(Hex.encodeHex(md.digest(content)));
		} catch (NoSuchAlgorithmException e2) {
			e2.printStackTrace();
			return null;
		}
	}
	
    public synchronized void clearFiles() {
        files.clear();
    }
}