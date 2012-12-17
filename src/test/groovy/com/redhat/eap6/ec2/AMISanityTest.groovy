package com.redhat.eap6.ec2

class AMISanityTest extends GroovyTestCase
{
    
    void testJBossEC2_EAP_RPM() {
        println "Testing jboss-ec2-eap RPM."
        println "**************************"
        def ec2rpm = execForOutput("rpm -ql jboss-ec2-eap")
        println "RPM content: " + ec2rpm
        
        assertTrue(ec2rpm.contains("standalone-ec2-ha.xml"))
        assertTrue(ec2rpm.contains("standalone-mod_cluster-ec2-ha.xml"))
    }

    
    void testYumCheck() {
        println "yum check all."
        println "**************"
        //assertEquals(0, execForExitValue("yum check all"))
    }
    
    void testJONAgentInstall() {
        println "chkconfig --list jon-agent-ec2."
        println "*******************************"
        def out = execForOutput("chkconfig --list jon-agent-ec2")
        println out
        assertTrue(out.contains("jon-agent-ec2"))
        assertFalse(out.contains(":on"))
        
        println "chkconfig --list jon-agent."
        println "***************************"
        out = execForOutput("chkconfig --list jon-agent")
        println out
        assertTrue(out.contains("jon-agent"))
        assertFalse(out.contains(":on"))        
    }
    
    void testCompareOldRelease() {
        def oldVersion = System.properties["oldVersion"]        
        if (oldVersion == null) oldVersion = "6.0.0"

        println "Comparing installed RPMs with previous version."
        println "***********************************************"

        def arch = execForOutput("arch").readLines()[0]
        println "Detected architecture: " + arch
        
        def out = execForOutput("rpm -qa --qf %{NAME}\\t%{VERSION}-%{release}\\t%{arch}\\n")        
        
        def installedPackages = [:]
        out.eachLine { line ->
            def columns = line.split()
            installedPackages.put(columns[0], columns[1])
        }
        
        println "Packages currently installed:" + installedPackages.count{key, value -> Boolean.TRUE}
  
        def stream = getClass().getResourceAsStream("/jboss-eap-" + oldVersion + "-" + arch + ".txt");
	def oldFile = stream.getText();	
        
        def oldPackages = [:]
        oldFile.eachLine { line ->
            def columns = line.split()
            oldPackages.put(columns[0], columns[1])
        }

        println "Packages installed in previous version:" + oldPackages.count{key, value -> Boolean.TRUE}

        def newPackages = []
        installedPackages.each{ name, version -> 
            if (! oldPackages.containsKey(name)) newPackages.add(name)
        }        
        println "New packages:"
        newPackages.each{ name -> println name }
        
        def missingPackages = []
        oldPackages.each{ name, version -> 
            if (! installedPackages.containsKey(name)) missingPackages.add(name)
        }        
        println "Missing packages:"
        missingPackages.each{ name -> println name }
        
        assert missingPackages.size()  == 0
     
    }

    void testCompareInstalledToRPMdist() {                
        println "Comparing installed RPMs with RPM distribution."
        println "***********************************************"
        
        def currentVersion = System.properties["currentVersion"]        
        if (currentVersion == null) currentVersion = "6.0.1.ER3"

        def arch = execForOutput("arch").readLines()[0]
        println "Detected architecture: " + arch
        
        def out = execForOutput("rpm -qa --qf %{NAME}\\t%{VERSION}-%{release}\\t%{arch}\\n")        
        
        def installedPackages = [:]
        out.eachLine { line ->
            def columns = line.split()
            installedPackages.put(columns[0], columns[1])
        }
        
        def stream = getClass().getResourceAsStream("/jboss-eap-" + currentVersion + "-" + arch + ".md5sums.txt");
	def sumsFile = stream.getText();
        
        def sumPackages = [:]
        sumsFile.eachLine { line ->
            println "Sum line: " + line
            def fields = line.split('-')
            def name = fields[0];
            for (i in 1..(fields.size()-2)) {
                    name = name + "-" + fields[i]
            }
            def versionArch = fields[fields.size()-1]
            println "versionArch: " + versionArch

            def verParts = versionArch.split('.')
            def version = verParts[0];
            for (i in 1..(verParts.size()-2)) {
                    version = version + "." + verParts[i]
            }            
            sumPackages.put(name, version)
        }
                
        println "Comparing RPM dist to installed packages:"
        
        def conflictingPackages = 0
        sumPackages.each{ name, version -> 
                    if (! installedPackages.containsKey(name)) missingPackages.add(name) {
                        println "Package " + name + " not installed."
                    } else {
                        if (version == installedPackages.get(name)) {
                            println "Package " + name + " version " + version + " OK."
                        } else {
                            println "Package " + name + " RPM version " + version + " conflicts with installed version " + installedPackages.get(name) + "."
                            conflictingPackages ++
                        }
                    }
                }        
        assert conflictingPackages  == 0
        
    }
    
    String execForOutput(cmd) {
        println "\$ " + cmd
        def proc = cmd.execute()
        proc.waitFor()
        assert proc.exitValue() == 0
        return proc.in.text
    }
    
    int execForExitValue(cmd) {
        println "\$ " + cmd
        def proc = cmd.execute()
        proc.waitFor()
        return proc.exitValue()
    }
}
