package com.buzzinate.jianghu.api;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import com.buzzinate.common.util.Constants;

import weibo4j.Weibo;
import weibo4j.http.AccessToken;


public class UserResrouceTest {

	@Test
	public void testGetUser() throws Exception {
//		ClientRequest request = new ClientRequest("http://221.238.253.146/jianghu-api/articles/like");
		ClientRequest request = new ClientRequest("http://localhost:9095/user/login2");
		request.accept(MediaType.APPLICATION_JSON);
//		Weibo weibo = new Weibo();
//		weibo.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
//		AccessToken at = weibo.getXAuthAccessToken("kun.xue@buzzinate.com", "ceibs32073208");
//		request.header("accessToken", "71140d1d2d5ac84d6ca20d7bc4199520");
//		ClientResponse<String> resp = request.get();
		request.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//		ClientResponse<String> resp = request.body("application/x-www-form-urlencoded; charset=UTF-8", "id=10531&comment=中文注释").post();
		ClientResponse<String> resp = request.formParameter("accessToken", "2.007PNJDC8B8pgB6ec716ec36PIJqgB").formParameter("uid", "1878790552").post();
//		ClientResponse<String> resp = request.get();
		String content = resp.getEntity(String.class);
		System.out.println(content);

//		ClientRequest request = new ClientRequest("http://221.238.253.146/jianghu-api/articles/like");
//		request.accept(MediaType.APPLICATION_JSON);
//		request.header("accessToken", "51fea106b3a882777da4ae9a71ce4f6c").formParameter("id", "789111").post();
//		
		//String content = resp.getEntity(String.class);
		//System.out.println("recommendention: " + content);
		
		Assert.assertEquals(200, resp.getStatus());
	}
}
