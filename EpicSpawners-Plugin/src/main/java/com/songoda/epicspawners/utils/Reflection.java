package com.songoda.epicspawners.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Created by songoda on 2/25/2017.
 */
public class Reflection {

    private static Class<?> classCraftItemStack, classNMSItemStack;
    private static Method methodAsNMSCopy, methodGetTag;

    public static Object getNMSItemStack(ItemStack item) {
        Class<?> classCraftItemStack = getCraftItemStack();

        try {
            return getAsNMSCopyMethod().invoke(classCraftItemStack, item);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getNBTTagCompound(Object nmsItem) {
        Class<?> clazz = nmsItem.getClass();
        if (!clazz.equals(getNMSItemStack())) {
            return null;
        }

        try {
            return getGetTagMethod().invoke(nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getCraftItemStack() {
        if (classCraftItemStack != null) return classCraftItemStack;

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return (classCraftItemStack = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack"));
        } catch (ReflectiveOperationException e) {
            System.out.println("Error in ItemNBTAPI! (Outdated plugin?)");
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getNMSItemStack() {
        if (classNMSItemStack != null) return classNMSItemStack;

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return (classNMSItemStack = Class.forName("net.minecraft.server." + version + ".ItemStack"));
        } catch (ReflectiveOperationException e) {
            System.out.println("Error in ItemNBTAPI! (Outdated plugin?)");
            e.printStackTrace();
            return null;
        }
    }

    private static Method getAsNMSCopyMethod() {
        if (methodAsNMSCopy != null) return methodAsNMSCopy;

        if (classCraftItemStack == null) {
            getCraftItemStack(); // Initializes the field as well
        }

        try {
            return (methodAsNMSCopy = classCraftItemStack.getMethod("asNMSCopy", ItemStack.class));
        } catch (ReflectiveOperationException e) {
            System.out.println("Error in ItemNBTAPI! (Outdated plugin?)");
            e.printStackTrace();
            return null;
        }
    }

    private static Method getGetTagMethod() {
        if (methodGetTag != null) return methodGetTag;

        if (classNMSItemStack == null) {
            getNMSItemStack(); // Initializes the field as well
        }

        try {
            return (methodGetTag = classNMSItemStack.getMethod("getTag"));
        } catch (ReflectiveOperationException e) {
            System.out.println("Error in ItemNBTAPI! (Outdated plugin?)");
            e.printStackTrace();
            return null;
        }
    }

}