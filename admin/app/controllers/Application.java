package controllers;

import model.AdminUser;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import play.mvc.Controller;
import utils.Constants;

public class Application extends Controller {

    public static void index() {
        render();
    }

}