package de.evilcodez.config.utils;

import java.lang.reflect.Constructor;

import sun.reflect.ReflectionFactory;

public class SilentObjectCreator {
	
	public static <T> T create(Class<T> clazz) {
		return create(clazz, Object.class);
	}

	public static <T> T create(Class<T> clazz, Class<? super T> parent) {
		try {
			final ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
			final Constructor<?> objDef = parent.getDeclaredConstructor();
			final Constructor<?> intConstr = rf.newConstructorForSerialization(clazz, objDef);
			return clazz.cast(intConstr.newInstance());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Cannot create object", e);
		}
	}
}