package com.liveensure.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * Utility class which will help determine what operating system the application
 * code is running on.
 * 
 * @author Billy Bacon (billy@palmtreetechnology.com)
 * @version 1.0
 * @since January 8 2008
 */
public class OSUtil {
    
    /** Flag indicating that we're on Windows; initialized when this class */
    protected static boolean _isWindows;

    /** Flag indicating that we're on MacOS; initialized when this class */
    protected static boolean _isMacOS;

    /** Flag indicating that we're on Linux; initialized when this class */
    protected static boolean _isLinux;
    
    /** Flag indicating that we're on Sun OS; initialized when this class */
    protected static boolean _isSunOS;
    
    /** Flag indicating that we're on Sun OS; initialized when this class */
    protected static boolean _isBlackBerry;

    /** Flag indicating that we're on Android OS; initialized when this class */
    protected static boolean _isAndroid;

    static {
        try {
            String osname   = System.getProperty("os.name");
            String mep      = System.getProperty("microedition.platform");
            if(osname == null && mep == null) throw new NullPointerException("");
            
            // now try to match based on pattern
            // the (?i: piece makes a case-insensitive match
            if (mep != null) {
                _isBlackBerry = (java.util.regex.Pattern.matches("(?i:RIM.*)", mep));
            }
            if (osname != null) {
                _isLinux     = (java.util.regex.Pattern.matches("(?i:.*linux.*)", osname));
                _isMacOS     = (java.util.regex.Pattern.matches("(?i:.*mac.*)", osname));
                _isSunOS     = (java.util.regex.Pattern.matches("(?i:.*sun.*)", osname));
                _isWindows   = (java.util.regex.Pattern.matches("(?i:.*windows.*)", osname));
                _isAndroid   = (java.util.regex.Pattern.matches("(?i:.*android.*)", osname));
            }
        } catch (Exception e) {
            throw new RuntimeException("++ Could not determine OS. The system property"+
                    " os.name not available",e);
        }    
    }
    
    /**
     * Returns true if we're running in a JVM that identifies its operating
     * system as Windows.
     */
    public static final boolean isWindows() { return _isWindows; }

    /**
     * Returns true if we're running in a JVM that identifies its operating
     * system as MacOS.
     */
    public static final boolean isMacOS() { return _isMacOS; }
    
    /**
     * Returns true if we're running in a JVM that identifies its operating
     * system as Linux.
     */
    public static final boolean isLinux() { return _isLinux; }
    
    /**
     * Returns true if we're running in a JVM that identifies its operating
     * system as SunOS.
     */
    public static final boolean isSunOS() { return _isSunOS; }
    
    /**
     * Returns true if we're running in a JVM that identifies its operating
     * system as Android.
     */
    public static final boolean isAndroid() { return _isAndroid; }
    
    /**
     * Returns true if we're running in a JVM on a RIM device
     */
    public static final boolean isBlackBerry() { return _isBlackBerry; }


    public static String getOSType() {
        String os = null;
        
        if (OSUtil.isWindows())
            os = "Windows";
        else if (OSUtil.isMacOS())
            os = "Mac";
        else if(OSUtil.isSunOS())
            os = "SunOS";
        else if (OSUtil.isBlackBerry())
            os = "RIM";
        else if (OSUtil.isAndroid())
            os = "Android";
        else /* isLinux() or something wacky */
            os = "Linux";
        
        return os;
    }

    
    /**
     * Utility method to determine what chip the OS is running. As of this
     * writing, this is really just used to determine between Mac PPC and Mac
     * Intel.
     * 
     * @return A String representing the architecture type or chip type for the
     *         current operating system.
     */
    public static boolean isIntel() {
        String osarch = System.getProperty("os.arch");
	// updated to catch strings such as i386 as well as x86_64
        return java.util.regex.Pattern.matches("(?i:[ix][1-9]?86.*)", osarch);
    }
    
    /**
     * Utility method which will extract the native shuffler library (@see #extractNativeLibrary)
     * and then load the shuffler into memory.
     */
    public static void loadNativeLibrary() {
        File nativeLibrary = extractNativeLibrary();
        
        try {
            //System.out.println("Native path: " + nativeLibrary.getAbsolutePath());
            Runtime.getRuntime().load(nativeLibrary.getAbsolutePath());
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException("Unable to load native library for os:" +
                    getOSType(), e);
        }
    }
    
    /**
     * Utility method which will extract native library code from the IDInsure 
     * jar file.
     */
    public static File extractNativeLibrary() {
        File tmp = null;
        InputStream is  = null;
        FileOutputStream fos = null;
        String prefix   = null;
        String suffix   = null;
        String name = null;
        
        try {
            if(isLinux()||isAndroid()) {
                prefix = "libgoShuffle_linux";
                suffix = ".so";
            } else if(isMacOS()) {
                if(isIntel()) {
                    prefix = "libgoShuffle_mac_x86";
                    suffix = ".jnilib";
                } else {
                    prefix = "libgoShuffle_mac";
                    suffix = ".jnilib";
                }
            } else if(isSunOS()) {
                prefix = "libgoShuffle_sunos";
                suffix = ".so";
            } else if(isWindows()) {
                prefix = "goShuffle_win32";
                suffix = ".dll";
            }
            name = prefix + suffix;
            
            URL url = OSUtil.class.getClassLoader().getResource(name);
            
            // Check if this actually found a file:... URL
            if(url != null && "file".equals(url.getProtocol()) ) {
                try {
                    tmp = new File(url.toURI());
                } catch( java.net.URISyntaxException ex ) {
                    tmp = new File(url.getPath());
                }
            }

            if(tmp != null) return tmp;
           
            // Check if we need to extract from Classpath to temp. file
            if(tmp == null && url != null) {
                is = url.openStream();
                tmp = File.createTempFile(prefix, suffix);
                fos = new FileOutputStream(tmp);
                int nRead = -1;
                byte[] buf = new byte[2*1024];
                while ((nRead = is.read(buf)) != -1) {
                    fos.write(buf, 0, nRead);
                }
                fos.flush();
                // If we had to extract to a temp file, mark for delete-on-exit
                tmp.deleteOnExit();

            // Fall back to searching java.library.path
            } else {
                String path = System.getProperty("java.library.path");
                String[] locs = (path==null) ?  null : path.split(File.pathSeparator);
                for( int i=0; locs != null && i < locs.length; ++i ) {
                    tmp = new File(locs[i], name);
                    if( tmp.canRead() ) {
                        break;
                    }
                    tmp = null;
                }
            }
        } catch(IOException io) {
            throw new RuntimeException("Error trying to load "+name,io);
        } finally {
            try { if(is !=null) is.close(); } catch(IOException io) {}
            try { if(fos !=null) fos.close(); } catch(IOException io) {}
        }
        if( tmp == null ) {
            throw new RuntimeException("Failed to find "+name+" in classpath"+
                    " or java.library.path");
        }
        
        return tmp;
    }
    
}
