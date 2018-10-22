package com.dayong.demo.springquartz.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;

/**
 * 任务处理的工具类
 *
 * @author Dayong Chan
 * @date 2018/10/22 17:38
 */
public class TaskUtils {
    private static Logger logger = LogManager.getLogger(TaskUtils.class);

    /**
     * 判断cron时间表达式正确性
     *
     * @param cronExpression
     * @return
     */
    public static boolean isValidExpression(final String cronExpression) {
        CronTriggerImpl trigger = new CronTriggerImpl();
        try {
            trigger.setCronExpression(cronExpression);
            Date date = trigger.computeFirstFireTime(null);
            if (date != null) {
                //return date.after(new Date());
                return true;
            }
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 获取作业的标识
     *
     * @param group the group
     * @param name  the name
     * @return the identity
     */
    public static String getIdentity(String group, String name) {
        return "jobDefinition" + group + "_" + name;
    }

    /**
     * 通过反射调用scheduleJob中定义的方法
     *
     * @param beanClass  the bean class
     * @param methodName the method name
     * @param parameter  the parameter
     * @throws NoSuchMethodException     the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException    the illegal access exception
     * @throws IOException               the io exception
     * @throws InstantiationException    the instantiation exception
     * @throws ClassNotFoundException    the class not found exception
     */
    public static boolean invokeMethod(String beanClass, String methodName, String parameter) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, InstantiationException, ClassNotFoundException {
        Object object = getInstance(beanClass);
        Class<?> clazz = object.getClass();
        Object result;
        if (parameter != null && !parameter.isEmpty()) {
            Object param = getParameter(clazz, methodName, parameter);
            Method method = clazz.getMethod(methodName, param.getClass());
            result = method.invoke(object, param);
        } else {
            Method method = clazz.getDeclaredMethod(methodName);
            result = method.invoke(object);
        }
        if (result == null) {
            return true;
        } else {
            if (result instanceof Boolean) {
                return (boolean) result;
            } else {
                return true;
            }
        }
    }

    /**
     * 获取类的实例
     *
     * @param beanClass
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static Object getInstance(String beanClass) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> clazz;
        if (beanClass != null && !"".equals(beanClass.trim())) {
            clazz = Class.forName(beanClass);
            return clazz.newInstance();
        }
        return null;
    }

    /**
     * 获取参数的对象
     *
     * @param clazz
     * @param methodName
     * @param parameter
     * @return
     * @throws IOException
     * @throws NoSuchMethodException
     */
    private static Object getParameter(Class clazz, String methodName, String parameter) throws IOException, NoSuchMethodException {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 0) {

                } else {
                    for (Class cls : parameterTypes) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        Object value = objectMapper.readValue(parameter, cls);
                        if (value != null) {
                            return value;
                        }
                    }
                }
            }
        }
        throw new NoSuchMethodException(String.format("Can't found %s", methodName));
    }
}
