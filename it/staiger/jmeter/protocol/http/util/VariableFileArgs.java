/*
 * @@@LICENSE
 *
 */

package it.staiger.jmeter.protocol.http.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

import it.staiger.jmeter.protocol.http.util.VariableFileArg;

/**
 * A set of VariableFileArg objects.
 *
 */
public class VariableFileArgs extends ConfigTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    /** The name of the property used to store the files. */
    private static final String VARIABLE_FILE_ARGS = "VariableFileArgs.files"; //$NON-NLS-1$

    /**
     * Create a new VariableFileArgs object with no files.
     */
    public VariableFileArgs() {
        setProperty(new CollectionProperty(VARIABLE_FILE_ARGS, new ArrayList<VariableFileArg>()));
    }

    /**
     * Get the files.
     *
     * @return the files
     */
    public CollectionProperty getVariableFileArgsCollection() {
        return (CollectionProperty) getProperty(VARIABLE_FILE_ARGS);
    }

    /**
     * Clear the files.
     */
    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(VARIABLE_FILE_ARGS, new ArrayList<VariableFileArg>()));
    }

    /**
     * Set the list of files. Any existing files will be lost.
     *
     * @param files the new files
     */
    public void setVariableFileArgs(List<VariableFileArg> files) {
        setProperty(new CollectionProperty(VARIABLE_FILE_ARGS, files));
    }

    /**
     * Add a new file with the given path.
     *
     * @param content the files content.
     * @param name the files name.
     */
    public void addVariableFileArg(String content, String name) {
        addVariableFileArg(new VariableFileArg(content, name));
    }

    /**
     * Add a new file.
     *
     * @param file
     *  the new file
     */
    public void addVariableFileArg(VariableFileArg file) {
        TestElementProperty newVariableFileArg = new TestElementProperty(file.getName(), file);
        if (isRunningVersion()) {
            this.setTemporary(newVariableFileArg);
        }
        getVariableFileArgsCollection().addItem(newVariableFileArg);
    }

    /**
     * adds a new File to the VariableFileArgs list to be uploaded with http
     * request.
     *
     * @param content the files content.
     * @param name the files name.
     * @param param http parameter name.
     * @param mime mime type of file.
     */
    public void addVariableFileArg(String content, String name, String param, String mime) {
        addVariableFileArg(new VariableFileArg(content, name, param, mime));
    }

    /**
     * Get a PropertyIterator of the files.
     *
     * @return an iteration of the files
     */
    public PropertyIterator iterator() {
        return getVariableFileArgsCollection().iterator();
    }

    /**
     * Get the current arguments as an array.
     *
     * @return an array of file arguments
     */
    public VariableFileArg[] asArray(){
        CollectionProperty props = getVariableFileArgsCollection();
        final int size = props.size();
        VariableFileArg[] args = new VariableFileArg[size];
        for(int i=0; i<size; i++){
            args[i]=(VariableFileArg) props.get(i).getObjectValue();
        }
        return args;
    }
    /**
     * Create a string representation of the files.
     *
     * @return the string representation of the files
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        PropertyIterator iter = getVariableFileArgsCollection().iterator();
        while (iter.hasNext()) {
            VariableFileArg file = (VariableFileArg) iter.next().getObjectValue();
            str.append(file.toString());
            if (iter.hasNext()) {
                str.append("\n"); //$NON-NLS-1$
            }
        }
        return str.toString();
    }

    /**
     * Remove the specified file from the list.
     *
     * @param row
     *  the index of the file to remove
     */
    public void removeVariableFileArg(int row) {
        if (row < getVariableFileArgCount()) {
            getVariableFileArgsCollection().remove(row);
        }
    }

    /**
     * Remove the specified file from the list.
     *
     * @param file
     *  the file to remove
     */
    public void removeVariableFileArg(VariableFileArg file) {
        PropertyIterator iter = getVariableFileArgsCollection().iterator();
        while (iter.hasNext()) {
            VariableFileArg item = (VariableFileArg) iter.next().getObjectValue();
            if (file.equals(item)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove the file with the specified path.
     *
     * @param name
     *  the name of the file to remove
     */
    public void removeVariableFileArg(String name) {
        PropertyIterator iter = getVariableFileArgsCollection().iterator();
        while (iter.hasNext()) {
            VariableFileArg file = (VariableFileArg) iter.next().getObjectValue();
            if (file.getName().equals(name)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove all files from the list.
     */
    public void removeAllVariableFileArgs() {
        getVariableFileArgsCollection().clear();
    }

    /**
     * Add a new empty file to the list. The new file will have the
     * empty string as its path.
     */
    public void addEmptyVariableFileArg() {
        addVariableFileArg(new VariableFileArg("", ""));
    }

    /**
     * Get the number of files in the list.
     *
     * @return the number of files
     */
    public int getVariableFileArgCount() {
        return getVariableFileArgsCollection().size();
    }

    /**
     * Get a single file.
     *
     * @param row
     *  the index of the file to return.
     * @return the file at the specified index, or null if no file
     *  exists at that index.
     */
    public VariableFileArg getVariableFileArg(int row) {
        VariableFileArg file = null;
        if (row < getVariableFileArgCount()) {
            file = (VariableFileArg) getVariableFileArgsCollection().get(row).getObjectValue();
        }
        return file;
    }
}
