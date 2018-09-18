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
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author njacinto
 */
@RunWith(Parameterized.class)
public class PivotSelectionStrategyTest {

    @Parameters(name = "{index}: testGetPivot(strategy={0}, file={1}, expected={2})")
    public static Collection<Object[]> data() {
        Object[][] tests = new Object[][]{
            {new PivotSelectionStrategyMiddle(), "medianFinderTestData00-0.txt", Long.MIN_VALUE},
            {new PivotSelectionStrategyMean(), "medianFinderTestData00-0.txt", Long.MIN_VALUE},
            {new PivotSelectionStrategyBestOfN(), "medianFinderTestData00-0.txt", Long.MIN_VALUE},
            {new PivotSelectionStrategyMiddle(), "medianFinderTestData00-1.txt", 1},
            {new PivotSelectionStrategyMean(), "medianFinderTestData00-1.txt", 1},
            {new PivotSelectionStrategyBestOfN(), "medianFinderTestData00-1.txt", 1},
            {new PivotSelectionStrategyMiddle(), "medianFinderTestData00-2.txt", 1},
            {new PivotSelectionStrategyMean(), "medianFinderTestData00-2.txt", 1},
            {new PivotSelectionStrategyBestOfN(), "medianFinderTestData00-2.txt", 1},
            {new PivotSelectionStrategyMiddle(), "medianFinderTestData00-3.txt", 2},
            {new PivotSelectionStrategyMean(), "medianFinderTestData00-3.txt", 2},
            {new PivotSelectionStrategyBestOfN(), "medianFinderTestData00-3.txt", 2},
            {new PivotSelectionStrategyMiddle(), "medianFinderTestData01.txt", 51},
            {new PivotSelectionStrategyMiddle(), "medianFinderTestData02.txt", 49},
            {new PivotSelectionStrategyMiddle(), "medianFinderTestData03.txt", 1},
            {new PivotSelectionStrategyMean(), "medianFinderTestData01.txt", 50},
            {new PivotSelectionStrategyMean(), "medianFinderTestData02.txt", 50},
            {new PivotSelectionStrategyMean(), "medianFinderTestData03.txt", 50},
            {new PivotSelectionStrategyBestOfN(), "medianFinderTestData01.txt", 51},
            {new PivotSelectionStrategyBestOfN(), "medianFinderTestData02.txt", 49},
            {new PivotSelectionStrategyBestOfN(), "medianFinderTestData03.txt", 51}
        };
        return Arrays.asList(tests);
    }
    // -------------------------------------------------------------------------
    private final PivotSelectionStrategy strategy;
    private final String filename;
    private final long expected;

    public PivotSelectionStrategyTest(PivotSelectionStrategy strategy, String filename, long expected) {
        this.strategy = strategy;
        this.filename = filename;
        this.expected = expected;
    }

    /**
     * Test of getPivot method, of class PivotSelectionStrategyBestOfN.
     */
    @Test
    public void testGetPivot() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource(filename).getFile());
        RandomAccessFile in = new RandomAccessFile(file, "r");
        long result = strategy.getPivot(file, in);
        assertEquals(expected, result);
    }
}
