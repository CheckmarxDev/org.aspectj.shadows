package com.checkmarx.repackaged.aspectj.build;

import java.io.File;

/**
 * Created by Natali on 28.07.2017.
 */
public class Compiler {

    public static void main(String[] args) {
        try {
            String[] ajcArgs = {
                    "-1.8",
                    "-showWeaveInfo",
                    "-verbose",
                    "-classpath", "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/apt" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/aspectj" + File.pathSeparator +
                    "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/batch" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/codeassist" + File.pathSeparator +
                    "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/compiler" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/dom" + File.pathSeparator +
                    "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/eval" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/formatter" + File.pathSeparator +
                    "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/model" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/search" + File.pathSeparator +
                   "D:\\IdeaProjects\\checkmarx\\test_appps\\aspectJ\\aspectJLib\\org.aspectj\\aj-build\\dist\\tools\\lib\\aspectjrt.jar"+ File.pathSeparator +
                //    "D:\\work\\upwork projects\\checkmarx\\aspectJ\\aspectjrt-1.8.9.jar" +File.pathSeparator +

                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.equinox.common_3.8.0.v20160509-1230.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.equinox.registry_3.6.100.v20160223-2218.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.osgi_3.11.2.v20161107-1947.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.core.jobs_3.8.0.v20160509-0411.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.core.resources_3.11.0.v20160503-1608.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.core.runtime_3.12.0.v20160606-1342.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.equinox.preferences_3.6.1.v20160815-1406.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.equinox.app_1.3.400.v20150715-1528.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.text_3.6.0.v20160503-1849.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.core.filesystem_1.6.0.v20160503-1608.jar"  + File.pathSeparator +
                    "D:/work/upwork projects/checkmarx/aspectJ/eclipse_jars2/org.eclipse.core.contenttype_3.5.100.v20160418-1621.jar",



                    "-sourceroots",  "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/apt" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/aspectj" + File.pathSeparator +
                    "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/batch" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/codeassist" + File.pathSeparator +
                    "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/compiler" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/dom" + File.pathSeparator +
                    "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/eval" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/formatter" + File.pathSeparator +
                    "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/model" + File.pathSeparator + "/../../eclipseJDTCoreRepackaged/org.aspectj.shadows/org.eclipse.jdt.core/search",

                    "-d", "D:\\IdeaProjects\\checkmarx\\test_appps\\aspectJ\\eclipseJDTCoreRepackaged\\org.aspectj.shadows\\org.eclipse.jdt.core\\bin",


            };
            com.checkmarx.repackaged.aspectj.tools.ajc.Main.main(ajcArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
