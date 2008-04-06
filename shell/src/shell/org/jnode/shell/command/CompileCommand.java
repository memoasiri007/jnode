/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ClassNameArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmMagic;
import org.jnode.vm.classmgr.VmType;

/**
 * @author epr
 * @author crawley@jnode.org
 */
public class CompileCommand extends AbstractCommand {
    // FIXME ... it would be good if something exposed the current maximum 
    // compiler optimization level via(s) a getter method.
//    private final VmArchitecture arch = VmMagic.currentProcessor().getArchitecture();
//    private final Object[] compilers = arch.getCompilers();
//    private final Object[] testCompilers = arch.getTestCompilers();
//    private final int maxLevel = Math.max(
//            compilers.length, (testCompilers == null) ? 0 : testCompilers.length) - 1;
    
    // FIXME ... temporary hack until I work out what is causing the System.out stream
    // to be toasted in the compiler thread.  (The commented out code causes the compiler
    // to try to print "MagicPermission is not granted ..." to System.out, and a NPE is
    // being thrown because (I think) the proclet context thinks that the global output
    // stream is null.)
    private final int maxLevel = 3;
    
	private final ClassNameArgument ARG_CLASS = 
	    new ClassNameArgument("className", Argument.MANDATORY, "the class file to compile");
	private final IntegerArgument ARG_LEVEL = 
	    new IntegerArgument("level", Argument.OPTIONAL, 0, maxLevel, "the optimization level");
	private final FlagArgument ARG_TEST = 
	    new FlagArgument("test", Argument.OPTIONAL, "use the test version of the compiler");
	
	public CompileCommand() {
        super("compile a Java class (bytecodes) to native code");
        registerArguments(ARG_CLASS, ARG_LEVEL, ARG_TEST);
    }
	
	public static void main(String[] args) throws Exception {
	    new CompileCommand().execute(args);
	}
	
	@Override
    public void execute(CommandLine commandLine, InputStream in,
            PrintStream out, PrintStream err) 
	throws Exception {
		final String className = ARG_CLASS.getValue();
		final int level = ARG_LEVEL.isSet() ? ARG_LEVEL.getValue() : 0;
		final boolean test = ARG_TEST.isSet();
		
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final Class<?> cls;
		try {
		    cls = cl.loadClass(className);
		}
		catch (ClassNotFoundException ex) {
		    err.println("Class '" + className + "' not found");
		    exit(1);
		    // not reached
		    return;  
		}
		final VmType<?> type = cls.getVmClass();
		final long start = System.currentTimeMillis();
		final int count = type.compileRuntime(level, test);
		final long end = System.currentTimeMillis();
		out.println("Compiling " + count + " methods took " + (end - start) + "ms");
	}

}
