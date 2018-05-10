package com.songoda.epicspawners.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 2/25/2017.
 */
@SuppressWarnings({"ConstantConditions", "unchecked", "rawtypes"})
public class Reflection {

    public static Object getNMSItemStack(ItemStack item) {
        Class cis = getCraftItemStack();
        java.lang.reflect.Method method;
        try {
            method = cis.getMethod("asNMSCopy", ItemStack.class);
            return method.invoke(cis, item);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Object getNBTTagCompound(Object nmsItem) {
        Class c = nmsItem.getClass();
        java.lang.reflect.Method method;
        try {
            method = c.getMethod("getTag");
            return method.invoke(nmsItem);
        } catch (Exception e) {
            //Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Class getCraftItemStack() {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        try {
            //Constructor<?> cons = c.getConstructor(ItemStack.class);
            //return cons.newInstance(item);
            return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (Exception ex) {
            System.out.println("Error in ItemNBTAPI! (Outdated plugin?)");
            ex.printStackTrace();
            return null;
        }
    }
}