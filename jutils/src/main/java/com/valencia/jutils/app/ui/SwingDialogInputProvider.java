/**
 * 
 */
package com.valencia.jutils.app.ui;

import java.awt.Component;
import java.io.File;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.valencia.jutils.app.InputProvider;

/**
 * An input provider that requests input from a Swing dialog box.
 * 
 * @author Gabriel Valencia, gee4vee@me.com
 */
public class SwingDialogInputProvider extends InputProvider {
    
    public static final String ID = SwingDialogInputProvider.class.getSimpleName();
    
    public static final String PROP_KEY_DIALOG_PARENT_COMPONENT = "SwingDialogParent";
    
    public static final String PROP_KEY_DIALOG_TYPE = "SwingFileDialogType";
    public static final String PROP_VALUE_FILE_OPEN_DIALOG = "SwingFileOpenDialog";
    public static final String PROP_VALUE_FILE_SAVE_DIALOG = "SwingFileSaveDialog";
    
    public static final String PROP_KEY_FILE_SELECT_MODE = "SwingDialogFileSelectMode";
    public static final String PROP_VALUE_FILES_ONLY = "SwingDialogFilesOnly";
    public static final String PROP_VALUE_DIRS_ONLY = "SwingDialogDirsOnly";
    public static final String PROP_VALUE_FILES_AND_DIRS = "SwingDialogFilesAndDirs";

    public SwingDialogInputProvider() {
        this.id = ID;
        // TODO support parent component on input dialog
    }
    
    @Override
    public String getInput(String contextInfo, String message, Map<String, Object> properties) throws Exception {
        String input = JOptionPane.showInputDialog(null, message, contextInfo, JOptionPane.QUESTION_MESSAGE);
        if (input == null) {
            return INPUT_CANCEL;
        }
        return input;
    }
    
    @Override
    public String getInput(String contextInfo, String message) throws Exception {
        return this.getInput(contextInfo, message, null);
    }

    @Override
    public String getInput(String contextInfo) throws Exception {
        return this.getInput(contextInfo, "", null);
    }
    
    @Override
    public boolean getBooleanInput(String contextInfo) throws Exception {
        return this.getBooleanInput(contextInfo, "", null);
    }
    
    @Override
    public boolean getBooleanInput(String contextInfo, String message, Map<String, Object> properties) throws Exception {
        int input = JOptionPane.showConfirmDialog(null, message, contextInfo, JOptionPane.YES_NO_CANCEL_OPTION);
        boolean result;
        if (input == JOptionPane.NO_OPTION || input == JOptionPane.CANCEL_OPTION) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }
    
    @Override
    public File getFileInput(String contextInfo, String message, Map<String, Object> properties) throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(message);
        String typeStr = null;
        if (properties.containsKey(PROP_KEY_DIALOG_TYPE)) {
            typeStr = properties.get(PROP_KEY_DIALOG_TYPE).toString();
            if (PROP_VALUE_FILE_OPEN_DIALOG.equals(typeStr)) {
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            } else if (PROP_VALUE_FILE_SAVE_DIALOG.equals(typeStr)) {
                chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            }
        }
        
        if (properties.containsKey(PROP_KEY_FILE_SELECT_MODE)) {
            String mode = properties.get(PROP_KEY_FILE_SELECT_MODE).toString();
            if (PROP_VALUE_FILES_ONLY.equals(mode)) {
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            } else if (PROP_VALUE_DIRS_ONLY.equals(mode)) {
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            } else {
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            }
        } else {
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
        
        Component parent = null;
        if (properties.containsKey(PROP_KEY_DIALOG_PARENT_COMPONENT)) {
            parent = (Component)properties.get(PROP_KEY_DIALOG_PARENT_COMPONENT);
        }
        
        int result;
        if (typeStr != null) {
            if (PROP_VALUE_FILE_OPEN_DIALOG.equals(typeStr)) {
                result = chooser.showOpenDialog(parent);
                
            } else if (PROP_VALUE_FILE_SAVE_DIALOG.equals(typeStr)) {
                result = chooser.showSaveDialog(parent);
                
            } else {
                result = chooser.showDialog(parent, "Select");
            }
            
        } else {
            result = chooser.showDialog(parent, "Select");
        }
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            return selectedFile;
            
        } else {
            return null;
        }
    }
    
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingDialogInputProvider ip = new SwingDialogInputProvider();
        
        System.out.println("requesting string input...");
        System.out.println(ip.getInput("Test", "Please enter input:"));

        System.out.println("requesting file input...");
        System.out.println(ip.getFileInput("Test", "Please choose a file"));
    }

}
