package org.kp.cpc.helpers;

/**
 * Constants used throughout the Content Publication Console project
 * 
 * @author joshua.boyle
 */
public class SharedConstants {
	public static final String ROOT_PATH = "/content";
    public static final String START = "start";
    public static final String END = "end";
    public static final String TYPE = "type";
    
    public static final String LAST_MODIFIED_PROPERTY = "cq:lastModified";
    public static final String LAST_MODIFIED_BY_PROPERTY = "cq:lastModifiedBy";
    public static final String LAST_ACTIVATED_PROPERTY = "cq:lastReplicated";
    public static final String LAST_ACTIVATED_BY_PROPERTY = "cq:lastReplicatedBy";
    public static final String LAST_MODIFIED_KEY = "columnc";
    public static final String LAST_MODIFIED_BY_KEY = "columnb";
    public static final String LAST_ACTIVATED_KEY = "columnc";
    public static final String LAST_ACTIVATED_BY_KEY = "columnb";
    public static final String LAST_MODIFIED_HEADER = "Last Modified";
    public static final String LAST_ACTIVATED_HEADER = "Last Activated";
    public static final String LAST_MODIFIED_BY_HEADER = "Last Modified By";
    public static final String LAST_ACTIVATED_BY_HEADER = "Last Activated By";
    
    public static final String UNKNOWN_ACTIVATOR = "Unknown Activator";
    public static final String UNKNOWN_ACTIVATION_DATE = "Unknown Date";
    
    public static final String UNKNOWN_MODIFIER = "Unknown Modifier";
    public static final String UNKNOWN_MODIFICATION_DATE = "Unknown Date";
    
    public static final String ACTIVATION_REPORT = "activated";
    public static final String MODIFICATION_REPORT = "modified";
    public static final String LOCKED_REPORT = "locked";
    
    public static final String FLUSH_SERVICE_ENDPOINT = "/bin/cpc/getflushagents";
}
