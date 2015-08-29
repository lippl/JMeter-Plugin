/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

package it.staiger.jmeter.protocol.http.util;

import java.io.Serializable;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * Class representing a file parameter for http upload.
 * Consists of a http parameter name/file name pair with (optional) mimetype.
 *
 * Also provides temporary storage for the headers which are sent with files.
 *
 */
public class VariableFileArg extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    /** Name used to store the file's content. */
    private static final String CONTENT = "File.content";

    /** Name used to store the file's name. */
    private static final String FILENAME = "File.name";

    /** Name used to store the file's paramname. */
    private static final String PARAMNAME = "File.paramname";

    /** Name used to store the file's mimetype. */
    private static final String MIMETYPE = "File.mimetype";

    /** temporary storage area for the body header. */
    private String header;

    /**
     * Constructor for an empty VariableFileArg object
     */
    public VariableFileArg() {
    }

    /**
     * Constructor for the VariableFileArg object with given name.
     *
     * @param name
     *            name to the file to use
     * @throws IllegalArgumentException
     *             if <code>name</code> is <code>null</code>
     */
    public VariableFileArg(String content, String name) {
        this(content, name, "", "");
    }

    /**
     * Constructor for the VariableFileArg object with full information.
     *
     * @param content
     *            content of the file
     * @param name
     *            name of the file to use
     * @param paramname
     *            name of the http parameter to use for the file
     * @param mimetype
     *            mimetype of the file
     * @throws IllegalArgumentException
     *             if any parameter is <code>null</code>
     */
    public VariableFileArg(String content, String name, String paramname, String mimetype) {
        if (content == null || name == null || paramname == null || mimetype == null){
            throw new IllegalArgumentException("Parameters must not be null");
        }
        setContent(content);
        setName(name);
        setParamName(paramname);
        setMimeType(mimetype);
    }

    /**
     * Constructor for the VariableFileArg object with full information,
     * using existing properties
     *
     * @param content
     *            content of the file
     * @param name
     *            name of the file to use
     * @param paramname
     *            name of the http parameter to use for the file
     * @param mimetype
     *            mimetype of the file
     * @throws IllegalArgumentException
     *             if any parameter is <code>null</code>
     */
    public VariableFileArg(JMeterProperty content, JMeterProperty name, JMeterProperty paramname, JMeterProperty mimetype) {
        if (content == null || name == null || paramname == null || mimetype == null){
            throw new IllegalArgumentException("Parameters must not be null");
        }
        setProperty(CONTENT, content);
        setProperty(FILENAME, name);
        setProperty(MIMETYPE, mimetype);
        setProperty(PARAMNAME, paramname);
    }

    private void setProperty(String name, JMeterProperty prop) {
        JMeterProperty jmp = prop.clone();
        jmp.setName(name);
        setProperty(jmp);
    }

    /**
     * Copy Constructor.
     *
     * @param file
     *            {@link VariableFileArg} to get information about the name, http
     *            parameter name and mimetype of the file
     * @throws IllegalArgumentException
     *             if any of those retrieved information is <code>null</code>
     */
    public VariableFileArg(VariableFileArg file) {
        this(file.getContent(), file.getName(), file.getParamName(), file.getMimeType());
    }

    /**
     * Set the http parameter name of the File.
     *
     * @param newParamName
     * the new http parameter name
     */
    public void setParamName(String newParamName) {
        setProperty(new StringProperty(PARAMNAME, newParamName));
    }

    /**
     * Get the http parameter name of the File.
     *
     * @return the http parameter name
     */
    public String getParamName() {
        return getPropertyAsString(PARAMNAME);
    }

    /**
     * Set the mimetype of the File.
     *
     * @param newMimeType
     * the new mimetype
     */
    public void setMimeType(String newMimeType) {
        setProperty(new StringProperty(MIMETYPE, newMimeType));
    }

    /**
     * Get the mimetype of the File.
     *
     * @return the http parameter mimetype
     */
    public String getMimeType() {
        return getPropertyAsString(MIMETYPE);
    }

    /**
     * Set the name of the File.
     *
     * @param newName
     *  the new name
     */
    public void setName(String newName) {
        setProperty(new StringProperty(FILENAME, newName));
    }

    /**
     * Get the name of the File.
     *
     * @return the file's name
     */
    public String getName() {
        return getPropertyAsString(FILENAME);
    }

    /**
     * Set the content of the File.
     *
     * @param newContent
     *  the new content
     */
    public void setContent(String newContent) {
        setProperty(new StringProperty(CONTENT, newContent));
    }

    /**
     * Get the content of the File.
     *
     * @return the file's name
     */
    public String getContent() {
        return getPropertyAsString(CONTENT);
    }

   /**
    * Sets the body header for the VariableFileArg object. Header
    * contains name, parameter name and mime type information.
    * This is only intended for use by methods which need to store information
    * temporarily whilst creating the HTTP body.
    * 
    * @param newHeader
    *  the new Header value
    */
   public void setHeader(String newHeader) {
       header = newHeader;
   }

   /**
    * Gets the saved body header for the VariableFileArg object.
    *
    * @return saved body header
    */
   public String getHeader() {
       return header;
   }

    /**
     * returns name, param name, mime type information of
     * VariableFileArg object.
     *
     * @return the string demonstration of VariableFileArg object in this
     * format:
     *    "name:'&lt;PATH&gt;'|param:'&lt;PARAM NAME&gt;'|mimetype:'&lt;MIME TYPE&gt;'"
     */
    @Override
    public String toString() {
        return "name:'" + getName()
            + "'|param:'" + getParamName()
            + "'|mimetype:'" + getMimeType()
        	+ "'|content:'" + getContent() + "'";
    }

    /**
     * Check if the entry is not empty.
     * @return true if Path, name or mimetype fields are not the empty string
     */
    public boolean isNotEmpty() {
        return getName().length() > 0 && getContent().length() > 0;
    }

}
