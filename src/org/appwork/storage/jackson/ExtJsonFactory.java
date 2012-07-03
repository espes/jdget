/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.jackson;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.MappingJsonFactory;

/**
 * @author thomas
 * 
 */
public class ExtJsonFactory extends MappingJsonFactory {

    /**
     * Method for constructing json generator for writing json content to
     * specified file, overwriting contents it might have (or creating it if
     * such file does not yet exist). Encoding to use must be specified, and
     * needs to be one of available types (as per JSON specification).
     * <p>
     * Underlying stream <b>is owned</b> by the generator constructed, i.e.
     * generator will handle closing of file when {@link JsonGenerator#close} is
     * called.
     * 
     * @param f
     *            File to write contents to
     * @param enc
     *            Character encoding to use
     */
    @Override
    public JsonGenerator createJsonGenerator(final File f, final JsonEncoding enc) throws IOException {
        final JsonGenerator ret = super.createJsonGenerator(f, enc);
        ret.useDefaultPrettyPrinter();
        return ret;
    }

    /**
     * Method for constructing JSON generator for writing JSON content using
     * specified output stream. Encoding to use must be specified, and needs to
     * be one of available types (as per JSON specification).
     * <p>
     * Underlying stream <b>is NOT owned</b> by the generator constructed, so
     * that generator will NOT close the output stream when
     * {@link JsonGenerator#close} is called (unless auto-closing feature,
     * {@link org.codehaus.jackson.JsonGenerator.Feature#AUTO_CLOSE_TARGET} is
     * enabled). Using application needs to close it explicitly if this is the
     * case.
     * 
     * @param out
     *            OutputStream to use for writing JSON content
     * @param enc
     *            Character encoding to use
     */
    @Override
    public JsonGenerator createJsonGenerator(final OutputStream out, final JsonEncoding enc) throws IOException {
        final JsonGenerator ret = super.createJsonGenerator(out, enc);
        ret.useDefaultPrettyPrinter();
        return ret;
    }

    /**
     * Method for constructing JSON generator for writing JSON content using
     * specified Writer.
     * <p>
     * Underlying stream <b>is NOT owned</b> by the generator constructed, so
     * that generator will NOT close the Reader when {@link JsonGenerator#close}
     * is called (unless auto-closing feature,
     * {@link org.codehaus.jackson.JsonGenerator.Feature#AUTO_CLOSE_TARGET} is
     * enabled). Using application needs to close it explicitly.
     * 
     * @param out
     *            Writer to use for writing JSON content
     */
    @Override
    public JsonGenerator createJsonGenerator(final Writer out) throws IOException {
        final JsonGenerator ret = super.createJsonGenerator(out);
        ret.useDefaultPrettyPrinter();
        return ret;
    }
}
