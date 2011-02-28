/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package com.ovea.jetty.session.serializer.jboss.serial.objectmetamodel.safecloning;

import com.ovea.jetty.session.serializer.jboss.serial.util.ClassMetaConsts;
import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;

/**
 * $Id: SafeCloningRepository.java 263 2006-05-02 04:45:03Z csuconic $
 * <p/>
 * Some objects may be completely reused during cloning operations. (For instance InvocationContext)
 *
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public class SafeCloningRepository implements ClassMetaConsts {


    public SafeCloningRepository(SafeClone safeClone) {
        this.safeClone = safeClone;
    }

    private SafeClone safeClone;

    TObjectIntHashMap safeToReuse = new TObjectIntHashMap(identityHashStrategy);
    ArrayList reuse = new ArrayList();

    public void clear() {
        reuse.clear();
        safeToReuse.clear();
    }

    public int storeSafe(Object obj) {
        if (safeClone.isSafeToReuse(obj)) {
            int description = safeToReuse.get(obj);

            if (description == 0) {
                safeToReuse.put(obj, safeToReuse.size() + 1);
                description = safeToReuse.size();
                reuse.add(obj);
            }
            return description;
        } else {
            return 0;
        }
    }

    public Object findReference(int reference) {
        Object retobject = reuse.get(reference - 1);
        return retobject;
    }
}
