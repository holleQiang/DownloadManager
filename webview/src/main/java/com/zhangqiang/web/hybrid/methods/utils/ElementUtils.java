package com.zhangqiang.web.hybrid.methods.utils;

import com.zhangqiang.common.utils.ListUtils;
import com.zhangqiang.web.hybrid.methods.element.AElement;
import com.zhangqiang.web.hybrid.methods.element.Element;
import com.zhangqiang.web.hybrid.methods.element.ImgElement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementUtils {

    private static final Map<String, Class<? extends Element>> tagClassMap = new HashMap<>();

    static {
        tagClassMap.put("a", AElement.class);
        tagClassMap.put("img", ImgElement.class);
    }

    public static Class<? extends Element> getElementClassByTagName(String tagName) {
        Class<? extends Element> aClass = tagClassMap.get(tagName);
        if (aClass == null) {
            return Element.class;
        }
        return aClass;
    }

    private static <T extends Element> List<String> getElementFields(Class<T> tClass) {
        Field[] declaredFields = tClass.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            Class<?> type = declaredField.getType();
            String name = declaredField.getName();
            if (type == int.class || type == float.class || type == double.class || type == String.class || type == short.class || type == long.class) {
                fieldNames.add(name);
            }
        }
        Class<? super T> superclass = tClass.getSuperclass();
        if (Element.class.isAssignableFrom(superclass)) {
            fieldNames.addAll(getElementFields((Class<T>) superclass));
        }
        return fieldNames;
    }

    public static <T extends Element> String generatorJSCode(Class<T> tClass) {

        String elementFields = ListUtils.join(getElementFields(tClass));
        return "const {" + elementFields + "} = element;\n" + "const data = { " + elementFields + " };\n";
    }

//    public static String generateElementSerializableJSCode(String) {
//
//    }
}
