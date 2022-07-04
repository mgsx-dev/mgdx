package net.mgsx.gdx.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Comparator;

import com.badlogic.gdx.graphics.profiling.GLInterceptor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectIntMap.Entry;

public class AdvancedProfiler {
	
	private final Comparator<Method> byCallsDesc = new Comparator<Method>() {
		@Override
		public int compare(Method o1, Method o2) {
			return Integer.compare(calls.get(o2, 0), calls.get(o1, 0));
		}
	};
	
	private final ObjectIntMap<Method> calls = new ObjectIntMap<Method>();

	private static class Delegator<T> implements InvocationHandler {
		protected final T object;
		public Delegator(T object) {
			this.object = object;
		}
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try{
				return method.invoke(object, args);
			}catch(Throwable e){
				throw e;
			}
		}
	}
	
	private static <T> T makeProxy(Class<T> type, InvocationHandler handler){
		return (T) Proxy.newProxyInstance(
				AdvancedProfiler.class.getClassLoader(), 
				new Class[] { type }, 
				handler);
	}
	
	public <T> T create(Class<T> type, GLInterceptor glInterceptorBase){
		return makeProxy(type, new Delegator(glInterceptorBase){
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				calls.getAndIncrement(method, 0, 1);
				return super.invoke(proxy, method, args);
			}
		});
	}
	
	public void resetMethods(){
		calls.clear();
	}

	public int getTopMethodCalls(Array<Method> methods, int max) {
		for(Entry<Method> e : calls){
			methods.add(e.key);
		}
		methods.sort(byCallsDesc);
		int remaining = Math.max(0, calls.size - max);
		methods.truncate(max);
		return remaining;
	}

	public int getCalls(Method m) {
		return calls.get(m, 0);
	}
}
