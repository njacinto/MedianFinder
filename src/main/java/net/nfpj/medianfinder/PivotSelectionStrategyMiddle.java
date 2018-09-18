/*
 * Copyright (C) 2015.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package net.nfpj.medianfinder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Finds the number in the middle of the file and returns it;
 *
 * @author njacinto
 */
public class PivotSelectionStrategyMiddle implements PivotSelectionStrategy {

    public static final PivotSelectionStrategyMiddle INSTANCE = new PivotSelectionStrategyMiddle();

    @Override
    public long getPivot(File file, RandomAccessFile in) throws IOException {
        final byte[] readBuff = new byte[Constants.BUFFER_SIZE];
        long pos = file.length()>1 ? (file.length() >> 1) - 1 : 0;
        in.seek(pos);
        int read;
        while ((read = in.read(readBuff)) != -1) {
            long num = NumberUtil.getFirstNumberFromBuffer(readBuff, read, pos==0);
            if (num != Long.MIN_VALUE) {
                return num;
            }
        }
        return Long.MIN_VALUE;
    }

    @Override
    public String toString() {
        return "PivotSelectionStrategyMiddle{" + '}';
    }
}
