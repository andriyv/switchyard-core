/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.switchyard.serial.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.switchyard.common.io.CountingOutputStream;
import org.switchyard.serial.CompressionType;
import org.switchyard.serial.Serializer;
import org.switchyard.serial.WrapperSerializer;

/**
 * A wrapper serializer that performs ZIP compression/decompression.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; &copy; 2012 Red Hat Inc.
 */
public final class ZIPSerializer extends WrapperSerializer {

    /**
     * Constructor with a serializer to wrap.
     * @param serializer the serializer to wrap
     */
    public ZIPSerializer(Serializer serializer) {
        super(serializer, CompressionType.ZIP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> int serialize(T obj, Class<T> type, OutputStream out) throws IOException {
        out = new CountingOutputStream(out);
        ZipOutputStream zip = new ZipOutputStream(out);
        try {
            zip.putNextEntry(new ZipEntry("z"));
            getWrapped().serialize(obj, type, zip);
            zip.closeEntry();
            zip.finish();
            zip.flush();
        } finally {
            if (isCloseEnabled()) {
                zip.close();
            }
        }
        return ((CountingOutputStream)out).getCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T deserialize(InputStream in, Class<T> type) throws IOException {
        ZipInputStream zip = new ZipInputStream(in);
        try {
            zip.getNextEntry();
            return getWrapped().deserialize(zip, type);
        } finally {
            if (isCloseEnabled()) {
                zip.close();
            }
        }
    }

}
