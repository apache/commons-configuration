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
package org.apache.commons.configuration2.builder;

import java.util.Map;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.reloading.ReloadingController;
import org.apache.commons.configuration2.reloading.ReloadingControllerSupport;
import org.apache.commons.configuration2.reloading.ReloadingDetector;

/**
 * <p>
 * A specialized {@code ConfigurationBuilder} implementation which can handle
 * configurations read from a {@link FileHandler} and supports reloading.
 * </p>
 * <p>
 * This builder class exposes a {@link ReloadingController} object controlling
 * reload operations on the file-based configuration produced as result object.
 * For the {@code FileHandler} defining the location of the configuration a
 * configurable {@link ReloadingDetector} is created and associated with the
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
 * @since 2.0
 * @param <T> the concrete type of {@code Configuration} objects created by this
 *        builder
 */
public class ReloadingFileBasedConfigurationBuilder<T extends FileBasedConfiguration>
        extends FileBasedConfigurationBuilder<T> implements ReloadingControllerSupport
{
    /** The default factory for creating reloading detector objects. */
    private static final ReloadingDetectorFactory DEFAULT_DETECTOR_FACTORY =
            new DefaultReloadingDetectorFactory();

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
    public ReloadingFileBasedConfigurationBuilder(final Class<? extends T> resCls,
            final Map<String, Object> params)
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
    public ReloadingFileBasedConfigurationBuilder(final Class<? extends T> resCls,
            final Map<String, Object> params, final boolean allowFailOnInit)
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
    public ReloadingFileBasedConfigurationBuilder(final Class<? extends T> resCls)
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
    @Override
    public ReloadingController getReloadingController()
    {
        return reloadingController;
    }

    /**
     * {@inheritDoc} This method is overridden here to change the result type.
     */
    @Override
    public ReloadingFileBasedConfigurationBuilder<T> configure(
            final BuilderParameters... params)
    {
        super.configure(params);
        return this;
    }

    /**
     * Creates a {@code ReloadingDetector} which monitors the passed in
     * {@code FileHandler}. This method is called each time a new result object
     * is created with the current {@code FileHandler}. This implementation
     * checks whether a {@code ReloadingDetectorFactory} is specified in the
     * current parameters. If this is the case, it is invoked. Otherwise, a
     * default factory is used to create a {@code FileHandlerReloadingDetector}
     * object. Note: This method is called from a synchronized block.
     *
     * @param handler the current {@code FileHandler}
     * @param fbparams the object with parameters related to file-based builders
     * @return a {@code ReloadingDetector} for this {@code FileHandler}
     * @throws ConfigurationException if an error occurs
     */
    protected ReloadingDetector createReloadingDetector(final FileHandler handler,
            final FileBasedBuilderParametersImpl fbparams)
            throws ConfigurationException
    {
        return fetchDetectorFactory(fbparams).createReloadingDetector(handler,
                fbparams);
    }

    /**
     * {@inheritDoc} This implementation also takes care that a new
     * {@code ReloadingDetector} for the new current {@code FileHandler} is
     * created. Also, the reloading controller's reloading state has to be
     * reset; after the creation of a new result object changes in the
     * underlying configuration source have to be monitored again.
     */
    @Override
    protected void initFileHandler(final FileHandler handler)
            throws ConfigurationException
    {
        super.initFileHandler(handler);

        resultReloadingDetector =
                createReloadingDetector(handler,
                        FileBasedBuilderParametersImpl.fromParameters(
                                getParameters(), true));
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
        final ReloadingDetector ctrlDetector = createReloadingDetectorForController();
        final ReloadingController ctrl = new ReloadingController(ctrlDetector);
        connectToReloadingController(ctrl);
        return ctrl;
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
            @Override
            public void reloadingPerformed()
            {
                final ReloadingDetector detector = resultReloadingDetector;
                if (detector != null)
                {
                    detector.reloadingPerformed();
                }
            }

            @Override
            public boolean isReloadingRequired()
            {
                final ReloadingDetector detector = resultReloadingDetector;
                return (detector != null) && detector.isReloadingRequired();
            }
        };
    }

    /**
     * Returns a {@code ReloadingDetectorFactory} either from the passed in
     * parameters or a default factory.
     *
     * @param params the current parameters object
     * @return the {@code ReloadingDetectorFactory} to be used
     */
    private static ReloadingDetectorFactory fetchDetectorFactory(
            final FileBasedBuilderParametersImpl params)
    {
        final ReloadingDetectorFactory factory = params.getReloadingDetectorFactory();
        return (factory != null) ? factory : DEFAULT_DETECTOR_FACTORY;
    }
}
