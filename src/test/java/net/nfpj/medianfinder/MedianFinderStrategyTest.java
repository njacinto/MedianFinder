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
import java.util.ArrayList;
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
public class MedianFinderStrategyTest {

    @Parameters(name = "{index}: testGetPivot(strategy={0}, file={1}, expected={2})")
    public static Collection<Object[]> data() {
        MedianFinderStrategy[] strategies = new MedianFinderStrategy[]{
            new MedianFinderStrategyPivotWithSingleBeforeAfter(),
            new MedianFinderStrategyPivotWithCache(),
            new MedianFinderStrategyPivotWithCache(3),
            new MedianFinderStrategyPivotWithCache(45),
            new MedianFinderStrategyPivotWithCacheMultiThreaded(),
            new MedianFinderStrategyPivotWithCacheMultiThreaded(3),
            new MedianFinderStrategyPivotWithCacheMultiThreaded(45),
            new MedianFinderStrategyPivotWithCacheMultiThreaded(1024, 1),
            new MedianFinderStrategyPivotWithCacheMultiThreaded(1024, 2),
            new MedianFinderStrategyPivotWithCacheMultiThreaded(1024, 3)
        };
        Object[][] fileMedian = new Object[][]{
            {"medianFinderTestData01.txt", 50, false},
            {"medianFinderTestData02.txt", 50, false},
            {"medianFinderTestData03.txt", 50, false},
            {"medianFinderTestData04.txt", 49, false},
            {"medianFinderTestData01-2.txt", 49, false},
            {"medianFinderTestData02-2.txt", 49, false},
            {"medianFinderTestData03-2.txt", 49, false},
            {"medianFinderTestData01-SPACE.txt", 49, false},
            {"medianFinderTestData01-Neg.txt", -49, false},
            {"medianFinderTestData02-Neg.txt", -49, false},
            {"medianFinderTestData04-Neg.txt", -49, false},
            {"medianFinderTestData01-Dup.txt", 49, true}
        };
        ArrayList<Object[]> ret = new ArrayList<>(fileMedian.length * strategies.length);
        for (MedianFinderStrategy strategy : strategies) {
            for (Object[] fm : fileMedian) {
                ret.add(new Object[]{strategy, fm[0], fm[1], fm[2]});
            }
        }
        return ret;
    }
    // -------------------------------------------------------------------------
    private final MedianFinderStrategy strategy;
    private final String filename;
    private final long expected;
    private final boolean throwsException;

    public MedianFinderStrategyTest(MedianFinderStrategy strategy, String filename,
            long expected, boolean throwsException) {
        this.strategy = strategy;
        this.filename = filename;
        this.expected = expected;
        this.throwsException = throwsException;
    }

    @Test
    public void testFindMedian() throws Exception {
        if (!throwsException) {
            File file = new File(this.getClass().getClassLoader().getResource(filename).getFile());
            long result = strategy.findMedian(file);
            assertEquals(expected, result);
        }
    }

    @Test(expected = MedianFinderException.class)
    public void testFindMedianException() throws Exception {
        if (throwsException) {
            File file = new File(this.getClass().getClassLoader().getResource(filename).getFile());
            long result = strategy.findMedian(file);
            assertEquals(expected, result);
        } else {
            throw new MedianFinderException();
        }
    }
}
