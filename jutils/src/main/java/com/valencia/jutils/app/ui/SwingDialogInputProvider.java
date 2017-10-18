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
    
    /* (non-Javadoc)
     * @see com.valencia.jutils.app.InputProvider#getInput(java.lang.String, java.lang.String)
     */
    @Override
    public String getInput(String contextInfo, String message) throws Exception {
        String input = JOptionPane.showInputDialog(null, message, contextInfo, JOptionPane.QUESTION_MESSAGE);
        return input;
    }
    
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingDialogInputProvider ip = new SwingDialogInputProvider();
        
        System.out.println(ip.getInput("Test", "Please enter input:"));
    }

}
