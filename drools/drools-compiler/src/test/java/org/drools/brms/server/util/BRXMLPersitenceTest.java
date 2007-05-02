package org.drools.brms.server.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.drools.brms.client.modeldriven.SuggestionCompletionEngine;
import org.drools.brms.client.modeldriven.brxml.ActionAssertFact;
import org.drools.brms.client.modeldriven.brxml.ActionFieldValue;
import org.drools.brms.client.modeldriven.brxml.ActionModifyField;
import org.drools.brms.client.modeldriven.brxml.ActionRetractFact;
import org.drools.brms.client.modeldriven.brxml.CompositeFactPattern;
import org.drools.brms.client.modeldriven.brxml.Constraint;
import org.drools.brms.client.modeldriven.brxml.DSLSentence;
import org.drools.brms.client.modeldriven.brxml.FactPattern;
import org.drools.brms.client.modeldriven.brxml.RuleAttribute;
import org.drools.brms.client.modeldriven.brxml.RuleModel;
import org.drools.lang.DRLParser;

public class BRXMLPersitenceTest extends TestCase {

    public void testGenerateEmptyXML() {
        final BRLPersistence p = BRXMLPersistence.getInstance();
        final String xml = p.marshal( new RuleModel() );
        assertNotNull( xml );
        assertFalse( xml.equals( "" ) );

        assertTrue( xml.startsWith( "<rule>" ) );
        assertTrue( xml.endsWith( "</rule>" ) );
    }

    public void testBasics() {
        final BRLPersistence p = BRXMLPersistence.getInstance();
        final RuleModel m = new RuleModel();
        m.addLhsItem( new FactPattern( "Person" ) );
        m.addLhsItem( new FactPattern( "Accident" ) );
        m.addAttribute( new RuleAttribute( "no-loop",
                                           "true" ) );

        m.addRhsItem( new ActionAssertFact( "Report" ) );
        m.name = "my rule";
        final String xml = p.marshal( m );
        //System.out.println(xml);
        assertTrue( xml.indexOf( "Person" ) > -1 );
        assertTrue( xml.indexOf( "Accident" ) > -1 );
        assertTrue( xml.indexOf( "no-loop" ) > -1 );
        assertTrue( xml.indexOf( "org.drools" ) == -1 );

    }

    public void testMoreComplexRendering() {
        final BRLPersistence p = BRXMLPersistence.getInstance();
        final RuleModel m = getComplexModel();

        final String xml = p.marshal( m );
        //System.out.println( xml );

        assertTrue( xml.indexOf( "org.drools" ) == -1 );

    }

    public void testRoundTrip() {
        final RuleModel m = getComplexModel();

        final String xml = BRXMLPersistence.getInstance().marshal( m );

        final RuleModel m2 = BRXMLPersistence.getInstance().unmarshal( xml );
        assertNotNull( m2 );
        assertEquals( m.name,
                      m2.name );
        assertEquals( m.lhs.length,
                      m2.lhs.length );
        assertEquals( m.rhs.length,
                      m2.rhs.length );
        assertEquals( 1,
                      m.attributes.length );

        final RuleAttribute at = m.attributes[0];
        assertEquals( "no-loop",
                      at.attributeName );
        assertEquals( "true",
                      at.value );

        final String newXML = BRXMLPersistence.getInstance().marshal( m2 );
        assertEquals( xml,
                      newXML );

    }

    /**
     * This will verify that we can load an old BRXML change. If this fails,
     * then backwards compatability is broken.
     */
    public void testBackwardsCompat() throws Exception {
        RuleModel m2 = BRXMLPersistence.getInstance().unmarshal( loadResource( "existing_brxml.xml" ) );
        
        assertNotNull(m2);
        assertEquals(3, m2.rhs.length);
    }
    
    private String loadResource(final String name) throws Exception {

        //        System.err.println( getClass().getResource( name ) );
        final InputStream in = getClass().getResourceAsStream( name );

    
        final Reader reader = new InputStreamReader( in );

        final StringBuffer text = new StringBuffer();

        final char[] buf = new char[1024];
        int len = 0;

        while ( (len = reader.read( buf )) >= 0 ) {
            text.append( buf,
                         0,
                         len );
        }

        return text.toString();
    }    

    private RuleModel getComplexModel() {
        final RuleModel m = new RuleModel();

        m.addAttribute( new RuleAttribute( "no-loop",
                                           "true" ) );

        final FactPattern pat = new FactPattern();
        pat.boundName = "p1";
        pat.factType = "Person";
        final Constraint con = new Constraint();
        con.fieldBinding = "f1";
        con.fieldName = "age";
        con.operator = "<";
        con.value = "42";
        pat.addConstraint( con );

        m.addLhsItem( pat );

        final CompositeFactPattern comp = new CompositeFactPattern( "not" );
        comp.addFactPattern( new FactPattern( "Cancel" ) );
        m.addLhsItem( comp );

        final ActionModifyField set = new ActionModifyField();
        set.variable = "p1";
        set.addFieldValue( new ActionFieldValue( "status",
                                                 "rejected",
                                                 SuggestionCompletionEngine.TYPE_STRING ) );
        m.addRhsItem( set );

        final ActionRetractFact ret = new ActionRetractFact( "p1" );
        m.addRhsItem( ret );

        final DSLSentence sen = new DSLSentence();
        sen.sentence = "Send an email to {administrator}";

        m.addRhsItem( sen );
        return m;
    }

    public void testLoadEmpty() {
        RuleModel m = BRXMLPersistence.getInstance().unmarshal( null );
        assertNotNull( m );

        m = BRXMLPersistence.getInstance().unmarshal( "" );
        assertNotNull( m );
    }

}
