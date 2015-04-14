package burp;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

public class BurpExtender implements IBurpExtender, IContextMenuFactory{
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private static PrintWriter burpStdout;
    private static PrintWriter burpStderr;
    
    private final static String TARGET_CHARASET_UTF_8 = "UTF-8";
    private final static String TARGET_CHARASET_SJIS = "Shift_JIS";
    private final static String TARGET_CHARASET_EUC_JP = "EUC-JP";
    private final static String EXTENDER_NAME = "OgaCopy v1.1";
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks){
        this.callbacks = callbacks;        
        helpers = callbacks.getHelpers();        
        callbacks.setExtensionName(EXTENDER_NAME);
        callbacks.registerContextMenuFactory(this);
        burpStdout = new PrintWriter(callbacks.getStdout(), true);
        burpStderr = new PrintWriter(callbacks.getStderr(), true);
        
        burpStdout.println(EXTENDER_NAME + " Load OK!");
    }
    
    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        List<JMenuItem> addMenuItemList = new ArrayList<>();
        addMenuItemList.add(newAddMenuItem(invocation,TARGET_CHARASET_UTF_8));
        addMenuItemList.add(newAddMenuItem(invocation,TARGET_CHARASET_SJIS));
        addMenuItemList.add(newAddMenuItem(invocation,TARGET_CHARASET_EUC_JP));
        return addMenuItemList;
    }
    
    private JMenuItem newAddMenuItem(IContextMenuInvocation invocation,String menuItemName){
    	JMenuItem addItem = new JMenuItem(getMenuItemName(menuItemName));
        addItem.addActionListener(e -> copyAction(invocation,menuItemName));
        return addItem;
    }
    
    private String getMenuItemName(String chareSet){
        return "[OgaCopy] copy by " + chareSet;
    }
    
    private void copyAction(IContextMenuInvocation invocation,String targetChareSet){
        
        IHttpRequestResponse [] httpReqRes = invocation.getSelectedMessages();
        if(httpReqRes.length < 0){
            return;
        }
    
        byte [] targetHttpMessage = null;
        switch (invocation.getInvocationContext()) {
            case IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST :
            case IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST :
                targetHttpMessage = httpReqRes[0].getRequest();
                break;
            case IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_RESPONSE :
            case IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_RESPONSE :
                targetHttpMessage = httpReqRes[0].getResponse();
                break;
        }
        
        int[] selectBounds =  invocation.getSelectionBounds();           
        int beginIndex = -1;
        int endIndex = -1;
        if (selectBounds.length == 2){
              beginIndex = selectBounds[0]; 
              endIndex =  selectBounds[1];
        }else{
            return;
        }
    
        byte[] tmpByte = new byte[endIndex-beginIndex];
        int tmpByteIndex = 0;
        for(int i=beginIndex;i<endIndex;i++){
            tmpByte[tmpByteIndex] = targetHttpMessage[i];
            tmpByteIndex++;
        }
        
        String retString = null;
        try {
            retString = new String(tmpByte,targetChareSet);
        } catch (Exception e1) {
            burpStderr.println(e1.getMessage());
        }
        
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(retString);
        clipboard.setContents(stringSelection, stringSelection);        
    }
}
