package com.buzzinate.jianghu.api;

import java.util.HashMap;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("status")
@Produces({"application/json"})
public class ApiStatusResource {
	private HashMap<String, String> ok = new HashMap<String, String>();
	
	public ApiStatusResource() {
		ok.put("status", "ok");
	}
	
	@GET @Path("")
	@PermitAll
	public HashMap<String, String> getStatus() {
		return ok;
	}
}
