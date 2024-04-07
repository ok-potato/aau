package at.jku.risc.uarau;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {
    
    public GreetingResponse greetServer(String input) {
        GreetingResponse response = new GreetingResponse();
        
        response.setServerInfo(getServletContext().getServerInfo());
        response.setUserAgent(getThreadLocalRequest().getHeader("User-Agent"));
        
        response.setGreeting("Hello, " + input + "!");
        
        return response;
    }
}
