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
        assertEquals(0, execForExitValue("yum check all"))
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
        
        oldVersion = "6.0.0"

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
    
    String execForOutput(cmd) {
        def proc = cmd.execute()
        proc.waitFor()
        assert proc.exitValue() == 0
        return proc.in.text
    }
    
    int execForExitValue(cmd) {
        def proc = cmd.execute()
        proc.waitFor()
        return proc.exitValue()
    }
}
