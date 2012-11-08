//
// Generated from archetype; please customize.
//

package com.redhat.eap6.ec2

class AMISanityTest extends GroovyTestCase
{
    
    void testJBossEC2_EAP_RPM() {
        print "Testing jboss-ec2-eap RPM."
        def ec2rpm = exec("rpm -ql jboss-ec2-eap")

        assertTrue(ec2rpm.contains("standalone-ec2-ha.xml"))
        assertTrue(ec2rpm.contains("standalone-mod_cluster-ec2-ha.xml"))
    }

    String exec(cmd) {
        def proc = cmd.execute()
        proc.waitFor()
        assert proc.exitValue() == 0
        return proc.in.text
    }
}
