package lycanite.lycanitesmobs.api.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class LMReflectionHelper {
	/**
	 * Removes final from a field.
	 * @param classToAccess
	 * @param instance
	 * @param fieldNames
	 * @return
	 */
	public static <T> Field removeFinal(Class <? super T > classToAccess, T instance, String... fieldNames) {
    	Field field = ReflectionHelper.findField(classToAccess, ObfuscationReflectionHelper.remapFieldNames(classToAccess.getName(), fieldNames));
    	
    	try {
    		Field modifiersField = Field.class.getDeclaredField("modifiers");
    		modifiersField.setAccessible(true);
    		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return field;
	}
	
	
	/**
	 * Sets the value of a private final field.
	 * @param classToAccess
	 * @param instance
	 * @param value
	 * @param fieldNames
	 */
    public static <T, E> void setPrivateFinalValue(Class <? super T > classToAccess, T instance, E value, String... fieldNames) {
    	Field field = ReflectionHelper.findField(classToAccess, ObfuscationReflectionHelper.remapFieldNames(classToAccess.getName(), fieldNames));
    	
    	try {
    		Field modifiersField = Field.class.getDeclaredField("modifiers");
    		modifiersField.setAccessible(true);
    		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    		field.set(instance, value);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
