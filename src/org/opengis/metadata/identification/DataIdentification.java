/*$************************************************************************************************
 **
 ** $Id$
 **
 ** $Source$
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.opengis.metadata.identification;

// J2SE direct dependencies
import java.util.Collection;
import java.util.Locale;

// OpenGIS direct dependencies
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicDescription;
import org.opengis.metadata.spatial.SpatialRepresentationType;
import org.opengis.util.InternationalString;

// Annotations
///import org.opengis.annotation.UML;
///import org.opengis.annotation.Profile;
///import static org.opengis.annotation.Obligation.*;
///import static org.opengis.annotation.ComplianceLevel.*;


/**
 * Information required to identify a dataset.
 *
 * @author ISO 19115
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 * @version <A HREF="http://www.opengis.org/docs/01-111.pdf">Abstract specification 5.0</A>
 */
///@Profile (level=CORE)
///@UML (identifier="MD_DataIdentification")
public interface DataIdentification extends Identification {
    /**
     * Method used to spatially represent geographic information.
     */
/// @Profile (level=CORE)
/// @UML (identifier="spatialRepresentationType", obligation=OPTIONAL)
    Collection/*<SpatialRepresentationType>*/ getSpatialRepresentationTypes();

    /**
     * Factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
/// @Profile (level=CORE)
/// @UML (identifier="spatialResolution", obligation=OPTIONAL)
    Collection/*<Resolution>*/ getSpatialResolutions();

    /**
     * Language(s) used within the dataset.
     */
/// @Profile (level=CORE)
/// @UML (identifier="language", obligation=MANDATORY)
    Collection/*<Locale>*/ getLanguage();

    /**
     * Full name of the character coding standard used for the dataset.
     *
     * @revisit We should use {@link java.nio.charset.Charset} if J2SE 1.4 is allowed.
     */
/// @Profile (level=CORE)
/// @UML (identifier="characterSet", obligation=CONDITIONAL)
    String getCharacterSet();

    /**
     * Main theme(s) of the datset.
     */
/// @Profile (level=CORE)
/// @UML (identifier="topicCategory", obligation=MANDATORY)
    Collection/*<TopicCategory>*/ getTopicCategories();

    /**
     * Minimum bounding rectangle within which data is available.
     * Only one of <code>getGeographicBox()</code> and {@link #getGeographicDescription()}
     * should be provided.
     */
/// @Profile (level=CORE)
/// @UML (identifier="geographicBox", obligation=CONDITIONAL)
    Collection/*<GeographicBoundingBox>*/ getGeographicBox();

    /**
     * Description of the geographic area within which data is available.
     * Only one of {@link #getGeographicBox()} and <code>getGeographicDescription()</code>
     * should be provided.
     */
/// @Profile (level=CORE)
/// @UML (identifier="geographicDescription", obligation=CONDITIONAL)
    Collection/*<GeographicDescription>*/ getGeographicDescription();

    /**
     * Description of the dataset in the producerís processing environment, including items
     * such as the software, the computer operating system, file name, and the dataset size.
     */
/// @UML (identifier="environmentDescription", obligation=OPTIONAL)
    InternationalString getEnvironmentDescription();

    /**
     * Additional extent information including the bounding polygon, vertical, and temporal
     * extent of the dataset.
     */
/// @Profile (level=CORE)
/// @UML (identifier="extent", obligation=OPTIONAL)
    Collection/*<Extent>*/ getExtent();

    /**
     * Any other descriptive information about the dataset.
     */
/// @UML (identifier="supplementalInformation", obligation=OPTIONAL)
    InternationalString getSupplementalInformation();
}
