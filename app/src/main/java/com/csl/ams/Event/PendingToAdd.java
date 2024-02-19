package com.csl.ams.Event;

import com.csl.ams.Entity.Asset;
import com.csl.ams.Entity.SPEntityP2.AssetsDetail;
import com.csl.ams.Entity.SPUser;
import com.csl.ams.Response.LevelData;

import java.util.ArrayList;
import java.util.List;

public class PendingToAdd {
    public ArrayList<LevelData> levelData = new ArrayList<>();
    public String fatherNo;
    public int level;
    public int type = -1;
    public String typeString;
    public List<AssetsDetail> assetsDetail = null;
    public List<Asset> assetList = null;

    public List<SPUser> spUsers = null;
 }
