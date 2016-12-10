/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import cz.incad.kramerius.AbstractObjectPath.Between;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.CollectionGet;

public class ObjectPidsPathTest {

    @Test
    public void testInjectObjectsPaths() {
        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };
        List<String> expecting = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:article");
                add("uuid:page");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromRootToLeaf()));

        ObjectPidsPath npath = path.injectObjectBetween("uuid:article",
                new AbstractObjectPath.Between("uuid:internalpart", "uuid:page"));
        String[] pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

        npath = path.injectObjectBetween("uuid:article",
                new AbstractObjectPath.Between("uuid:page", "uuid:internalpart"));
        pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

    }

    @Test
    public void testInjectObjectsPathsAtTheBeginning() {

        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> expecting = new ArrayList<String>() {
            {
                add("uuid:superroot");
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromRootToLeaf()));

        ObjectPidsPath npath = path.injectObjectBetween("uuid:superroot",
                new AbstractObjectPath.Between(null, "uuid:root"));
        String[] pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

        npath = path.injectObjectBetween("uuid:superroot", new AbstractObjectPath.Between("uuid:root", null));
        pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

    }

    @Test
    public void testInjectObjectsPathsInTheEnd() {

        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> expecting = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
                add("uuid:subpage");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromRootToLeaf()));

        ObjectPidsPath npath = path.injectObjectBetween("uuid:subpage",
                new AbstractObjectPath.Between(null, "uuid:page"));
        String[] pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

        npath = path.injectObjectBetween("uuid:subpage", new AbstractObjectPath.Between("uuid:page", null));
        pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

    }

    @Test
    public void testPidPaths() {
        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };
        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromRootToLeaf()));

        Collections.reverse(relsExtPath);
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromLeafToRoot()));
    }

    @Test
    public void testCutHeads() {
        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> cutHeadPath0 = new ArrayList<String>() {
            {
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> cutHeadPath1 = new ArrayList<String>() {
            {
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> cutHeadPath2 = new ArrayList<String>() {
            {
                add("uuid:page");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));

        ObjectPidsPath cutHead0 = path.cutHead(0);
        TestCase.assertEquals(Arrays.asList(cutHead0.getPathFromRootToLeaf()), cutHeadPath0);

        ObjectPidsPath cutHead1 = path.cutHead(1);
        TestCase.assertEquals(Arrays.asList(cutHead1.getPathFromRootToLeaf()), cutHeadPath1);

        ObjectPidsPath cutHead2 = path.cutHead(2);
        TestCase.assertEquals(Arrays.asList(cutHead2.getPathFromRootToLeaf()), cutHeadPath2);
    }

    
    @Test
    public void testCutTails() {
        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> cutTailPath0 = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
            }
        };

        List<String> cutTailPath1 = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
            }
        };

        List<String> cutTailPath2 = new ArrayList<String>() {
            {
                add("uuid:root");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));

        ObjectPidsPath cutTail0 = path.cutTail(0);
        TestCase.assertEquals(Arrays.asList(cutTail0.getPathFromRootToLeaf()), cutTailPath0);

        ObjectPidsPath cutTail1 = path.cutTail(1);
        TestCase.assertEquals(Arrays.asList(cutTail1.getPathFromRootToLeaf()), cutTailPath1);

        ObjectPidsPath cutTail2 = path.cutTail(2);
        TestCase.assertEquals(Arrays.asList(cutTail2.getPathFromRootToLeaf()), cutTailPath2);
    }

    
    @Test
    public void testEhanceByCollection() throws JSONException, IOException, NoSuchMethodException, IllegalAccessException {
        
        InputStream itemStream = ObjectPidsPathTest.class.getResourceAsStream("objectpidpathitem.json");
        JSONObject obj = new JSONObject(IOUtils.readAsString(itemStream, Charset.forName("UTF-8"), true));

        InputStream colsInputStream = ObjectPidsPathTest.class.getResourceAsStream("collections.json");
        JSONArray collections = new JSONArray(IOUtils.readAsString(colsInputStream, Charset.forName("UTF-8"), true));
        
        ObjectPidsPath path = createMockBuilder(ObjectPidsPath.class)
            .withConstructor(List.class)
             .withArgs( Arrays.asList("uuid:periodical","uuid:periodicalvolume","uuid:periodicalitem"))
            .addMockedMethod("getItemJSON")
            .createMock(); 
        
        CollectionGet colGet = EasyMock.createMock(CollectionGet.class);
        
        
        EasyMock.expect(path.getItemJSON("uuid:periodical")).andReturn(obj).anyTimes();
        EasyMock.expect(path.getItemJSON("uuid:periodicalvolume")).andReturn(obj).anyTimes();
        EasyMock.expect(path.getItemJSON("uuid:periodicalitem")).andReturn(obj).anyTimes();

        EasyMock.expect(colGet.collections()).andReturn(collections).anyTimes();
        replay(path, colGet);

        Assert.assertEquals(path.injectCollections(colGet).getRoot(), "vc:ebc58201-b12d-4be5-baa6-b0cdcf7f1ae3");
        Assert.assertEquals(path.injectCollections(colGet).injectRepository().getRoot(), "uuid:1");
    }
    

    @Test
    public void testEhanceByCollection3() throws JSONException, IOException, NoSuchMethodException, IllegalAccessException {
        ObjectPidsPath p = new ObjectPidsPath();

        CollectionGet colGet = EasyMock.createMock(CollectionGet.class);
        replay( colGet);
        ObjectPidsPath cols = p.injectCollections(colGet);
        Assert.assertTrue(cols.isEmptyPath());
    }
    
    @Test
    public void testEhanceByCollection2() throws JSONException, IOException, NoSuchMethodException, IllegalAccessException {
        InputStream itemStream = ObjectPidsPathTest.class.getResourceAsStream("objectpidpathitem.json");
        JSONObject obj = new JSONObject(IOUtils.readAsString(itemStream, Charset.forName("UTF-8"), true));

        InputStream colsInputStream = ObjectPidsPathTest.class.getResourceAsStream("collections.json");
        JSONArray collections = new JSONArray(IOUtils.readAsString(colsInputStream, Charset.forName("UTF-8"), true));
        

        ObjectPidsPath repoPath = createMockBuilder(ObjectPidsPath.class)
                .withConstructor(List.class)
                 .withArgs( Arrays.asList(SpecialObjects.REPOSITORY.getPid(),"uuid:periodical","uuid:periodicalvolume","uuid:periodicalitem"))
                .addMockedMethod("getItemJSON")
                .createMock(); 

        EasyMock.expect(repoPath.getItemJSON("uuid:1")).andReturn(obj).anyTimes();
        EasyMock.expect(repoPath.getItemJSON("uuid:periodical")).andReturn(obj).anyTimes();
        EasyMock.expect(repoPath.getItemJSON("uuid:periodicalvolume")).andReturn(obj).anyTimes();
        EasyMock.expect(repoPath.getItemJSON("uuid:periodicalitem")).andReturn(obj).anyTimes();

        CollectionGet colGet = EasyMock.createMock(CollectionGet.class);

        EasyMock.expect(colGet.collections()).andReturn(collections).anyTimes();
        replay(repoPath,colGet);

        Assert.assertEquals(repoPath.injectCollections(colGet).getRoot(), "uuid:1");
        List<String> arr =Arrays.asList(repoPath.injectCollections(colGet).getPathFromRootToLeaf());
        List<String> expectingList = Arrays.asList("uuid:1", "vc:ebc58201-b12d-4be5-baa6-b0cdcf7f1ae3", "uuid:periodical", "uuid:periodicalvolume", "uuid:periodicalitem");
        Assert.assertTrue(arr.size() == expectingList.size());
        for (int i = 0,ll=arr.size(); i < ll; i++) {
            Assert.assertTrue(arr.get(i).equals(expectingList.get(i)));
        }
    }
}
