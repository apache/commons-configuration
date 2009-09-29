package org.apache.commons.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: rgoers
 * Date: Sep 29, 2009
 * Time: 12:50:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Lock
{
    private final String name;
    private final int instanceId;

    private static String counterLock = "Lock";
    private static int counter = 0;

    public Lock(String name)
    {
        this.name = name;
        synchronized(counterLock)
        {
            instanceId = ++counter;
        }
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return "Lock: " + name + " id = " + instanceId;
    }
}
