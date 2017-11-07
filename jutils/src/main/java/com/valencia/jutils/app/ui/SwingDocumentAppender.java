/**
 * 
 */
package com.valencia.jutils.app.ui;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * <p>An appender that writes log events to a Swing Document that can be used in a <code>JTextArea</code> or similar UI component.
 * Set up this appender as follows:
 * <ol>
 * <li>Add this class' package to the <code>packages</code> attribute in your log4j2 configuration. For example, 
 * if using a properties file, you would add this line: <code>packages = com.valencia.jutils.app.ui</code>.
 * <li>In the log4j configuration, define a new appender and set its type to the plugin name defined for this class. You can define 
 * additional attributes such as the layout and filter much like you would for the Console Appender.
 * <li>Make sure your logger(s) reference this new appender.
 * <li>In your Swing code, call {@link JTextArea#setDocument(Document)}, passing in the return value of 
 * {@link SwingDocumentAppender#getDocument(String)}, where you specify the name of the appender from your log4j configuration. 
 * <li>In order to update the text area so that it scrolls down to the latest log event, add a listener to the returned <code>Document</code> 
 * that will make the following call in all its methods (assuming your text area is named logArea):<br>
 * <code>logArea.setCaretPosition(logArea.getText().length() - 1)</code>
 * <li>If you still are not seeing log events in the text area, insert some text into the <code>Document</code> after the above, e.g.:<br>
 * <code>Document doc = SwingDocumentAppender.getDocument("LogTextArea");<br>
 * ...<br>
 * synchronized (doc) {<br>                                        
   &nbsp;&nbsp;&nbsp;&nbsp;try {<br>                                                
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;doc.insertString(0, "Whatever you want here...", null);<br>     
   &nbsp;&nbsp;&nbsp;&nbsp;} catch (BadLocationException e1) { }<br>                 
  }
 * </code><br>
 * This explicit insertion only needs to happen once. The synchronized block is necessary in order to coordinate with incoming log events.
 * </ol>
 * 
 * @author Gabriel Valencia, <gee4vee@me.com>
 */
@Plugin(name = "SwingDocAppender", category = "Core", elementType = "appender", printObject = true)
public class SwingDocumentAppender extends AbstractAppender {
    
    /**
     * The key is the appender name, the value is the associated <code>Document</code>.
     */
    private static final Map<String, Document> textAreaDocs = new ConcurrentHashMap<>();
    
    public static Document getDocument(String appenderName) {
        Document doc = textAreaDocs.computeIfAbsent(appenderName, (name) -> {
            return new PlainDocument();
        });
        return doc;
    }

    /**
     * @param name
     * @param filter
     * @param layout
     */
    public SwingDocumentAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout);
    }

    /**
     * @param name
     * @param filter
     * @param layout
     * @param ignoreExceptions
     */
    @PluginFactory
    public static SwingDocumentAppender createAppender(@PluginAttribute("name") String name,
                                                    @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
                                                    @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                    @PluginElement("Filters") Filter filter) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new SwingDocumentAppender(name, filter, layout);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.core.LogEvent)
     */
    @Override
    public void append(LogEvent event) {
        byte[] eventBytes = this.getLayout().toByteArray(event);
        String eventStr;
        try {
            eventStr = new String(eventBytes, "UTF-8");
            Document doc = getDocument(this.getName());
            synchronized (doc) {
                doc.insertString(doc.getLength()-1, eventStr, null);
            }
        } catch (UnsupportedEncodingException | BadLocationException e) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(e);
            }
        }
    }

}
