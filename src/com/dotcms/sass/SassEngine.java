package com.dotcms.sass;

import java.io.IOException;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;

public class SassEngine {
    public static enum Syntax {sass,scss};
    
    public static String evalSass(String input, Syntax syntax, String sassPath, String libsPath) throws IOException, ScriptException {
        
        ScriptEngine rubyEngine = new ScriptEngineManager().getEngineByName("jruby");
        ScriptContext context = rubyEngine.getContext();
        
        String script = IOUtils.toString(SassEngine.class.getResourceAsStream("sass_driver.rb"));
        rubyEngine.put("sasscode", input);
        rubyEngine.put("ext", syntax.toString());
        
        //rubyEngine.eval("$LOAD_PATH << '/home/jorgeu/dotcms/com.dotcms.sass/ROOT/dotCMS/WEB-INF/jruby-libs'",context);
        //rubyEngine.eval("$LOAD_PATH << '/home/jorgeu/dotcms/com.dotcms.sass/ROOT/dotCMS/WEB-INF/sass-gem/lib'",context);
        
        rubyEngine.eval("$LOAD_PATH << '"+libsPath+"'",context);
        rubyEngine.eval("$LOAD_PATH << '"+sassPath+"'",context);
        
        return (String)rubyEngine.eval(script, context);
    }
}
