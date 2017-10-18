/**
 * 
 */
package com.valencia.jutils.app.ui;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.valencia.jutils.app.InputProvider;

/**
 * An input provider that requests input from a Swing dialog box.
 * 
 * @author Gabriel Valencia, gee4vee@me.com
 */
public class SwingDialogInputProvider extends InputProvider {

    public SwingDialogInputProvider() {
        // TODO support parent component on input dialog
    }
    
    @Override
    public String getInput(String contextInfo, String message) throws Exception {
        String input = JOptionPane.showInputDialog(null, message, contextInfo, JOptionPane.QUESTION_MESSAGE);
        if (input == null) {
            return INPUT_CANCEL;
        }
        return input;
    }
    
    @Override
    public String getInput(String contextInfo) throws Exception {
        return this.getInput(contextInfo, "");
    }
    
    @Override
    public boolean getBooleanInput(String contextInfo) throws Exception {
        return this.getBooleanInput(contextInfo, "");
    }
    
    @Override
    public boolean getBooleanInput(String contextInfo, String message) throws Exception {
        int input = JOptionPane.showConfirmDialog(null, message, contextInfo, JOptionPane.YES_NO_CANCEL_OPTION);
        boolean result;
        if (input == JOptionPane.NO_OPTION || input == JOptionPane.CANCEL_OPTION) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }
    
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingDialogInputProvider ip = new SwingDialogInputProvider();
        
        System.out.println(ip.getInput("Test", "Please enter input:"));
    }

}
