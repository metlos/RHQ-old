/*
 * RHQ Management Platform
 * Copyright (C) 2005-2011 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.rhq.bindings.engine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.python.jsr223.PyScriptEngineFactory;

/**
 * 
 *
 * @author Lukas Krejci
 */
public class JythonEngineInitializer implements ScriptEngineInitializer {

    private PyScriptEngineFactory engineFactory = new PyScriptEngineFactory();
    
    @Override
    public boolean implementsLanguage(String language) {
        return engineFactory.getLanguageName().equals(language); 
    }

    @Override
    public ScriptEngine instantiate(Set<String> packages) throws ScriptException {
        ScriptEngine ret = engineFactory.getScriptEngine();
        
        for(String packageName : packages) {
            try {
                for(String className : getClassNamesInPackage(packageName)) {
                    ret.eval("from " + packageName + " import " + className);
                }
            } catch (Exception e) {
                throw new ScriptException(e);
            }
        }
        
        return ret;
    }

    @Override
    public String generateIndirectionMethod(String boundObjectName, Method method) {
        String methodName = method.getName();
        int argCount = method.getParameterTypes().length;

        StringBuilder functionBuilder = new StringBuilder();
        functionBuilder.append(methodName).append("(");
        for (int i = 0; i < argCount; ++i) {
            if (i != 0) {
                functionBuilder.append(", ");
            }
            functionBuilder.append("arg_" + i);
        }
        functionBuilder.append(")");
        String functionFragment = functionBuilder.toString();
        boolean returnsVoid = method.getReturnType().equals(Void.TYPE);

        String functionDefinition = "def " + functionFragment + ":\n\t " + (returnsVoid ? "" : "return ")
            + boundObjectName + "." + functionFragment + "\n";
        
        return functionDefinition;
    }

    @Override
    public String extractUserFriendlyErrorMessage(ScriptException e) {
        return e.getMessage();
    }

    private static List<String> getClassNamesInPackage(String packageName) throws IOException, ClassNotFoundException {
        String packagePath = packageName.replace(".", "/");
        
        Enumeration<URL> classes = getClassLoaderToUse().getResources(packagePath);
        
        List<String> ret = new ArrayList<String>();
        
        while (classes.hasMoreElements()) {
            URL url = classes.nextElement();
            File dir = new File(url.getFile());
            
            if (dir.exists() && dir.isDirectory() && dir.canRead()) {
                for(File f : dir.listFiles()) {
                    if (f.isFile()) {
                        ret.add(Class.forName(packageName + "." + f.getName()).getName());
                    }
                }
            }
        }
        
        return ret;
    }
    
    private static ClassLoader getClassLoaderToUse() {
        ClassLoader ret = Thread.currentThread().getContextClassLoader();
        
        if (ret == null) {
            ret = JythonEngineInitializer.class.getClassLoader();
        }
        
        return ret;
    }
}
