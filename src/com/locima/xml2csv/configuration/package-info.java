/**
 * Contains the classes that models the configuration specified by a user.
 * <p>
 * All types of mappings: i.e. those that produce values or are containers, inherit from {@link com.locima.xml2csv.configuration.IMapping}. Beneath
 * there, container implement {@link com.locima.xml2csv.configuration.IMappingContainer} and value mappings implement
 * {@link com.locima.xml2csv.configuration.IValueMapping}.
 * <p>
 * Functionality common to all mappings is provided in {@link java.util.AbstractMap}. Code common to containers (
 * {@link com.locima.xml2csv.configuration.MappingList} and {@link com.locima.xml2csv.configuration.PivotMapping}) is contained within
 * {@link com.locima.xml2csv.configuration.AbstractMappingContainer}.
 * <p>
 * {@link com.locima.xml2csv.configuration.MappingConfiguration} is the top-level container for all configurations.
 */
package com.locima.xml2csv.configuration;