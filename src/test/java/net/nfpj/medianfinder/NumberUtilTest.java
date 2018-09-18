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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author njacinto
 */
public class NumberUtilTest {

    public NumberUtilTest() {
    }

    /**
     * Test of toLong method, of class NumberUtil.
     */
    @Test
    public void testToLong() {
        byte[] arr = new byte[]{'1', '2', '3', '4', '5'};
        assertEquals(12345L, NumberUtil.toLong(arr, 0, arr.length - 1));
        assertEquals(2345L, NumberUtil.toLong(arr, 1, arr.length - 1));
        assertEquals(1234L, NumberUtil.toLong(arr, 0, arr.length - 2));
        assertEquals(234L, NumberUtil.toLong(arr, 1, arr.length - 2));
        assertEquals(3L, NumberUtil.toLong(arr, 2, arr.length - 3));
    }

    @Test
    public void testToLongNegative() {
        byte[] arr = new byte[]{'-', '1', '2', '3', '4', '5'};
        assertEquals(-12345L, NumberUtil.toLong(arr, 0, arr.length - 1));
        assertEquals(-1234L, NumberUtil.toLong(arr, 0, arr.length - 2));
    }
}
