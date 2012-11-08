//
// Generated from archetype; please customize.
//

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
        assertEquals(0, execForExitValue("yum check all"))
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
