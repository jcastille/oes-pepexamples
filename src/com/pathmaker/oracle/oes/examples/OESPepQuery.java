package com.pathmaker.oracle.oes.examples;


//Begin comments for this issue/branch
import java.util.*;

import org.openliberty.openaz.azapi.constants.PepRequestQueryType;
import org.openliberty.openaz.azapi.pep.*;

import com.bea.security.RuntimeAction;

import oracle.security.jps.openaz.pep.*;
import weblogic.security.principal.WLSUserImpl;
import weblogic.security.principal.WLSGroupImpl;

import javax.security.auth.Subject;

public class OESPepQuery {
    
    public static void main(String[] args) throws Exception {
        System.out.println("Starting the OESPepQuery Sample Application");
        
        String APPLICATION_NAME=null;
    	String RESOURCE_TYPE=null;
    	String RESOURCE_NAME=null;
    	String USER_NAME=null;
    	String GROUP_NAME=null;
    	if (args.length < 4) {
        	System.out.println("Usage: OESPepQuery <application_name> <resource_type> <resource_name> <user_name> <group_name>");
            System.exit(0);

        } else {
        	APPLICATION_NAME = args[0];
        	RESOURCE_TYPE = args[1];
        	RESOURCE_NAME = args[2];
        	USER_NAME = args[3];
        	if (args.length > 4){
        		GROUP_NAME = args[4];
        	}
        }
        OESPepQuery app = new OESPepQuery();
        app.pepQueryExample(APPLICATION_NAME,RESOURCE_TYPE,RESOURCE_NAME,USER_NAME,GROUP_NAME);
        System.exit(0);
    }
    
    @SuppressWarnings("rawtypes")
	public void pepQueryExample(String APPLICATION_NAME, String RESOURCE_TYPE, String RESOURCE_NAME, String USER_NAME, String GROUP_NAME){
    	Subject subject;
    	subject = new Subject();
    	subject.getPrincipals().add(new WLSUserImpl(USER_NAME));
    	if (GROUP_NAME != null)
    		subject.getPrincipals().add(new WLSGroupImpl(GROUP_NAME));
    	
    	PepRequestFactory pepFactory;
    	pepFactory = PepRequestFactoryImpl.getPepRequestFactory();

		try{
            Map<String, String> env = new HashMap<String, String>();
            /*
             * This query scope string has following definition:
             * resource = <resourceString>
             * It will return the results for this object and it children (for hierarchical objects)
             */
            String RESOURCE_STRING = APPLICATION_NAME + "/" + RESOURCE_TYPE + "/" + RESOURCE_NAME;
            String scope = "resource=" + RESOURCE_STRING;
            System.out.println("PepSampleApp: query scope is: " + scope);
            System.out.println("-----------------------------------------------------------------------------------------");
            /*Invoke newPepQueryRequest passing Subject, environment, scope and PepRequestQueryType (Verbose will return both allow and deny)*/
            PepRequest pepRequest = pepFactory.newQueryPepRequest(subject, env, scope, PepRequestQueryType.VERBOSE);

            /* Retrieves the PepResponse object*/
            PepResponse pepResponse = pepRequest.decide();
            while (pepResponse.next()){;
            	System.out.println();
            	/*retrieve the list of actions from the PepResponse*/
            	ArrayList arrayList = (ArrayList) pepResponse.getAction();
            	List grantedActions = null;
            	List deniedActions = null;
            	if (arrayList != null) {
            		/*grantedActions is returned as the first element of the list*/
            		grantedActions = (List) arrayList.get(0);
            		/*deniedActions is returned as the second element of the list*/
            		deniedActions = (List) arrayList.get(1);
            	}
            	if (grantedActions != null) {
            		Iterator iterator = grantedActions.iterator();
            		String thisAction =null;
            		while (iterator.hasNext()) {
            			thisAction = ((RuntimeAction) iterator.next()).getAction();
            			System.out.println(pepResponse.getResource().toString() + " granted action:  " + thisAction);
            			/* Perform a direct requests using the Decision API to get the obligations if there are any for this allowed action*/
            			PepResponse response = PepRequestFactoryImpl.getPepRequestFactory().newPepRequest(subject,thisAction,pepResponse.getResource().toString(),env).decide();
                  		Map<String, Obligation> obligations = response.getObligations();
                  		for (String name : obligations.keySet()){                
                  			System.out.println("obligation: name = " + name + ", values = " + obligations.get(name).getStringValues());         
                  		}
            		}
            	}
            	if (deniedActions != null) {
            		Iterator iterator = deniedActions.iterator();
            		while (iterator.hasNext()) {
            			System.out.println(pepResponse.getResource().toString() + " denied action:  " + ((RuntimeAction) iterator.next()).getAction());
            		}
            	}
            	
            }
            System.out.println("");
        } catch (Exception e) {
            System.out.println("OESPepQuery.pepQueryExample: Exception: " + e);
            e.printStackTrace();
        }
    }
}
