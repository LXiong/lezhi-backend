package com.buzzinate.jianghu.config;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class InjectorSingleton {
	private static Injector injector = null;
	
	public synchronized static Injector getInjector(){
		if(null == injector){
			injector = Guice.createInjector(new JianghuModule());
		}
		return injector;
	}
}
