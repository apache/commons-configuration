/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration.builder;

import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileBasedConfiguration;
import org.apache.commons.configuration.io.FileHandler;
import org.apache.commons.configuration.reloading.FileHandlerReloadingDetector;
import org.apache.commons.configuration.reloading.ReloadingController;
import org.apache.commons.configuration.reloading.ReloadingDetector;
import org.apache.commons.configuration.reloading.ReloadingEvent;
import org.apache.commons.configuration.reloading.ReloadingListener;

/**
 * <p>
 * A specialized {@code ConfigurationBuilder} implementation which can handle
 * configurations read from a {@link FileHandler} and supports reloading.
 * </p>
 * <p>
 * This builder class exposes a {@link ReloadingController} object controlling
 * reload operations on the file-based configuration produced as result object.
 * For the {@code FileHandler} defining the location of the configuration a
 * {@link FileHandlerReloadingDetector} is created and associated with the
 * controller. So changes on the source file can be detected. When ever such a
 * change occurs, the result object of this builder is reset. This means that
 * the next time {@code getConfiguration()} is called a new
 * {@code Configuration} object is created which is loaded from the modified
 * file.
 * </p>
 * <p>
 * Client code interested in notifications can register a listener at this
 * builder to receive reset events. When such an event is received the new
 * result object can be requested. This way client applications can be sure to
 * work with an up-to-date configuration. It is also possible to register a
 * listener directly at the {@code ReloadingController}.
 * </p>
 * <p>
 * This builder does not actively trigger the {@code ReloadingController} to
 * perform a reload check. This has to be done by an external component, e.g. a
 * timer.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 * @param <T> the concrete type of {@code Configuration} objects created by this
 *        builder
 */
public class ReloadingFileBasedConfigurationBuilder<T extends FileBasedConfiguration>
        extends FileBasedConfigurationBuilder<T>
{
    /** The reloading controller associated with this object. */
    private final ReloadingController reloadingController;

    /**
     * The reloading detector which does the actual reload check for the current
     * result object. A new instance is created whenever a new result object
     * (and thus a new current file handler) becomes available. The field must
     * be volatile because it is accessed by the reloading controller probably
     * from within another thread.
     */
    private volatile ReloadingDetector resultReloadingDetector;

    /**
     * Creates a new instance of {@code ReloadingFileBasedConfigurationBuilder}
     * which produces result objects of the specified class and sets
     * initialization parameters.
     *
     * @param resCls the result class (must not be <b>null</b>
     * @param params a map with initialization parameters
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public ReloadingFileBasedConfigurationBuilder(Class<T> resCls,
            Map<String, Object> params)
    {
        super(resCls, params);
        reloadingController = createReloadingController();
    }

    /**
     * Creates a new instance of {@code ReloadingFileBasedConfigurationBuilder}
     * which produces result objects of the specified class and sets
     * initialization parameters and the <em>allowFailOnInit</em> flag.
     *
     * @param resCls the result class (must not be <b>null</b>
     * @param params a map with initialization parameters
     * @param allowFailOnInit the <em>allowFailOnInit</em> flag
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public ReloadingFileBasedConfigurationBuilder(Class<T> resCls,
            Map<String, Object> params, boolean allowFailOnInit)
    {
        super(resCls, params, allowFailOnInit);
        reloadingController = createReloadingController();
    }

    /**
     * Creates a new instance of {@code ReloadingFileBasedConfigurationBuilder}
     * which produces result objects of the specified class.
     *
     * @param resCls the result class (must not be <b>null</b>
     * @throws IllegalArgumentException if the result class is <b>null</b>
     */
    public ReloadingFileBasedConfigurationBuilder(Class<T> resCls)
    {
        super(resCls);
        reloadingController = createReloadingController();
    }

    /**
     * Returns the {@code ReloadingController} associated with this builder.
     * This controller is directly created. However, it becomes active (i.e.
     * associated with a meaningful reloading detector) not before a result
     * object was created.
     *
     * @return the {@code ReloadingController}
     */
    public ReloadingController getReloadingController()
    {
        return reloadingController;
    }

    /**
     * Creates a {@code ReloadingDetector} which monitors the passed in
     * {@code FileHandler}. This method is called each time a new result object
     * is created with the current {@code FileHandler}. The
     * {@code ReloadingDetector} associated with this builder's
     * {@link ReloadingController} delegates to this object. This implementation
     * returns a new {@code FileHandlerReloadingDetector} object. Note: This
     * method is called from a synchronized block.
     *
     * @param handler the current {@code FileHandler}
     * @param fbparams the object with parameters related to file-based builders
     * @return a {@code ReloadingDetector} for this {@code FileHandler}
     */
    protected ReloadingDetector createReloadingDetector(FileHandler handler,
            FileBasedBuilderParametersImpl fbparams)
    {
        return new FileHandlerReloadingDetector(handler,
                fbparams.getReloadingRefreshDelay());
    }

    /**
     * {@inheritDoc} This implementation also takes care that a new
     * {@code ReloadingDetector} for the new current {@code FileHandler} is
     * created. Also, the reloading controller's reloading state has to be
     * reset; after the creation of a new result object changes in the
     * underlying configuration source have to be monitored again.
     */
    @Override
    protected void initFileHandler(FileHandler handler)
            throws ConfigurationException
    {
        super.initFileHandler(handler);

        resultReloadingDetector =
                createReloadingDetector(handler,
                        FileBasedBuilderParametersImpl.fromParameters(
                                getParameters(), true));
        getReloadingController().resetReloadingState();
    }

    /**
     * Creates the {@code ReloadingController} associated with this object. The
     * controller is assigned a specialized reloading detector which delegates
     * to the detector for the current result object. (
     * {@code FileHandlerReloadingDetector} does not support changing the file
     * handler, and {@code ReloadingController} does not support changing the
     * reloading detector; therefore, this level of indirection is needed to
     * change the monitored file dynamically.)
     *
     * @return the new {@code ReloadingController}
     */
    private ReloadingController createReloadingController()
    {
        ReloadingDetector ctrlDetector = createReloadingDetectorForController();
        ReloadingController ctrl = new ReloadingController(ctrlDetector);
        ctrl.addReloadingListener(createReloadingListener());
        return ctrl;
    }

    /**
     * Creates a listener object to be registered at the associated reloading
     * controller. This listener resets the builder's result object whenever a
     * change in the monitored file is detected. This will trigger a builder
     * reset event, and the next time {@code getConfiguration()} is called, a
     * new result object is created.
     *
     * @return the listener for the associated {@code ReloadingController}
     */
    private ReloadingListener createReloadingListener()
    {
        return new ReloadingListener()
        {
            public void reloadingRequired(ReloadingEvent event)
            {
                resetResult();
            }
        };
    }

    /**
     * Creates a {@code ReloadingDetector} wrapper to be passed to the
     * associated {@code ReloadingController}. This detector wrapper simply
     * delegates to the current {@code ReloadingDetector} if it is available.
     *
     * @return the wrapper {@code ReloadingDetector}
     */
    private ReloadingDetector createReloadingDetectorForController()
    {
        return new ReloadingDetector()
        {
            public void reloadingPerformed()
            {
                ReloadingDetector detector = resultReloadingDetector;
                if (detector != null)
                {
                    detector.reloadingPerformed();
                }
            }

            public boolean isReloadingRequired()
            {
                ReloadingDetector detector = resultReloadingDetector;
                return (detector != null) ? detector.isReloadingRequired()
                        : false;
            }
        };
    }
}
