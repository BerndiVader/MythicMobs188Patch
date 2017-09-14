package com.gmail.berndivader.mm188patch;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class PatchMobEggListener {
	private static Class<?> CLASS_MOBEGGLISTENER;
	
	public PatchMobEggListener() {
		try {
			CtClass clazz = ClassPool.getDefault().get("io.lumine.xikage.mythicmobs.adapters.bukkit.events.MobEggListener");
		} catch (NotFoundException ex) {
			Core.logger.info("MobEggListener class not found!");
			return;
		}
	}

}
