/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source$
 **
 ** Copyright (C) 2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.layer.source;

// J2SE direct dependencies
import java.io.IOException;
import java.net.URI;
import java.util.Map;

// OpenGIS direct dependencies
import org.opengis.util.InternationalString;


/**
 * A factory for {@link LayerSource} objects.
 * 
 * @author Jesse Crossley (SYS Technologies)
 * @since GeoAPI 2.0
 */
public interface LayerSourceFactory {    
    /**
     * Ask for a {@code LayerSource} connecting to the indicated provider or service.
     * The returned {@code LayerSource} may have been previously cached.
     * <p>
     * Additional hints or configuration information may be provided according
     * to the metadata indicated by {@link #getParametersInfo}. This information
     * often includes security information such as username and password.
     * 
     * @param provider Often a URI or JDBC URI locating the service to connect to.
     * @param params Map of hints or configuration information.
     * @return {@code GraphicStore} connected to the indicated provider or service.
     * @throws IOException if the {@code LayerSource} cannot connect to its source.
     * @throws LayerException if the {@code LayerSource} cannot be created for some other reason.
     */
    LayerSource createLayerSource(URI provider, Map<String,Object> params) throws IOException, LayerSourceException;

    /**
     * Ask for a new {@code LayerSource} connecting to the indicated provider or service.
     * <p>
     * Additional hints or configuration information may be provided according
     * to the metadata indicated by {@link #getParametersInfo}. This information
     * often includes security information such as username and password.
     * 
     * @param provider Often a URI or JDBC URI locating the service to connect to.
     * @param params Map of hints or configuration information.
     * @return {@code LayerSource} connected to the newly created provider or service.
     * @throws IOException if the {@code LayerSource} cannot connect to its source.
     * @throws LayerException if the {@code LayerSource} cannot be created for some other reason.
     */
    LayerSource createNewLayerSource(URI provider, Map<String,Object> params) throws IOException, LayerSourceException;
    
    /**
     * Icon representing this category of layer source.
     *
     * @return URI to a icon (GIF or PNG) representing this factory.
     */
    URI getIcon();

    /**
     * Display name used to communicate this type of FeatureStore to end users.
     */
    InternationalString getDisplayName();
    
    /** 
     * Description of this type of FeatureStore.
     */
    InternationalString getDescription();

    /**
     * Gets an {@code Object} array relating to the parameters needed (beyond
     * the URI) to instantiate a {@code FeatureStore}.
     * 
     * @todo Should be replaced with a {@code Param}[] based on ISO standards (ISO 19119?).
     */
    Map<String,Class> getParametersInfo();

    /**
     * Indicates this {@code FeatureStoreFactory} communicate with the indicated provider or service.
     * <p>
     * This method should not fail, if a connection needs to be made
     * to parse a {@code GetCapabilities} file or negotiate WMS versions any
     * IO problems simply indicate the inabiity to process.
     * <p>
     * This method may be considered the same as:
     * <code>{@linkplain #canProcess(URI,Map) canProcess}(provider, hints)</code>
     * where hints was generated by using all the default values specified by the
     * {@link #getParametersInfo} method.
     *
     * @param provider Provider or Server of spatial information. 
     * @return {@code true} if this factory can communicate with the provider.
     */
    boolean canProcess(URI provider);

    /**
     * Indicates this {@code FeatureStoreFactory} communicate with the indicated provider or service.
     * <p>
     * This method differs from {@link #canProcess(URI)} in that additional configuration
     * information may be supplied. 
     *
     * @param provider Provider or Server of spatial information. 
     * @param params additional configuration information.
     * @return {@code true} if this factory can communicate with the provider.
     */
    boolean canProcess(URI provider, Map<String,Object> params);

    /**
     * Allows a {@code FeatureStoreFactory} to ensure all its preconditions are met,
     * such as the presense of required libraries.
     */
    boolean isAvailable();
}